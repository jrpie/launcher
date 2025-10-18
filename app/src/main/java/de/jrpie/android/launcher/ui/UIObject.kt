package de.jrpie.android.launcher.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.preferences.theme.Background

/**
 * An interface implemented by every [Activity], Fragment etc. in Launcher.
 * It handles themes and window flags - a useful abstraction as it is the same everywhere.
 */
@Suppress("deprecation") // FLAG_FULLSCREEN is required to support API level < 30
fun setWindowFlags(window: Window, homeScreen: Boolean) {
    window.setFlags(0, 0) // clear flags

    // Display notification bar
    if (LauncherPreferences.display().hideStatusBar())
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    else window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

    // Screen Timeout
    if (LauncherPreferences.display().screenTimeoutDisabled())
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    if (!homeScreen) {
        LauncherPreferences.theme().background().applyToWindow(window)

    }

}


interface UIObject {
    fun onCreate() {
        if (this !is Activity) {
            return
        }
        setWindowFlags(window, isHomeScreen())

        if (!LauncherPreferences.display().rotateScreen()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        }
    }

    fun onStart() {
        setOnClicks()
        adjustLayout()
    }

    fun modifyTheme(theme: Resources.Theme): Resources.Theme {
        LauncherPreferences.theme().colorTheme().applyToTheme(
            theme,
            LauncherPreferences.theme().textShadow()
        )

        if (isHomeScreen()) {
            Background.TRANSPARENT.applyToTheme(theme)
            LauncherPreferences.clock().font().applyToTheme(theme)
        } else {
            LauncherPreferences.theme().background().applyToTheme(theme)
            LauncherPreferences.theme().font().applyToTheme(theme)
        }

        return theme
    }

    // fun applyTheme() { }
    fun setOnClicks() {}
    fun adjustLayout() {}

    fun isHomeScreen(): Boolean {
        return false
    }


    @Suppress("DEPRECATION")
    fun hideNavigationBar() {
        if (this !is Activity) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.navigationBars())
                systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Try to hide the navigation bar but do not hide the status bar
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        }
    }

    fun useSoftInputResizeWorkaround(container: View) {
        if (this !is Activity) {
            return
        }
        // android:windowSoftInputMode="adjustResize" doesn't work in full screen.
        // workaround from https://stackoverflow.com/a/57623505
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            this.window.decorView.viewTreeObserver.addOnGlobalLayoutListener {
                val r = Rect()
                window.decorView.getWindowVisibleDisplayFrame(r)
                val height: Int =
                    container.context.resources.displayMetrics.heightPixels
                val diff = height - r.bottom
                if (diff != 0 &&
                    LauncherPreferences.display().hideStatusBar()
                ) {
                    if (container.paddingBottom != diff) {
                        container.setPadding(0, 0, 0, diff)
                    }
                } else {
                    if (container.paddingBottom != 0) {
                        container.setPadding(0, 0, 0, 0)
                    }
                }
            }
        }
    }

}

abstract class UIObjectActivity: AppCompatActivity(), UIObject {
    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()
    }
    override fun onStart() {
        super<AppCompatActivity>.onStart()
        super<UIObject>.onStart()
    }
    override fun getTheme(): Resources.Theme? {
        return modifyTheme(super.getTheme())
    }
}