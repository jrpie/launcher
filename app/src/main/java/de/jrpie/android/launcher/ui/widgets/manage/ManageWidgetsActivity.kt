package de.jrpie.android.launcher.ui.widgets.manage

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.ui.widgets.WidgetContainerView
import de.jrpie.android.launcher.widgets.AppWidget
import de.jrpie.android.launcher.widgets.WidgetPosition
import kotlin.math.min


// http://coderender.blogspot.com/2012/01/hosting-android-widgets-my.html

const val REQUEST_CREATE_APPWIDGET = 1
const val REQUEST_PICK_APPWIDGET = 2

class ManageWidgetsActivity : Activity(), UIObject {

    private var sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            if (prefKey?.startsWith("internal.widgets") == true) {
                findViewById<WidgetContainerView>(R.id.manage_widgets_container).updateWidgets(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<Activity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()
        setContentView(R.layout.activity_manage_widgets)
        findViewById<FloatingActionButton>(R.id.manage_widgets_button_add).setOnClickListener {
            selectWidget()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<WidgetContainerView>(R.id.manage_widgets_container).updateWidgets(this)
    }

    override fun onStart() {
        super<Activity>.onStart()
        super<UIObject>.onStart()

        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(sharedPreferencesListener)

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

    override fun onDestroy() {
        LauncherPreferences.getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
        super.onDestroy()
    }


    fun selectWidget() {
        val appWidgetHost = (application as Application).appWidgetHost
        startActivityForResult(
            Intent(this, SelectWidgetActivity::class.java).also {
                it.putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    appWidgetHost.allocateAppWidgetId()
                )
            }, REQUEST_PICK_APPWIDGET
        )
    }


    fun createWidget(data: Intent) {
        Log.i("Launcher", "creating widget")
        val appWidgetManager = (application as Application).appWidgetManager
        val appWidgetId = data.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: return

        val provider = appWidgetManager.getAppWidgetInfo(appWidgetId)

        val display = windowManager.defaultDisplay

        val position = WidgetPosition.fromAbsoluteRect(
            Rect(0,0,
            min(400, appWidgetManager.getAppWidgetInfo(appWidgetId).minWidth),
            min(400, appWidgetManager.getAppWidgetInfo(appWidgetId).minHeight)
            ),
            display.width,
            display.height
        )

        val widget = AppWidget(appWidgetId, provider, position)
        LauncherPreferences.internal().widgets(
            (LauncherPreferences.internal().widgets() ?: HashSet()).also {
                it.add(widget)
            }
        )

        findViewById<WidgetContainerView>(R.id.manage_widgets_container).updateWidgets(this)
    }

    private fun configureWidget(data: Intent) {
        val extras = data.extras
        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
        val appWidgetHost = (application as Application).appWidgetHost
        val appWidgetInfo: AppWidgetProviderInfo =
            (application as Application).appWidgetManager.getAppWidgetInfo(appWidgetId) ?: return
        if (appWidgetInfo.configure != null) {
            appWidgetHost.startAppWidgetConfigureActivityForResult(
                this,
                appWidgetId,
                0,
                REQUEST_CREATE_APPWIDGET,
                null
            )

        } else {
            createWidget(data)
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            Log.i("Manage Widgets", "result ok")
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data!!)
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data!!)
            }
        } else if (resultCode == RESULT_CANCELED && data != null) {
            Log.i("Manage Widgets", "result canceled")
            val appWidgetId =
                data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                AppWidget(appWidgetId).delete(this)
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