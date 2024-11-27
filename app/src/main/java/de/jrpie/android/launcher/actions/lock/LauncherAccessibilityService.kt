package de.jrpie.android.launcher.actions.lock

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.core.content.getSystemService
import de.jrpie.android.launcher.BuildConfig
import de.jrpie.android.launcher.R

class LauncherAccessibilityService : AccessibilityService() {
    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally left blank, we are not interested in any AccessibilityEvents.
        // DO NOT ADD ANY CODE HERE!
    }

    companion object {
        private const val TAG = "Launcher Accessibility"
        const val ACTION_LOCK_SCREEN = "ACTION_LOCK_SCREEN"

        fun lockScreen(context: Context) {
            try {
                context.startService(
                    Intent(
                        context,
                        LauncherAccessibilityService::class.java
                    ).apply {
                        action = ACTION_LOCK_SCREEN
                    })
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    context.getString(R.string.alert_lock_screen_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        fun isEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            return enabledServices.split(":")
                .contains("${context.packageName}/${LauncherAccessibilityService::class.java.name}")
                .also { Log.d(TAG, "Accessibility Service enabled: $it") }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            if (!isEnabled(this)) {
                Toast.makeText(
                    this,
                    getString(R.string.toast_accessibility_service_not_enabled),
                    Toast.LENGTH_LONG
                ).show()
                requestEnable()
                return START_NOT_STICKY
            }

            when (action) {
                ACTION_LOCK_SCREEN -> handleLockScreen()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun requestEnable() {
        startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        )
    }

    private fun handleLockScreen() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Toast.makeText(
                this,
                getText(R.string.toast_lock_screen_not_supported),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val success = performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        if (!success) {
            Toast.makeText(
                this,
                getText(R.string.alert_lock_screen_failed),
                Toast.LENGTH_LONG
            ).show()
            requestEnable()
        }
    }
}