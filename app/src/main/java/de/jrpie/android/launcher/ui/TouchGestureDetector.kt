package de.jrpie.android.launcher.ui

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.preferences.LauncherPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.tan

class TouchGestureDetector(
    private val context: Context,
    val width: Int,
    val height: Int,
    var edgeWidth: Float,
    val onLog: (String) -> Unit
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

    var log_output = ""
    private var log_output_ui = ""

    private fun log(s: String, ui:Boolean = true) {
        Log.i("Gesture Detection", s)
        log_output += "$s"
        if (ui) {
            var lines = 50
            log_output_ui = (log_output_ui + "$s").takeLastWhile {
                if (it == '\n') {
                    lines--
                }
                lines > 0
            }
            onLog(log_output_ui)
        }
    }



    @Serializable
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


    @Serializable
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

    private fun PointerPath.isTap(): Boolean {
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

        log(
            "TOUCH_SLOP: $TOUCH_SLOP\n" +
                    "DOUBLE_TAP_SLOP: $DOUBLE_TAP_SLOP\n" +
                    "LONG_PRESS_TIMEOUT: $LONG_PRESS_TIMEOUT\n" +
                    "TAP_TIMEOUT: $TAP_TIMEOUT\n" +
                    "DOUBLE_TAP_TIMEOUT: $DOUBLE_TAP_TIMEOUT\n======================\n"
        )
    }

    private var paths = HashMap<Int, PointerPath>()

    private var lastTappedTime = 0L
    private var lastTappedLocation: Vector? = null

    fun onTouchEvent(event: MotionEvent): Boolean {
        val pointerIdToIndex =
            (0..<event.pointerCount).associateBy { event.getPointerId(it) }

        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            log("\n========================\nNew gesture")
            paths = HashMap()
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
            classifyPaths(paths, event.downTime, event.eventTime)
        }
        return true
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
        log(" - gesture complete")
        log("\npaths = ${paths.entries.map { (x,y) -> "$x: ${Json.encodeToString(y)}"}}", false)
        val duration = timeEnd - timeStart
        val pointerCount = paths.entries.size
        log("\nDuration: $duration, pointers: $pointerCount")
        if (paths.entries.isEmpty()) {
            return
        }

        val mainPointerPath = paths.entries.firstOrNull { it.value.number == 0 }?.value ?: return

        // Ignore swipes at the very top, since this interferes with the status bar.
        // TODO: replace 100px by sensible dp value (e.g. twice the height of the status bar)
        if (paths.entries.any { it.value.start.y < 100 }) {
            log("\nToo close to the top")
            return
        }

        if (pointerCount == 1 && mainPointerPath.isTap()) {
            // detect taps
            log("\nSlop: ${sqrt(mainPointerPath.sizeSquared())} - click")
            log("\nTime since last click: ${timeStart - lastTappedTime}")
            log(
                "\nDistance to last click: ${
                    sqrt(
                        (mainPointerPath.last - (lastTappedLocation ?: Vector(
                            Float.NEGATIVE_INFINITY,
                            Float.NEGATIVE_INFINITY
                        ))).absSquared()
                            .toDouble()
                    )
                }"
            )

            if (duration in 0..TAP_TIMEOUT) {
                if (timeStart - lastTappedTime < DOUBLE_TAP_TIMEOUT &&
                    lastTappedLocation?.let {
                        (mainPointerPath.last - it).absSquared() < DOUBLE_TAP_SLOP_SQUARE
                    } == true
                ) {
                    log("\nDouble click detected")
                    Gesture.DOUBLE_CLICK.invoke(context)
                } else {
                    log("\nNot a double click: last tap too long ago")
                    lastTappedTime = timeEnd
                    lastTappedLocation = mainPointerPath.last
                }
            } else if (duration > LONG_PRESS_TIMEOUT) {
                // TODO: Don't wait until the finger is lifted.
                // Instead set a timer to start long click as soon as LONG_PRESS_TIMEOUT is reached
                log("\nLong click detected")
                Gesture.LONG_CLICK.invoke(context)
            }
        } else {
            log("\nSlop: ${sqrt(mainPointerPath.sizeSquared())} - not a click")
            // detect swipes

            val doubleActions = LauncherPreferences.enabled_gestures().doubleSwipe()
            val edgeActions = LauncherPreferences.enabled_gestures().edgeSwipe()

            var gesture = getGestureForDirection(mainPointerPath.getDirection())
            log("\nbase gesture: ${gesture}")

            if (doubleActions && pointerCount > 1) {
                if (paths.entries.any { getGestureForDirection(it.value.getDirection()) != gesture }) {
                    // the directions of the pointers don't match
                    log("\ndirections of the pointers don't match")
                    return
                }
                gesture = gesture?.let(Gesture::getDoubleVariant)
                log(", double variant: ${gesture}")
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
            log(", v-variant: $gesture")

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
            log(", edge-variant: $gesture")

            if (timeStart - lastTappedTime < 2 * DOUBLE_TAP_TIMEOUT) {
                gesture = gesture?.getTapComboVariant()
            }
            log(", tap-combo-variant: $gesture")
            gesture?.invoke(context)
        }
    }
}