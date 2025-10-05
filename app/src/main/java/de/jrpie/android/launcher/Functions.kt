package de.jrpie.android.launcher

import android.app.Activity
import android.app.Service
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.ShortcutQuery
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.os.Build
import android.os.Bundle
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.actions.ShortcutAction
import de.jrpie.android.launcher.apps.AbstractAppInfo.Companion.INVALID_USER
import de.jrpie.android.launcher.apps.AbstractDetailedAppInfo
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.apps.DetailedAppInfo
import de.jrpie.android.launcher.apps.DetailedPinnedShortcutInfo
import de.jrpie.android.launcher.apps.PinnedShortcutInfo
import de.jrpie.android.launcher.apps.getPrivateSpaceUser
import de.jrpie.android.launcher.apps.isPrivateSpaceSupported
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.tutorial.TutorialActivity
import androidx.core.net.toUri


const val LOG_TAG = "Launcher"

const val REQUEST_SET_DEFAULT_HOME = 42

fun isDefaultHomeScreen(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
    } else {
        val testIntent = Intent(Intent.ACTION_MAIN)
        testIntent.addCategory(Intent.CATEGORY_HOME)
        val defaultHome = testIntent.resolveActivity(context.packageManager)?.packageName
        return defaultHome == context.packageName
    }
}

fun setDefaultHomeScreen(context: Context, checkDefault: Boolean = false) {
    val isDefault = isDefaultHomeScreen(context)
    if (checkDefault && isDefault) {
        // Launcher is already the default home app
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        && context is Activity
        && checkDefault // using role manager only works when µLauncher is not already the default.
    ) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        context.startActivityForResult(
            roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME),
            REQUEST_SET_DEFAULT_HOME
        )
        return
    }

    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
    context.startActivity(intent)
}

fun getUserFromId(userId: Int?, context: Context): UserHandle {
    /* TODO: this is an ugly hack.
        Use userManager#getUserForSerialNumber instead (breaking change to SharedPreferences!)
     */
    val userManager = context.getSystemService(Service.USER_SERVICE) as UserManager
    val profiles = userManager.userProfiles
    return profiles.firstOrNull { it.hashCode() == userId } ?: profiles[0]
}

@RequiresApi(Build.VERSION_CODES.N_MR1)
fun removeUnusedShortcuts(context: Context) {
    val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
    fun getShortcuts(profile: UserHandle): List<ShortcutInfo>? {
        return try {
            launcherApps.getShortcuts(
                ShortcutQuery().apply {
                    setQueryFlags(ShortcutQuery.FLAG_MATCH_PINNED)
                },
                profile
            )
        } catch (e: Exception) {
            // https://github.com/jrpie/launcher/issues/116
            return null
        }
    }

    val userManager = context.getSystemService(Service.USER_SERVICE) as UserManager
    val boundActions: MutableSet<PinnedShortcutInfo> =
        Gesture.entries.mapNotNull { Action.forGesture(it) as? ShortcutAction }.map { it.shortcut }
            .toMutableSet()
    LauncherPreferences.apps().pinnedShortcuts()?.let { boundActions.addAll(it) }
    try {
        userManager.userProfiles.filter { !userManager.isQuietModeEnabled(it) }.forEach { profile ->
            getShortcuts(profile)?.groupBy { it.`package` }?.forEach { (p, shortcuts) ->
                launcherApps.pinShortcuts(p,
                    shortcuts.filter { boundActions.contains(PinnedShortcutInfo(it)) }
                        .map { it.id }.toList(),
                    profile
                )
            }
        }
    } catch (_: SecurityException) { }
}

fun openInBrowser(url: String, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    intent.putExtras(Bundle().apply { putBoolean("new_window", true) })
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.toast_activity_not_found_browser, Toast.LENGTH_LONG).show()
    }
}

fun openTutorial(context: Context) {
    context.startActivity(Intent(context, TutorialActivity::class.java))
}


/**
 * Load all apps.
 */
fun getApps(
    packageManager: PackageManager,
    context: Context
): MutableList<AbstractDetailedAppInfo> {
    var start = System.currentTimeMillis()
    val loadList = mutableListOf<AbstractDetailedAppInfo>()

    val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
    val userManager = context.getSystemService(Service.USER_SERVICE) as UserManager

    val privateSpaceUser = getPrivateSpaceUser(context)

    // TODO: shortcuts - launcherApps.getShortcuts()
    val users = userManager.userProfiles
    for (user in users) {
        // don't load apps from a user profile that has quiet mode enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (userManager.isQuietModeEnabled(user)) {
                // hide paused apps
                if (LauncherPreferences.apps().hidePausedApps()) {
                    continue
                }
                // hide apps from private space
                if (isPrivateSpaceSupported() &&
                    launcherApps.getLauncherUserInfo(user)?.userType == UserManager.USER_TYPE_PROFILE_PRIVATE
                ) {
                    continue
                }
            }
        }
        try {
            launcherApps.getActivityList(null, user).forEach {
                loadList.add(DetailedAppInfo(it, it.user == privateSpaceUser))
            }
        } catch (e: Exception) {
            // getActivityList seems to be broken on some Android distributions.
            // DeadSystemException, BadParcelableException
            Log.w(LOG_TAG, "exception thrown while loading apps", e)
        }
    }

    // fallback option
    if (loadList.isEmpty()) {
        Log.w(LOG_TAG, "using fallback option to load packages")
        val i = Intent(Intent.ACTION_MAIN, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)

        val allApps = try {
            packageManager.queryIntentActivities(i, 0)
        } catch (e: Exception) {
            // DeadSystemException
            Log.w(LOG_TAG, "exception thrown while loading apps (fallback method)", e)
            listOf()
        }
        for (ri in allApps) {
            val app = AppInfo(ri.activityInfo.packageName, null, INVALID_USER)
            val detailedAppInfo = DetailedAppInfo(
                app,
                ri.loadLabel(packageManager),
                ri.activityInfo.loadIcon(packageManager),
                false
            )
            loadList.add(detailedAppInfo)
        }
    }
    loadList.sortBy { it.getCustomLabel(context) }

    var end = System.currentTimeMillis()
    Log.i(LOG_TAG, "${loadList.size} apps loaded (${end - start}ms)")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        start = System.currentTimeMillis()
        LauncherPreferences.apps().pinnedShortcuts()
            ?.mapNotNull { DetailedPinnedShortcutInfo.fromPinnedShortcutInfo(it, context) }
            ?.let {
                end = System.currentTimeMillis()
                Log.i(LOG_TAG, "${it.size} shortcuts loaded (${end - start}ms)")
                loadList.addAll(it)
            }
    }

    return loadList
}

// used for the bug report button
fun getDeviceInfo(): String {
    return """
        µLauncher version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
        Commit ${BuildConfig.GIT_COMMIT.take(8)}
        Android version: ${Build.VERSION.RELEASE} (sdk ${Build.VERSION.SDK_INT})
        Model: ${Build.MODEL}
        Device: ${Build.DEVICE}
        Brand: ${Build.BRAND}
        Manufacturer: ${Build.MANUFACTURER}
    """.trimIndent()
}

fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("Debug Info", text)
    clipboardManager.setPrimaryClip(clipData)
}

fun writeEmail(context: Context, to: String, subject: String, text: String) {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = "mailto:".toUri()
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_email)))
}

