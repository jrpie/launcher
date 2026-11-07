package de.jrpie.android.launcher.apps

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.util.Log
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.preferences.LauncherPreferences

/**
 * This interface is implemented by [DetailedAppInfo] and [DetailedPinnedShortcutInfo]
 */
sealed interface AbstractDetailedAppInfo {
    fun getRawInfo(): AbstractAppInfo
    fun getLabel(): String
    fun getIcon(context: Context): Drawable?
    fun getUser(context: Context): UserHandle
    fun isPrivate(): Boolean
    fun isRemovable(): Boolean
    fun getAction(): Action


    fun getCustomLabel(context: Context): String {
        val map = (context.applicationContext as? Application)?.getCustomAppNames()
        return map?.get(getRawInfo()) ?: getLabel()
    }


    fun setCustomLabel(label: CharSequence?) {
        Log.i("Launcher", "Setting custom label for ${this.getRawInfo()} to ${label}.")
        val map = LauncherPreferences.apps().customNames() ?: HashMap<AbstractAppInfo, String>()

        if (label.isNullOrEmpty()) {
            map.remove(getRawInfo())
        } else {
            map[getRawInfo()] = label.toString()
        }
        LauncherPreferences.apps().customNames(map)
    }

}