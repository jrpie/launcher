package de.jrpie.android.launcher.ui.widgets

import android.app.Activity
import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import androidx.core.graphics.contains
import androidx.core.view.size
import de.jrpie.android.launcher.widgets.Widget
import de.jrpie.android.launcher.widgets.WidgetPanel
import de.jrpie.android.launcher.widgets.WidgetPosition
import kotlin.math.max


/**
 * This only works in an Activity, not AppCompatActivity
 */
open class WidgetContainerView(
    var widgetPanelId: Int,
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {
    constructor(context: Context, attrs: AttributeSet) : this(WidgetPanel.HOME.id, context, attrs)

    var widgetViewById = HashMap<Int, View>()

    open fun updateWidgets(activity: Activity, widgets: Collection<Widget>?) {
        synchronized(widgetViewById) {
            if (widgets == null) {
                return
            }
            Log.i("WidgetContainer", "updating ${activity.localClassName}")
            widgetViewById.forEach { removeView(it.value) }
            widgetViewById.clear()
            widgets.filter { it.panelId == widgetPanelId }.forEach { widget ->
                widget.createView(activity)?.let {
                    addView(it, LayoutParams(widget.position))
                    widgetViewById[widget.id] = it
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (ev == null) {
            return false
        }
        val position = PointF(ev.x, ev.y)

        return widgetViewById.filter {
            RectF(
                it.value.x,
                it.value.y,
                it.value.x + it.value.width,
                it.value.y + it.value.height
            ).contains(position) == true
        }.any {
            Widget.byId(it.key)?.allowInteraction == false
        }
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        var maxHeight = suggestedMinimumHeight
        var maxWidth = suggestedMinimumWidth

        val mWidth = MeasureSpec.getSize(widthMeasureSpec)
        val mHeight = MeasureSpec.getSize(heightMeasureSpec)

        (0..<size).map { getChildAt(it) }.forEach {
            val position = (it.layoutParams as LayoutParams).position.getAbsoluteRect(mWidth, mHeight)
            it.measure(makeMeasureSpec(position.width(), MeasureSpec.EXACTLY), makeMeasureSpec(position.height(), MeasureSpec.EXACTLY))
        }

        // Find rightmost and bottom-most child
        (0..<size).map { getChildAt(it) }.filter { it.visibility != GONE }.forEach {
            val position = (it.layoutParams as LayoutParams).position.getAbsoluteRect(mWidth, mHeight)
            maxWidth = max(maxWidth, position.left + it.measuredWidth)
            maxHeight = max(maxHeight, position.top + it.measuredHeight)
        }

        setMeasuredDimension(
            resolveSizeAndState(maxWidth.toInt(), widthMeasureSpec, 0),
            resolveSizeAndState(maxHeight.toInt(), heightMeasureSpec, 0)
        )
    }

    /**
     * Returns a set of layout parameters with a width of
     * [ViewGroup.LayoutParams.WRAP_CONTENT],
     * a height of [ViewGroup.LayoutParams.WRAP_CONTENT]
     * and with the coordinates (0, 0).
     */
    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(WidgetPosition(0,0,1,1))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0..<size) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams
            val position = lp.position.getAbsoluteRect(r - l, b - t)
            child.layout(position.left, position.top, position.right, position.bottom)
            child.layoutParams.width = position.width()
            child.layoutParams.height = position.height()
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    // Override to allow type-checking of LayoutParams.
    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    companion object {
        class LayoutParams : ViewGroup.LayoutParams {
            var position = WidgetPosition(0,0,4,4)


            constructor(position: WidgetPosition) : super(WRAP_CONTENT, WRAP_CONTENT) {
                this.position = position
            }
            constructor(c: Context, attrs: AttributeSet?) : super(c, attrs)
            constructor(source: ViewGroup.LayoutParams?) : super(source)

        }
    }
}
