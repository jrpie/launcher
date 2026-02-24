package de.jrpie.android.launcher.preferences

import android.app.Service
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.UserManager
import android.util.Log
import de.jrpie.android.launcher.apps.AbstractAppInfo.Companion.INVALID_USER
import de.jrpie.android.launcher.apps.getPrivateSpaceUser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.long
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant

private const val TAG = "Launcher - ConfigIO"

private const val FORMAT_VERSION = 1

private const val KEY_FORMAT_VERSION = "formatVersion"
private const val KEY_PREFERENCE_VERSION = "preferenceVersion"
private const val KEY_EXPORTED_AT = "exportedAt"
private const val KEY_PREFERENCES = "preferences"
private const val KEY_USER_PROFILES = "userProfiles"

/**
 * Keys that cannot be meaningfully restored on another device and are skipped during import.
 * - Pinned shortcuts are bound to the originating device's ShortcutManager.
 * - Widgets and widget panels hold host-allocated IDs that are device-specific.
 */
private val SKIP_KEYS = setOf(
    "settings_apps_pinned_shortcuts",
    "settings_widgets_widgets",
    "settings_widgets_custom_panels"
)

private val exportJson = Json { prettyPrint = true }

// --- User profile helpers ---

private const val PROFILE_TYPE_MAIN = "main"
private const val PROFILE_TYPE_WORK = "work"
private const val PROFILE_TYPE_PRIVATE = "private"

/**
 * Build a mapping from userId (UserHandle.hashCode()) to a portable profile type string.
 */
private fun buildUserProfileMap(context: Context): Map<Int, String> {
    val userManager = context.getSystemService(Service.USER_SERVICE) as UserManager
    val profiles = userManager.userProfiles
    val map = mutableMapOf<Int, String>()

    // First profile is always the main user
    if (profiles.isNotEmpty()) {
        map[profiles[0].hashCode()] = PROFILE_TYPE_MAIN
    }

    // Detect private space user if supported
    val privateSpaceUser = getPrivateSpaceUser(context)
    if (privateSpaceUser != null) {
        map[privateSpaceUser.hashCode()] = PROFILE_TYPE_PRIVATE
    }

    // Remaining profiles that aren't main or private are work profiles
    for (profile in profiles.drop(1)) {
        if (!map.containsKey(profile.hashCode())) {
            map[profile.hashCode()] = PROFILE_TYPE_WORK
        }
    }

    return map
}

/**
 * Build a reverse mapping from profile type string to local userId.
 */
private fun buildProfileTypeToUserIdMap(context: Context): Map<String, Int> {
    return buildUserProfileMap(context).entries.associate { (k, v) -> v to k }
}

// --- Export ---

fun exportConfig(context: Context, outputStream: OutputStream) {
    val prefs = LauncherPreferences.getSharedPreferences()
    val allEntries = prefs.all

    val userProfileMap = buildUserProfileMap(context)

    val prefsMap = mutableMapOf<String, JsonElement>()
    for ((key, value) in allEntries) {
        if (value == null) continue
        prefsMap[key] = toJsonElement(value)
    }

    val envelope = JsonObject(
        mapOf(
            KEY_FORMAT_VERSION to JsonPrimitive(FORMAT_VERSION),
            KEY_PREFERENCE_VERSION to JsonPrimitive(
                LauncherPreferences.internal().versionCode()
            ),
            KEY_EXPORTED_AT to JsonPrimitive(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    Instant.now().toString()
                else
                    System.currentTimeMillis().toString()
            ),
            KEY_USER_PROFILES to JsonObject(
                userProfileMap.map { (id, type) ->
                    id.toString() to JsonPrimitive(type)
                }.toMap()
            ),
            KEY_PREFERENCES to JsonObject(prefsMap)
        )
    )

    outputStream.bufferedWriter().use { writer ->
        writer.write(exportJson.encodeToString(JsonObject.serializer(), envelope))
    }
}

// --- Import ---

sealed class ImportResult {
    data object Success : ImportResult()
    data class Error(val message: String) : ImportResult()
}

