package de.jrpie.android.launcher.apps

import android.app.Service
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import de.jrpie.android.launcher.apps.AbstractAppInfo.Companion.INVALID_USER
import de.jrpie.android.launcher.getUserFromId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an app installed on the users device.
 * Contains the minimal amount of data required to identify the app.
 */
@Serializable
@SerialName("app")
data class AppInfo(
    val packageName: String,
    val activityName: String?,
    val user: Int = INVALID_USER
) : AbstractAppInfo {

    fun getLauncherActivityInfo(
        context: Context
    ): LauncherActivityInfo? {
        val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
        val userHandle = getUserFromId(user, context)
        val activityList = launcherApps.getActivityList(packageName, userHandle)
        return activityList.firstOrNull { app -> app.name == activityName }
            ?: activityList.firstOrNull()
    }
}