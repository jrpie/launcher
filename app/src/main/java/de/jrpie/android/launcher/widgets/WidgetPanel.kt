package de.jrpie.android.launcher.widgets

import android.content.Context
import de.jrpie.android.launcher.preferences.LauncherPreferences
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
@SerialName("panel")
class WidgetPanel(val id: Int, var label: String) {

    override fun equals(other: Any?): Boolean {
        return (other as? WidgetPanel)?.id == id
    }

    override fun hashCode(): Int {
        return id
    }

    fun serialize(): String {
        return Json.encodeToString(this)
    }

    fun delete(context: Context) {
        LauncherPreferences.widgets().customPanels(
            (LauncherPreferences.widgets().customPanels() ?: setOf()).minus(this)
        )
        (LauncherPreferences.widgets().widgets() ?: return)
            .filter { it.panelId == this.id }.forEach { it.delete(context) }
    }

    fun getWidgets(): List<Widget> {
        return LauncherPreferences.widgets().widgets().filter {
            it.panelId == this.id
        }
    }


    companion object {
        val HOME = WidgetPanel(0, "home")
        fun byId(id: Int): WidgetPanel? {
            if (id == 0) {
                return HOME
            }
            return LauncherPreferences.widgets().customPanels()?.firstOrNull { it.id == id }
        }

        fun allocateId(): Int {
            return (
                    (LauncherPreferences.widgets().customPanels() ?: setOf())
                        .plus(HOME)
                        .maxOfOrNull { it.id } ?: 0
                    ) + 1
        }

        fun deserialize(serialized: String): WidgetPanel {
            return Json.decodeFromString(serialized)
        }

    }
}