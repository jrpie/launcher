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
    var width: Int,
    var height: Int,
    var edgeWidth: Float
) {
    private val ANGULAR_THRESHOLD = tan(Math.PI / 6)
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


    data class Vector(val x: Float, val y: Float) {
        fun absSquared(): Float {
            return this.x * this.x + this.y * this.y
        }

        fun plus(vector: Vector): Vector {
            return Vector(this.x + vector.x, this.y + vector.y)
        }

        fun max(other: Vector): Vector {
            return Vector(max(this.x, other.x), max(this.y, other.y))
        }

        fun min(other: Vector): Vector {
            return Vector(min(this.x, other.x), min(this.y, other.y))
        }

        operator fun minus(vector: Vector): Vector {
            return Vector(this.x - vector.x, this.y - vector.y)
        }
    }


    class PointerPath(
        val number: Int,
        val start: Vector,
        var last: Vector = start
    ) {
        var min = Vector(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        var max = Vector(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
        fun sizeSquared(): Float {
            return (max - min).absSquared()
        }

        fun getDirection(): Vector {
            return last - start
        }

        fun update(vector: Vector) {
            min = min.min(vector)
            max = max.max(vector)
            last = vector
        }
    }

    private fun PointerPath.startIntersectsSystemGestureInsets(): Boolean {
        // ignore x, since this makes edge swipes very hard to execute
        return start.y < systemGestureInsetTop
                || start.y > height - systemGestureInsetBottom
    }

    private fun PointerPath.intersectsSystemGestureInsets(): Boolean {
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

    private var paths = HashMap<Int, PointerPath>()

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

        val pointerIdToIndex =
            (0..<event.pointerCount).associateBy { event.getPointerId(it) }

        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            synchronized(this@TouchGestureDetector) {
                paths = HashMap()
                cancelled = false
            }
            longPressHandler.postDelayed({
                synchronized(this@TouchGestureDetector) {
                    if (cancelled) {
                        return@postDelayed
                    }
                    if (paths.entries.size == 1 && paths.entries.firstOrNull()?.value?.isTap() == true) {
                        cancelled = true
                        Gesture.LONG_CLICK.invoke(context)
                    }
                }
            }, LONG_PRESS_TIMEOUT.toLong())
        }

        // add new pointers
        for (i in 0..<event.pointerCount) {
            if (paths.containsKey(event.getPointerId(i))) {
                continue
            }
            val index = pointerIdToIndex[i] ?: continue
            paths[i] = PointerPath(
                paths.entries.size,
                Vector(event.getX(index), event.getY(index))
            )
        }

        for (i in 0..<event.pointerCount) {
            val index = pointerIdToIndex[i] ?: continue

            for (j in 0..<event.historySize) {
                paths[i]?.update(
                    Vector(
                        event.getHistoricalX(index, j),
                        event.getHistoricalY(index, j)
                    )
                )
            }
            paths[i]?.update(Vector(event.getX(index), event.getY(index)))
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
            classifyPaths(paths, event.downTime, event.eventTime)
        }
        return
    }

    private fun getGestureForDirection(direction: Vector): Gesture? {
        return if (ANGULAR_THRESHOLD * abs(direction.x) > abs(direction.y)) { // horizontal swipe
            if (direction.x > TOUCH_SLOP)
                Gesture.SWIPE_RIGHT
            else if (direction.x < -TOUCH_SLOP)
                Gesture.SWIPE_LEFT
            else null
        } else if (ANGULAR_THRESHOLD * abs(direction.y) > abs(direction.x)) { // vertical swipe
            if (direction.y < -TOUCH_SLOP)
                Gesture.SWIPE_UP
            else if (direction.y > TOUCH_SLOP)
                Gesture.SWIPE_DOWN
            else null
        } else null
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

        if (pointerCount == 1 && mainPointerPath.isTap()) {
            // detect taps

            if (duration in 0..TAP_TIMEOUT) {
                if (timeStart - lastTappedTime < DOUBLE_TAP_TIMEOUT &&
                    lastTappedLocation?.let {
                        (mainPointerPath.last - it).absSquared() < DOUBLE_TAP_SLOP_SQUARE
                    } == true
                ) {
                    Gesture.DOUBLE_CLICK.invoke(context)
                } else {
                    lastTappedTime = timeEnd
                    lastTappedLocation = mainPointerPath.last
                }
            }
        } else {
            // detect swipes

            val doubleActions = LauncherPreferences.enabled_gestures().doubleSwipe()
            val edgeActions = LauncherPreferences.enabled_gestures().edgeSwipe()

            var gesture = getGestureForDirection(mainPointerPath.getDirection())

            if (doubleActions && pointerCount > 1) {
                if (paths.entries.any { getGestureForDirection(it.value.getDirection()) != gesture }) {
                    // the directions of the pointers don't match
                    return
                }
                gesture = gesture?.let(Gesture::getDoubleVariant)
            }

            // detect triangles
            val startEndMin = mainPointerPath.start.min(mainPointerPath.last)
            val startEndMax = mainPointerPath.start.max(mainPointerPath.last)
            when (gesture) {
                Gesture.SWIPE_DOWN -> {
                    if (startEndMax.x + MIN_TRIANGLE_HEIGHT < mainPointerPath.max.x) {
                        gesture = Gesture.SWIPE_LARGER
                    } else if (startEndMin.x - MIN_TRIANGLE_HEIGHT > mainPointerPath.min.x) {
                        gesture = Gesture.SWIPE_SMALLER
                    }
                }

                Gesture.SWIPE_UP -> {
                    if (startEndMax.x + MIN_TRIANGLE_HEIGHT < mainPointerPath.max.x) {
                        gesture = Gesture.SWIPE_LARGER_REVERSE
                    } else if (startEndMin.x - MIN_TRIANGLE_HEIGHT > mainPointerPath.min.x) {
                        gesture = Gesture.SWIPE_SMALLER_REVERSE
                    }
                }

                Gesture.SWIPE_RIGHT -> {
                    if (startEndMax.y + MIN_TRIANGLE_HEIGHT < mainPointerPath.max.y) {
                        gesture = Gesture.SWIPE_V
                    } else if (startEndMin.y - MIN_TRIANGLE_HEIGHT > mainPointerPath.min.y) {
                        gesture = Gesture.SWIPE_LAMBDA
                    }
                }

                Gesture.SWIPE_LEFT -> {
                    if (startEndMax.y + MIN_TRIANGLE_HEIGHT < mainPointerPath.max.y) {
                        gesture = Gesture.SWIPE_V_REVERSE
                    } else if (startEndMin.y - MIN_TRIANGLE_HEIGHT > mainPointerPath.min.y) {
                        gesture = Gesture.SWIPE_LAMBDA_REVERSE
                    }
                }

                else -> {}
            }

            if (edgeActions) {
                if (mainPointerPath.max.x < edgeWidth * width) {
                    gesture = gesture?.getEdgeVariant(Gesture.Edge.LEFT)
                } else if (mainPointerPath.min.x > (1 - edgeWidth) * width) {
                    gesture = gesture?.getEdgeVariant(Gesture.Edge.RIGHT)
                }

                if (mainPointerPath.max.y < edgeWidth * height) {
                    gesture = gesture?.getEdgeVariant(Gesture.Edge.TOP)
                } else if (mainPointerPath.min.y > (1 - edgeWidth) * height) {
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
