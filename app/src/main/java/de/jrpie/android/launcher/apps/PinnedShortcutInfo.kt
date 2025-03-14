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
class PinnedShortcutInfo(
    val id: String,
    val packageName: String,
    val activityName: String,
    val user: Int
): AbstractAppInfo {

    constructor(info: ShortcutInfo) : this(info.id, info.`package`, info.activity?.className ?: "", info.userHandle.hashCode())

    fun getShortcutInfo(context: Context): ShortcutInfo? {
        val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps

        return try {
            launcherApps.getShortcuts(
                ShortcutQuery().apply {
                    setQueryFlags(
                    ShortcutQuery.FLAG_MATCH_CACHED
                    or ShortcutQuery.FLAG_MATCH_DYNAMIC
                            or ShortcutQuery.FLAG_MATCH_MANIFEST
                            or ShortcutQuery.FLAG_MATCH_PINNED_BY_ANY_LAUNCHER )
                    setPackage(packageName)
                    setActivity(ComponentName(packageName, activityName))
                    setShortcutIds(listOf(id))
                },
                getUserFromId(user, context)
            )?.firstOrNull()
        } catch(_: Exception) {
            // can throw SecurityException or IllegalStateException when profile is locked
            null
        }
    }

    override fun equals(other: Any?): Boolean {
        return (other as? PinnedShortcutInfo)?.let {
            packageName == this.packageName &&
                    activityName == this.activityName &&
                    id == this.id &&
                    user == this.user
        } ?: false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + activityName.hashCode()
        result = 31 * result + user
        return result
    }

    override fun toString(): String {
        return "PinnedShortcutInfo { package=$packageName, activity=$activityName, user=$user, id=$id}"
    }
}