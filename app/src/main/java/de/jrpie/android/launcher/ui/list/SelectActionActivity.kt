package de.jrpie.android.launcher.ui.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.apps.AppFilter
import de.jrpie.android.launcher.databinding.ActivitySelectActionBinding
import de.jrpie.android.launcher.ui.list.apps.ListFragmentApps
import de.jrpie.android.launcher.ui.list.other.ListFragmentOther


/**
 * The [SelectActionActivity] is used to select an action (i.e. an app or one of [de.jrpie.android.launcher.actions.LauncherAction])
 */
class SelectActionActivity : AbstractListActivity() {
    override val intention = AbstractListActivity.Companion.Intention.PICK
    private lateinit var binding: ActivitySelectActionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise layout
        binding = ActivitySelectActionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        useSoftInputResizeWorkaround(binding.selectContainer)

        val sectionsPagerAdapter = ListSectionsPagerAdapter(this)
        binding.selectActionViewpager.apply {
            adapter = sectionsPagerAdapter
        }
        TabLayoutMediator(binding.selectActionTabs, binding.selectActionViewpager) { tab, position ->
            tab.text = sectionsPagerAdapter.getPageTitle(position)
        }.attach()
    }

    override fun setOnClicks() {
        binding.selectActionClose.setOnClickListener { finish() }
    }

    companion object {
        fun selectAction(context: Context, gesture: Gesture) {
            val intent = Intent(context, SelectActionActivity::class.java)
            intent.putExtra(KEY_HIDDEN_VISIBILITY, AppFilter.Companion.AppSetVisibility.VISIBLE)
            intent.putExtra(KEY_FOR_GESTURE, gesture.id) // for which action we choose the app
            context.startActivity(intent)
        }
    }
}

class ListSectionsPagerAdapter(val activity: SelectActionActivity) :
    FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ListFragmentApps()
            1 -> ListFragmentOther()
            else -> Fragment()
        }
    }

    fun getPageTitle(position: Int): CharSequence {
        return activity.resources.getString(TAB_TITLES[position])
    }

    override fun getItemCount(): Int {
        return 2
    }

    companion object {
        private val TAB_TITLES = arrayOf(
            R.string.list_tab_app,
            R.string.list_tab_other
        )
    }
}
