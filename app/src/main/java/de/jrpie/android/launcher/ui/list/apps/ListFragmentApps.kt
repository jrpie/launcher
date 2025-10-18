package de.jrpie.android.launcher.ui.list.apps

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.apps.AppFilter
import de.jrpie.android.launcher.databinding.ListAppsBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.ui.closeSoftKeyboard
import de.jrpie.android.launcher.ui.list.AbstractListActivity
import de.jrpie.android.launcher.ui.list.AppListActivity
import de.jrpie.android.launcher.ui.openSoftKeyboard
import kotlin.math.absoluteValue


/**
 * The [ListFragmentApps] is used as a tab in ListActivity.
 *
 * It is a list of all installed applications that can be launched.
 */
class ListFragmentApps : Fragment(), UIObject {
    private lateinit var binding: ListAppsBinding
    private lateinit var appsRecyclerAdapter: AppsRecyclerAdapter


    private var sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            appsRecyclerAdapter.updateAppsList()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ListAppsBinding.inflate(inflater)
        return binding.root
    }

    override fun onStart() {
        super<Fragment>.onStart()
        super<UIObject>.onStart()
        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(sharedPreferencesListener)

        binding.listAppsCheckBoxFavorites.isChecked =
            ((activity as? AppListActivity)?.favoritesVisibility == AppFilter.Companion.AppSetVisibility.EXCLUSIVE)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        binding.listAppsRview.layoutManager?.let {
            LauncherPreferences.list().layout().updateLayoutManager(requireContext(), it)
        }

    }

    override fun onStop() {
        super.onStop()
        LauncherPreferences.getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }


    override fun setOnClicks() {}

    override fun adjustLayout() {
        val listActivity = (activity as? AbstractListActivity) ?: return

        appsRecyclerAdapter =
            AppsRecyclerAdapter(
                listActivity, binding.root, listActivity.intention, listActivity.forGesture,
                appFilter = AppFilter(
                    requireContext(),
                    "",
                    favoritesVisibility = listActivity.favoritesVisibility,
                    privateSpaceVisibility = listActivity.privateSpaceVisibility,
                    hiddenVisibility = listActivity.hiddenVisibility
                ),
                layout = LauncherPreferences.list().layout(),
                nameFormat = LauncherPreferences.list().appNameFormat()
            )


        // set up the list / recycler
        binding.listAppsRview.apply {
            // improve performance (since content changes don't change the layout size)
            setHasFixedSize(true)
            layoutManager = LauncherPreferences.list().layout().layoutManager(context)
                .also {
                    if (LauncherPreferences.list().reverseLayout()) {
                        (it as? LinearLayoutManager)?.reverseLayout = true
                        (it as? GridLayoutManager)?.reverseLayout = true
                    }
                }
            adapter = appsRecyclerAdapter
            if (LauncherPreferences.functionality().searchAutoCloseKeyboard()) {
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    var totalDy: Int = 0
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        totalDy += dy

                        if (totalDy.absoluteValue > 100) {
                            totalDy = 0
                            closeSoftKeyboard(requireActivity())
                        }
                    }
                })
            }
        }

        binding.listAppsSearchview.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                appsRecyclerAdapter.setSearchString(query)

                if (LauncherPreferences.functionality().searchWeb()) {
                    val i = Intent(Intent.ACTION_WEB_SEARCH).putExtra("query", query)
                    try {
                        activity?.startActivity(i)
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            requireContext(),
                            R.string.toast_activity_not_found_search_web,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {
                    appsRecyclerAdapter.selectItem(0)
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {

                if (newText == " " &&
                    !appsRecyclerAdapter.disableAutoLaunch &&
                    (activity as? AbstractListActivity)?.intention
                        == AbstractListActivity.Companion.Intention.VIEW &&
                    LauncherPreferences.functionality().searchAutoLaunch()
                ) {
                    appsRecyclerAdapter.disableAutoLaunch = true
                    binding.listAppsSearchview.apply {
                        queryHint = context.getString(R.string.list_apps_search_hint_no_auto_launch)
                        setQuery("", false)
                    }
                    return false
                }

                appsRecyclerAdapter.setSearchString(newText)
                return false
            }
        })

        binding.listAppsCheckBoxFavorites.setOnClickListener {
            listActivity.favoritesVisibility =
                if (binding.listAppsCheckBoxFavorites.isChecked) {
                    AppFilter.Companion.AppSetVisibility.EXCLUSIVE
                } else {
                    AppFilter.Companion.AppSetVisibility.VISIBLE
                }
            appsRecyclerAdapter.setFavoritesVisibility(listActivity.favoritesVisibility)
            (activity as? AppListActivity)?.updateTitle()
        }

        if (listActivity.intention == AbstractListActivity.Companion.Intention.VIEW
            && LauncherPreferences.functionality().searchAutoOpenKeyboard()
        ) {
            binding.listAppsSearchview.openSoftKeyboard(requireContext())
        }
    }

}
