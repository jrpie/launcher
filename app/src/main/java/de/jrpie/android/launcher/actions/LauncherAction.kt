package de.jrpie.android.launcher.actions

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.BuildConfig
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.lock.LauncherAccessibilityService
import de.jrpie.android.launcher.apps.AppFilter
import de.jrpie.android.launcher.apps.hidePrivateSpaceWhenLocked
import de.jrpie.android.launcher.apps.isPrivateSpaceSupported
import de.jrpie.android.launcher.apps.togglePrivateSpaceLock
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.list.AbstractListActivity
import de.jrpie.android.launcher.ui.list.AppListActivity
import de.jrpie.android.launcher.ui.settings.SettingsActivity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@Serializable(with = LauncherActionSerializer::class)
@SerialName("action:launcher")
enum class LauncherAction(
    val id: String,
    val label: Int,
    val icon: Int,
    val launch: (Context) -> Unit,
    private val canReachSettings: Boolean = false,
    val available: (Context) -> Boolean = { true },
) : Action {
    SETTINGS(
        "settings",
        R.string.list_other_settings,
        R.drawable.baseline_settings_24,
        ::openSettings,
        true
    ),
    CHOOSE(
        "choose",
        R.string.list_other_list,
        R.drawable.baseline_menu_24,
        ::openAppsList,
        true
    ),
    CHOOSE_FROM_FAVORITES(
        "choose_from_favorites",
        R.string.list_other_list_favorites,
        R.drawable.baseline_favorite_24,
        { context -> openAppsList(context, favorite = true) },
        true
    ),
    CHOOSE_FROM_PRIVATE_SPACE(
        "choose_from_private_space",
        R.string.list_other_list_private_space,
        R.drawable.baseline_security_24,
        { context ->
            if ((context.applicationContext as Application).privateSpaceLocked.value != true
                || !hidePrivateSpaceWhenLocked(context)
            ) {
                openAppsList(context, private = true)
            }
        },
        available = { _ ->
            isPrivateSpaceSupported()
        }
    ),
    TOGGLE_PRIVATE_SPACE_LOCK(
        "toggle_private_space_lock",
        R.string.list_other_toggle_private_space_lock,
        R.drawable.baseline_security_24,
        ::togglePrivateSpaceLock,
        available = { _ -> isPrivateSpaceSupported() }
    ),
    VOLUME_UP(
        "volume_up",
        R.string.list_other_volume_up,
        R.drawable.baseline_volume_up_24,
        { context -> audioVolumeAdjust(context, AudioManager.ADJUST_RAISE) }
    ),
    VOLUME_DOWN(
        "volume_down",
        R.string.list_other_volume_down,
        R.drawable.baseline_volume_down_24,
        { context -> audioVolumeAdjust(context, AudioManager.ADJUST_LOWER) }
    ),
    VOLUME_ADJUST(
        "volume_adjust",
        R.string.list_other_volume_adjust,
        R.drawable.baseline_volume_adjust_24,
        { context -> audioVolumeAdjust(context, AudioManager.ADJUST_SAME) }
    ),
    TRACK_PLAY_PAUSE(
        "play_pause_track",
        R.string.list_other_track_play_pause,
        R.drawable.baseline_play_arrow_24,
        { context -> audioManagerPressKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) }
    ),
    TRACK_NEXT(
        "next_track",
        R.string.list_other_track_next,
        R.drawable.baseline_skip_next_24,
        { context -> audioManagerPressKey(context, KeyEvent.KEYCODE_MEDIA_NEXT) }
    ),
    TRACK_PREV(
        "previous_track",
        R.string.list_other_track_previous,
        R.drawable.baseline_skip_previous_24,
        { context -> audioManagerPressKey(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS) }
    ),
    EXPAND_NOTIFICATIONS_PANEL(
        "expand_notifications_panel",
        R.string.list_other_expand_notifications_panel,
        R.drawable.baseline_notifications_24,
        ::expandNotificationsPanel
    ),
    EXPAND_SETTINGS_PANEL(
        "expand_settings_panel",
        R.string.list_other_expand_settings_panel,
        R.drawable.baseline_settings_applications_24,
        ::expandSettingsPanel
    ),
    RECENT_APPS(
        "recent_apps",
        R.string.list_other_recent_apps,
        R.drawable.baseline_apps_24,
        LauncherAccessibilityService::openRecentApps,
        false,
        { _ -> BuildConfig.USE_ACCESSIBILITY_SERVICE }
    ),
    LOCK_SCREEN(
        "lock_screen",
        R.string.list_other_lock_screen,
        R.drawable.baseline_lock_24,
        { c -> LauncherPreferences.actions().lockMethod().lockOrEnable(c) }
    ),
    TORCH(
        "toggle_torch",
        R.string.list_other_torch,
        R.drawable.baseline_flashlight_on_24,
        ::toggleTorch,
    ),
    LAUNCH_OTHER_LAUNCHER(
        "launcher_other_launcher",
        R.string.list_other_launch_other_launcher,
        R.drawable.baseline_home_24,
        ::launchOtherLauncher
    ),
    NOP("nop", R.string.list_other_nop, R.drawable.baseline_not_interested_24, {});

    override fun invoke(context: Context, rect: Rect?): Boolean {
        launch(context)
        return true
    }

    override fun label(context: Context): String {
        return context.getString(label)
    }

    override fun getIcon(context: Context): Drawable? {
        return AppCompatResources.getDrawable(context, icon)
    }

    override fun isAvailable(context: Context): Boolean {
        return this.available(context)
    }

    override fun canReachSettings(): Boolean {
        return this.canReachSettings
    }

    companion object {
        fun byId(id: String): LauncherAction? {
            return entries.singleOrNull { it.id == id }
        }
    }
}


