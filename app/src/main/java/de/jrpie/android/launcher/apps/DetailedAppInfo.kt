package de.jrpie.android.launcher.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.graphics.drawable.Drawable
import android.os.UserHandle
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.AppAction
import de.jrpie.android.launcher.getUserFromId
import de.jrpie.android.launcher.icons.IconPack

/**
 * Stores information used to create [de.jrpie.android.launcher.ui.list.apps.AppsRecyclerAdapter] rows.
 */
class DetailedAppInfo(
    private val app: AppInfo,
    private val label: CharSequence,
    private val icon: Drawable?,
    private val privateSpace: Boolean,
    private val removable: Boolean = true,
) : AbstractDetailedAppInfo {

    constructor(activityInfo: LauncherActivityInfo, icon: Drawable?, private: Boolean) : this(
        AppInfo.fromLauncherActivityInfo(activityInfo),
        activityInfo.label,
        icon,
        private,
        // App can be uninstalled iff it is not a system app
        activityInfo.applicationInfo.flags.and(ApplicationInfo.FLAG_SYSTEM) == 0
    )


    override fun getLabel(): String {
        return label.toString()
    }

    override fun getIcon(context: Context): Drawable? {
        return icon
    }

    override fun getRawInfo(): AppInfo {
        return app
    }

    override fun getUser(context: Context): UserHandle {
        return getUserFromId(app.user, context)
    }

    override fun isPrivate(): Boolean {
        return privateSpace
    }

    override fun isRemovable(): Boolean {
        return removable
    }

    override fun getAction(): Action {
        return AppAction(app)
    }


    companion object {
        fun fromAppInfo(appInfo: AppInfo, context: Context): DetailedAppInfo? {
            return appInfo.getLauncherActivityInfo(context)?.let {
                DetailedAppInfo(it,
                    (context.applicationContext as? Application)?.iconPack?.loadIcon(context, appInfo),
                    it.user == getPrivateSpaceUser(context))
            }
        }
    }
}