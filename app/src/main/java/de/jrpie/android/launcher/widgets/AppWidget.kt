package de.jrpie.android.launcher.widgets;

import android.app.Activity
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.SizeF
import android.view.View
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.ui.HomeActivity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("widget:app")
class AppWidget(
    override val id: Int,
    override var position: WidgetPosition = WidgetPosition(0,0,1,1),
    override var allowInteraction: Boolean = false,

    // We keep track of packageName, className and user to make it possible to restore the widget
    // on a new device when restoring settings (currently not implemented)
    // In normal operation only id and position are used.
    val packageName: String? = null,
    val className: String? = null,
    val user: Int? = null
): Widget() {


    constructor(id: Int, widgetProviderInfo: AppWidgetProviderInfo, position: WidgetPosition) :
            this(
                id,
                position,
                false,
                widgetProviderInfo.provider.packageName,
                widgetProviderInfo.provider.className,
                widgetProviderInfo.profile.hashCode()
            )

    /**
     * Get the [AppWidgetProviderInfo] by [id].
     * If the widget is not installed, use [restoreAppWidgetProviderInfo] instead.
     */
    fun getAppWidgetProviderInfo(context: Context): AppWidgetProviderInfo? {
        if (id < 0) {
            return null
        }
        return (context.applicationContext as Application).appWidgetManager
            .getAppWidgetInfo(id)
    }

    /**
     *  Restore the AppWidgetProviderInfo from [user], [packageName] and [className].
     *  Only use this when the widget is not installed,
     *  in normal operation use [getAppWidgetProviderInfo] instead.
     */
    /*fun restoreAppWidgetProviderInfo(context: Context): AppWidgetProviderInfo? {
        return getAppWidgetProviders(context).firstOrNull {
            it.profile.hashCode() == user
                    && it.provider.packageName == packageName
                    && it.provider.className == className
        }
    }*/

    override fun toString(): String {
        return "WidgetInfo(id=$id, position=$position, packageName=$packageName, className=$className, user=$user)"
    }

    override fun createView(activity: Activity): AppWidgetHostView? {
        val providerInfo = activity.getAppWidgetManager().getAppWidgetInfo(id) ?: return null
        val view = activity.getAppWidgetHost()
                .createView(activity, this.id, providerInfo)

        val dp = activity.resources.displayMetrics.density
        val screenWidth = activity.resources.displayMetrics.widthPixels
        val screenHeight = activity.resources.displayMetrics.heightPixels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val absolutePosition = position.getAbsoluteRect(screenWidth, screenHeight)
            view.updateAppWidgetSize(Bundle.EMPTY,
                listOf(SizeF(
                    absolutePosition.width() / dp,
                    absolutePosition.height() / dp
                )))
        }
        view.setPadding(0,0,0,0)
        return view
    }

    override fun findView(views: Sequence<View>): AppWidgetHostView? {
        return views.mapNotNull { it as? AppWidgetHostView }.firstOrNull { it.appWidgetId == id }
    }

    override fun getIcon(context: Context): Drawable? {
        return context.getAppWidgetManager().getAppWidgetInfo(id)?.loadIcon(context, DisplayMetrics.DENSITY_HIGH)
    }

    override fun getPreview(context: Context): Drawable? {
        return context.getAppWidgetManager().getAppWidgetInfo(id)?.loadPreviewImage(context, DisplayMetrics.DENSITY_HIGH)
    }

    override fun isConfigurable(context: Context): Boolean {
        return context.getAppWidgetManager().getAppWidgetInfo(id)?.configure != null
    }
    override fun configure(activity: Activity, requestCode: Int) {
        if (!isConfigurable(activity)) {
            return
        }
        activity.getAppWidgetHost().startAppWidgetConfigureActivityForResult(
            activity,
            id,
            0,
            requestCode,
            null
        )
    }
}
