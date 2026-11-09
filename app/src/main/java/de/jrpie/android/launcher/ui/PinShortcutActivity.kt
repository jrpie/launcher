package de.jrpie.android.launcher.ui

import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.core.content.edit
import de.jrpie.android.launcher.actions.ShortcutAction
import de.jrpie.android.launcher.apps.DetailedPinnedShortcutInfo
import de.jrpie.android.launcher.apps.PinnedShortcutInfo
import de.jrpie.android.launcher.databinding.ActivityPinShortcutBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.settings.DialogBindToGesture

class PinShortcutActivity : UIObjectActivity() {
    private lateinit var binding: ActivityPinShortcutBinding

    private var isBound = false
    private var request: PinItemRequest? = null

    // an idempotent wrapper around PinItemRequest#accept()
    private fun acceptRequest() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        synchronized(this@PinShortcutActivity) {
            if (!isBound && request?.isValid == true) {
                if (request?.accept() == true) {
                    isBound = true
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            finish()
            return
        }

        binding = ActivityPinShortcutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val launcherApps = getSystemService(LAUNCHER_APPS_SERVICE) as LauncherApps

        val request = launcherApps.getPinItemRequest(intent)
        this.request = request
        if (request == null) {
            finish()
            return
        }

        if (request.requestType == PinItemRequest.REQUEST_TYPE_APPWIDGET) {
            // TODO handle app widgets
            Log.w("Launcher", "widgets currently unsupported")
            request.getAppWidgetProviderInfo(this)
            finish()
            return
        }

        if (request.requestType != PinItemRequest.REQUEST_TYPE_SHORTCUT) {
            Log.w("Launcher", "unsupported request")
            finish()
            return
        }

        val detailedPinnedShortcutInfo = DetailedPinnedShortcutInfo(this, request.shortcutInfo!!)

        binding.pinShortcutLabel.text = request.shortcutInfo!!.shortLabel ?: "?"
        binding.pinShortcutLabel.setCompoundDrawables(
            detailedPinnedShortcutInfo.getIcon(this).also {
                val size = (40 * resources.displayMetrics.density).toInt()
                it.setBounds(0, 0, size, size)
            }, null, null, null
        )

        binding.pinShortcutButtonBind.setOnClickListener {
            DialogBindToGesture(this)
            { gesture ->
                acceptRequest()
                LauncherPreferences.getSharedPreferences().edit {
                    ShortcutAction(PinnedShortcutInfo(request.shortcutInfo!!)).bindToGesture(
                        this,
                        gesture.id
                    )
                }
            }
        }

        binding.pinShortcutClose.setOnClickListener { finish() }
        binding.pinShortcutButtonOk.setOnClickListener { finish() }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (binding.pinShortcutSwitchVisible.isChecked) {
                acceptRequest()
                request?.shortcutInfo?.let {
                    Log.e("Launcher", "add to list")
                    val set = LauncherPreferences.apps().pinnedShortcuts() ?: mutableSetOf()
                    set.add(PinnedShortcutInfo(it))
                    LauncherPreferences.apps().pinnedShortcuts(set)
                }
            }
        }
        super.onDestroy()
    }
}