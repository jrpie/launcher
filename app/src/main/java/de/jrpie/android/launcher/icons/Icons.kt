package de.jrpie.android.launcher.icons

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log

private val iconPackIntentActions = listOf(
    "com.novalauncher.THEME",
    "fr.neamar.kiss.THEMES",
    "org.adw.launcher.THEMES"
)

fun loadIconPacks(context: Context): List<CustomIconPackHandle> {

    val iconPacks: List<CustomIconPackHandle> = iconPackIntentActions.flatMap { action ->
        context.packageManager.queryIntentActivities(
            Intent(action), PackageManager.GET_META_DATA
        )
    }.map { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val label = resolveInfo.activityInfo.loadLabel(context.packageManager).toString()
            CustomIconPackHandle(packageName, label)
    }.distinct().toList()

    iconPacks.forEach {
        Log.i("Launcher", "Found icon pack: ${it.label} (${it.packageName})")
    }

    return iconPacks
}