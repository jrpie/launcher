package de.jrpie.android.launcher.preferences.legacy

import android.content.Context
import androidx.core.content.edit
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.AppAction
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.actions.LauncherAction
import de.jrpie.android.launcher.apps.AbstractAppInfo.Companion.INVALID_USER
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.preferences.PREFERENCE_VERSION
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONException
import org.json.JSONObject


@Serializable
@Suppress("unused")
private class LegacyMapEntry(val key: AppInfo, val value: String)

private fun serializeMapAppInfo(value: Map<AppInfo, String>?): Set<String>? {
    return value?.map { (key, value) ->
        Json.encodeToString(LegacyMapEntry(key, value))
    }?.toSet()
}


val oldLauncherActionIds: Map<String, LauncherAction> =
    mapOf(
        Pair("launcher:settings", LauncherAction.SETTINGS),
        Pair("launcher:choose", LauncherAction.CHOOSE),
        Pair("launcher:chooseFromFavorites", LauncherAction.CHOOSE_FROM_FAVORITES),
        Pair("launcher:volumeUp", LauncherAction.VOLUME_UP),
        Pair("launcher:volumeDown", LauncherAction.VOLUME_DOWN),
        Pair("launcher:nextTrack", LauncherAction.TRACK_NEXT),
        Pair("launcher:previousTrack", LauncherAction.TRACK_PREV),
        Pair("launcher:expandNotificationsPanel", LauncherAction.EXPAND_NOTIFICATIONS_PANEL),
        Pair("launcher:expandSettingsPanel", LauncherAction.EXPAND_SETTINGS_PANEL),
        Pair("launcher:lockScreen", LauncherAction.LOCK_SCREEN),
        Pair("launcher:toggleTorch", LauncherAction.TORCH),
        Pair("launcher:nop", LauncherAction.NOP),
    )

private fun AppInfo.Companion.legacyDeserialize(serialized: String): AppInfo {
    val values = serialized.split(";")
    val packageName = values[0]
    val user = Integer.valueOf(values[1])
    val activityName = values.getOrNull(2) ?: "" // TODO
    return AppInfo(packageName, activityName, user)
}

/**
 * Get an action for a specific id.
 * An id is of the form:
 *  - "launcher:${launcher_action_name}", see [LauncherAction]
 *  - "${package_name}", see [AppAction]
 *  - "${package_name}:${activity_name}", see  [AppAction]
 *
 *  @param id
 *  @param user a user id, ignored if the action is a [LauncherAction].
 */
private fun Action.Companion.legacyFromId(id: String, user: Int?): Action? {
    if (id.isEmpty()) {
        return null
    }
    oldLauncherActionIds[id]?.let { return it }

    val values = id.split(";")

    return AppAction(
        AppInfo(
            values[0], values.getOrNull(1) ?: "", user ?: INVALID_USER
        )
    )
}

private fun Action.Companion.legacyFromPreference(id: String): Action? {
    val preferences = LauncherPreferences.getSharedPreferences()
    val actionId = preferences.getString("$id.app", "")!!
    var u: Int? = preferences.getInt(
        "$id.user",
        INVALID_USER
    )
    u = if (u == INVALID_USER) null else u

    return Action.legacyFromId(actionId, u)
}

private fun migrateAppInfoStringMap(key: String) {
    val preferences = LauncherPreferences.getSharedPreferences()
    serializeMapAppInfo(
        preferences.getStringSet(key, setOf())?.mapNotNull { entry ->
            try {
                val obj = JSONObject(entry)
                val info = AppInfo.legacyDeserialize(obj.getString("key"))
                val value = obj.getString("value")
                Pair(info, value)
            } catch (_: JSONException) {
                null
            }
        }?.toMap(HashMap())
    )?.let {
        preferences.edit { putStringSet(key, it) }
    }
}

private fun migrateAppInfoSet(key: String) {
    (LauncherPreferences.getSharedPreferences().getStringSet(key, setOf()) ?: return)
        .map(AppInfo.Companion::legacyDeserialize)
        .map(AppInfo::serialize)
        .toSet()
        .let { LauncherPreferences.getSharedPreferences().edit { putStringSet(key, it) } }
}

private fun migrateAction(key: String) {
    Action.legacyFromPreference(key)?.let { action ->
        LauncherPreferences.getSharedPreferences().edit {
            putString(key, Json.encodeToString(action))
                .remove("$key.app")
                .remove("$key.user")
        }
    }

}

/**
 * Migrate preferences from version 1 (used until version j-0.0.18) to the current format
 * (see [PREFERENCE_VERSION])
 */
fun migratePreferencesFromVersion1(context: Context) {
    assert(LauncherPreferences.internal().versionCode() == 1)
    Gesture.entries.forEach { g -> migrateAction(g.id) }
    migrateAppInfoSet(LauncherPreferences.apps().keys().hidden())
    migrateAppInfoSet(LauncherPreferences.apps().keys().favorites())
    migrateAppInfoStringMap(LauncherPreferences.apps().keys().customNames())
    LauncherPreferences.internal().versionCode(2)

    migratePreferencesFromVersion2(context)
}