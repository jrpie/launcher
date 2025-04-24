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
import androidx.core.view.children
import de.jrpie.android.launcher.ui.widgets.WidgetContainerView
import de.jrpie.android.launcher.widgets.Widget
import de.jrpie.android.launcher.widgets.WidgetPosition
import de.jrpie.android.launcher.widgets.updateWidget
import kotlin.math.max
import kotlin.math.min

/**
 * A variant of the [WidgetContainerView] which allows to manage widgets.
 */
class WidgetManagerView(context: Context, attrs: AttributeSet? = null) :
    WidgetContainerView(context, attrs) {

    val TOUCH_SLOP: Int
    val TOUCH_SLOP_SQUARE: Int
    val LONG_PRESS_TIMEOUT: Long

    init {
        val configuration = ViewConfiguration.get(context)
        TOUCH_SLOP = configuration.scaledTouchSlop
        TOUCH_SLOP_SQUARE = TOUCH_SLOP * TOUCH_SLOP

        LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout().toLong()
    }



    enum class EditMode(val resize: (dx: Int, dy: Int, rect: Rect) -> Rect) {
        MOVE({ dx, dy, rect ->
            Rect(rect.left + dx, rect.top + dy, rect.right + dx, rect.bottom + dy)
        }),
        TOP({ dx, dy, rect ->
            Rect(rect.left, min(rect.top + dy, rect.bottom - 200), rect.right, rect.bottom)
        }),
        BOTTOM({ dx, dy, rect ->
            Rect(rect.left, rect.top, rect.right, max(rect.top + 200, rect.bottom + dy))
        }),
        LEFT({ dx, dy, rect ->
            Rect(min(rect.left + dx, rect.right - 200), rect.top, rect.right, rect.bottom)
        }),
        RIGHT({ dx, dy, rect ->
            Rect(rect.left, rect.top, max(rect.left + 200, rect.right + dx), rect.bottom)
        }),
    }

    var selectedWidgetOverlayView: WidgetOverlayView? = null
    var selectedWidgetView: View? = null
    var currentGestureStart: Point? = null
    var startWidgetPosition: Rect? = null
    var lastPosition = Rect()

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
                val view = children.mapNotNull { it as? WidgetOverlayView }.firstOrNull {
                    RectF(it.x, it.y, it.x + it.width, it.y + it.height).toRect().contains(start) == true
                } ?: return false

                val position = (view.layoutParams as Companion.LayoutParams).position.getAbsoluteRect(width, height)
                selectedWidgetOverlayView = view
                selectedWidgetView = widgetViewById.get(view.widgetId) ?: return true
                startWidgetPosition = position

                val positionInView = start.minus(Point(position.left, position.top))
                view.mode = view.getHandles().firstOrNull { it.position.contains(positionInView) }?.mode ?: EditMode.MOVE

                longPressHandler.postDelayed({
                    synchronized(this@WidgetManagerView) {
                        view.showPopupMenu()
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        endInteraction()
                    }
                }, LONG_PRESS_TIMEOUT)
            }
            if (event.actionMasked == MotionEvent.ACTION_MOVE ||
                event.actionMasked == MotionEvent.ACTION_UP
            ) {
                val distanceX = event.x - (currentGestureStart?.x ?: return true)
                val distanceY = event.y - (currentGestureStart?.y ?: return true)
                if (distanceX * distanceX + distanceY * distanceY > TOUCH_SLOP_SQUARE) {
                    longPressHandler.removeCallbacksAndMessages(null)
                }
                val view = selectedWidgetOverlayView ?: return true
                val start = startWidgetPosition ?: return true
                val absoluteNewPosition = view.mode?.resize(
                        distanceX.toInt(),
                        distanceY.toInt(),
                        start
                    ) ?: return true
                val newPosition = WidgetPosition.fromAbsoluteRect(
                    absoluteNewPosition, width, height
                )
                if (newPosition != lastPosition) {
                    lastPosition = absoluteNewPosition
                    (view.layoutParams as Companion.LayoutParams).position = newPosition
                    (selectedWidgetView?.layoutParams as? Companion.LayoutParams)?.position = newPosition
                    requestLayout()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
                    }
                }

                if (event.actionMasked == MotionEvent.ACTION_UP) {
                    longPressHandler.removeCallbacksAndMessages(null)
                    val id = selectedWidgetOverlayView?.widgetId ?: return true
                    val widget = Widget.byId(context, id) ?: return true
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
        startWidgetPosition = null
        selectedWidgetOverlayView?.mode = null
    }

    override fun updateWidgets(activity: Activity, widgets: Set<Widget>?) {
        super.updateWidgets(activity, widgets)
        if (widgets == null) {
            return
        }

        widgets.forEach { widget ->
            WidgetOverlayView(activity).let {
                addView(it)
                it.widgetId = widget.id
                (it.layoutParams as Companion.LayoutParams).position = widget.position
            }
        }
    }
}