package de.jrpie.android.launcher.widgets

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics

sealed class LauncherWidgetProvider {
    abstract val label: String?

    abstract fun loadPreviewImage(context: Context): Drawable?
    abstract fun loadIcon(context: Context): Drawable?
    abstract fun loadDescription(context: Context): CharSequence?
}

class LauncherAppWidgetProvider(val info: AppWidgetProviderInfo) : LauncherWidgetProvider() {
    override val label: String? = info.label
    override fun loadPreviewImage(context: Context): Drawable? {
        return info.loadPreviewImage(context, DisplayMetrics.DENSITY_DEFAULT)
    }

    override fun loadIcon(context: Context): Drawable? {
        return info.loadIcon(context, DisplayMetrics.DENSITY_DEFAULT)
    }

    override fun loadDescription(context: Context): CharSequence? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            info.loadDescription(context)
        } else {
            null
        }
    }

}
class LauncherClockWidgetProvider : LauncherWidgetProvider() {
    override val label: String?
        get() = "Clock"

    override fun loadPreviewImage(context: Context): Drawable? {
        return null
    }

    override fun loadIcon(context: Context): Drawable? {
        return null
    }

    override fun loadDescription(context: Context): CharSequence? {
        return null
    }
}

