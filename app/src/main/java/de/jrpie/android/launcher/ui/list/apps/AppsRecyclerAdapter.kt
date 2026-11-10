package de.jrpie.android.launcher.ui.list.apps

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.apps.AbstractDetailedAppInfo
import de.jrpie.android.launcher.apps.AppFilter
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.databinding.AppContextMenuBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.preferences.list.AppNameFormat
import de.jrpie.android.launcher.preferences.list.ListLayout
import de.jrpie.android.launcher.ui.list.AbstractListActivity
import de.jrpie.android.launcher.ui.transformMonochrome

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
    val activity: AppCompatActivity,
    val root: View,
    private val intention: AbstractListActivity.Companion.Intention = AbstractListActivity.Companion.Intention.VIEW,
    private val forGesture: String? = "",
    private var appFilter: AppFilter = AppFilter(activity, ""),
    private val layout: ListLayout,
    private val nameFormat: AppNameFormat
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val apps = (activity.applicationContext as Application).apps
    private val appsListDisplayed: MutableList<AbstractDetailedAppInfo> = mutableListOf()
    private val theme = LauncherPreferences.theme()
    private val colorTheme = theme.colorTheme()
    private val grayscale = colorTheme.monochromeIcons()

    // temporarily disable auto launch
    var disableAutoLaunch: Boolean = false

    private val animate = theme.animations()
    private var recyclerView: RecyclerView? = null

    private val collapseOnBack = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            collapseMenu()
        }
    }

    private var openMenu: OpenMenu? = null
        set(value) {
            field = value
            collapseOnBack.isEnabled = value != null
            recyclerView?.stopScroll()
        }
    private val menuPosition: Int
        get() = openMenu?.position ?: -1

    private val collapseOnStop = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            collapseMenu(keepAppInPlace = false)
        }
    }

    init {
        apps.observe(this.activity as AppCompatActivity) {
            updateAppsList()
        }
        updateAppsList()
    }

    private class MenuViewHolder(val binding: AppContextMenuBinding) : RecyclerView.ViewHolder(binding.root)

    private class OpenMenu(val app: AbstractDetailedAppInfo, val position: Int)


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textView: TextView = itemView.findViewById(R.id.list_apps_row_name)
        var img: ImageView = itemView.findViewById(R.id.list_apps_row_icon)

        override fun onClick(v: View) {
            val rect = Rect()
            img.getGlobalVisibleRect(rect)
            selectItem(appIndex(bindingAdapterPosition), rect)
        }

        init {
            itemView.setOnClickListener(this)

            // decide when to show the options menu
            if (intention == AbstractListActivity.Companion.Intention.VIEW) {
                textView.setOnLongClickListener { showOptionsMenu(this) }
                img.setOnLongClickListener { showOptionsMenu(this) }
                // ensure onClicks are actually caught
                textView.setOnClickListener { onClick(textView) }
                img.setOnClickListener { onClick(img) }
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MenuViewHolder -> bindMenu(holder)
            is ViewHolder -> bindApp(holder, appIndex(position))
        }
    }

    private fun bindApp(viewHolder: ViewHolder, i: Int) {
        var appLabel = appsListDisplayed[i].getCustomLabel(activity)

        val appIcon = appsListDisplayed[i].getIcon(activity)

        viewHolder.img.transformMonochrome(grayscale, colorTheme)
        viewHolder.img.setImageDrawable(appIcon?.constantState?.newDrawable() ?: appIcon)

        if (layout.useBadgedText) {
            appLabel = activity.packageManager.getUserBadgedLabel(
                appLabel,
                appsListDisplayed[i].getUser(activity)
            ).toString()
        }
        viewHolder.textView.text = nameFormat.format(appLabel)
    }

    @Suppress("SameReturnValue")
    private fun showOptionsMenu(viewHolder: ViewHolder): Boolean {
        val index = appIndex(viewHolder.bindingAdapterPosition)
        if (index in appsListDisplayed.indices) {
            expandMenu(index)
        }
        return true
    }

    private fun appIndex(position: Int): Int =
        if (menuPosition in 0 until position) position - 1 else position

    private fun appPosition(index: Int): Int =
        if (menuPosition in 0..index) index + 1 else index

    private fun offsetFromStart(view: View, layoutManager: LinearLayoutManager): Int {
        val helper = OrientationHelper.createVerticalHelper(layoutManager)
        return if (layoutManager.reverseLayout) {
            helper.endAfterPadding - helper.getDecoratedEnd(view)
        } else {
            helper.getDecoratedStart(view) - helper.startAfterPadding
        }
    }

    private fun expandMenu(index: Int) {
        val rv = recyclerView ?: return
        val layoutManager = rv.layoutManager as? LinearLayoutManager ?: return
        val offset = layoutManager.findViewByPosition(appPosition(index))
            ?.let { offsetFromStart(it, layoutManager) }

        openMenu?.let {
            openMenu = null
            notifyItemRemoved(it.position)
        }

        val span = (layoutManager as? GridLayoutManager)?.spanCount ?: 1
        val rowStart = index / span * span
        val rowEnd = minOf(rowStart + span, appsListDisplayed.size)
        val menu = OpenMenu(
            appsListDisplayed[index],
            if (layoutManager.reverseLayout) rowStart else rowEnd
        )
        openMenu = menu
        notifyItemInserted(menu.position)

        if (offset != null) {
            layoutManager.scrollToPositionWithOffset(appPosition(index), offset)
        }
        revealMenu(index, menu, layoutManager)
    }

    private fun revealMenu(index: Int, menu: OpenMenu, layoutManager: LinearLayoutManager) {
        layoutManager.startSmoothScroll(object : LinearSmoothScroller(activity) {
            override fun getVerticalSnapPreference() = SNAP_TO_ANY
            override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
                val dy = super.calculateDyToMakeVisible(view, snapPreference)
                val appView = layoutManager.findViewByPosition(appPosition(index)) ?: return dy
                val helper = OrientationHelper.createVerticalHelper(layoutManager)
                return maxOf(dy, helper.startAfterPadding - helper.getDecoratedStart(appView))
            }
            override fun calculateTimeForScrolling(dx: Int) =
                if (animate) super.calculateTimeForScrolling(dx) else INSTANT_SCROLL_MS
        }.apply { targetPosition = menu.position })
    }

    fun collapseMenu(keepAppInPlace: Boolean = true) {
        val menu = openMenu ?: return
        val index = appsListDisplayed.indexOf(menu.app)
        val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager
        val offset = if (keepAppInPlace && index >= 0 && layoutManager != null) {
            layoutManager.findViewByPosition(appPosition(index))
                ?.let { offsetFromStart(it, layoutManager) }
        } else {
            null
        }

        openMenu = null
        notifyItemRemoved(menu.position)

        if (offset != null) {
            layoutManager?.scrollToPositionWithOffset(index, offset)
        }
    }

    private fun bindMenu(holder: MenuViewHolder) {
        val menu = openMenu ?: return
        holder.binding.bindApp(menu.app, activity) { collapseMenu() }
    }

    private fun isMenu(position: Int): Boolean = position == menuPosition

    override fun getItemViewType(position: Int): Int =
        if (isMenu(position)) VIEW_TYPE_MENU else VIEW_TYPE_APP

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        (recyclerView.layoutManager as? GridLayoutManager)?.let { grid ->
            grid.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int =
                    if (isMenu(position)) grid.spanCount else 1
            }
        }
        activity.onBackPressedDispatcher.addCallback(collapseOnBack)
        activity.lifecycle.addObserver(collapseOnStop)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        collapseOnBack.remove()
        activity.lifecycle.removeObserver(collapseOnStop)
        this.recyclerView = null
    }

    override fun getItemCount(): Int {
        return appsListDisplayed.size + if (openMenu != null) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == VIEW_TYPE_MENU) {
            val binding = AppContextMenuBinding.inflate(inflater, parent, false)
            binding.root.setOnClickListener { collapseMenu() }
            return MenuViewHolder(binding)
        }
        return ViewHolder(inflater.inflate(layout.layoutResource, parent, false))
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
        openMenu = null
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

    companion object {
        private const val VIEW_TYPE_APP = 0
        private const val VIEW_TYPE_MENU = 1
        private const val INSTANT_SCROLL_MS = 1
    }
}
