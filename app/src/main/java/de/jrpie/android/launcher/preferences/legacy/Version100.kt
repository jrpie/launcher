package de.jrpie.android.launcher.preferences.legacy

import android.content.Context
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.preferences.PREFERENCE_VERSION
import de.jrpie.android.launcher.widgets.ClockWidget
import de.jrpie.android.launcher.widgets.DebugInfoWidget
import de.jrpie.android.launcher.widgets.generateInternalId
import de.jrpie.android.launcher.widgets.updateWidget

fun migratePreferencesFromVersion100(context: Context) {
    assert(PREFERENCE_VERSION == 101)
    assert(LauncherPreferences.internal().versionCode() == 100)

    val widgets = LauncherPreferences.widgets().widgets() ?: setOf()
    widgets.forEach { widget ->
        when (widget) {
            is ClockWidget -> {
                val id = widget.id
                val newId = generateInternalId()
                (context.applicationContext as Application).appWidgetHost.deleteAppWidgetId(id)
                widget.delete(context)
                widget.id = newId
                updateWidget(widget)
            }

            is DebugInfoWidget -> {
                val id = widget.id
                val newId = generateInternalId()
                (context.applicationContext as Application).appWidgetHost.deleteAppWidgetId(id)
                widget.delete(context)
                widget.id = newId
                updateWidget(widget)
            }

            else -> {}
        }
    }
    LauncherPreferences.internal().versionCode(101)
}