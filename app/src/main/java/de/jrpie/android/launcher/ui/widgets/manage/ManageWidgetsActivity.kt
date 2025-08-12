package de.jrpie.android.launcher.ui.widgets.manage

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.databinding.ActivityManageWidgetsBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.widgets.AppWidget
import de.jrpie.android.launcher.widgets.GRID_SIZE
import de.jrpie.android.launcher.widgets.WidgetPanel
import de.jrpie.android.launcher.widgets.WidgetPosition
import kotlin.math.max
import kotlin.math.roundToInt


// http://coderender.blogspot.com/2012/01/hosting-android-widgets-my.html

const val REQUEST_CREATE_APPWIDGET = 1
const val REQUEST_PICK_APPWIDGET = 2

const val EXTRA_PANEL_ID = "widgetPanelId"

// We can't use AppCompatActivity, since some AppWidgets don't work there.
class ManageWidgetsActivity : UIObject, Activity() {

    private var panelId: Int = WidgetPanel.HOME.id
    private lateinit var binding: ActivityManageWidgetsBinding

    private var sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            if (prefKey == LauncherPreferences.widgets().keys().widgets()) {
                binding.manageWidgetsContainer.updateWidgets(
                    this,
                    LauncherPreferences.widgets().widgets()
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Activity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()
        binding = ActivityManageWidgetsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        panelId = intent.extras?.getInt(EXTRA_PANEL_ID, WidgetPanel.HOME.id) ?: WidgetPanel.HOME.id

        binding.manageWidgetsButtonAdd.setOnClickListener {
            selectWidget()
        }

        // The widget container should extend below the status and navigation bars,
        // so let's set an empty WindowInsetsListener to prevent it from being moved.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            windowInsets
        }

        // The button must not be placed under the navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.manageWidgetsButtonAdd) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }
            WindowInsetsCompat.CONSUMED
        }

        binding.manageWidgetsContainer.let {
            it.widgetPanelId = panelId
            it.updateWidgets(this, LauncherPreferences.widgets().widgets())
        }
    }

    override fun onStart() {
        super<Activity>.onStart()
        super<UIObject>.onStart()

        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(sharedPreferencesListener)

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

        binding.manageWidgetsContainer.updateWidgets(
            this,
            LauncherPreferences.widgets().widgets()
        )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && LauncherPreferences.display().hideNavigationBar()) {
            hideNavigationBar()
        }
    }

    override fun getTheme(): Resources.Theme {
        return modifyTheme(super.getTheme())
    }

    override fun onDestroy() {
        LauncherPreferences.getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
        super.onDestroy()
    }


    private fun selectWidget() {
        startActivityForResult(
            Intent(this, SelectWidgetActivity::class.java).also {
                it.putExtra(
                    EXTRA_PANEL_ID,
                    panelId
                )
            }, REQUEST_PICK_APPWIDGET
        )
    }


    private fun createWidget(data: Intent) {
        Log.i("Launcher", "creating widget")
        val appWidgetManager = (application as Application).appWidgetManager
        val appWidgetHost = (application as Application).appWidgetHost
        val appWidgetId = data.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: return

        val display = windowManager.defaultDisplay

        val widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
        if (widgetInfo == null) {
            Log.w("Launcher", "can't access widget")
            appWidgetHost.deleteAppWidgetId(appWidgetId)
            return
        }

        val position = WidgetPosition.findFreeSpace(
            WidgetPanel.byId(panelId),
            max(3, (GRID_SIZE * (widgetInfo.minWidth) / display.width.toFloat()).roundToInt()),
            max(3, (GRID_SIZE * (widgetInfo.minHeight) / display.height.toFloat()).roundToInt())
        )

        val widget = AppWidget(appWidgetId, position, panelId, widgetInfo)
        LauncherPreferences.widgets().widgets(
            (LauncherPreferences.widgets().widgets() ?: HashSet()).also {
                it.add(widget)
            }
        )
    }

    private fun configureWidget(data: Intent) {
        val extras = data.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val widget = AppWidget(appWidgetId, panelId = panelId)
        if (widget.isConfigurable(this)) {
            widget.configure(this, REQUEST_CREATE_APPWIDGET)
        } else {
            createWidget(data)
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        when (resultCode) {
            RESULT_OK -> {
                when (requestCode) {
                    REQUEST_PICK_APPWIDGET -> configureWidget(data)
                    REQUEST_CREATE_APPWIDGET -> createWidget(data)
                }
            }
            RESULT_CANCELED -> {
                val appWidgetId =
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                if (appWidgetId != -1) {
                    AppWidget(appWidgetId).delete(this)
                }
            }
        }
    }


    /**
     * For a better preview, [ManageWidgetsActivity] should behave exactly like [HomeActivity]
     */
    override fun isHomeScreen(): Boolean {
        return true
    }
}
