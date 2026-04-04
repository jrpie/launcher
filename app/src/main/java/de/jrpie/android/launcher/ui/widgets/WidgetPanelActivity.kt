package de.jrpie.android.launcher.ui.widgets

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.databinding.ActivityWidgetPanelBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.ui.util.LauncherGestureActivity
import de.jrpie.android.launcher.ui.widgets.manage.EXTRA_PANEL_ID
import de.jrpie.android.launcher.widgets.WidgetPanel

class WidgetPanelActivity : LauncherGestureActivity(), UIObject {
    var binding: ActivityWidgetPanelBinding? = null

    var widgetPanelId: Int = WidgetPanel.HOME.id

    override fun onCreate(savedInstanceState: Bundle?) {
        super<LauncherGestureActivity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()
        binding = ActivityWidgetPanelBinding.inflate(layoutInflater)
        val binding = binding!!
        setContentView(binding.root)

        widgetPanelId = intent.getIntExtra(EXTRA_PANEL_ID, WidgetPanel.HOME.id)

        // The widget container should extend below the status and navigation bars,
        // so let's set an empty WindowInsetsListener to prevent it from being moved.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            windowInsets
        }

        binding.widgetPanelWidgetContainer.widgetPanelId = widgetPanelId
        binding.widgetPanelWidgetContainer.updateWidgets(
            this,
            LauncherPreferences.widgets().widgets()
        )
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && LauncherPreferences.display().hideNavigationBar()) {
            hideNavigationBar()
        }
    }

    override fun onStart() {
        super<LauncherGestureActivity>.onStart()
        super<UIObject>.onStart()
    }

    override fun onPause() {
        try {
            (application as Application).appWidgetHost.stopListening()
        } catch (e: Exception) {
            // Throws a NullPointerException on Android 12 an earlier, see #172
            e.printStackTrace()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        (application as Application).appWidgetHost.startListening()
    }

    override fun getRootView(): View? {
        return binding?.root
    }

    override fun handleBack() {
        finish()
    }

    override fun isHomeScreen(): Boolean {
        return true
    }
}