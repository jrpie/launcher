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

fun deleteAllWidgets(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.getAppWidgetHost().appWidgetIds.forEach { AppWidget(it).delete(context) }
    }
}

/**
 * Tries to bind [providerInfo] to the id [id].
 * @param providerInfo The widget to be bound.
 * @param id The id to bind the widget to. If -1 is provided, a new id is allocated.
 * @param
 * @param requestCode Used to start an activity to request permission to bind the widget.
 *
 * @return true iff the app widget was bound successfully.
 */
fun bindAppWidgetOrRequestPermission(activity: Activity, providerInfo: AppWidgetProviderInfo, id: Int, requestCode: Int? = null): Boolean {
    val appWidgetId = if(id == -1) {
        activity.getAppWidgetHost().allocateAppWidgetId()
    } else { id }

    Log.i("Launcher", "Binding new widget ${appWidgetId}")
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
    val list = mutableListOf<LauncherWidgetProvider>(LauncherClockWidgetProvider())
    val appWidgetManager = context.getAppWidgetManager()
    val profiles =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps).profiles
        } else {
            (context.getSystemService(Service.USER_SERVICE) as UserManager).userProfiles
        }
    list.addAll(
        profiles.map {
            appWidgetManager.getInstalledProvidersForProfile(it)
                .map { LauncherAppWidgetProvider(it) }
        }.flatten()
    )


    return list
}


fun updateWidget(widget: Widget) {
    var widgets = LauncherPreferences.widgets().widgets() ?: setOf()
    widgets = widgets.minus(widget).plus(widget)
    LauncherPreferences.widgets().widgets(widgets)
}

fun Context.getAppWidgetHost(): AppWidgetHost {
    return (this.applicationContext as Application).appWidgetHost
}
fun Context.getAppWidgetManager(): AppWidgetManager {
    return (this.applicationContext as Application).appWidgetManager
}
