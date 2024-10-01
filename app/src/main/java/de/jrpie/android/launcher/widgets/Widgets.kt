package de.jrpie.android.launcher.widgets

import android.app.Activity
import android.app.Service
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.UserManager
import android.util.Log
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.preferences.LauncherPreferences
import kotlin.math.absoluteValue
import kotlin.random.Random

fun deleteAllWidgets(activity: Activity) {
    val appWidgetHost = (activity.application as Application).appWidgetHost
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        appWidgetHost.appWidgetIds.forEach { deleteAppWidget(activity, WidgetInfo(it, 0,0)) }
    }
}

fun bindAppWidget(activity: Activity, providerInfo: AppWidgetProviderInfo): WidgetInfo? {
    val appWidgetHost = (activity.application as Application).appWidgetHost
    val appWidgetManager = (activity.application as Application).appWidgetManager
    val appWidgetId = appWidgetHost.allocateAppWidgetId()

    Log.i("Launcher", "Binding new widget ${appWidgetId}")
    if (!appWidgetManager.bindAppWidgetIdIfAllowed(
            appWidgetId,
            providerInfo.provider
        )
    ) {
        requestAppWidgetPermission(activity, appWidgetId, providerInfo)
        return null
    }
    try {
        Log.e("widgets", "configure widget")
        appWidgetHost.startAppWidgetConfigureActivityForResult(activity, appWidgetId, 0, 1, null)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val widget = WidgetInfo(appWidgetId, 500, 500)
    LauncherPreferences.internal().widgets(
        (LauncherPreferences.internal().widgets() ?: HashSet()).also {
            it.add(widget)
        }
    )


    return widget
}

fun deleteAppWidget(activity: Activity, widget: WidgetInfo) {
    Log.i("Launcher", "Deleting widget ${widget.id}")
    val appWidgetHost = (activity.application as Application).appWidgetHost

    appWidgetHost.deleteAppWidgetId(widget.id)

    LauncherPreferences.internal().widgets(
        LauncherPreferences.internal().widgets()?.also {
            it.remove(widget)
        }
    )
}

fun createAppWidgetView(activity: Activity, widget: WidgetInfo): AppWidgetHostView? {
    val appWidgetHost = (activity.application as Application).appWidgetHost
    val appWidgetManager = (activity.application as Application).appWidgetManager
    val providerInfo = appWidgetManager.getAppWidgetInfo(widget.id) ?: return null
    val view = appWidgetHost.createView(activity, widget.id, providerInfo)
        .apply {
            setAppWidget(appWidgetId, appWidgetInfo)
        }


    val newOptions = Bundle().apply {
        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widget.width)
        putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widget.width)
        putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, widget.height)
        putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, widget.height)
    }
    appWidgetManager.updateAppWidgetOptions(
        widget.id,
        newOptions
    )
    //view.minimumWidth = widget.width
    //view.minimumHeight = widget.height

    return view
}

fun getAppWidgetProviders(context: Context): List<AppWidgetProviderInfo> {
    return appWidgetProviders(context, (context.applicationContext as Application).appWidgetManager)
}

fun requestAppWidgetPermission(context: Activity, widgetId: Int, info: AppWidgetProviderInfo) {
    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
    }
    context.startActivityForResult(intent, 0)//REQUEST_CODE_BIND_WIDGET)
}

fun appWidgetProviders(
    context: Context,
    appWidgetManager: AppWidgetManager
): List<AppWidgetProviderInfo> {
    val userManager = context.getSystemService(Service.USER_SERVICE) as UserManager
    return userManager.userProfiles.map {
        appWidgetManager.getInstalledProvidersForProfile(it)
    }.flatten()
}
fun Activity.bindRandomWidget() {
    val selectedWidget =
        getAppWidgetProviders(this).let { it.get(Random.nextInt().absoluteValue % it.size) }
    bindAppWidget(this, selectedWidget) ?: return
}

