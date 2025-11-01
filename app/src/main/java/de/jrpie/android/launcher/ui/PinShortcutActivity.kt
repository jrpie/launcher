package de.jrpie.android.launcher.ui

import android.app.AlertDialog
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.actions.ShortcutAction
import de.jrpie.android.launcher.apps.DetailedPinnedShortcutInfo
import de.jrpie.android.launcher.apps.PinnedShortcutInfo
import de.jrpie.android.launcher.databinding.ActivityPinShortcutBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences

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
                if(request?.accept() == true) {
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
                it.setBounds(0,0, size, size)
            }, null, null, null)

        binding.pinShortcutButtonBind.setOnClickListener {
            AlertDialog.Builder(this, R.style.AlertDialogCustom)
                .setTitle(getString(R.string.pin_shortcut_button_bind))
                .setView(R.layout.dialog_select_gesture)
                .setNegativeButton(android.R.string.cancel, null)
                .create().also { it.show() }.let { dialog ->
                    val viewManager = LinearLayoutManager(dialog.context)
                    val viewAdapter = GestureRecyclerAdapter (dialog.context) { gesture ->
                        acceptRequest()
                        LauncherPreferences.getSharedPreferences().edit {
                            ShortcutAction(PinnedShortcutInfo(request.shortcutInfo!!)).bindToGesture(
                                this,
                                gesture.id
                            )
                        }
                        dialog.dismiss()
                    }
                    dialog.findViewById<RecyclerView>(R.id.dialog_select_gesture_recycler).apply {
                        setHasFixedSize(true)
                        layoutManager = viewManager
                        adapter = viewAdapter
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

    inner class GestureRecyclerAdapter(val context: Context, val onClick: (Gesture) -> Unit): RecyclerView.Adapter<GestureRecyclerAdapter.ViewHolder>() {
        private val gestures = Gesture.entries.filter { it.isEnabled() }.toList()
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val label: TextView = itemView.findViewById(R.id.dialog_select_gesture_row_name)
            val description: TextView = itemView.findViewById(R.id.dialog_select_gesture_row_description)
            val icon: ImageView = itemView.findViewById(R.id.dialog_select_gesture_row_icon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view: View = inflater.inflate(R.layout.dialog_select_gesture_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val gesture = gestures[position]
            val (icon, label) = Action.forGesture(gesture)
                ?.getIconAndContentDescription(context)
                ?: Pair(null, null)
            holder.label.text = gesture.getLabel(context)
            holder.description.text = gesture.getDescription(context)
            holder.icon.setImageDrawable(icon)
            holder.icon.contentDescription = label
            holder.itemView.setOnClickListener {
                onClick(gesture)
            }
        }

        override fun getItemCount(): Int {
            return gestures.size
        }
    }
}