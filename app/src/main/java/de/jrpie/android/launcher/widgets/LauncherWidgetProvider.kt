package de.jrpie.android.launcher.widgets

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.pm.PackageManager
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
        try {
            context.packageManager.getApplicationInfo(info.provider.packageName, 0)
                .loadLabel(context.packageManager)
        } catch (_: PackageManager.NameNotFoundException) {
            // e.g. apps from other user profiles; fall back to the package name
            info.provider.packageName
        },
        info.loadIcon(context, DisplayMetrics.DENSITY_DEFAULT),
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