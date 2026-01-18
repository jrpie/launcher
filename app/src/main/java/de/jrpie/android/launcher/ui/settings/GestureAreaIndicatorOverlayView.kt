package de.jrpie.android.launcher.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import de.jrpie.android.launcher.preferences.LauncherPreferences

/*
 * An overlay to indicate the areas where edge-gestures are detected
 */
class GestureAreaIndicatorOverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var horizontalWidth = 0.1f
    private var verticalWidth = 0.1f

    private lateinit var edgeLeft: Rect
    private lateinit var edgeRight: Rect
    private lateinit var edgeTop: Rect
    private lateinit var edgeBottom: Rect

    private val hideTask = Runnable {
        visibility = INVISIBLE
    }

    private var sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            if (prefKey == LauncherPreferences.enabled_gestures().keys().edgeSwipeEdgeWidth()) {

                this.removeCallbacks(hideTask)
                visibility = VISIBLE

                update()

                requestLayout()
                invalidate()

                this.postDelayed(hideTask, 3000)
            }
        }


    constructor(context: Context) : this(context, null)

    private val overlayPaint = Paint()

    init {
        overlayPaint.setARGB(50, 255, 0, 0)
        overlayPaint.strokeWidth = 10f

        update()
    }

    private fun update() {
        horizontalWidth = LauncherPreferences.enabled_gestures().edgeSwipeEdgeWidth() / 100f
        verticalWidth = horizontalWidth

        edgeTop = Rect(0, 0, (width * horizontalWidth).toInt(), height)
        edgeBottom = Rect((width * (1 - horizontalWidth)).toInt(), 0, width, height)
        edgeLeft = Rect(0, 0, width, (height * verticalWidth).toInt())
        edgeRight = Rect(0, (height * (1 - verticalWidth)).toInt(), width, height)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    override fun onDetachedFromWindow() {
        LauncherPreferences.getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {

        arrayOf(
            edgeLeft,
            edgeRight, edgeTop, edgeBottom
        ).forEach { e ->
            canvas.drawRect(e, overlayPaint)
        }
    }
}