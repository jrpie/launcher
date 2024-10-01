package de.jrpie.android.launcher.widgets;

import de.jrpie.android.launcher.apps.AbstractAppInfo
import kotlinx.serialization.SerialName;
import kotlinx.serialization.Serializable;
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@SerialName("widget")
class WidgetInfo(val id: Int, val width: Int, val height: Int) {
    fun serialize(): String {
        return Json.encodeToString(this)
    }

    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        return (other as? WidgetInfo)?.id == id
    }
    companion object {
        fun deserialize(serialized: String): WidgetInfo {
            return Json.decodeFromString(serialized)
        }
    }
}
