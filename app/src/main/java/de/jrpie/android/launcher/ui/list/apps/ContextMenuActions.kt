package de.jrpie.android.launcher.ui.list.apps

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.google.android.material.snackbar.Snackbar
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.apps.AbstractAppInfo
import de.jrpie.android.launcher.apps.AbstractDetailedAppInfo
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.apps.PinnedShortcutInfo
import de.jrpie.android.launcher.getUserFromId
import de.jrpie.android.launcher.preferences.LauncherPreferences

private const val LOG_TAG = "AppContextMenu"

fun AppInfo.openSettings(
    context: Context,
    sourceBounds: Rect? = null,
    opts: Bundle? = null
) {
    val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
    this.getLauncherActivityInfo(context)?.let { app ->
        launcherApps.startAppDetailsActivity(app.componentName, app.user, sourceBounds, opts)
    }
}

fun AbstractAppInfo.uninstall(activity: Activity) {
    if (this is AppInfo) {
        val packageName = this.packageName
        val userId = this.user

        Log.i(LOG_TAG, "uninstalling $this")

        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = "package:$packageName".toUri()
        getUserFromId(userId, activity).let { user ->
            intent.putExtra(Intent.EXTRA_USER, user)
        }
        activity.startActivity(intent)

    } else if(this is PinnedShortcutInfo) {
        val pinned = LauncherPreferences.apps().pinnedShortcuts() ?: mutableSetOf()
        pinned.remove(this)
        LauncherPreferences.apps().pinnedShortcuts(pinned)
    }
}

fun AbstractAppInfo.toggleFavorite() {
    val favorites: MutableSet<AbstractAppInfo> =
        LauncherPreferences.apps().favorites() ?: mutableSetOf()

    if (favorites.contains(this)) {
        favorites.remove(this)
        Log.i(LOG_TAG, "Removing $this from favorites.")
    } else {
        Log.i(LOG_TAG, "Adding $this to favorites.")
        favorites.add(this)
    }

    LauncherPreferences.apps().favorites(favorites)
}

/**
 * @param view: used to show a snackbar letting the user undo the action
 */
fun AbstractAppInfo.toggleHidden(view: View) {
    val hidden: MutableSet<AbstractAppInfo> =
        LauncherPreferences.apps().hidden() ?: mutableSetOf()
    if (hidden.contains(this)) {
        hidden.remove(this)
    } else {
        hidden.add(this)

        Snackbar.make(view, R.string.snackbar_app_hidden, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) {
                LauncherPreferences.apps().hidden(
                    LauncherPreferences.apps().hidden().minus(this)
                )
            }.show()
    }
    LauncherPreferences.apps().hidden(hidden)
}

fun AbstractDetailedAppInfo.showRenameDialog(context: Context) {
    AlertDialog.Builder(context, R.style.AlertDialogCustom).apply {
        setTitle(context.getString(R.string.dialog_rename_title, getLabel()))
        setView(R.layout.dialog_rename_app)
        setNegativeButton(android.R.string.cancel) { d, _ -> d.cancel() }
        setPositiveButton(android.R.string.ok) { d, _ ->
            setCustomLabel(
                (d as? AlertDialog)
                    ?.findViewById<EditText>(R.id.dialog_rename_app_edit_text)
                    ?.text.toString()
            )
        }
    }.create().also { it.show() }.apply {
        val input = findViewById<EditText>(R.id.dialog_rename_app_edit_text)
        input?.setText(getCustomLabel(context))
        input?.hint = getLabel()
    }
}


