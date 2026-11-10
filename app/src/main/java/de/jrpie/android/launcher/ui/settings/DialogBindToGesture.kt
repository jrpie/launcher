package de.jrpie.android.launcher.ui.settings

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.styledAlertDialogBuilder
import de.jrpie.android.launcher.ui.transformMonochrome

class DialogBindToGesture(activity: Activity, onSelect: (Gesture) -> Unit) {
    init {
        styledAlertDialogBuilder(activity)
            .setTitle(activity.getString(R.string.pin_shortcut_button_bind))
            .setView(R.layout.dialog_select_gesture)
            .setNegativeButton(android.R.string.cancel, null)
            .create().also { it.show() }.let { dialog ->
                val viewManager = LinearLayoutManager(dialog.context)
                val viewAdapter = GestureRecyclerAdapter(dialog.context) { gesture ->
                    onSelect(gesture)
                    dialog.dismiss()
                }
                dialog.findViewById<RecyclerView>(R.id.dialog_select_gesture_recycler).apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                }
            }
    }

    inner class GestureRecyclerAdapter(val context: Context, val onClick: (Gesture) -> Unit) :
        RecyclerView.Adapter<GestureRecyclerAdapter.ViewHolder>() {
        private val gestures = Gesture.entries.filter { it.isEnabled() }.toList()
        private val colorTheme = LauncherPreferences.theme().colorTheme()
        private val monochromeIcons = colorTheme.monochromeIcons()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val label: TextView = itemView.findViewById(R.id.dialog_select_gesture_row_name)
            val description: TextView =
                itemView.findViewById(R.id.dialog_select_gesture_row_description)
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
            holder.icon.transformMonochrome(monochromeIcons, colorTheme)
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