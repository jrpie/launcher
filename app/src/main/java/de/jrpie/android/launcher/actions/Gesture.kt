package de.jrpie.android.launcher.actions

import android.content.Context
import android.util.Log
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.preferences.LauncherPreferences

/**
 * @param id internal id to serialize the action. Used as a key in shared preferences.
 * @param defaultsResource res id of array of default actions for the gesture.
 * @param labelResource res id of the name of the gesture.
 * @param animationIn res id of transition animation (in) when using the gesture to launch an app.
 * @param animationOut res id of transition animation (out) when using the gesture to launch an app.
 */
enum class Gesture(
    val id: String,
    private val labelResource: Int,
    private val descriptionResource: Int,
    internal val defaultsResource: Int,
    val animationIn: Int = android.R.anim.fade_in,
    val animationOut: Int = android.R.anim.fade_out
) {
    VOLUME_UP(
        "action.volume_up",
        R.string.settings_gesture_vol_up,
        R.string.settings_gesture_description_vol_up,
        R.array.default_volume_up,
        0,
        0
    ),
    VOLUME_DOWN(
        "action.volume_down",
        R.string.settings_gesture_vol_down,
        R.string.settings_gesture_description_vol_down,
        R.array.default_volume_down, 0, 0
    ),
    TIME(
        "action.time",
        R.string.settings_gesture_time,
        R.string.settings_gesture_description_time,
        R.array.default_time
    ),
    DATE(
        "action.date",
        R.string.settings_gesture_date,
        R.string.settings_gesture_description_date,
        R.array.default_date
    ),
    BACK(
        "action.back",
        R.string.settings_gesture_back,
        R.string.settings_gesture_description_back,
        R.array.default_back
    ),
    LONG_CLICK(
        "action.long_click",
        R.string.settings_gesture_long_click,
        R.string.settings_gesture_description_long_click,
        R.array.default_long_click, 0, 0
    ),
    DOUBLE_CLICK(
        "action.double_click",
        R.string.settings_gesture_double_click,
        R.string.settings_gesture_description_double_click,
        R.array.default_double_click, 0, 0
    ),
    SWIPE_UP(
        "action.up",
        R.string.settings_gesture_up,
        R.string.settings_gesture_description_up,
        R.array.default_up,
        R.anim.bottom_up
    ),
    SWIPE_UP_LEFT_EDGE(
        "action.up_left",
        R.string.settings_gesture_up_left_edge,
        R.string.settings_gesture_description_up_left_edge,
        R.array.default_up_left,
        R.anim.bottom_up
    ),
    SWIPE_UP_RIGHT_EDGE(
        "action.up_right",
        R.string.settings_gesture_up_right_edge,
        R.string.settings_gesture_description_up_right_edge,
        R.array.default_up_right,
        R.anim.bottom_up
    ),
    TAP_AND_SWIPE_UP(
        "action.tap_up",
        R.string.settings_gesture_tap_up,
        R.string.settings_gesture_description_tap_up,
        R.array.default_up,
        R.anim.bottom_up
    ),
    SWIPE_UP_DOUBLE(
        "action.double_up",
        R.string.settings_gesture_double_up,
        R.string.settings_gesture_description_double_up,
        R.array.default_double_up,
        R.anim.bottom_up
    ),
    SWIPE_DOWN(
        "action.down",
        R.string.settings_gesture_down,
        R.string.settings_gesture_description_down,
        R.array.default_down,
        R.anim.top_down
    ),
    SWIPE_DOWN_LEFT_EDGE(
        "action.down_left",
        R.string.settings_gesture_down_left_edge,
        R.string.settings_gesture_description_down_left_edge,
        R.array.default_down_left,
        R.anim.top_down
    ),
    SWIPE_DOWN_RIGHT_EDGE(
        "action.down_right",
        R.string.settings_gesture_down_right_edge,
        R.string.settings_gesture_description_down_right_edge,
        R.array.default_down_right,
        R.anim.top_down
    ),
    TAP_AND_SWIPE_DOWN(
        "action.tap_down",
        R.string.settings_gesture_tap_down,
        R.string.settings_gesture_description_tap_down,
        R.array.default_down,
        R.anim.bottom_up
    ),
    SWIPE_DOWN_DOUBLE(
        "action.double_down",
        R.string.settings_gesture_double_down,
        R.string.settings_gesture_description_double_down,
        R.array.default_double_down,
        R.anim.top_down
    ),
    SWIPE_LEFT(
        "action.left",
        R.string.settings_gesture_left,
        R.string.settings_gesture_description_left,
        R.array.default_messengers,
        R.anim.right_left
    ),
    SWIPE_LEFT_TOP_EDGE(
        "action.left_top",
        R.string.settings_gesture_left_top_edge,
        R.string.settings_gesture_description_left_top_edge,
        R.array.default_messengers,
        R.anim.right_left
    ),
    SWIPE_LEFT_BOTTOM_EDGE(
        "action.left_bottom",
        R.string.settings_gesture_left_bottom_edge,
        R.string.settings_gesture_description_left_bottom_edge,
        R.array.default_messengers,
        R.anim.right_left
    ),
    TAP_AND_SWIPE_LEFT(
        "action.tap_left",
        R.string.settings_gesture_tap_left,
        R.string.settings_gesture_description_tap_left,
        R.array.default_messengers,
        R.anim.right_left
    ),
    SWIPE_LEFT_DOUBLE(
        "action.double_left",
        R.string.settings_gesture_double_left,
        R.string.settings_gesture_description_double_left,
        R.array.default_messengers,
        R.anim.right_left
    ),
    SWIPE_RIGHT(
        "action.right",
        R.string.settings_gesture_right,
        R.string.settings_gesture_description_right,
        R.array.default_right,
        R.anim.left_right
    ),
    SWIPE_RIGHT_TOP_EDGE(
        "action.right_top",
        R.string.settings_gesture_right_top_edge,
        R.string.settings_gesture_description_right_top_edge,
        R.array.default_right_top,
        R.anim.left_right
    ),
    SWIPE_RIGHT_BOTTOM_EDGE(
        "action.right_bottom",
        R.string.settings_gesture_right_bottom_edge,
        R.string.settings_gesture_description_right_bottom_edge,
        R.array.default_right_bottom,
        R.anim.left_right
    ),
    TAP_AND_SWIPE_RIGHT(
        "action.tap_right",
        R.string.settings_gesture_tap_right,
        R.string.settings_gesture_description_tap_right,
        R.array.default_right,
        R.anim.left_right
    ),
    SWIPE_RIGHT_DOUBLE(
        "action.double_right",
        R.string.settings_gesture_double_right,
        R.string.settings_gesture_description_double_right,
        R.array.default_double_right,
        R.anim.left_right
    ),
    SWIPE_LARGER(
        "action.larger",
        R.string.settings_gesture_swipe_larger,
        R.string.settings_gesture_description_swipe_larger,
        R.array.no_default
    ),
    SWIPE_LARGER_REVERSE(
        "action.larger_reverse",
        R.string.settings_gesture_swipe_larger_reverse,
        R.string.settings_gesture_description_swipe_larger_reverse,
        R.array.no_default
    ),
    SWIPE_SMALLER(
        "action.smaller",
        R.string.settings_gesture_swipe_smaller,
        R.string.settings_gesture_description_swipe_smaller,
        R.array.no_default
    ),
    SWIPE_SMALLER_REVERSE(
        "action.smaller_reverse",
        R.string.settings_gesture_swipe_smaller_reverse,
        R.string.settings_gesture_description_swipe_smaller_reverse,
        R.array.no_default
    ),
    SWIPE_LAMBDA(
        "action.lambda",
        R.string.settings_gesture_swipe_lambda,
        R.string.settings_gesture_description_swipe_lambda,
        R.array.no_default
    ),
    SWIPE_LAMBDA_REVERSE(
        "action.lambda_reverse",
        R.string.settings_gesture_swipe_lambda_reverse,
        R.string.settings_gesture_description_swipe_lambda_reverse,
        R.array.no_default
    ),
    SWIPE_V(
        "action.v",
        R.string.settings_gesture_swipe_v,
        R.string.settings_gesture_description_swipe_v,
        R.array.no_default
    ),
    SWIPE_V_REVERSE(
        "action.v_reverse",
        R.string.settings_gesture_swipe_v_reverse,
        R.string.settings_gesture_description_swipe_v_reverse,
        R.array.no_default
    );

    enum class Edge {
        TOP, BOTTOM, LEFT, RIGHT
    }

    fun getLabel(context: Context): String {
        return context.resources.getString(this.labelResource)
    }

    fun getDescription(context: Context): String {
        return context.resources.getString(this.descriptionResource)
    }

    fun getDoubleVariant(): Gesture {
        return when (this) {
            SWIPE_UP -> SWIPE_UP_DOUBLE
            SWIPE_DOWN -> SWIPE_DOWN_DOUBLE
            SWIPE_LEFT -> SWIPE_LEFT_DOUBLE
            SWIPE_RIGHT -> SWIPE_RIGHT_DOUBLE
            else -> this
        }
    }

    fun getEdgeVariant(edge: Edge): Gesture {
        return when (edge) {
            Edge.TOP ->
                when (this) {
                    SWIPE_LEFT -> SWIPE_LEFT_TOP_EDGE
                    SWIPE_RIGHT -> SWIPE_RIGHT_TOP_EDGE
                    else -> this
                }

            Edge.BOTTOM ->
                when (this) {
                    SWIPE_LEFT -> SWIPE_LEFT_BOTTOM_EDGE
                    SWIPE_RIGHT -> SWIPE_RIGHT_BOTTOM_EDGE
                    else -> this
                }

            Edge.LEFT ->
                when (this) {
                    SWIPE_UP -> SWIPE_UP_LEFT_EDGE
                    SWIPE_DOWN -> SWIPE_DOWN_LEFT_EDGE
                    else -> this
                }

            Edge.RIGHT ->
                when (this) {
                    SWIPE_UP -> SWIPE_UP_RIGHT_EDGE
                    SWIPE_DOWN -> SWIPE_DOWN_RIGHT_EDGE
                    else -> this
                }
        }
    }

    fun getTapComboVariant(): Gesture {
        return when (this) {
            SWIPE_UP -> TAP_AND_SWIPE_UP
            SWIPE_DOWN -> TAP_AND_SWIPE_DOWN
            SWIPE_LEFT -> TAP_AND_SWIPE_LEFT
            SWIPE_RIGHT -> TAP_AND_SWIPE_RIGHT
            else -> this
        }

    }

    fun isDoubleVariant(): Boolean {
        return when (this) {
            SWIPE_UP_DOUBLE,
            SWIPE_DOWN_DOUBLE,
            SWIPE_LEFT_DOUBLE,
            SWIPE_RIGHT_DOUBLE -> true

            else -> false
        }
    }

    fun isEdgeVariant(): Boolean {
        return when (this) {
            SWIPE_UP_RIGHT_EDGE,
            SWIPE_UP_LEFT_EDGE,
            SWIPE_DOWN_LEFT_EDGE,
            SWIPE_DOWN_RIGHT_EDGE,
            SWIPE_LEFT_TOP_EDGE,
            SWIPE_LEFT_BOTTOM_EDGE,
            SWIPE_RIGHT_TOP_EDGE,
            SWIPE_RIGHT_BOTTOM_EDGE -> true

            else -> false
        }
    }

    fun isEnabled(): Boolean {
        if (isEdgeVariant()) {
            return LauncherPreferences.enabled_gestures().edgeSwipe()
        }
        if (isDoubleVariant()) {
            return LauncherPreferences.enabled_gestures().doubleSwipe()
        }
        return true
    }

    operator fun invoke(context: Context) {
        Log.i("Launcher", "Detected gesture: $this")
        val action = Action.forGesture(this)
        Action.launch(action, context, this)
    }

    companion object {
        fun byId(id: String): Gesture? {
            return Gesture.entries.firstOrNull { it.id == id }
        }
    }

}
