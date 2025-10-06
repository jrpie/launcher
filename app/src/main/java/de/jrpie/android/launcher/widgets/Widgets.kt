package de.jrpie.android.launcher.widgets

import android.app.Activity
import android.app.Service
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Build
import android.os.UserManager
import android.util.Log
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.preferences.LauncherPreferences
import kotlin.math.min


/**
 * Tries to bind [providerInfo] to the id [id].
 * @param providerInfo The widget to be bound.
 * @param id The id to bind the widget to. If -1 is provided, a new id is allocated.
 * @param
 * @param requestCode Used to start an activity to request permission to bind the widget.
 *
 * @return true iff the app widget was bound successfully.
 */
fun bindAppWidgetOrRequestPermission(activity: Activity, providerInfo: AppWidgetProviderInfo, appWidgetId: Int, requestCode: Int? = null): Boolean {

    Log.i("Launcher", "Binding new widget $appWidgetId")
    if (!activity.getAppWidgetManager().bindAppWidgetIdIfAllowed(
            appWidgetId,
            providerInfo.provider
        )
    ) {
        Log.i("Widgets", "requesting permission for widget")
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, providerInfo.provider)
        }
        activity.startActivityForResult(intent, requestCode ?: 0)
        return false
    }
    return true
}


fun getAppWidgetProviders( context: Context ): List<LauncherWidgetProvider> {
    val list = mutableListOf<LauncherWidgetProvider>(LauncherClockWidgetProvider(context))
    val appWidgetManager = context.getAppWidgetManager()
    val profiles =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps).profiles
        } else {
            (context.getSystemService(Service.USER_SERVICE) as UserManager).userProfiles
        }
    list.addAll(
        profiles.map { profile ->
            appWidgetManager.getInstalledProvidersForProfile(profile)
                .map {
                    LauncherAppWidgetProvider(it, context) }
        }.flatten()
    )

    return list
}

fun updateWidget(widget: Widget) {
    LauncherPreferences.widgets().widgets(
        (LauncherPreferences.widgets().widgets() ?: setOf())
            .minus(widget)
            .plus(widget)
    )
}


// TODO: this needs to be improved
fun generateInternalId(): Int {
    val minId = min(-5,(LauncherPreferences.widgets().widgets() ?: setOf()).minOfOrNull { it.id } ?: 0)
    return minId -1
}

fun updateWidgetPanel(widgetPanel: WidgetPanel) {
    LauncherPreferences.widgets().customPanels(
        (LauncherPreferences.widgets().customPanels() ?: setOf())
            .minus(widgetPanel)
            .plus(widgetPanel)
    )
}

fun Context.getAppWidgetHost(): AppWidgetHost {
    return (this.applicationContext as Application).appWidgetHost
}
fun Context.getAppWidgetManager(): AppWidgetManager {
    return (this.applicationContext as Application).appWidgetManager
}