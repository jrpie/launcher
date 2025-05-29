package de.jrpie.android.launcher.preferences.legacy

import android.content.Context
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.widgets.ClockWidget
import de.jrpie.android.launcher.widgets.WidgetPanel
import de.jrpie.android.launcher.widgets.WidgetPosition
import de.jrpie.android.launcher.widgets.generateInternalId

fun migratePreferencesFromVersion4(context: Context) {
    assert(LauncherPreferences.internal().versionCode() < 100)

    LauncherPreferences.widgets().widgets(
        setOf(
            ClockWidget(
                generateInternalId(),
                WidgetPosition(1, 3, 10, 4),
                WidgetPanel.HOME.id
            )
        )
    )
    LauncherPreferences.internal().versionCode(100)
    migratePreferencesFromVersion100(context)
}