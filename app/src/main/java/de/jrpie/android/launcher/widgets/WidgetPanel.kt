package de.jrpie.android.launcher.widgets

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("panel")
data class WidgetPanel(val id: Int, val label: String) {
    companion object {
        val DEFAULT = WidgetPanel(0, "home")
    }
}