fun importConfig(context: Context, inputStream: InputStream): ImportResult {
    val text = try {
        inputStream.bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to read config file", e)
        return ImportResult.Error("Failed to read file: ${e.message}")
    }

    val envelope: JsonObject = try {
        Json.decodeFromString(JsonObject.serializer(), text)
    } catch (e: Exception) {
        Log.e(TAG, "Failed to parse config JSON", e)
        return ImportResult.Error("Invalid JSON: ${e.message}")
    }

    // Validate format version
    val formatVersion = (envelope[KEY_FORMAT_VERSION] as? JsonPrimitive)?.intOrNull
    if (formatVersion == null || formatVersion != FORMAT_VERSION) {
        return ImportResult.Error(
            "Unsupported config format version: $formatVersion (expected $FORMAT_VERSION)"
        )
    }

    // Warn about preference version mismatch (non-fatal, migration will handle it)
    val prefVersion = (envelope[KEY_PREFERENCE_VERSION] as? JsonPrimitive)?.intOrNull
    if (prefVersion != null && prefVersion != PREFERENCE_VERSION) {
        Log.w(TAG, "Importing config from preference version $prefVersion (current: $PREFERENCE_VERSION)")
    }

    val preferences = envelope[KEY_PREFERENCES] as? JsonObject
        ?: return ImportResult.Error("Missing 'preferences' in config file")

    // Build user ID remapping from exported profile types to local user IDs
    val userIdRemap = buildUserIdRemap(context, envelope)

    val editor = LauncherPreferences.getSharedPreferences().edit()
    editor.clear()

    for ((key, value) in preferences) {
        if (key in SKIP_KEYS) {
            Log.i(TAG, "Skipping non-portable preference key: $key")
            continue
        }
        try {
            applyJsonToEditor(editor, key, remapUserIds(value, userIdRemap))
        } catch (e: Exception) {
            Log.w(TAG, "Failed to import preference '$key': ${e.message}")
        }
    }

    // Ensure preference version is written so migration can run
    if (prefVersion != null) {
        editor.putInt(
            "settings_internal_version_code",
            prefVersion
        )
    }

    editor.apply()

    // Run migration to bring imported prefs up to current version
    migratePreferencesToNewVersion(context)

    return ImportResult.Success
}

/**
 * Build a mapping from exported user IDs to local user IDs based on profile types.
 */
private fun buildUserIdRemap(context: Context, envelope: JsonObject): Map<Int, Int> {
    val userProfiles = envelope[KEY_USER_PROFILES] as? JsonObject ?: return emptyMap()
    val localMap = buildProfileTypeToUserIdMap(context)
    val remap = mutableMapOf<Int, Int>()

    for ((exportedIdStr, typeElement) in userProfiles) {
        val exportedId = exportedIdStr.toIntOrNull() ?: continue
        val profileType = (typeElement as? JsonPrimitive)?.content ?: continue
        val localId = localMap[profileType]
        if (localId != null) {
            remap[exportedId] = localId
        } else {
            Log.w(TAG, "No local profile match for type '$profileType' (exported user $exportedId), using INVALID_USER")
            remap[exportedId] = INVALID_USER
        }
    }

    return remap
}

/**
 * Recursively remap user IDs in JSON values.
 * User IDs appear as integers in serialized AppInfo/PinnedShortcutInfo objects
 * inside JSON strings stored in SharedPreferences (e.g. in Set<String> values or
 * action bindings). We process string values that look like JSON containing "user" fields.
 */
private fun remapUserIds(element: JsonElement, remap: Map<Int, Int>): JsonElement {
    if (remap.isEmpty()) return element

    return when (element) {
        is JsonPrimitive -> {
            if (element.isString) {
                val content = element.content
                // Try to remap user IDs in JSON strings (action bindings, serialized app info)
                JsonPrimitive(remapUserIdsInString(content, remap))
            } else {
                element
            }
        }
        is JsonArray -> JsonArray(element.map { remapUserIds(it, remap) })
        is JsonObject -> JsonObject(element.map { (k, v) -> k to remapUserIds(v, remap) }.toMap())
        is JsonNull -> element
    }
}

