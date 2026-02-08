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

        fun maximize(x: Float, y: Float) {
            this.x = max(this.x, x)
            this.y = max(this.y, y)
        }

        fun minimize(x: Float, y: Float) {
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
        end: Vector = start.copy()
    ) {
        private val start = start.copy()
        private val end = end.copy()
        private val min = Vector(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        private val max = Vector(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)

        fun sizeSquared(): Float {
            return (max - min).absSquared()
        }

        fun getDisplacement(): Vector {
            return end - start
        }

        fun update(x: Float, y: Float) {
            min.minimize(x, y)
            max.maximize(x, y)
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
     *  - the gesture was cancelled by MotionEvent.ACTION_CANCEL
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
                    event.getHistoricalX(index, j), event.getHistoricalY(index, j)
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

    private fun classifyPaths(paths: Map<Int, PointerPath>, timeStart: Long, timeEnd: Long) {
        val duration = timeEnd - timeStart
        val pointerCount = paths.entries.size
        if (paths.entries.isEmpty()) {
            return
        }

        val mainPointerPath = paths.entries.firstOrNull { it.value.number == 0 }?.value ?: return

        // Ignore swipes starting at the very top and the very bottom
        if (paths.entries.any { it.value.startIntersectsSystemGestureInsets() }) {
            return
        }

        val mainPointerPathEnd = mainPointerPath.getEnd()

        if (pointerCount == 1 && mainPointerPath.isTap()) {
            // detect taps

            if (duration in 0..TAP_TIMEOUT) {
                if (timeStart - lastTappedTime < DOUBLE_TAP_TIMEOUT &&
                    lastTappedLocation?.let {
                        (mainPointerPathEnd - it).absSquared() < DOUBLE_TAP_SLOP_SQUARE
                    } == true
                ) {
                    Gesture.DOUBLE_CLICK.invoke(context)
                } else {
                    lastTappedTime = timeEnd
                    lastTappedLocation = mainPointerPathEnd
                }
            }
        } else {
            // detect swipes

            val doubleActions = LauncherPreferences.enabled_gestures().doubleSwipe()
            val edgeActions = LauncherPreferences.enabled_gestures().edgeSwipe()

            var gesture = getGestureForDirection(mainPointerPath.getDisplacement())

            if (doubleActions && pointerCount > 1) {
                if (paths.entries.any { getGestureForDirection(it.value.getDisplacement()) != gesture }) {
                    // the directions of the pointers don't match
                    return
                }
                gesture = gesture?.let(Gesture::getDoubleVariant)
            }

            // detect triangles
            val mainPointerPathStart = mainPointerPath.getStart()
            val startEndMin = mainPointerPathStart.minimum(mainPointerPathEnd)
            val startEndMax = mainPointerPathStart.maximum(mainPointerPathEnd)
            val mainPointerPathMax = mainPointerPath.getMax()
            val mainPointerPathMin = mainPointerPath.getMin()
            when (gesture) {
                Gesture.SWIPE_DOWN -> {
                    if (startEndMax.x + MIN_TRIANGLE_HEIGHT < mainPointerPathMax.x) {
                        gesture = Gesture.SWIPE_LARGER
                    } else if (startEndMin.x - MIN_TRIANGLE_HEIGHT > mainPointerPathMin.x) {
                        gesture = Gesture.SWIPE_SMALLER
                    }
                }

                Gesture.SWIPE_UP -> {
                    if (startEndMax.x + MIN_TRIANGLE_HEIGHT < mainPointerPathMax.x) {
                        gesture = Gesture.SWIPE_LARGER_REVERSE
                    } else if (startEndMin.x - MIN_TRIANGLE_HEIGHT > mainPointerPathMin.x) {
                        gesture = Gesture.SWIPE_SMALLER_REVERSE
                    }
                }

                Gesture.SWIPE_RIGHT -> {
                    if (startEndMax.y + MIN_TRIANGLE_HEIGHT < mainPointerPathMax.y) {
                        gesture = Gesture.SWIPE_V
                    } else if (startEndMin.y - MIN_TRIANGLE_HEIGHT > mainPointerPathMin.y) {
                        gesture = Gesture.SWIPE_LAMBDA
                    }
                }

                Gesture.SWIPE_LEFT -> {
                    if (startEndMax.y + MIN_TRIANGLE_HEIGHT < mainPointerPathMax.y) {
                        gesture = Gesture.SWIPE_V_REVERSE
                    } else if (startEndMin.y - MIN_TRIANGLE_HEIGHT > mainPointerPathMin.y) {
                        gesture = Gesture.SWIPE_LAMBDA_REVERSE
                    }
                }

                else -> {}
            }

            if (edgeActions) {
                if (mainPointerPathMax.x < edgeWidth * width) {
                    gesture = gesture?.getEdgeVariant(Gesture.Edge.LEFT)
                } else if (mainPointerPathMin.x > (1 - edgeWidth) * width) {
                    gesture = gesture?.getEdgeVariant(Gesture.Edge.RIGHT)
                }

                if (mainPointerPathMax.y < edgeWidth * height) {
                    gesture = gesture?.getEdgeVariant(Gesture.Edge.TOP)
                } else if (mainPointerPathMin.y > (1 - edgeWidth) * height) {
                    gesture = gesture?.getEdgeVariant(Gesture.Edge.BOTTOM)
                }
            }

            if (timeStart - lastTappedTime < 2 * DOUBLE_TAP_TIMEOUT) {
                gesture = gesture?.getTapComboVariant()
            }
            gesture?.invoke(context)
        }
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
