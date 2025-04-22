package de.jrpie.android.launcher.ui.widgets

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.util.SizeF
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import androidx.core.view.size
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.widgets.WidgetPosition
import de.jrpie.android.launcher.widgets.createAppWidgetView
import kotlin.math.max


// TODO: implement layout logic instead of linear layout
/**
 * This only works in an Activity, not AppCompatActivity
 */
open class WidgetContainerView(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    open fun updateWidgets(activity: Activity) {
        Log.i("WidgetContainer", "updating ${activity.localClassName}")
        (0..<size).forEach { removeViewAt(0) }
        val dp = activity.resources.displayMetrics.density
        val screenWidth = activity.resources.displayMetrics.widthPixels
        val screenHeight = activity.resources.displayMetrics.heightPixels
        LauncherPreferences.internal().widgets()?.forEach { widget ->
                createAppWidgetView(activity, widget)?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val absolutePosition = widget.position.getAbsoluteRect(screenWidth, screenHeight)
                        it.updateAppWidgetSize(Bundle.EMPTY,
                            listOf(SizeF(
                                absolutePosition.width() / dp,
                                absolutePosition.height() / dp
                        )))
                        Log.i("WidgetContainer", "Adding widget ${widget.id} at ${widget.position} ($absolutePosition)")
                    } else {
                        Log.i("WidgetContainer", "Adding widget ${widget.id} at ${widget.position}")
                    }
                    addView(it, WidgetContainerView.Companion.LayoutParams(widget.position))
                }
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
            Log.e("measure", "$position")
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
            //if (child.visibility != GONE) {
                val lp = child.layoutParams as LayoutParams

                val position = lp.position.getAbsoluteRect(r - l, b - t)
                Log.e("onLayout", "$l, $t, $r, $b, absolute rect: $position")
                child.layout(position.left, position.top, position.right, position.bottom)
            child.layoutParams.width = position.width()
            child.layoutParams.height = position.height()
            //}
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