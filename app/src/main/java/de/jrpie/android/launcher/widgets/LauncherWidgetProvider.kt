package de.jrpie.android.launcher.widgets

import android.app.Service
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics
import androidx.appcompat.content.res.AppCompatResources
import de.jrpie.android.launcher.R

sealed class LauncherWidgetProvider(
    val label: CharSequence?,
    val description: CharSequence?,
    val appName: CharSequence?,
    val icon: Drawable?,
    val previewImage: Drawable?
) {
    fun matchesSearch(query: String): Boolean {
        return sequenceOf(label, appName, description)
            .filterNotNull()
            .any { it.contains(query, ignoreCase = true) }
    }
}

class LauncherAppWidgetProvider(
    val info: AppWidgetProviderInfo,
    label: CharSequence?,
    description: CharSequence?,
    appName: CharSequence?,
    icon: Drawable?,
    previewImage: Drawable?,
) : LauncherWidgetProvider(label, description, appName, icon, previewImage) {

    constructor(info: AppWidgetProviderInfo, context: Context) : this(
        info,
        info.loadLabel(context.packageManager),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            info.loadDescription(context)
        } else {
            null
        },
        // use LauncherApps to resolve labels of apps from other user profiles
        (context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps)
            .getActivityList(info.provider.packageName, info.profile)
            .firstOrNull()?.label ?: info.provider.packageName,
        info.loadIcon(context, DisplayMetrics.DENSITY_DEFAULT)?.let {
            // badge the icon so widgets from other profiles can be distinguished
            context.packageManager.getUserBadgedIcon(it, info.profile)
        },
        info.loadPreviewImage(context, DisplayMetrics.DENSITY_DEFAULT)
    )
}

class LauncherClockWidgetProvider(context: Context) : LauncherWidgetProvider(
    context.getString(R.string.widget_clock_label),
    context.getString(R.string.widget_clock_description),
    context.getString(R.string.app_name),
    AppCompatResources.getDrawable(context, R.drawable.baseline_clock_24),
    null
)