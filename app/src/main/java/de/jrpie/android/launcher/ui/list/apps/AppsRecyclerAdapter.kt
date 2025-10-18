package de.jrpie.android.launcher.ui.list.apps

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.apps.AbstractDetailedAppInfo
import de.jrpie.android.launcher.apps.AppFilter
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.apps.DetailedAppInfo
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.preferences.list.AppNameFormat
import de.jrpie.android.launcher.preferences.list.ListLayout
import de.jrpie.android.launcher.ui.list.AbstractListActivity
import de.jrpie.android.launcher.ui.transformGrayscale

/**
 * A [RecyclerView] (efficient scrollable list) containing all apps on the users device.
 * The apps details are represented by [AppInfo].
 *
 * @param activity - the activity this is in
 * @param intention - why the list is displayed ("view", "pick")
 * @param forGesture - the action which an app is chosen for (when the intention is "pick")
 */
@SuppressLint("NotifyDataSetChanged")
class AppsRecyclerAdapter(
    val activity: Activity,
    val root: View,
    private val intention: AbstractListActivity.Companion.Intention = AbstractListActivity.Companion.Intention.VIEW,
    private val forGesture: String? = "",
    private var appFilter: AppFilter = AppFilter(activity, ""),
    private val layout: ListLayout,
    private val nameFormat: AppNameFormat
) :
    RecyclerView.Adapter<AppsRecyclerAdapter.ViewHolder>() {



    private val apps = (activity.applicationContext as Application).apps
    private val appsListDisplayed: MutableList<AbstractDetailedAppInfo> = mutableListOf()
    private val grayscale = LauncherPreferences.theme().monochromeIcons()

    // temporarily disable auto launch
    var disableAutoLaunch: Boolean = false

    init {
        apps.observe(this.activity as AppCompatActivity) {
            updateAppsList()
        }
        updateAppsList()
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textView: TextView = itemView.findViewById(R.id.list_apps_row_name)
        var img: ImageView = itemView.findViewById(R.id.list_apps_row_icon)

        override fun onClick(v: View) {
            val rect = Rect()
            img.getGlobalVisibleRect(rect)
            selectItem(bindingAdapterPosition, rect)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        var appLabel = appsListDisplayed[i].getCustomLabel(activity)

        val appIcon = appsListDisplayed[i].getIcon(activity)

        viewHolder.img.transformGrayscale(grayscale)
        viewHolder.img.setImageDrawable(appIcon.constantState?.newDrawable() ?: appIcon)

        if (layout.useBadgedText) {
            appLabel = activity.packageManager.getUserBadgedLabel(
                appLabel,
                appsListDisplayed[i].getUser(activity)
            ).toString()
        }
        viewHolder.textView.text = nameFormat.format(appLabel)


        // decide when to show the options popup menu about
        if (intention == AbstractListActivity.Companion.Intention.VIEW) {
            viewHolder.textView.setOnLongClickListener {
                showOptionsPopup(
                    viewHolder,
                    appsListDisplayed[i]
                )
            }
            viewHolder.img.setOnLongClickListener {
                showOptionsPopup(
                    viewHolder,
                    appsListDisplayed[i]
                )
            }
            // ensure onClicks are actually caught
            viewHolder.textView.setOnClickListener { viewHolder.onClick(viewHolder.textView) }
            viewHolder.img.setOnClickListener { viewHolder.onClick(viewHolder.img) }
        }
    }

    @Suppress("SameReturnValue")
    private fun showOptionsPopup(
        viewHolder: ViewHolder,
        appInfo: AbstractDetailedAppInfo
    ): Boolean {
        //create the popup menu

        val popup = PopupMenu(activity, viewHolder.img)
        popup.inflate(R.menu.menu_app)

        if (!appInfo.isRemovable()) {
            popup.menu.findItem(R.id.app_menu_delete).isVisible = false
        }

        if (appInfo !is DetailedAppInfo) {
            popup.menu.findItem(R.id.app_menu_info).isVisible = false
        }

        if (LauncherPreferences.apps().hidden()?.contains(appInfo.getRawInfo()) == true) {
            popup.menu.findItem(R.id.app_menu_hidden).setTitle(R.string.list_app_hidden_remove)
        }

        if (LauncherPreferences.apps().favorites()?.contains(appInfo.getRawInfo()) == true) {
            popup.menu.findItem(R.id.app_menu_favorite).setTitle(R.string.list_app_favorite_remove)
        }


        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.app_menu_delete -> {
                    appInfo.getRawInfo().uninstall(activity); true
                }

                R.id.app_menu_info -> {
                    (appInfo.getRawInfo() as? AppInfo)?.openSettings(activity); true
                }

                R.id.app_menu_favorite -> {
                    appInfo.getRawInfo().toggleFavorite(); true
                }

                R.id.app_menu_hidden -> {
                    appInfo.getRawInfo().toggleHidden(root); true
                }

                R.id.app_menu_rename -> {
                    appInfo.showRenameDialog(activity); true
                }

                else -> false
            }
        }

        popup.show()
        return true
    }

    override fun getItemCount(): Int {
        return appsListDisplayed.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(layout.layoutResource, parent, false)
        val viewHolder = ViewHolder(view)
        return viewHolder
    }

    fun selectItem(pos: Int, rect: Rect = Rect()) {
        val appInfo = appsListDisplayed.getOrNull(pos) ?: return
        when (intention) {
            AbstractListActivity.Companion.Intention.VIEW -> {
                appInfo.getAction().invoke(activity, rect)
            }

            AbstractListActivity.Companion.Intention.PICK -> {
                activity.finish()
                forGesture ?: return
                val gesture = Gesture.byId(forGesture) ?: return
                Action.setActionForGesture(gesture, appInfo.getAction())
            }
        }
    }

    fun updateAppsList(triggerAutoLaunch: Boolean = false) {
        appsListDisplayed.clear()
        apps.value?.let { appsListDisplayed.addAll(appFilter(it)) }

        if (triggerAutoLaunch &&
            appsListDisplayed.size == 1
            && intention == AbstractListActivity.Companion.Intention.VIEW
            && !disableAutoLaunch
            && LauncherPreferences.functionality().searchAutoLaunch()
        ) {
            val app = appsListDisplayed[0]
            app.getAction().invoke(activity)

            val inputMethodManager =
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(View(activity).windowToken, 0)
        }

        notifyDataSetChanged()
    }

    /**
     * The function [setSearchString] is used to search elements within this [RecyclerView].
     */
    fun setSearchString(search: String) {
        appFilter.query = search
        updateAppsList(true)

    }

    fun setFavoritesVisibility(v: AppFilter.Companion.AppSetVisibility) {
        appFilter.favoritesVisibility = v
        updateAppsList()
    }
}
