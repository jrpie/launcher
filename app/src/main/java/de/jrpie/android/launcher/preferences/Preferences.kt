package de.jrpie.android.launcher.preferences

import android.content.Context
import android.util.Log
import de.jrpie.android.launcher.BuildConfig
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.apps.AbstractAppInfo
import de.jrpie.android.launcher.apps.AbstractAppInfo.Companion.INVALID_USER
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.apps.DetailedAppInfo
import de.jrpie.android.launcher.preferences.legacy.migratePreferencesFromVersion1
import de.jrpie.android.launcher.preferences.legacy.migratePreferencesFromVersion100
import de.jrpie.android.launcher.preferences.legacy.migratePreferencesFromVersion2
import de.jrpie.android.launcher.preferences.legacy.migratePreferencesFromVersion3
import de.jrpie.android.launcher.preferences.legacy.migratePreferencesFromVersion4
import de.jrpie.android.launcher.preferences.legacy.migratePreferencesFromVersionUnknown
import de.jrpie.android.launcher.sendCrashNotification
import de.jrpie.android.launcher.ui.HomeActivity
import de.jrpie.android.launcher.widgets.ClockWidget
import de.jrpie.android.launcher.widgets.DebugInfoWidget
import de.jrpie.android.launcher.widgets.WidgetPanel
import de.jrpie.android.launcher.widgets.WidgetPosition
import de.jrpie.android.launcher.widgets.generateInternalId
import de.jrpie.android.launcher.widgets.getAppWidgetHost

/* Current version of the structure of preferences.
 * Increase when breaking changes are introduced and write an appropriate case in
 * `migratePreferencesToNewVersion`
 */
const val PREFERENCE_VERSION = 101
const val UNKNOWN_PREFERENCE_VERSION = -1
private const val TAG = "Launcher - Preferences"


/*
 * Tries to detect preferences written by older versions of the app
 * and migrate them to the current format.
 */
fun migratePreferencesToNewVersion(context: Context) {
    try {
        when (LauncherPreferences.internal().versionCode()) {
            // Check versions, make sure transitions between versions go well
            PREFERENCE_VERSION -> { /* the version installed and used previously are the same */
            }

            UNKNOWN_PREFERENCE_VERSION -> { /* still using the old preferences file */
                migratePreferencesFromVersionUnknown(context)
                Log.i(
                    TAG,
                    "migration of preferences  complete (${UNKNOWN_PREFERENCE_VERSION} -> ${PREFERENCE_VERSION})."
                )
            }

            1 -> {
                migratePreferencesFromVersion1(context)
                Log.i(TAG, "migration of preferences  complete (1 -> ${PREFERENCE_VERSION}).")
            }

            2 -> {
                migratePreferencesFromVersion2(context)
                Log.i(TAG, "migration of preferences  complete (2 -> ${PREFERENCE_VERSION}).")
            }

            3 -> {
                migratePreferencesFromVersion3(context)
                Log.i(TAG, "migration of preferences  complete (3 -> ${PREFERENCE_VERSION}).")
            }

            // There was a bug where instead of the preference version the app version was written.
            in 4..99 -> {
                migratePreferencesFromVersion4(context)
                Log.i(TAG, "migration of preferences  complete (4 -> ${PREFERENCE_VERSION}).")
            }

            100 -> {
                migratePreferencesFromVersion100(context)
                Log.i(TAG, "migration of preferences  complete (100 -> ${PREFERENCE_VERSION}).")
            }

            else -> {
                Log.w(
                    TAG,
                    "Shared preferences were written by a newer version of the app (${
                        LauncherPreferences.internal().versionCode()
                    })!"
                )
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Unable to restore preferences:\n${e.stackTrace}")
        sendCrashNotification(context, e)
        resetPreferences(context)
    }
}

fun resetPreferences(context: Context) {
    Log.i(TAG, "Resetting preferences")
    LauncherPreferences.clear()
    LauncherPreferences.internal().versionCode(PREFERENCE_VERSION)
    context.getAppWidgetHost().deleteHost()

    LauncherPreferences.widgets().widgets(
        setOf(
            ClockWidget(
                generateInternalId(),
                WidgetPosition(1, 3, 10, 4),
                WidgetPanel.HOME.id
            )
        )
    )

    if (BuildConfig.DEBUG) {
        LauncherPreferences.widgets().widgets(
            LauncherPreferences.widgets().widgets().also {
                it.add(
                    DebugInfoWidget(
                        generateInternalId(),
                        WidgetPosition(1, 1, 10, 4),
                        WidgetPanel.HOME.id
                    )
                )
            }
        )
    }

    val hidden: MutableSet<AbstractAppInfo> = mutableSetOf()

    if (!BuildConfig.DEBUG) {
        val launcher = DetailedAppInfo.fromAppInfo(
            AppInfo(
                BuildConfig.APPLICATION_ID,
                HomeActivity::class.java.name,
                INVALID_USER
            ), context
        )
        launcher?.getRawInfo()?.let { hidden.add(it) }
        Log.i(TAG, "Hiding ${launcher?.getRawInfo()}")
    }
    LauncherPreferences.apps().hidden(hidden)

    Action.resetToDefaultActions(context)
}