/**
 * Attempt to remap user IDs within a string that may contain serialized JSON.
 * Handles both top-level JSON objects and sets of serialized objects.
 */
private fun remapUserIdsInString(value: String, remap: Map<Int, Int>): String {
    if (!value.contains("\"user\"")) return value

    return try {
        val parsed = Json.decodeFromString(JsonElement.serializer(), value)
        val remapped = remapUserIdsInJsonElement(parsed, remap)
        Json.encodeToString(JsonElement.serializer(), remapped)
    } catch (_: Exception) {
        value
    }
}

private fun remapUserIdsInJsonElement(element: JsonElement, remap: Map<Int, Int>): JsonElement {
    return when (element) {
        is JsonObject -> {
            val entries = element.entries.map { (k, v) ->
                if (k == "user" && v is JsonPrimitive && v.intOrNull != null) {
                    val oldId = v.int
                    k to JsonPrimitive(remap.getOrDefault(oldId, oldId))
                } else {
                    k to remapUserIdsInJsonElement(v, remap)
                }
            }
            JsonObject(entries.toMap())
        }
        is JsonArray -> JsonArray(element.map { remapUserIdsInJsonElement(it, remap) })
        else -> element
    }
}

// --- JSON <-> SharedPreferences helpers ---

// Type tags used in the exported JSON to preserve SharedPreferences types across import/export.
// Each preference value is stored as {"type": "<tag>", "value": <json_value>} to avoid
// ambiguity when deserializing JSON numbers (e.g. int vs long).
private const val TYPE_BOOLEAN = "boolean"
private const val TYPE_INT = "int"
private const val TYPE_LONG = "long"
private const val TYPE_FLOAT = "float"
private const val TYPE_STRING = "string"
private const val TYPE_STRING_SET = "string_set"

@Suppress("UNCHECKED_CAST")
private fun toJsonElement(value: Any): JsonElement {
    val (type, jsonValue) = when (value) {
        is Boolean -> TYPE_BOOLEAN to JsonPrimitive(value)
        is Int -> TYPE_INT to JsonPrimitive(value)
        is Long -> TYPE_LONG to JsonPrimitive(value)
        is Float -> TYPE_FLOAT to JsonPrimitive(value)
        is String -> TYPE_STRING to JsonPrimitive(value)
        is Set<*> -> TYPE_STRING_SET to JsonArray((value as Set<String>).map { JsonPrimitive(it) })
        else -> {
            Log.w(TAG, "Unknown preference type: ${value::class.java.name}")
            TYPE_STRING to JsonPrimitive(value.toString())
        }
    }
    return JsonObject(mapOf("type" to JsonPrimitive(type), "value" to jsonValue))
}

private fun applyJsonToEditor(editor: SharedPreferences.Editor, key: String, value: JsonElement) {
    if (value is JsonObject && value.containsKey("type") && value.containsKey("value")) {
        val type = (value["type"] as? JsonPrimitive)?.content
        val inner = value["value"]!!
        when (type) {
            TYPE_BOOLEAN -> editor.putBoolean(key, (inner as JsonPrimitive).boolean)
            TYPE_INT -> editor.putInt(key, (inner as JsonPrimitive).int)
            TYPE_LONG -> editor.putLong(key, (inner as JsonPrimitive).long)
            TYPE_FLOAT -> editor.putFloat(key, (inner as JsonPrimitive).float)
            TYPE_STRING -> editor.putString(key, (inner as JsonPrimitive).content)
            TYPE_STRING_SET -> editor.putStringSet(
                key,
                (inner as JsonArray).mapNotNull { (it as? JsonPrimitive)?.content }.toSet()
            )
            else -> Log.w(TAG, "Cannot import preference '$key': unknown type '$type'")
        }
    } else {
        Log.w(TAG, "Cannot import preference '$key': unexpected JSON structure")
    }
}
