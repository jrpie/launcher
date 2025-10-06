package de.jrpie.android.launcher.ui.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Insets
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.window.OnBackInvokedDispatcher
import androidx.annotation.RequiresApi
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.actions.LauncherAction
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.TouchGestureDetector

/**
 * An activity with a  [TouchGestureDetector] as well as handling of volume and back keys set up.
 */
abstract class LauncherGestureActivity: Activity() {
    protected var touchGestureDetector: TouchGestureDetector? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        touchGestureDetector?.onTouchEvent(event)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle back key / gesture on Android 13+, cf. onKeyDown()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_OVERLAY
            ) {
                handleBack()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        /* This should be initialized in onCreate()
           However on some devices there seems to be a bug where the touchGestureDetector
           is not working properly after resuming the app.
           Reinitializing the touchGestureDetector every time the app is resumed might help to fix that.
           (see issue #138)
         */
        touchGestureDetector = TouchGestureDetector(
            this, 0, 0,
            LauncherPreferences.enabled_gestures().edgeSwipeEdgeWidth() / 100f
        ).also {
            it.updateScreenSize(windowManager)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getRootView()?.setOnApplyWindowInsetsListener { _, windowInsets ->
                Log.i("Launcher", "onApplyWindowInsets")
                @Suppress("deprecation") // required to support API 29
                val oldInsets = windowInsets.systemGestureInsets

                Log.i("Launcher", "insets (old method) $oldInsets")
                val newInsets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    windowInsets.getInsets(WindowInsets.Type.systemGestures())
                } else {
                    null
                }
                Log.i("Launcher", "insets (new method) $oldInsets")
                touchGestureDetector?.setSystemGestureInsets(newInsets ?: oldInsets)

                windowInsets
            }
        }
    }

    @SuppressLint("GestureBackNavigation")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                // Only used pre Android 13, cf. onBackInvokedDispatcher
                handleBack()
            }

            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (Action.forGesture(Gesture.VOLUME_UP) == LauncherAction.VOLUME_UP) {
                    // Let the OS handle the key event. This works better with some custom ROMs
                    // and apps like Samsung Sound Assistant.
                    return false
                }
                Gesture.VOLUME_UP(this)
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (Action.forGesture(Gesture.VOLUME_DOWN) == LauncherAction.VOLUME_DOWN) {
                    // see above
                    return false
                }
                Gesture.VOLUME_DOWN(this)
            }
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        touchGestureDetector?.updateScreenSize(windowManager)
    }

    protected abstract fun getRootView(): View?
    protected abstract fun handleBack()
}