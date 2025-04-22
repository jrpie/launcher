package de.jrpie.android.launcher.widgets;

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import de.jrpie.android.launcher.Application
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@SerialName("widget")
class WidgetInfo(
    val id: Int,
    var position: WidgetPosition = WidgetPosition(0,0,1,1),

    // We keep track of packageName, className and user to make it possible to restore the widget
    // on a new device when restoring settings (currently not implemented)
    // In normal operation only id and position are used.
    val packageName: String? = null,
    val className: String? = null,
    val user: Int? = null
) {


    constructor(id: Int, widgetProviderInfo: AppWidgetProviderInfo, position: WidgetPosition) :
            this(
                id, position,
                widgetProviderInfo.provider.packageName,
                widgetProviderInfo.provider.className,
                widgetProviderInfo.profile.hashCode()
            )

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

    /**
     * Get the [AppWidgetProviderInfo] by [id].
     * If the widget is not installed, use [restoreAppWidgetProviderInfo] instead.
     */
    fun getAppWidgetProviderInfo(context: Context): AppWidgetProviderInfo? {
        return (context.applicationContext as Application).appWidgetManager
            .getAppWidgetInfo(id)
    }

    /**
     *  Restore the AppWidgetProviderInfo from [user], [packageName] and [className].
     *  Only use this when the widget is not installed,
     *  in normal operation use [getAppWidgetProviderInfo] instead.
     */
    fun restoreAppWidgetProviderInfo(context: Context): AppWidgetProviderInfo? {
        return getAppWidgetProviders(context).firstOrNull {
            it.profile.hashCode() == user
                    && it.provider.packageName == packageName
                    && it.provider.className == className
        }
    }

    override fun toString(): String {
        return "WidgetInfo(id=$id, position=$position, packageName=$packageName, className=$className, user=$user)"
    }
}