/* Media player actions */
private fun audioManagerPressKey(context: Context, key: Int) {
    val mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val eventTime: Long = SystemClock.uptimeMillis()
    val downEvent =
        KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, key, 0)
    mAudioManager.dispatchMediaKeyEvent(downEvent)
    val upEvent = KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, key, 0)
    mAudioManager.dispatchMediaKeyEvent(upEvent)

}

private fun audioVolumeAdjust(context: Context, direction: Int) {
    val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    audioManager.adjustStreamVolume(
        AudioManager.STREAM_MUSIC,
        direction,
        AudioManager.FLAG_SHOW_UI
    )
}

/* End media player actions */

private fun toggleTorch(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        Toast.makeText(
            context,
            context.getString(R.string.alert_requires_android_m),
            Toast.LENGTH_LONG
        ).show()
        return
    }

    (context.applicationContext as Application).torchManager?.toggleTorch(context)
}

private fun expandNotificationsPanel(context: Context) {
    /* https://stackoverflow.com/a/15582509 */
    try {
        @Suppress("SpellCheckingInspection")
        val statusBarService: Any? = context.getSystemService("statusbar")
        val statusBarManager = Class.forName("android.app.StatusBarManager")
        val showStatusBar = statusBarManager.getMethod("expandNotificationsPanel")
        showStatusBar.invoke(statusBarService)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.alert_cant_expand_status_bar_panel),
            Toast.LENGTH_LONG
        ).show()
    }
}


private fun expandSettingsPanel(context: Context) {
    /* https://stackoverflow.com/a/31898506 */
    try {
        @Suppress("SpellCheckingInspection")
        val statusBarService: Any? = context.getSystemService("statusbar")
        val statusBarManager = Class.forName("android.app.StatusBarManager")
        val showStatusBar = statusBarManager.getMethod("expandSettingsPanel")
        showStatusBar.invoke(statusBarService)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.alert_cant_expand_status_bar_panel),
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun launchOtherLauncher(context: Context) {
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_MAIN).also { it.addCategory(Intent.CATEGORY_HOME) },
            context.getString(R.string.list_other_launch_other_launcher)
        )
    )
}

private fun openSettings(context: Context) {
    context.startActivity(Intent(context, SettingsActivity::class.java))
}

fun openAppsList(
    context: Context,
    favorite: Boolean = false,
    hidden: Boolean = false,
    private: Boolean = false
) {
    val intent = Intent(context, AppListActivity::class.java)
    intent.putExtra(
        AbstractListActivity.KEY_FAVORITES_VISIBILITY,
        if (favorite) {
            AppFilter.Companion.AppSetVisibility.EXCLUSIVE
        } else {
            AppFilter.Companion.AppSetVisibility.VISIBLE
        }
    )
    intent.putExtra(
        AbstractListActivity.KEY_HIDDEN_VISIBILITY,
        if (hidden) {
            AppFilter.Companion.AppSetVisibility.EXCLUSIVE
        } else {
            AppFilter.Companion.AppSetVisibility.HIDDEN
        }
    )
    intent.putExtra(
        AbstractListActivity.KEY_PRIVATE_SPACE_VISIBILITY,
        if (private) {
            AppFilter.Companion.AppSetVisibility.EXCLUSIVE
        } else if (!hidden && LauncherPreferences.apps().hidePrivateSpaceApps()) {
            AppFilter.Companion.AppSetVisibility.HIDDEN
        } else {
            AppFilter.Companion.AppSetVisibility.VISIBLE
        }
    )

    context.startActivity(intent)
}

/* A custom serializer is required to store type information,
   see https://github.com/Kotlin/kotlinx.serialization/issues/1486
 */
private class LauncherActionSerializer : KSerializer<LauncherAction> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        "action:launcher",
    ) {
        element("value", String.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): LauncherAction {
        val s = decoder.decodeStructure(descriptor) {
            decodeElementIndex(descriptor)
            decodeSerializableElement(descriptor, 0, String.serializer())
        }
        return LauncherAction.byId(s) ?: throw SerializationException()
    }

    override fun serialize(encoder: Encoder, value: LauncherAction) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, String.serializer(), value.id)
        }
    }
}