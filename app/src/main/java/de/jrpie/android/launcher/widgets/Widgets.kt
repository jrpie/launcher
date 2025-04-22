package de.jrpie.android.launcher.widgets

import android.app.Activity
import android.app.Service
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Bundle
import android.os.UserManager
import android.util.Log
import android.util.SizeF
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.preferences.LauncherPreferences

fun deleteAllWidgets(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.getAppWidgetHost().appWidgetIds.forEach { deleteAppWidget(context, WidgetInfo(it)) }
    }
}

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
        Log.e("Launcher", "not allowed to bind widget")
        requestAppWidgetPermission(activity, appWidgetId, providerInfo, requestCode)
        return false
    }
    return true
}

fun deleteAppWidget(context: Context, widget: WidgetInfo) {
    Log.i("Launcher", "Deleting widget ${widget.id}")
    val appWidgetHost = (context.applicationContext as Application).appWidgetHost

    appWidgetHost.deleteAppWidgetId(widget.id)

    LauncherPreferences.internal().widgets(
        LauncherPreferences.internal().widgets()?.also {
            it.remove(widget)
        }
    )
}

fun createAppWidgetView(activity: Activity, widget: WidgetInfo): AppWidgetHostView? {
    val providerInfo = activity.getAppWidgetManager().getAppWidgetInfo(widget.id) ?: return null

    val dp = activity.resources.displayMetrics.density

    val view = activity.getAppWidgetHost()
        .createView(activity, widget.id, providerInfo)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        view.updateAppWidgetSize(Bundle.EMPTY, listOf(SizeF(widget.position.width / dp, widget.position.height / dp)))
    }
    view.setPadding(0,0,0,0)
    return view
}

fun requestAppWidgetPermission(context: Activity, widgetId: Int, info: AppWidgetProviderInfo, requestCode: Int?) {
    Log.i("Widgets", "requesting permission for widget")
    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
    }
    context.startActivityForResult(intent, requestCode ?: 0)
}

fun getAppWidgetProviders( context: Context ): List<AppWidgetProviderInfo> {
    val appWidgetManager = context.getAppWidgetManager()
    val profiles =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps).profiles
        } else {
            (context.getSystemService(Service.USER_SERVICE) as UserManager).userProfiles
        }
    Log.i("Widgets", "profiles: ${profiles.size}, $profiles")

    return profiles.map {
            appWidgetManager.getInstalledProvidersForProfile(it)
        }.flatten()
}

fun getWidgetById(id: Int): WidgetInfo? {
    return (LauncherPreferences.internal().widgets() ?: setOf()).firstOrNull {
        it.id == id
    }
}

fun updateWidget(widget: WidgetInfo) {
    var widgets = LauncherPreferences.internal().widgets() ?: setOf()
    widgets = widgets.minus(widget).plus(widget)
    LauncherPreferences.internal().widgets(widgets)
}

private fun Context.getAppWidgetHost(): AppWidgetHost {
    return (this.applicationContext as Application).appWidgetHost
}
private fun Context.getAppWidgetManager(): AppWidgetManager {
    return (this.applicationContext as Application).appWidgetManager
}
