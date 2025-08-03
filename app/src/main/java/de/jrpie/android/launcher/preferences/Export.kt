package de.jrpie.android.launcher.preferences

import android.util.Log
import de.jrpie.android.launcher.BuildConfig
import de.jrpie.android.launcher.util.getIntOrNull
import de.jrpie.android.launcher.util.getJSONObjectOrNull
import de.jrpie.android.launcher.util.getOrNull
import de.jrpie.android.launcher.util.toStringSet
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

const val KEY_PREFERENCE_VERSION = "preference-version"
const val KEY_DATA = "data"

fun exportPreferences(): JSONObject {
    val sharedPreferences = LauncherPreferences.getSharedPreferences()
    val entries = sharedPreferences.all.map {
        if (it.value is Set<*>) {
            val value = (it.value as? Set<*>)?.toList()
            return@map Pair(it.key, value)
        }
        return@map Pair(it.key, it.value)
    }.toMap()


    val entriesJson = JSONObject(entries)

    // wrap the data inside
    val exportJson = JSONObject()
    exportJson.put(KEY_PREFERENCE_VERSION, PREFERENCE_VERSION)
    exportJson.put("launcher-version", BuildConfig.VERSION_CODE)
    exportJson.put("launcher-version-name", BuildConfig.VERSION_NAME)
    exportJson.put(KEY_DATA, entriesJson)

    return exportJson
}

fun importPreferences(json: JSONObject): Boolean {
    val version = json.getIntOrNull(KEY_PREFERENCE_VERSION) ?: return false

    if (version != PREFERENCE_VERSION) {
        return false
    }

    LauncherPreferences.getSharedPreferences().edit {
        val entriesJson = json.getJSONObjectOrNull(KEY_DATA) ?: return false
        entriesJson.keys().forEach {
            val value = entriesJson.getOrNull(it)
            when (value) {
                is String -> putString(it, value)
                is Int -> putInt(it, value)
                is Long -> putLong(it, value)
                is Float -> putFloat(it, value)
                is Double -> putFloat(it, value.toFloat())
                is JSONArray -> {
                    putStringSet(it, value.toStringSet())
                }
                else -> {
                    Log.w(
                        "Launcher",
                        "unknown type: key=${it}, value=${value.toString()}, type=${value?.javaClass.toString()}"
                    )
                }
            }
        }
    }
    return true
}