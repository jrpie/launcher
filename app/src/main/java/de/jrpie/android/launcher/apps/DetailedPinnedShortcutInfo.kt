package de.jrpie.android.launcher.apps

import android.app.Service
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.UserHandle
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.ShortcutAction
import de.jrpie.android.launcher.getUserFromId

@RequiresApi(Build.VERSION_CODES.N_MR1)
class DetailedPinnedShortcutInfo(
    private val shortcutInfo: PinnedShortcutInfo,
    private val label: String,
    private val icon: Drawable,
    private val privateSpace: Boolean
) : AbstractDetailedAppInfo {

    constructor(context: Context, shortcut: ShortcutInfo) : this(
        PinnedShortcutInfo(shortcut),
        (shortcut.longLabel ?: shortcut.shortLabel).toString(),
        loadBadgedIcon(context, shortcut),

        shortcut.userHandle == getPrivateSpaceUser(context)
    )

    override fun getRawInfo(): AbstractAppInfo {
        return shortcutInfo
    }

    override fun getLabel(): String {
        return label
    }

    // TODO: make nullable?
    override fun getIcon(context: Context): Drawable {
        return icon
    }

    override fun getUser(context: Context): UserHandle {
        return getUserFromId(shortcutInfo.user, context)
    }

    override fun isPrivate(): Boolean {
        return privateSpace
    }

    override fun isRemovable(): Boolean {
        return true
    }

    override fun getAction(): Action {
       return ShortcutAction(shortcutInfo)
    }

    companion object {
        fun fromPinnedShortcutInfo(shortcutInfo: PinnedShortcutInfo, context: Context): DetailedPinnedShortcutInfo? {
            return shortcutInfo.getShortcutInfo(context)?.let {
                DetailedPinnedShortcutInfo(context, it)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.N_MR1)
private fun loadBadgedIcon(context: Context, shortcut: ShortcutInfo): Drawable {
    val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
    return try {
        launcherApps.getShortcutBadgedIconDrawable(shortcut, 0)
    } catch (_: IllegalStateException) {
        null
    } ?: AppCompatResources.getDrawable(context, R.drawable.baseline_question_mark_24)!!
}