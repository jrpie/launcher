package de.jrpie.android.launcher.preferences.theme

import android.content.res.Resources
import android.os.Build
import android.view.Window
import android.view.WindowManager
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.preferences.LauncherPreferences

@Suppress("unused")
enum class Background(val id: Int, val dim: Boolean = false, val blur: Boolean = false) {
    TRANSPARENT(R.style.backgroundWallpaper),
    DIM(R.style.backgroundWallpaper, dim = true),
    BLUR(R.style.backgroundWallpaper, dim = true, blur = true),
    SOLID(R.style.backgroundSolid),
    ;

    fun applyToTheme(theme: Resources.Theme) {
        theme.applyStyle(id, true)
    }

    fun applyToWindow(window: Window) {
        val layoutParams: WindowManager.LayoutParams = window.attributes
        // TODO: add a setting to change this?
        var dimAmount = 0.7f
        val dim = this.dim
        var blur = this.blur

        // replace blur by more intense dim on old devices
        if (blur && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            blur = false
            dimAmount += 0.1f
        }

        if (LauncherPreferences.theme().colorTheme() == ColorTheme.LIGHT) {
            dimAmount = 0f
        }

        if (dim) {
            layoutParams.dimAmount = dimAmount
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (blur) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                layoutParams.blurBehindRadius = 10
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                layoutParams.blurBehindRadius = 0
            }
        }

        window.attributes = layoutParams
    }
}