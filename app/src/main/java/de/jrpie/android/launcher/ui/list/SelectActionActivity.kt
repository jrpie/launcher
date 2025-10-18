package de.jrpie.android.launcher.ui.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.databinding.ActivitySelectActionBinding
import de.jrpie.android.launcher.ui.list.apps.ListFragmentApps
import de.jrpie.android.launcher.ui.list.other.ListFragmentOther


/**
 * The [SelectActionActivity] is used to select an action (i.e. an app or one of [de.jrpie.android.launcher.actions.LauncherAction])
 */
class SelectActionActivity : AbstractListActivity() {
    override val intention = Companion.Intention.PICK
    private lateinit var binding: ActivitySelectActionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise layout
        binding = ActivitySelectActionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        useSoftInputResizeWorkaround(binding.selectContainer)
    }

    override fun setOnClicks() {
        binding.selectActionClose.setOnClickListener { finish() }
    }

    override fun adjustLayout() {
        val sectionsPagerAdapter = ListSectionsPagerAdapter(this)
        binding.selectActionViewpager.let {
            it.adapter = sectionsPagerAdapter
            binding.selectActionTabs.setupWithViewPager(it)
        }
    }
}

/**
 * The [ListSectionsPagerAdapter] returns the fragment,
 * which corresponds to the selected tab in [AppListActivity].
 *
 * This should eventually be replaced by a [FragmentStateAdapter]
 * However this keyboard does not open when using [ViewPager2]
 * so currently [ViewPager] is used here.
 * https://github.com/jrpie/launcher/issues/130
 */
@Suppress("deprecation")
class ListSectionsPagerAdapter(val activity: SelectActionActivity) :
    FragmentPagerAdapter(activity.supportFragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> ListFragmentApps()
            1 -> ListFragmentOther()
            else -> Fragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return activity.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return 2
    }

    companion object {
        private val TAB_TITLES = arrayOf(
            R.string.list_tab_app,
            R.string.list_tab_other
        )
    }
}
