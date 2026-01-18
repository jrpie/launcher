package de.jrpie.android.launcher.apps

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.ShortcutQuery
import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.annotation.RequiresApi
import de.jrpie.android.launcher.getUserFromId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@RequiresApi(Build.VERSION_CODES.N_MR1)
@Serializable
@SerialName("shortcut")
data class PinnedShortcutInfo(
    val id: String,
    val packageName: String,
    val activityName: String,
    val user: Int
) : AbstractAppInfo {

    constructor(info: ShortcutInfo) : this(
        info.id,
        info.`package`,
        info.activity?.className ?: "",
        info.userHandle.hashCode()
    )

    fun getShortcutInfo(context: Context): ShortcutInfo? {
        val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps

        return try {
            launcherApps.getShortcuts(
                ShortcutQuery().apply {
                    setQueryFlags(ShortcutQuery.FLAG_MATCH_PINNED)
                    setPackage(packageName)
                    setActivity(ComponentName(packageName, activityName))
                    setShortcutIds(listOf(id))
                },
                getUserFromId(user, context)
            )?.firstOrNull()
        } catch (_: Exception) {
            // can throw SecurityException or IllegalStateException when profile is locked
            null
        }
    }
}