package de.jrpie.android.launcher.ui.list.apps

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.apps.AppFilter
import de.jrpie.android.launcher.databinding.ListAppsBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.ui.list.ListActivity
import de.jrpie.android.launcher.ui.list.favoritesVisibility
import de.jrpie.android.launcher.ui.list.forGesture
import de.jrpie.android.launcher.ui.list.hiddenVisibility
import de.jrpie.android.launcher.ui.list.intention
import de.jrpie.android.launcher.ui.openSoftKeyboard


/**
 * The [ListFragmentApps] is used as a tab in ListActivity.
 *
 * It is a list of all installed applications that are can be launched.
 */
class ListFragmentApps : Fragment(), UIObject {
    private lateinit var binding: ListAppsBinding
    private lateinit var appsRViewAdapter: AppsRecyclerAdapter

    private var sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            appsRViewAdapter.updateAppsList()
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
            (favoritesVisibility == AppFilter.Companion.AppSetVisibility.EXCLUSIVE)
    }

    override fun onStop() {
        super.onStop()
        LauncherPreferences.getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }


    override fun setOnClicks() {}

    override fun adjustLayout() {

        appsRViewAdapter =
            AppsRecyclerAdapter(
                requireActivity(), binding.root, intention, forGesture,
                appFilter = AppFilter(
                    requireContext(),
                    "",
                    favoritesVisibility = favoritesVisibility,
                    hiddenVisibility = hiddenVisibility
                ),
                layout = LauncherPreferences.list().layout()
            )

        // set up the list / recycler
        binding.listAppsRview.apply {
            // improve performance (since content changes don't change the layout size)
            setHasFixedSize(true)
            layoutManager = LauncherPreferences.list().layout().layoutManager(context)
            adapter = appsRViewAdapter
        }

        binding.listAppsSearchview.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                appsRViewAdapter.setSearchString(query)
                //appsRViewAdapter.selectItem(0)
                val i = Intent(Intent.ACTION_WEB_SEARCH).putExtra("query", query)
                activity?.startActivity(i)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                appsRViewAdapter.setSearchString(newText)
                return false
            }
        })

        binding.listAppsCheckBoxFavorites.setOnClickListener {
            favoritesVisibility =
                if (binding.listAppsCheckBoxFavorites.isChecked) {
                    AppFilter.Companion.AppSetVisibility.EXCLUSIVE
                } else {
                    AppFilter.Companion.AppSetVisibility.VISIBLE
                }
            appsRViewAdapter.setFavoritesVisibility(favoritesVisibility)
            (activity as? ListActivity)?.updateTitle()
        }

        if (intention == ListActivity.ListActivityIntention.VIEW
            && LauncherPreferences.functionality().searchAutoOpenKeyboard()
        ) {
            binding.listAppsSearchview.openSoftKeyboard(requireContext())
        }
    }
}
