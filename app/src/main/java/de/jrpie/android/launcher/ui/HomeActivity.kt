package de.jrpie.android.launcher.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.window.OnBackInvokedDispatcher
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.actions.LauncherAction
import de.jrpie.android.launcher.databinding.HomeBinding
import de.jrpie.android.launcher.openTutorial
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.tutorial.TutorialActivity

/**
 * [HomeActivity] is the actual application Launcher,
 * what makes this application special / unique.
 *
 * In this activity we display the date and time,
 * and we listen for actions like tapping, swiping or button presses.
 *
 * As it also is the first thing that is started when someone opens Launcher,
 * it also contains some logic related to the overall application:
 * - Setting global variables (preferences etc.)
 * - Opening the [TutorialActivity] on new installations
 */
class HomeActivity : UIObject, Activity() {

    private lateinit var binding: HomeBinding
    private var touchGestureDetector: TouchGestureDetector? = null

    private var sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            if (prefKey?.startsWith("clock.") == true ||
                prefKey?.startsWith("display.") == true
            ) {
                recreate()
            } else if (prefKey?.startsWith("action.") == true) {
                updateSettingsFallbackButtonVisibility()
            } else if (prefKey == LauncherPreferences.widgets().keys().widgets()) {
                binding.homeWidgetContainer.updateWidgets(this@HomeActivity,
                    LauncherPreferences.widgets().widgets()
                )
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Activity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()


        // Initialise layout
        binding = HomeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Handle back key / gesture on Android 13+, cf. onKeyDown()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_OVERLAY
            ) {
                handleBack()
            }
        }
        binding.buttonFallbackSettings.setOnClickListener {
            LauncherAction.SETTINGS.invoke(this)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        touchGestureDetector?.updateScreenSize(windowManager)
    }

    override fun onStart() {
        super<Activity>.onStart()
        super<UIObject>.onStart()

        // If the tutorial was not finished, start it
        if (!LauncherPreferences.internal().started()) {
            openTutorial(this)
        }

        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(sharedPreferencesListener)

        (application as Application).appWidgetHost.startListening()

    }



    override fun onStop() {
        (application as Application).appWidgetHost.stopListening()
        super.onStop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && LauncherPreferences.display().hideNavigationBar()) {
            hideNavigationBar()
        }
    }


    private fun updateSettingsFallbackButtonVisibility() {
        // If µLauncher settings can not be reached from any action bound to an enabled gesture,
        // show the fallback button.
        binding.buttonFallbackSettings.visibility = if (
            !Gesture.entries.any { g ->
                g.isEnabled() && Action.forGesture(g)?.canReachSettings() == true
            }
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun getTheme(): Resources.Theme {
        val mTheme = modifyTheme(super.getTheme())
        mTheme.applyStyle(R.style.backgroundWallpaper, true)
        LauncherPreferences.clock().font().applyToTheme(mTheme)
        LauncherPreferences.theme().colorTheme().applyToTheme(
            mTheme,
            LauncherPreferences.theme().textShadow()
        )

        return mTheme
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
            binding.root.setOnApplyWindowInsetsListener { _, windowInsets ->
                @Suppress("deprecation") // required to support API 29
                val insets = windowInsets.systemGestureInsets
                touchGestureDetector?.setSystemGestureInsets(insets)

                windowInsets
            }
        }
        updateSettingsFallbackButtonVisibility()

        binding.homeWidgetContainer.updateWidgets(this@HomeActivity,
            LauncherPreferences.widgets().widgets()
        )
    }

    override fun onDestroy() {
        LauncherPreferences.getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
        super.onDestroy()
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        android.util.Log.e("Launcher", "on touch")
        touchGestureDetector?.onTouchEvent(event)
        return true
    }

    private fun handleBack() {
        Gesture.BACK(this)
    }

    override fun isHomeScreen(): Boolean {
        return true
    }
}
