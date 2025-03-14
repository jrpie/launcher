package de.jrpie.android.launcher.apps

import android.app.Service
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.UserHandle
import android.view.Gravity
import androidx.annotation.GravityInt
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.ShortcutAction
import de.jrpie.android.launcher.getUserFromId

@RequiresApi(Build.VERSION_CODES.N_MR1)
class DetailedPinnedShortcutInfo(
    private val shortcutInfo: PinnedShortcutInfo,
    private val appInfo: DetailedAppInfo?,
    private val label: String,
    private val icon: Drawable,
    private val privateSpace: Boolean
) : AbstractDetailedAppInfo {

    constructor(context: Context, shortcut: ShortcutInfo) : this(
        PinnedShortcutInfo(shortcut),
        DetailedAppInfo.fromAppInfo(AppInfo(shortcut.`package`, shortcut.activity?.className, shortcut.userHandle.hashCode()), context),
        (shortcut.longLabel ?: shortcut.shortLabel ?: shortcut.`package`).toString(),
        (context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps)
            .getShortcutBadgedIconDrawable(shortcut, 0) ?: ResourcesCompat.getDrawable(context.resources, R.drawable.baseline_question_mark_24, context.theme)!!,
        shortcut.userHandle == getPrivateSpaceUser(context)
    )

    override fun getRawInfo(): AbstractAppInfo {
        return shortcutInfo
    }

    override fun getLabel(): String {
        if (appInfo == null) {
            return label
        }
        // TODO different for pinned shortcuts
        return "${appInfo.getLabel()}: $label"
    }

    override fun getIcon(context: Context): Drawable {
        // TODO different for pinned shortcuts
        if (appInfo == null ) {
            return icon
        }
        val width = icon.intrinsicWidth
        val height = icon.intrinsicHeight
        return LayerDrawable(arrayOf(icon, appInfo.getIcon(context))).apply {
            setLayerWidth(1,width / 2)
            setLayerHeight(1,height / 2)
            setLayerGravity(1, Gravity.TOP or Gravity.END)
        }
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