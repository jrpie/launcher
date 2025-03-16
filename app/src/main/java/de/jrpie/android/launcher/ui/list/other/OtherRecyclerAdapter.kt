package de.jrpie.android.launcher.ui.list.other

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.actions.LauncherAction
import de.jrpie.android.launcher.ui.list.ListActivity

/**
 * The [OtherRecyclerAdapter] will only be displayed in the ListActivity,
 * if an app / intent / etc. is picked to be launched when an action is recognized.
 *
 * It lists `other` things to be launched that are not really represented by a URI,
 * rather by Launcher- internal conventions.
 */
class OtherRecyclerAdapter(val activity: Activity) :
    RecyclerView.Adapter<OtherRecyclerAdapter.ViewHolder>() {

    private val othersList: Array<LauncherAction> =
        LauncherAction.entries.filter { it.isAvailable(activity) }.toTypedArray()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textView: TextView = itemView.findViewById(R.id.list_other_row_name)
        var iconView: ImageView = itemView.findViewById(R.id.list_other_row_icon)


        override fun onClick(v: View) {
            val pos = bindingAdapterPosition
            val content = othersList[pos]

            activity.finish()
            val gestureId = (activity as? ListActivity)?.forGesture ?: return
            val gesture = Gesture.byId(gestureId) ?: return
            Action.setActionForGesture(gesture, content)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val otherLabel = activity.getString(othersList[i].label)
        val icon = othersList[i].icon

        viewHolder.textView.text = otherLabel
        viewHolder.iconView.setImageResource(icon)
    }

    override fun getItemCount(): Int {
        return othersList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.list_other_row, parent, false)
        return ViewHolder(view)
    }
}