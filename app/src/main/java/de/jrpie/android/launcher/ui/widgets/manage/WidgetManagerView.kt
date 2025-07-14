package de.jrpie.android.launcher.ui.widgets.manage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.graphics.contains
import androidx.core.graphics.minus
import androidx.core.graphics.toRect
import de.jrpie.android.launcher.ui.widgets.WidgetContainerView
import de.jrpie.android.launcher.widgets.GRID_SIZE
import de.jrpie.android.launcher.widgets.Widget
import de.jrpie.android.launcher.widgets.WidgetPanel
import de.jrpie.android.launcher.widgets.WidgetPosition
import de.jrpie.android.launcher.widgets.updateWidget

/**
 * A variant of the [WidgetContainerView] which allows to manage widgets.
 */
class WidgetManagerView(widgetPanelId: Int, context: Context, attrs: AttributeSet? = null) :
    WidgetContainerView(widgetPanelId, context, attrs) {
    constructor(context: Context, attrs: AttributeSet?) : this(WidgetPanel.HOME.id, context, attrs)

    val touchSlop: Int
    val touchSlopSquare: Int
    val longPressTimeout: Long


    private var overlayViewById = HashMap<Int, WidgetOverlayView>()

    init {
        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop
        touchSlopSquare = touchSlop * touchSlop

        longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
    }


    enum class EditMode(val resize: (dx: Int, dy: Int, screenWidth: Int, screenHeight: Int, rect: Rect) -> Rect) {
        MOVE({ dx, dy, sw, sh, rect ->
            val rx = (-rect.left)..(sw - rect.right)
            val ry = (-rect.top)..(sh - rect.bottom)

            val cdx = if (rx.isEmpty()) { 0 } else { dx.coerceIn(rx) }
            val cdy = if (ry.isEmpty()) { 0 } else { dy.coerceIn(ry) }
            Rect(rect.left + cdx, rect.top + cdy, rect.right + cdx, rect.bottom + cdy)
        }),
        TOP({ _, dy, _, sh, rect ->
            val range = (-rect.top)..(rect.bottom - rect.top - (2 * sh / GRID_SIZE) + 5)
            if (range.isEmpty()) {
                rect
            } else {
                Rect(rect.left, rect.top + dy.coerceIn(range), rect.right, rect.bottom)
            }
        }),
        BOTTOM({ _, dy, _, sh, rect ->
            val range = ((2 * sh / GRID_SIZE) + 5 + rect.top - rect.bottom)..(sh - rect.bottom)
            if (range.isEmpty()) {
                rect
            } else {
                Rect(rect.left, rect.top, rect.right, rect.bottom + dy.coerceIn(range))
            }
        }),
        LEFT({ dx, _, sw, _, rect ->
            val range = (-rect.left)..(rect.right - rect.left - (2 * sw / GRID_SIZE) + 5)
            if (range.isEmpty()) {
                rect
            } else {
                Rect(rect.left + dx.coerceIn(range), rect.top, rect.right, rect.bottom)
            }
        }),
        RIGHT({ dx, _, sw, _, rect ->
            val range = ((2 * sw / GRID_SIZE) + 5 + rect.left - rect.right)..(sw - rect.right)
            if (range.isEmpty()) {
                rect
            } else {
                Rect(rect.left, rect.top, rect.right + dx.coerceIn(range), rect.bottom)
            }
        }),
    }

    private var selectedWidgetOverlayView: WidgetOverlayView? = null
    private var selectedWidgetView: View? = null
    private var currentGestureStart: Point? = null
    private var startWidgetPosition: Rect? = null
    private var lastPosition = Rect()

    private val longPressHandler = Handler(Looper.getMainLooper())


    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }
        synchronized(this) {
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                val start = Point(event.x.toInt(), event.y.toInt())
                currentGestureStart = start
                val view = overlayViewById.asIterable()
                    .map { it.value }.firstOrNull { overlayView ->
                        RectF(
                            overlayView.x,
                            overlayView.y,
                            overlayView.x + overlayView.width,
                            overlayView.y + overlayView.height
                        )
                            .toRect()
                            .contains(start)
                    } ?: return true

                val position =
                    (view.layoutParams as Companion.LayoutParams).position.getAbsoluteRect(
                        width,
                        height
                    )
                selectedWidgetOverlayView = view
                selectedWidgetView = widgetViewById[view.widgetId]
                startWidgetPosition = position

                val positionInView = start.minus(Point(position.left, position.top))
                view.mode =
                    view.getHandles().firstOrNull { it.position.contains(positionInView) }?.mode
                        ?: EditMode.MOVE

                longPressHandler.postDelayed({
                    synchronized(this@WidgetManagerView) {
                        view.showPopupMenu()
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        endInteraction()
                    }
                }, longPressTimeout)
            }
            if (event.actionMasked == MotionEvent.ACTION_MOVE ||
                event.actionMasked == MotionEvent.ACTION_UP
            ) {
                val distanceX = event.x - (currentGestureStart?.x ?: return true)
                val distanceY = event.y - (currentGestureStart?.y ?: return true)
                if (distanceX * distanceX + distanceY * distanceY > touchSlopSquare) {
                    longPressHandler.removeCallbacksAndMessages(null)
                }
                val view = selectedWidgetOverlayView ?: return true
                val start = startWidgetPosition ?: return true
                val absoluteNewPosition = (view.mode ?: return true).resize(
                    distanceX.toInt(),
                    distanceY.toInt(),
                    width, height,
                    start
                )
                val newPosition = WidgetPosition.fromAbsoluteRect(
                    absoluteNewPosition, width, height
                )
                if (absoluteNewPosition != lastPosition) {
                    lastPosition = absoluteNewPosition
                    (view.layoutParams as Companion.LayoutParams).position = newPosition
                    (selectedWidgetView?.layoutParams as? Companion.LayoutParams)?.position =
                        newPosition
                    requestLayout()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
                    }
                }

                if (event.actionMasked == MotionEvent.ACTION_UP) {
                    val id = selectedWidgetOverlayView?.widgetId ?: return true
                    val widget = Widget.byId(id) ?: return true
                    widget.position = newPosition
                    endInteraction()
                    updateWidget(widget)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                    }
                }
            }
        }
        return true
    }

    private fun endInteraction() {
        synchronized(this) {
            longPressHandler.removeCallbacksAndMessages(null)
            startWidgetPosition = null
            selectedWidgetOverlayView?.mode = null
        }
    }

    override fun onDetachedFromWindow() {
        endInteraction()
        super.onDetachedFromWindow()
    }

    override fun updateWidgets(activity: Activity, widgets: Collection<Widget>?) {
        super.updateWidgets(activity, widgets)

        synchronized(overlayViewById) {
            overlayViewById.forEach { removeView(it.value) }
            overlayViewById.clear()
            widgets?.filter { it.panelId == widgetPanelId }?.forEach { widget ->
                WidgetOverlayView(activity).let {
                    it.widgetId = widget.id
                    addView(it)
                    (it.layoutParams as Companion.LayoutParams).position = widget.position
                    overlayViewById[widget.id] = it
                }
            }
        }
    }
}
