package de.jrpie.android.launcher.widgets

import android.app.Activity
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("widget:broken")
class BrokenAppWidget(
    override var id: Int,
    override var position: WidgetPosition = WidgetPosition(0,0,1,1),
    override var panelId: Int = WidgetPanel.HOME.id,
    override var allowInteraction: Boolean = false,

    val packageName: String? = null,
    val className: String? = null,
    val user: Int? = null
): Widget() {

    constructor(appWidget: AppWidget, id: Int = generateInternalId()): this (
        id,
        appWidget.position,
        appWidget.panelId,
        appWidget.allowInteraction,
        appWidget.packageName,
        appWidget.className,
        appWidget.user
    )

    constructor(
        id: Int,
        position: WidgetPosition,
        panelId: Int,
        widgetProviderInfo: AppWidgetProviderInfo
    ) :
            this(
                id,
                position,
                panelId,
                panelId != WidgetPanel.HOME.id,
                widgetProviderInfo.provider.packageName,
                widgetProviderInfo.provider.className,
                widgetProviderInfo.profile.hashCode()
            )


    override fun toString(): String {
        return "BrokenWidgetInfo(id=$id, position=$position, packageName=$packageName, className=$className, user=$user)"
    }

    override fun createView(activity: Activity): AppWidgetHostView? {
        return null
    }

    override fun findView(views: Sequence<View>): AppWidgetHostView? {
        return views.mapNotNull { it as? AppWidgetHostView }.firstOrNull { it.appWidgetId == id }
    }

    override fun getIcon(context: Context): Drawable? {
        return null
    }

    override fun getPreview(context: Context): Drawable? {
        return null
    }

    override fun isConfigurable(context: Context): Boolean {
        return false
    }
    override fun configure(activity: Activity, requestCode: Int) {
        return
    }
}
