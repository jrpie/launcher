package de.jrpie.android.launcher.ui.tutorial

import android.os.Build
import android.os.Bundle
import android.view.View
import android.window.OnBackInvokedDispatcher
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import de.jrpie.android.launcher.databinding.TutorialBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.blink
import de.jrpie.android.launcher.ui.tutorial.tabs.TutorialFragment0Start
import de.jrpie.android.launcher.ui.tutorial.tabs.TutorialFragment1Concept
import de.jrpie.android.launcher.ui.tutorial.tabs.TutorialFragment2Usage
import de.jrpie.android.launcher.ui.tutorial.tabs.TutorialFragment3AppList
import de.jrpie.android.launcher.ui.tutorial.tabs.TutorialFragment4Setup
import de.jrpie.android.launcher.ui.tutorial.tabs.TutorialFragment5Finish
import de.jrpie.android.launcher.ui.UIObjectActivity

/**
 * The [TutorialActivity] is displayed automatically on new installations.
 * It can also be opened from Settings.
 *
 * It tells the user about the concept behind launcher
 * and helps with the setup process (on new installations)
 */
class TutorialActivity : UIObjectActivity() {

    private lateinit var binding: TutorialBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise layout
        binding = TutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle back key / gesture on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_OVERLAY
            ) {
                // prevent going back when the tutorial is shown for the first time
                if (!LauncherPreferences.internal().started()) {
                    return@registerOnBackInvokedCallback
                }
                finish()
            }
        }


        // set up tabs and swiping in settings
        val sectionsPagerAdapter = TutorialSectionsPagerAdapter(this)
        binding.tutorialViewpager.apply {
            adapter = sectionsPagerAdapter
            currentItem = 0
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    binding.tutorialButtonNext.apply {
                        val lastItem = sectionsPagerAdapter.itemCount - 1
                        visibility = if (position == lastItem) {
                            View.INVISIBLE
                        } else {
                            View.VISIBLE
                        }
                        if (position == 0) {
                            blink()
                        } else {
                            clearAnimation()
                        }
                    }
                    binding.tutorialButtonBack.apply {
                        visibility = if (position == 0) {
                            View.INVISIBLE
                        } else {
                            View.VISIBLE
                        }
                    }
                }
            })
        }
        TabLayoutMediator(binding.tutorialTabs, binding.tutorialViewpager) { _, _ -> }.attach()
        binding.tutorialButtonNext.setOnClickListener {
            binding.tutorialViewpager.apply {
                setCurrentItem(
                    (currentItem + 1).coerceAtMost(sectionsPagerAdapter.itemCount - 1),
                    true
                )
            }
        }
        binding.tutorialButtonBack.setOnClickListener {
            binding.tutorialViewpager.apply {
                setCurrentItem((currentItem - 1).coerceAtLeast(0), true)
            }
        }
    }

    // prevent going back when the tutorial is shown for the first time
    @Deprecated("Deprecated in Java", ReplaceWith("use anyway"))
    @Suppress("deprecation") // support API level < 33
    override fun onBackPressed() {
        if (LauncherPreferences.internal().started())
            super.onBackPressed()
    }

}

/**
 * The [TutorialSectionsPagerAdapter] defines which fragments are shown when,
 * in the [TutorialActivity].
 *
 * Tabs: (Start | Concept | Usage | Setup | Finish)
 */
class TutorialSectionsPagerAdapter(activity: FragmentActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 6
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TutorialFragment0Start()
            1 -> TutorialFragment1Concept()
            2 -> TutorialFragment2Usage()
            3 -> TutorialFragment3AppList()
            4 -> TutorialFragment4Setup()
            5 -> TutorialFragment5Finish()
            else -> Fragment()
        }
    }
}
