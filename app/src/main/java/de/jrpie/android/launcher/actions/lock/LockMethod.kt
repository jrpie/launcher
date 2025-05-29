package de.jrpie.android.launcher.actions.lock

import android.content.Context
import android.os.Build
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import de.jrpie.android.launcher.BuildConfig
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.preferences.LauncherPreferences


enum class LockMethod(
    private val lock: (Context) -> Unit,
    private val isEnabled: (Context) -> Boolean,
    private val enable: (Context) -> Unit
) {
    DEVICE_ADMIN(
        LauncherDeviceAdmin::lockScreen,
        LauncherDeviceAdmin::isDeviceAdmin,
        LauncherDeviceAdmin::lockScreen
    ),
    ACCESSIBILITY_SERVICE(
        LauncherAccessibilityService::lockScreen,
        LauncherAccessibilityService::isEnabled,
        LauncherAccessibilityService::showEnableDialog
    ),
    ;

    fun lockOrEnable(context: Context) {
        if (!this.isEnabled(context)) {
            chooseMethod(context)
            return
        }
        this.lock(context)
    }

    companion object {
        fun chooseMethod(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P ||
                ! BuildConfig.USE_ACCESSIBILITY_SERVICE) {
                // only device admin is available
                setMethod(context, DEVICE_ADMIN)
                return
            }
            AlertDialog.Builder(context, R.style.AlertDialogCustom).apply {
                setNegativeButton(R.string.dialog_cancel) { _, _ -> }
                setView(R.layout.dialog_select_lock_method)
                // setTitle()
            }.create().also { it.show() }.apply {
                findViewById<Button>(R.id.dialog_select_lock_method_button_accessibility)
                    ?.setOnClickListener {
                        setMethod(context, ACCESSIBILITY_SERVICE)
                        cancel()
                    }
                findViewById<Button>(R.id.dialog_select_lock_method_button_device_admin)
                    ?.setOnClickListener {
                        setMethod(context, DEVICE_ADMIN)
                        cancel()
                }
            }
            return
        }

        private fun setMethod(context: Context, m: LockMethod) {
            LauncherPreferences.actions().lockMethod(m)
            if (!m.isEnabled(context))
                m.enable(context)
        }
    }
}