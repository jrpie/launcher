package de.jrpie.android.launcher.apps

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * This interface is implemented by [AppInfo] and [PinnedShortcutInfo].
 */
@Serializable
sealed interface AbstractAppInfo {
    fun serialize(): String {
        return Json.encodeToString(this)
    }
    companion object {
        const val INVALID_USER = -1

        fun deserialize(serialized: String): AbstractAppInfo {
            return Json.decodeFromString(serialized)
        }
    }
}