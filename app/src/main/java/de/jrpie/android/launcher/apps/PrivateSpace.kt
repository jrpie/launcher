package de.jrpie.android.launcher.apps

import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.LauncherUserInfo
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.isDefaultHomeScreen
import de.jrpie.android.launcher.setDefaultHomeScreen


/*
 * Checks whether the device supports private space.
 */
fun isPrivateSpaceSupported(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
}

fun getPrivateSpaceUser(context: Context): UserHandle? {
    if (!isPrivateSpaceSupported()) {
        return null
    }
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    return userManager.userProfiles.firstOrNull { u ->
        launcherApps.getLauncherUserInfo(u)?.userType == UserManager.USER_TYPE_PROFILE_PRIVATE
    }
}

/**
 * Check whether the user has created a private space and whether µLauncher can access it.
 */
fun isPrivateSpaceSetUp(
    context: Context,
    showToast: Boolean = false,
    launchSettings: Boolean = false
): Boolean {
    if (!isPrivateSpaceSupported()) {
        if (showToast) {
            Toast.makeText(
                context,
                context.getString(R.string.alert_requires_android_v),
                Toast.LENGTH_LONG
            ).show()
        }
        return false
    }
    val privateSpaceUser = getPrivateSpaceUser(context)
    if (privateSpaceUser != null) {
        return true
    }
    if (!isDefaultHomeScreen(context)) {
        if (showToast) {
            Toast.makeText(
                context,
                context.getString(R.string.toast_private_space_default_home_screen),
                Toast.LENGTH_LONG
            ).show()
        }
        if (launchSettings) {
            setDefaultHomeScreen(context)
        }
    } else {
        if (showToast) {
            Toast.makeText(
                context,
                context.getString(R.string.toast_private_space_not_available),
                Toast.LENGTH_LONG
            ).show()
        }
        if (launchSettings) {
            try {
                context.startActivity(Intent(Settings.ACTION_PRIVACY_SETTINGS))
            } catch (_: ActivityNotFoundException) {
            }
        }
    }
    return false
}

fun isPrivateSpaceLocked(context: Context): Boolean {
    if (!isPrivateSpaceSupported()) {
        return false
    }
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val privateSpaceUser = getPrivateSpaceUser(context) ?: return false
    return userManager.isQuietModeEnabled(privateSpaceUser)
}

fun lockPrivateSpace(context: Context, lock: Boolean) {
    if (!isPrivateSpaceSupported()) {
        return
    }
    Log.i("Launcher", if (lock) "Locking" else "Unlocking" + " private space")

    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val privateSpaceUser = getPrivateSpaceUser(context) ?: return
    userManager.requestQuietModeEnabled(lock, privateSpaceUser)
}

fun togglePrivateSpaceLock(context: Context) {
    if (!isPrivateSpaceSetUp(context, showToast = true, launchSettings = true)) {
        return
    }

    val lock = isPrivateSpaceLocked(context)
    lockPrivateSpace(context, !lock)
    if (!lock) {
        Toast.makeText(
            context,
            context.getString(R.string.toast_private_space_locked),
            Toast.LENGTH_LONG
        ).show()
    }
}

fun hidePrivateSpaceWhenLocked(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) {
        return false
    }
    val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
    val privateSpaceUser = getPrivateSpaceUser(context) ?: return false
    val privateSpaceLauncherUserInfo =
        launcherApps.getLauncherUserInfo(privateSpaceUser) ?: return false
    return privateSpaceLauncherUserInfo.userConfig.getBoolean(
        LauncherUserInfo.PRIVATE_SPACE_ENTRYPOINT_HIDDEN,
        true
    )
}