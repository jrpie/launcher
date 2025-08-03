package de.jrpie.android.launcher.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun JSONObject.getOrNull(key: String): Any? {
    return try {
        this.get(key)
    } catch (_: JSONException) {
        null
    }
}

fun JSONObject.getStringOrNull(key: String): String? {
    return try {
        this.getString(key)
    } catch (_: JSONException) {
        null
    }
}
fun JSONObject.getJSONArrayOrNull(key: String): JSONArray? {
    return try {
        this.getJSONArray(key)
    } catch (_: JSONException) {
        null
    }
}
fun JSONObject.getJSONObjectOrNull(key: String): JSONObject? {
    return try {
        this.getJSONObject(key)
    } catch (_: JSONException) {
        null
    }
}
fun JSONObject.getBooleanOrNull(key: String): Boolean? {
    return try {
        this.getBoolean(key)
    } catch (_: JSONException) {
        null
    }
}
fun JSONObject.getIntOrNull(key: String): Int? {
    return try {
        this.getInt(key)
    } catch (_: JSONException) {
        null
    }
}
fun JSONObject.getDoubleOrNull(key: String): Double? {
    return try {
        this.getDouble(key)
    } catch (_: JSONException) {
        null
    }
}
fun JSONArray.getStringOrNull(i: Int): String? {
    return try {
        this.getString(i)
    } catch (_: JSONException) {
        null
    }
}
fun JSONArray.toStringSet(): Set<String> {
    return (0..<this.length())
        .mapNotNull { this.getStringOrNull(it) }
        .toSet()
}