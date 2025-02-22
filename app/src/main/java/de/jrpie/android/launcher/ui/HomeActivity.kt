package de.jrpie.android.launcher.ui

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.actions.LauncherAction
import de.jrpie.android.launcher.databinding.HomeBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.tutorial.TutorialActivity
import java.util.Locale

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
class HomeActivity : UIObject, AppCompatActivity() {

    private lateinit var binding: HomeBinding
    private lateinit var touchGestureDetector: TouchGestureDetector

    private var sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            if (prefKey?.startsWith("clock.") == true ||
                prefKey?.startsWith("display.") == true
            ) {
                recreate()
            }

            if (prefKey?.startsWith("action.") == true) {
                updateSettingsFallbackButtonVisibility()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()


        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        touchGestureDetector = TouchGestureDetector(
            this,
            width,
            height,
            LauncherPreferences.enabled_gestures().edgeSwipeEdgeWidth() / 100f
        )

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

    override fun onStart() {
        super<AppCompatActivity>.onStart()

        super<UIObject>.onStart()

        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(sharedPreferencesListener)

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && LauncherPreferences.display().hideNavigationBar()) {
            hideNavigationBar()
        }
    }

    @Suppress("DEPRECATION")
    private fun hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.navigationBars())
                systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            val decorView = window.decorView
            val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

            // Try to hide the navigation bar but do not hide the status bar
            decorView.systemUiVisibility = uiOptions

            // Add listener to hide the navigation bar
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    decorView.systemUiVisibility = uiOptions
                }
            }
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

    private fun initClock() {
        val locale = Locale.getDefault()
        val dateVisible = LauncherPreferences.clock().dateVisible()
        val timeVisible = LauncherPreferences.clock().timeVisible()

        var dateFMT = "yyyy-MM-dd"
        var timeFMT = "HH:mm"
        if (LauncherPreferences.clock().showSeconds()) {
            timeFMT += ":ss"
        }

        if (LauncherPreferences.clock().localized()) {
            dateFMT = android.text.format.DateFormat.getBestDateTimePattern(locale, dateFMT)
            timeFMT = android.text.format.DateFormat.getBestDateTimePattern(locale, timeFMT)
        }

        var upperFormat = dateFMT
        var lowerFormat = timeFMT
        var upperVisible = dateVisible
        var lowerVisible = timeVisible

        if (LauncherPreferences.clock().flipDateTime()) {
            upperFormat = lowerFormat.also { lowerFormat = upperFormat }
            upperVisible = lowerVisible.also { lowerVisible = upperVisible }
        }

        binding.homeUpperView.isVisible = upperVisible
        binding.homeLowerView.isVisible = lowerVisible

        binding.homeUpperView.setTextColor(LauncherPreferences.clock().color())
        binding.homeLowerView.setTextColor(LauncherPreferences.clock().color())

        binding.homeLowerView.format24Hour = lowerFormat
        binding.homeUpperView.format24Hour = upperFormat
        binding.homeLowerView.format12Hour = lowerFormat
        binding.homeUpperView.format12Hour = upperFormat
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

        touchGestureDetector.edgeWidth =
            LauncherPreferences.enabled_gestures().edgeSwipeEdgeWidth() / 100f

        initClock()
        updateSettingsFallbackButtonVisibility()
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
        return touchGestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun setOnClicks() {

        binding.homeUpperView.setOnClickListener {
            if (LauncherPreferences.clock().flipDateTime()) {
                Gesture.TIME(this)
            } else {
                Gesture.DATE(this)
            }
        }

        binding.homeLowerView.setOnClickListener {
            if (LauncherPreferences.clock().flipDateTime()) {
                Gesture.DATE(this)
            } else {
                Gesture.TIME(this)
            }
        }
    }


    private fun handleBack() {
        Gesture.BACK(this)
    }

    override fun isHomeScreen(): Boolean {
        return true
    }
}
