package de.jrpie.android.launcher.ui

import android.content.Context
import android.graphics.Insets
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.annotation.RequiresApi
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.preferences.LauncherPreferences
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

@Suppress("PrivatePropertyName")
class TouchGestureDetector(
    private val context: Context,
    private var width: Int,
    private var height: Int,
    private var edgeWidth: Float
) {
    private val ANGULAR_THRESHOLD = tan(Math.PI / 8)
    private val TOUCH_SLOP: Int
    private val TOUCH_SLOP_SQUARE: Int
    private val DOUBLE_TAP_SLOP: Int
    private val DOUBLE_TAP_SLOP_SQUARE: Int
    private val LONG_PRESS_TIMEOUT: Int
    private val TAP_TIMEOUT: Int
    private val DOUBLE_TAP_TIMEOUT: Int

    private val MIN_TRIANGLE_HEIGHT = 250

    private var systemGestureInsetTop = 100
    private var systemGestureInsetBottom = 0
    private var systemGestureInsetLeft = 100
    private var systemGestureInsetRight = 100

    private val longPressHandler = Handler(Looper.getMainLooper())

    data class Vector(var x: Float, var y: Float) {
        fun absSquared(): Float {
            return this.x * this.x + this.y * this.y
        }

        operator fun plus(vector: Vector): Vector {
            return Vector(this.x + vector.x, this.y + vector.y)
        }

        operator fun minus(vector: Vector): Vector {
            return Vector(this.x - vector.x, this.y - vector.y)
        }

        fun maximum(v: Vector): Vector {
            return Vector(max(this.x, v.x), max(this.y, v.y))
        }

        fun minimum(v: Vector): Vector {
            return Vector(min(this.x, v.x), min(this.y, v.y))
        }

        fun updateMaximum(x: Float, y: Float) {
            this.x = max(this.x, x)
            this.y = max(this.y, y)
        }

        fun updateMinimum(x: Float, y: Float) {
            this.x = min(this.x, x)
            this.y = min(this.y, y)
        }

        fun update(x: Float, y: Float) {
            this.x = x
            this.y = y
        }
    }

    class PointerPath(
        val number: Int,
        start: Vector,
        end: Vector = start
    ) {
        private val start = start.copy()
        private val end = end.copy()
        private val min = Vector(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        private val max = Vector(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)

        fun sizeSquared(): Float {
            return (max - min).absSquared()
        }

        fun getDirection(): Vector {
            return end - start
        }

        fun update(x: Float, y: Float) {
            min.updateMinimum(x, y)
            max.updateMaximum(x, y)
            end.update(x, y)
        }

        fun getStart() = start.copy()
        fun getEnd() = end.copy()
        fun getMin() = min.copy()
        fun getMax() = max.copy()
    }

    private fun PointerPath.startIntersectsSystemGestureInsets(): Boolean {
        val start = this.getStart()
        // ignore x, since this makes edge swipes very hard to execute
        return start.y < systemGestureInsetTop
                || start.y > height - systemGestureInsetBottom
    }

    private fun PointerPath.intersectsSystemGestureInsets(): Boolean {
        val min = this.getMin()
        val max = this.getMax()
        return min.x < systemGestureInsetLeft
                || min.y < systemGestureInsetTop
                || max.x > width - systemGestureInsetRight
                || max.y > height - systemGestureInsetBottom
    }

    private fun PointerPath.isTap(): Boolean {
        if (intersectsSystemGestureInsets()) {
            return false
        }
        return sizeSquared() < TOUCH_SLOP_SQUARE
    }

    init {
        val configuration = ViewConfiguration.get(context)
        TOUCH_SLOP = configuration.scaledTouchSlop
        TOUCH_SLOP_SQUARE = TOUCH_SLOP * TOUCH_SLOP
        DOUBLE_TAP_SLOP = configuration.scaledDoubleTapSlop
        DOUBLE_TAP_SLOP_SQUARE = DOUBLE_TAP_SLOP * DOUBLE_TAP_SLOP

        LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout()
        TAP_TIMEOUT = ViewConfiguration.getTapTimeout()
        DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout()
    }

    private val idToPath = HashMap<Int, PointerPath>()

    /* Set when
     *  - the longPressHandler has detected this gesture as a long press
     *  - the gesture was canceled by MotionEvent.ACTION_CANCEL
     * In any case, the current gesture should be ignored by further detection logic.
     */
    private var cancelled = false

    private var lastTappedTime = 0L
    private var lastTappedLocation: Vector? = null

    fun onTouchEvent(event: MotionEvent) {

        if (event.actionMasked == MotionEvent.ACTION_CANCEL) {
            synchronized(this@TouchGestureDetector) {
                cancelled = true
            }
        }

        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            synchronized(this@TouchGestureDetector) {
                idToPath.clear()
                cancelled = false
            }
            longPressHandler.postDelayed({
                synchronized(this@TouchGestureDetector) {
                    if (cancelled) {
                        return@postDelayed
                    }
                    if (idToPath.size == 1 && idToPath.entries.firstOrNull()?.value?.isTap() == true) {
                        cancelled = true
                        Gesture.LONG_CLICK.invoke(context)
                    }
                }
            }, LONG_PRESS_TIMEOUT.toLong())
        }

        for (index in 0..<event.pointerCount) {
            val id = event.getPointerId(index)

            if (!idToPath.containsKey(id)) {
                idToPath[id] = PointerPath(
                    idToPath.size,
                    Vector(event.getX(index), event.getY(index))
                )
            }

            for (j in 0..<event.historySize) {
                idToPath[id]?.update(
                    event.getHistoricalX(index, j),
                    event.getHistoricalY(index, j)
                )
            }

            idToPath[id]?.update(event.getX(index), event.getY(index))
        }

        if (event.actionMasked == MotionEvent.ACTION_UP) {
            synchronized(this@TouchGestureDetector) {
                // if the long press handler is still running, kill it
                longPressHandler.removeCallbacksAndMessages(null)
                // if the gesture was already detected as a long click, there is nothing to do
                if (cancelled) {
                    return
                }
            }
            classifyPaths(idToPath, event.downTime, event.eventTime)
        }
        return
    }

    private fun getGestureForDirection(direction: Vector): Gesture? {
        val absX = abs(direction.x)
        val absY = abs(direction.y)

        return when {
            // horizontal
            ANGULAR_THRESHOLD * absX > absY -> {
                if (direction.x > TOUCH_SLOP) Gesture.SWIPE_RIGHT
                else if (direction.x < -TOUCH_SLOP) Gesture.SWIPE_LEFT
                else null
            }
            // vertical
            ANGULAR_THRESHOLD * absY > absX -> {
                if (direction.y < -TOUCH_SLOP) Gesture.SWIPE_UP
                else if (direction.y > TOUCH_SLOP) Gesture.SWIPE_DOWN
                else null
            }
            // diagonal
            else -> {
                if (direction.x > TOUCH_SLOP && direction.y < -TOUCH_SLOP) Gesture.SWIPE_SLASH_REVERSE
                else if (direction.x < -TOUCH_SLOP && direction.y < -TOUCH_SLOP) Gesture.SWIPE_BACKSLASH_REVERSE
                else if (direction.x > TOUCH_SLOP && direction.y > TOUCH_SLOP) Gesture.SWIPE_BACKSLASH
                else if (direction.x < -TOUCH_SLOP && direction.y > TOUCH_SLOP) Gesture.SWIPE_SLASH
                else null
            }
        }
    }

    private fun classifyPaths(idToPath: Map<Int, PointerPath>, timeStart: Long, timeEnd: Long) {
        val mainPointerPath = idToPath.values.firstOrNull { it.number == 0 } ?: return

        // Ignore swipes starting at the very top and the very bottom
        if (idToPath.values.any { it.startIntersectsSystemGestureInsets() }) {
            return
        }

        val timeSinceLastTap = timeStart - lastTappedTime

        val pointerCount = idToPath.size
        val mainPointerPathStart = mainPointerPath.getStart()
        val mainPointerPathEnd = mainPointerPath.getEnd()

        // detect taps
        if (pointerCount == 1 && timeEnd - timeStart in 0..TAP_TIMEOUT && mainPointerPath.isTap()) {
            if (timeSinceLastTap < DOUBLE_TAP_TIMEOUT && lastTappedLocation?.let {
                    (mainPointerPathEnd - it).absSquared() < DOUBLE_TAP_SLOP_SQUARE
                } == true) {
                Gesture.DOUBLE_CLICK.invoke(context)
            } else {
                lastTappedTime = timeEnd
                lastTappedLocation = mainPointerPathEnd
            }

            return
        }

        var gesture = getGestureForDirection(mainPointerPath.getDirection()) ?: return

        // ignore multiple concurrent swipes that don't match
        if (idToPath.values.any { getGestureForDirection(it.getDirection()) != gesture }) {
            return
        }

        // double swipe variants
        if (LauncherPreferences.enabled_gestures().doubleSwipe()) {
            if (pointerCount > 1) {
                gesture = gesture.let(Gesture::getDoubleVariant)
            }
        }

        val startEndMin = mainPointerPathStart.minimum(mainPointerPathEnd)
        val startEndMax = mainPointerPathStart.maximum(mainPointerPathEnd)

        val mainPointerPathMin = mainPointerPath.getMin()
        val mainPointerPathMax = mainPointerPath.getMax()

        // vertical triangle variants
        if (startEndMax.x + MIN_TRIANGLE_HEIGHT < mainPointerPathMax.x) {
            gesture = gesture.getTriangleVariant(Gesture.Direction.RIGHT)
        } else if (startEndMin.x - MIN_TRIANGLE_HEIGHT > mainPointerPathMin.x) {
            gesture = gesture.getTriangleVariant(Gesture.Direction.LEFT)
        }

        // horizontal triangle variants
        if (startEndMax.y + MIN_TRIANGLE_HEIGHT < mainPointerPathMax.y) {
            gesture = gesture.getTriangleVariant(Gesture.Direction.DOWN)
        } else if (startEndMin.y - MIN_TRIANGLE_HEIGHT > mainPointerPathMin.y) {
            gesture = gesture.getTriangleVariant(Gesture.Direction.UP)
        }

        if (LauncherPreferences.enabled_gestures().edgeSwipe()) {
            // left and right edge variants
            if (mainPointerPathMax.x < edgeWidth * width) {
                gesture = gesture.getEdgeVariant(Gesture.Edge.LEFT)
            } else if (mainPointerPathMin.x > (1 - edgeWidth) * width) {
                gesture = gesture.getEdgeVariant(Gesture.Edge.RIGHT)
            }

            // top and bottom edge variants
            if (mainPointerPathMax.y < edgeWidth * height) {
                gesture = gesture.getEdgeVariant(Gesture.Edge.TOP)
            } else if (mainPointerPathMin.y > (1 - edgeWidth) * height) {
                gesture = gesture.getEdgeVariant(Gesture.Edge.BOTTOM)
            }
        }

        // tap combo variants
        if (timeSinceLastTap < 2 * DOUBLE_TAP_TIMEOUT) {
            gesture = gesture.getTapComboVariant()
        }

        gesture.invoke(context)
    }

    fun updateScreenSize(windowManager: WindowManager) {
        val displayMetrics = DisplayMetrics()
        @Suppress("deprecation") // required to support API < 30
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        height = displayMetrics.heightPixels
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun setSystemGestureInsets(insets: Insets) {
        systemGestureInsetTop = insets.top
        systemGestureInsetBottom = insets.bottom
        systemGestureInsetLeft = insets.left
        systemGestureInsetRight = insets.right
    }
}
