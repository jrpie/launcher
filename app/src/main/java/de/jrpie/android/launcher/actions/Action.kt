package de.jrpie.android.launcher.actions

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences.Editor
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.edit
import com.google.android.material.snackbar.Snackbar
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.list.SelectActionActivity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


/**
 * Represents an action that can be bound to a [Gesture].
 * There are four types of actions: [AppAction], [ShortcutAction], [LauncherAction] and [WidgetPanelAction]
 */
@Serializable
sealed interface Action {
    fun invoke(context: Context, rect: Rect? = null): Boolean
    fun label(context: Context): String
    fun getIcon(context: Context): Drawable?
    fun isAvailable(context: Context): Boolean

    fun showConfigurationDialog(context: Context, onSuccess: (Action) -> Unit) {
        onSuccess(this)
    }

    // Can the action be used to reach µLauncher settings?
    fun canReachSettings(): Boolean


    fun bindToGesture(prefEditor: Editor, id: String) {
        prefEditor.putString(id, Json.encodeToString(this))
    }

    companion object {

        fun forGesture(gesture: Gesture): Action? {
            val id = gesture.id

            val preferences = LauncherPreferences.getSharedPreferences()
            val json = preferences.getString(id, "null")!!
            return Json.decodeFromString(json)
        }

        fun resetToDefaultActions(context: Context) {
            LauncherPreferences.getSharedPreferences().edit {
                val boundActions = HashSet<String>()
                Gesture.entries.forEach { gesture ->
                    context.resources
                        .getStringArray(gesture.defaultsResource)
                        .filterNot { boundActions.contains(it) }
                        .map { Pair(it, Json.decodeFromString<Action>(it)) }
                        .firstOrNull { it.second.isAvailable(context) }
                        ?.apply {
                            // allow to bind CHOOSE to multiple gestures
                            if (second != LauncherAction.CHOOSE) {
                                boundActions.add(first)
                            }
                            second.bindToGesture(this@edit, gesture.id)
                        }
                }
            }
        }

        fun setActionForGesture(gesture: Gesture, action: Action?) {
            if (action == null) {
                clearActionForGesture(gesture)
                return
            }
            LauncherPreferences.getSharedPreferences().edit {
                action.bindToGesture(this, gesture.id)
            }
        }

        fun clearActionForGesture(gesture: Gesture) {
            LauncherPreferences.getSharedPreferences().edit {
                remove(gesture.id)
            }
        }

        fun launch(
            action: Action?,
            context: Context,
            gesture: Gesture?
        ) {
            if (action != null && action.invoke(context)) {
                if (context is Activity) {
                    val animationIn = gesture?.animationIn ?: android.R.anim.fade_in
                    val animationOut = gesture?.animationOut ?: android.R.anim.fade_out
                    // There does not seem to be a good alternative to overridePendingTransition.
                    // Note that we can't use overrideActivityTransition here.
                    @Suppress("deprecation")
                    context.overridePendingTransition(animationIn, animationOut)
                }
            } else {
                if (context is Activity && gesture != null) {
                    val message = context.getString(
                        R.string.snackbar_cant_open_message,
                        gesture.getLabel(context)
                    )
                    Snackbar
                        .make(context.window.decorView.rootView, message, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.snackbar_cant_open_message_button) {
                            SelectActionActivity.selectAction(context, gesture)
                        }
                        .show()
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_cant_open_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun getIconAndContentDescription(context: Context): Pair<Drawable?, String> {
        var icon = getIcon(context)
        var label = label(context)

        // Use the unknown icon if there is an action, but we can't find its icon.
        // Probably an app was uninstalled.
        if (icon == null) {
            icon = AppCompatResources.getDrawable(context, R.drawable.baseline_question_mark_24)
            label = context.getString(R.string.action_unknown)
        }

        return Pair(icon, label)

    }
}