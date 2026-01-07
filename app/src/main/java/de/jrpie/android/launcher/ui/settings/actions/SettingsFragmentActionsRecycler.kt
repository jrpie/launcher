package de.jrpie.android.launcher.ui.settings.actions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.databinding.SettingsActionsRecyclerBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.ui.list.SelectActionActivity
import de.jrpie.android.launcher.ui.transformGrayscale

/**
 *  The [SettingsFragmentActionsRecycler] is a fragment containing the [ActionsRecyclerAdapter],
 *  which displays all selected actions / apps.
 *
 *  It is used in the Tutorial and in Settings
 */
class SettingsFragmentActionsRecycler : Fragment(), UIObject {

    private var savedScrollPosition = 0

    private var sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            actionViewAdapter?.updateActions()
        }
    private lateinit var binding: SettingsActionsRecyclerBinding
    private var actionViewAdapter: ActionsRecyclerAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsActionsRecyclerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super<Fragment>.onStart()

        // set up the list / recycler
        val actionViewManager = LinearLayoutManager(context)
        actionViewAdapter = ActionsRecyclerAdapter(requireActivity())

        binding.settingsActionsRview.apply {
            // improve performance (since content changes don't change the layout size)
            setHasFixedSize(true)
            layoutManager = actionViewManager
            adapter = actionViewAdapter

        }
        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(sharedPreferencesListener)

        super<UIObject>.onStart()
    }

    override fun onDestroy() {
        LauncherPreferences.getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)

        super.onDestroy()
    }


    override fun onPause() {
        savedScrollPosition =
            (binding.settingsActionsRview.layoutManager as LinearLayoutManager)
                .findFirstCompletelyVisibleItemPosition()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        (binding.settingsActionsRview.layoutManager)?.scrollToPosition(savedScrollPosition)
    }
}

class ActionsRecyclerAdapter(val activity: Activity) :
    RecyclerView.Adapter<ActionsRecyclerAdapter.ViewHolder>() {

    private val gesturesList: ArrayList<Gesture> =
        Gesture.entries.filter(Gesture::isEnabled) as ArrayList<Gesture>

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textView: TextView = itemView.findViewById(R.id.settings_actions_row_name)
        var descriptionTextView: TextView =
            itemView.findViewById(R.id.settings_actions_row_description)
        var img: ImageView = itemView.findViewById(R.id.settings_actions_row_icon_img)
        var chooseButton: Button = itemView.findViewById(R.id.settings_actions_row_button_choose)
        var removeAction: ImageView = itemView.findViewById(R.id.settings_actions_row_remove)

        override fun onClick(v: View) {}

        init {
            itemView.setOnClickListener(this)
        }
    }

    private fun updateViewHolder(gesture: Gesture, viewHolder: ViewHolder) {
        val action = Action.forGesture(gesture)

        if (action == null) {
            viewHolder.img.visibility = View.INVISIBLE
            viewHolder.removeAction.visibility = View.GONE
            viewHolder.chooseButton.visibility = View.VISIBLE
            return
        }

        val (icon, label) = action.getIconAndContentDescription(activity)

        viewHolder.img.visibility = View.VISIBLE
        viewHolder.removeAction.visibility = View.VISIBLE
        viewHolder.chooseButton.visibility = View.INVISIBLE
        viewHolder.img.setImageDrawable(icon)
        viewHolder.img.contentDescription = label
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val gesture = gesturesList[i]
        viewHolder.textView.text = gesture.getLabel(activity)

        val description = gesture.getDescription(activity)
        viewHolder.descriptionTextView.text = description

        viewHolder.img.transformGrayscale(LauncherPreferences.theme().monochromeIcons())

        updateViewHolder(gesture, viewHolder)
        viewHolder.img.setOnClickListener { SelectActionActivity.selectAction(activity, gesture) }
        viewHolder.chooseButton.setOnClickListener { SelectActionActivity.selectAction(activity, gesture) }
        viewHolder.removeAction.setOnClickListener { Action.clearActionForGesture(gesture) }
    }

    override fun getItemCount(): Int {
        return gesturesList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.settings_actions_row, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateActions() {
        val doubleActions = LauncherPreferences.enabled_gestures().doubleSwipe()
        val edgeActions = LauncherPreferences.enabled_gestures().edgeSwipe()
        this.gesturesList.clear()
        gesturesList.addAll(Gesture.entries.filter {
            (doubleActions || !it.isDoubleVariant())
                    && (edgeActions || !it.isEdgeVariant())
        })

        notifyDataSetChanged()
    }


}
