package de.jrpie.android.launcher

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.UserHandle
import android.os.UserManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import de.jrpie.android.launcher.actions.TorchManager
import de.jrpie.android.launcher.apps.AbstractAppInfo
import de.jrpie.android.launcher.apps.AbstractAppInfo.Companion.INVALID_USER
import de.jrpie.android.launcher.apps.AbstractDetailedAppInfo
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.apps.isPrivateSpaceLocked
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.preferences.migratePreferencesToNewVersion
import de.jrpie.android.launcher.preferences.resetPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.system.exitProcess


const val APP_WIDGET_HOST_ID = 42


class Application : android.app.Application() {
    val apps = MutableLiveData<List<AbstractDetailedAppInfo>>()
    val privateSpaceLocked = MutableLiveData<Boolean>()
    val onThemeChanged = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    lateinit var appWidgetHost: AppWidgetHost
    lateinit var appWidgetManager: AppWidgetManager

    private val profileAvailabilityBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // TODO: only update specific apps
            // use Intent.EXTRA_USER
            loadApps()
        }
    }

    private val launcherAppsCallback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(p0: String?, p1: UserHandle?) {
            updatePackage(p0, p1)
        }

        override fun onPackageAdded(p0: String?, p1: UserHandle?) {
            updatePackage(p0, p1)
        }

        override fun onPackageChanged(p0: String?, p1: UserHandle?) {
            updatePackage(p0, p1)
        }

        override fun onPackagesAvailable(p0: Array<out String>?, p1: UserHandle?, p2: Boolean) {
            // TODO
        }

        override fun onPackagesSuspended(packageNames: Array<out String>?, user: UserHandle?) {
            // TODO
        }

        override fun onPackagesUnsuspended(packageNames: Array<out String>?, user: UserHandle?) {
            // TODO
        }

        override fun onPackagesUnavailable(p0: Array<out String>?, p1: UserHandle?, p2: Boolean) {
            // TODO
        }

        override fun onPackageLoadingProgressChanged(
            packageName: String,
            user: UserHandle,
            progress: Float
        ) {
            // TODO
        }

        override fun onShortcutsChanged(
            packageName: String,
            shortcuts: MutableList<ShortcutInfo>,
            user: UserHandle
        ) {
            // TODO
        }
    }

    // All changes to the app list run sequentially on this scope.
    private val appsScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    private var loadedApps: List<AbstractDetailedAppInfo>? = null
    private var reloadJob: Job? = null
    private var iconJob: Job? = null

    var torchManager: TorchManager? = null
    private var customAppNames: HashMap<AbstractAppInfo, String>? = null
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, pref ->
        if (pref == getString(R.string.settings_apps_custom_names_key)) {
            customAppNames = LauncherPreferences.apps().customNames()
        } else if (pref == LauncherPreferences.apps().keys().pinnedShortcuts()) {
            loadApps()
        }

        if (pref?.startsWith("theme.") == true || pref?.startsWith("display.") == true) {
            onThemeChanged.tryEmit(Unit)
        }
    }

    override fun onCreate() {
        super.onCreate()
        // TODO  Error: Invalid resource ID 0x00000000.
        // DynamicColors.applyToActivitiesIfAvailable(this)

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            sendCrashNotification(this@Application, throwable)
            exitProcess(1)
        }


        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            torchManager = TorchManager(this)
        }

        appWidgetHost = AppWidgetHost(this.applicationContext, APP_WIDGET_HOST_ID)
        appWidgetManager = AppWidgetManager.getInstance(this.applicationContext)


        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        LauncherPreferences.init(preferences, this.resources)

        // Try to restore old preferences
        migratePreferencesToNewVersion(this)

        // First time opening the app: set defaults
        // The tutorial is started from HomeActivity#onStart, as starting it here is blocked by android
        if (!LauncherPreferences.internal().started()) {
            resetPreferences(this)
        }


        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(listener)


        val launcherApps = getSystemService(LAUNCHER_APPS_SERVICE) as LauncherApps
        launcherApps.registerCallback(launcherAppsCallback)

        if (Build.VERSION.SDK_INT >= VERSION_CODES.N) {
            val filter = IntentFilter().also {
                if (Build.VERSION.SDK_INT >= VERSION_CODES.VANILLA_ICE_CREAM) {
                    it.addAction(Intent.ACTION_PROFILE_AVAILABLE)
                    it.addAction(Intent.ACTION_PROFILE_UNAVAILABLE)
                } else {
                    it.addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
                    it.addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
                }
            }
            ContextCompat.registerReceiver(
                this, profileAvailabilityBroadcastReceiver, filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }

        if (Build.VERSION.SDK_INT >= VERSION_CODES.N_MR1) {
            removeUnusedShortcuts(this)
        }
        loadApps()

        createNotificationChannels(this)
    }

    fun getCustomAppNames(): HashMap<AbstractAppInfo, String> {
        return (customAppNames ?: LauncherPreferences.apps().customNames() ?: HashMap())
            .also { customAppNames = it }
    }

    private fun loadApps() {
        privateSpaceLocked.postValue(isPrivateSpaceLocked(this))
        // a reload that has not started yet is superseded by this one
        reloadJob?.cancel()
        reloadJob = appsScope.launch {
            publishApps(getApps(packageManager, applicationContext))
        }
    }

    /**
     * Reload only the apps of [packageName] instead of all apps.
     */
    private fun updatePackage(packageName: String?, user: UserHandle?) {
        if (packageName == null || user == null) {
            loadApps()
            return
        }
        appsScope.launch {
            // if no apps are loaded yet, a full reload is pending and will include this package
            val current = loadedApps ?: return@launch

            val userManager = getSystemService(USER_SERVICE) as UserManager
            // quiet profiles need the special handling of getApps
            val quiet = Build.VERSION.SDK_INT >= VERSION_CODES.N &&
                    userManager.isQuietModeEnabled(user)
            // apps loaded using the fallback method can't be updated individually
            val fallback = current.any {
                (it.getRawInfo() as? AppInfo)?.user == INVALID_USER
            }
            val updated = if (quiet || fallback) {
                null
            } else {
                getAppsOfPackage(packageName, user, applicationContext)
            }
            if (updated == null) {
                publishApps(getApps(packageManager, applicationContext))
                return@launch
            }

            val userId = user.hashCode()
            val apps = current.filterNot {
                val app = it.getRawInfo()
                app is AppInfo && app.packageName == packageName && app.user == userId
            } + updated
            publishApps(apps.sortedBy { it.getCustomLabel(applicationContext) })
        }
    }

    // only called from appsScope
    private fun publishApps(list: List<AbstractDetailedAppInfo>) {
        loadedApps = list
        apps.postValue(list)

        // load icons in the background, so they don't have to be loaded while scrolling
        iconJob?.cancel()
        iconJob = CoroutineScope(Dispatchers.IO).launch {
            list.sortedBy { it.getCustomLabel(applicationContext).lowercase(Locale.ROOT) }
                .forEach {
                    ensureActive()
                    it.getIcon(applicationContext)
                }
        }
    }
}
