package de.jrpie.android.launcher.ui.settings.actions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.apps.AppFilter
import de.jrpie.android.launcher.databinding.SettingsActionsRecyclerBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.ui.list.ListActivity
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

    private val drawableUnknown = AppCompatResources.getDrawable(activity, R.drawable.baseline_question_mark_24)

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

        var icon = action.getIcon(activity)
        var label = action.label(activity)

        // Use the unknown icon if there is an action, but we can't find its icon.
        // Probably an app was uninstalled.
        if (icon == null) {
            icon = drawableUnknown
            label = activity.getString(R.string.action_unknown)
        }

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
        viewHolder.img.setOnClickListener { chooseApp(gesture) }
        viewHolder.chooseButton.setOnClickListener { chooseApp(gesture) }
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

    private fun chooseApp(gesture: Gesture) {
        val intent = Intent(activity, ListActivity::class.java)
        intent.putExtra("intention", ListActivity.ListActivityIntention.PICK.toString())
        intent.putExtra("hiddenVisibility", AppFilter.Companion.AppSetVisibility.VISIBLE)
        intent.putExtra("forGesture", gesture.id) // for which action we choose the app
        activity.startActivity(intent)
    }
}
