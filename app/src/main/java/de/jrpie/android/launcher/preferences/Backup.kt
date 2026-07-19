package de.jrpie.android.launcher.preferences

import android.content.Context
import android.os.Process
import android.os.UserManager
import android.util.Log
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.apps.getPrivateSpaceUser
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject

/**
 * Serialization of all settings (preferences and gesture bindings) to and from JSON,
 * used for settings backup (see SettingsFragmentMeta).
 *
 * The backup contains:
 *  - all preferences annotated with `export = true` (via the generated
 *    [LauncherPreferences.exportPreferences] and [LauncherPreferences.importPreferences]),
 *    keyed by their preference key,
 *  - the [Action] bound to each [Gesture], keyed by the gesture id,
 *  - the [PREFERENCE_VERSION] the backup was created with, keyed by [BACKUP_VERSION_KEY],
 *  - a map of user ids to profile types (main / work profile / private space), keyed by
 *    [BACKUP_USERS_KEY], so that user ids referenced by the backup can be matched to the
 *    corresponding profiles when importing on another device.
 */

const val BACKUP_VERSION_KEY = "_version"
const val BACKUP_USERS_KEY = "_users"
private const val PROFILE_TYPE_MAIN = "MAIN"
private const val PROFILE_TYPE_WORK = "WORK"
private const val PROFILE_TYPE_PRIVATE = "PRIVATE"
private const val TAG = "Launcher - Backup"

class BackupVersionException(message: String) : Exception(message)

fun exportSettings(context: Context): JSONObject {
    val json = LauncherPreferences.exportPreferences()
    json.put(BACKUP_VERSION_KEY, PREFERENCE_VERSION)

    val users = JSONObject()
    getUserProfileTypes(context).forEach { (id, type) -> users.put(id.toString(), type) }
    json.put(BACKUP_USERS_KEY, users)

    Gesture.entries.forEach { gesture ->
        Action.forGesture(gesture)?.let { action ->
            json.put(gesture.id, JSONObject(Json.encodeToString<Action>(action)))
        }
    }
    return json
}

fun importSettings(context: Context, json: JSONObject) {
    val version = json.optInt(BACKUP_VERSION_KEY, UNKNOWN_PREFERENCE_VERSION)
    if (version > PREFERENCE_VERSION) {
        throw BackupVersionException(
            "Backup was created by a newer version of the app (version $version > $PREFERENCE_VERSION)."
        )
    }
    if (version < PREFERENCE_VERSION) {
        migrateBackupToNewVersion(json, version)
    }

    remapUserIds(context, json)

    LauncherPreferences.importPreferences(json)

    Gesture.entries.forEach { gesture ->
        json.optJSONObject(gesture.id)?.let {
            Action.setActionForGesture(gesture, Json.decodeFromString<Action>(it.toString()))
        }
    }

    // The imported preferences are in the current format and belong to a configured launcher.
    // Without this, `started == false` (e.g. import directly after a reset) would cause
    // `resetPreferences` to overwrite the imported settings on the next app start.
    LauncherPreferences.internal().started(true)
    LauncherPreferences.internal().versionCode(PREFERENCE_VERSION)

    Log.i(TAG, "settings imported (version $version)")
}

/*
 * Counterpart of `migratePreferencesToNewVersion` operating on a backup instead of
 * the shared preferences. When bumping PREFERENCE_VERSION, add a case here as well;
 * migrations that need to run in both places should be written against the values
 * (not the storage) so both callers can share them.
 *
 * Settings backups did not exist before PREFERENCE_VERSION 101, so versions < 101
 * cannot occur here.
 */
internal fun migrateBackupToNewVersion(json: JSONObject, version: Int) {
    when (version) {
        // Add migration cases here when PREFERENCE_VERSION is increased beyond 101.
        else -> {
            throw BackupVersionException("Cannot migrate backup from version $version.")
        }
    }
}

/**
 * Maps the user id (`UserHandle#hashCode`) of every profile on this device to a profile type
 * ([PROFILE_TYPE_MAIN], [PROFILE_TYPE_WORK] or [PROFILE_TYPE_PRIVATE]).
 */
private fun getUserProfileTypes(context: Context): Map<Int, String> {
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val mainUser = Process.myUserHandle()
    val privateSpaceUser = getPrivateSpaceUser(context)
    return userManager.userProfiles.associate { profile ->
        profile.hashCode() to when (profile) {
            mainUser -> PROFILE_TYPE_MAIN
            privateSpaceUser -> PROFILE_TYPE_PRIVATE
            else -> PROFILE_TYPE_WORK
        }
    }
}

/**
 * Rewrites all `user` fields in the backup from the user ids of the device the backup
 * was created on to the ids of this device's profiles of the same type
 * (using the [BACKUP_USERS_KEY] map embedded in the backup).
 * User ids without a matching profile on this device are left unchanged.
 */
private fun remapUserIds(context: Context, json: JSONObject) {
    val backupUsers = json.optJSONObject(BACKUP_USERS_KEY) ?: return

    val profilesByType = HashMap<String, Int>()
    getUserProfileTypes(context).forEach { (id, type) -> profilesByType.putIfAbsent(type, id) }

    val mapping = HashMap<Int, Int>()
    backupUsers.keys().forEach { id ->
        profilesByType[backupUsers.getString(id)]?.let { localId ->
            mapping[id.toInt()] = localId
        }
    }
    if (mapping.isEmpty()) return

    remapUserIds(json, mapping)
}

internal fun remapUserIds(value: Any?, mapping: Map<Int, Int>) {
    when (value) {
        is JSONObject -> {
            for (key in value.keys().asSequence().toList()) {
                val child = value.get(key)
                if (key == "user" && child is Int) {
                    mapping[child]?.let { value.put(key, it) }
                } else {
                    remapUserIds(child, mapping)
                }
            }
        }

        is JSONArray -> {
            for (i in 0 until value.length()) {
                remapUserIds(value.get(i), mapping)
            }
        }

        else -> {}
    }
}
