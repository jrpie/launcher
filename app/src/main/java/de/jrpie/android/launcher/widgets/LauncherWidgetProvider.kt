package de.jrpie.android.launcher.widgets

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.DisplayMetrics
import androidx.appcompat.content.res.AppCompatResources
import de.jrpie.android.launcher.R

sealed class LauncherWidgetProvider {
    abstract fun loadLabel(context: Context): CharSequence?
    abstract fun loadPreviewImage(context: Context): Drawable?
    abstract fun loadIcon(context: Context): Drawable?
    abstract fun loadDescription(context: Context): CharSequence?
}

class LauncherAppWidgetProvider(val info: AppWidgetProviderInfo) : LauncherWidgetProvider() {

    override fun loadLabel(context: Context): CharSequence? {
        return info.loadLabel(context.packageManager)
    }
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

    override fun loadLabel(context: Context): CharSequence? {
        return context.getString(R.string.widget_clock_label)
    }

    override fun loadDescription(context: Context): CharSequence? {
        return context.getString(R.string.widget_clock_description)
    }

    override fun loadPreviewImage(context: Context): Drawable? {
        return null
    }

    override fun loadIcon(context: Context): Drawable? {
        return AppCompatResources.getDrawable(context, R.drawable.baseline_clock_24)
    }
}

