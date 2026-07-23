package de.jrpie.android.launcher.ui.minimalist

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.apps.AbstractDetailedAppInfo
import de.jrpie.android.launcher.preferences.LauncherPreferences

/**
 * A minimal [RecyclerView.Adapter] showing a plain text list of the apps chosen for
 * minimalist mode ([LauncherPreferences.minimalist] `apps`). Tapping a row launches the
 * app directly, bypassing the gesture system entirely.
 */
@SuppressLint("NotifyDataSetChanged")
class MinimalistHomeAdapter(private val activity: Activity) :
    RecyclerView.Adapter<MinimalistHomeAdapter.ViewHolder>() {

    private val apps = (activity.applicationContext as Application).apps
    private val appsListDisplayed: MutableList<AbstractDetailedAppInfo> = mutableListOf()

    // HomeActivity is a plain Activity (not a LifecycleOwner), so this can't use the usual
    // apps.observe(lifecycleOwner, ...). Installed apps load asynchronously (Application.loadApps),
    // so without this, a cold start can leave the list empty forever if apps.value was still
    // null when the adapter was first built. destroy() must be called from onDestroy().
    private val appsObserver = Observer<List<AbstractDetailedAppInfo>> { updateAppsList() }

    init {
        updateAppsList()
        apps.observeForever(appsObserver)
    }

    fun destroy() {
        apps.removeObserver(appsObserver)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textView: TextView = itemView.findViewById(R.id.list_apps_row_name)

        override fun onClick(v: View) {
            val rect = Rect()
            v.getGlobalVisibleRect(rect)
            appsListDisplayed.getOrNull(bindingAdapterPosition)?.getAction()?.invoke(activity, rect)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_apps_row_variant_text, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = appsListDisplayed[position].getCustomLabel(activity)
    }

    override fun getItemCount(): Int = appsListDisplayed.size

    fun updateAppsList() {
        val selection = LauncherPreferences.minimalist().apps() ?: emptySet()
        appsListDisplayed.clear()
        apps.value?.let { all ->
            appsListDisplayed.addAll(
                all.filter { selection.contains(it.getRawInfo()) }
                    .sortedBy { it.getCustomLabel(activity).lowercase() }
            )
        }
        notifyDataSetChanged()
    }
}
