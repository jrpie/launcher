package de.jrpie.android.launcher.actions

import android.app.Service
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import de.jrpie.android.launcher.apps.DetailedPinnedShortcutInfo
import de.jrpie.android.launcher.apps.PinnedShortcutInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action:shortcut")
class ShortcutAction(val shortcut: PinnedShortcutInfo) : Action {

    override fun invoke(context: Context, rect: Rect?): Boolean {
        val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            // TODO
            return false
        }
        shortcut.getShortcutInfo(context)?.let {
            launcherApps.startShortcut(it, rect, null)
        }

        // TODO: handle null
        return true
    }

    override fun label(context: Context): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return "?"
        }

        return shortcut.getShortcutInfo(context)?.longLabel?.toString() ?: "?"
    }

    override fun getIcon(context: Context): Drawable? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return null
        }
        return DetailedPinnedShortcutInfo.fromPinnedShortcutInfo(shortcut, context)?.getIcon(context)
    }

    override fun isAvailable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return false
        }
        return shortcut.getShortcutInfo(context) != null
    }

    override fun canReachSettings(): Boolean {
        return false
    }
}