package de.jrpie.android.launcher.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.REQUEST_CHOOSE_APP
import de.jrpie.android.launcher.databinding.SettingsBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.preferences.theme.Background
import de.jrpie.android.launcher.preferences.theme.ColorTheme
import de.jrpie.android.launcher.saveListActivityChoice
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.ui.settings.actions.SettingsFragmentActions
import de.jrpie.android.launcher.ui.settings.launcher.SettingsFragmentLauncher
import de.jrpie.android.launcher.ui.settings.meta.SettingsFragmentMeta

/**
 * The [SettingsActivity] is a tabbed activity:
 *
 * | Actions    |   Choose apps or intents to be launched   | [SettingsFragmentActions] |
 * | Theme      |   Select a theme / Customize              | [SettingsFragmentLauncher]   |
 * | Meta       |   About Launcher / Contact etc.           | [SettingsFragmentMeta]    |
 *
 * Settings are closed automatically if the activity goes `onPause` unexpectedly.
 */
class SettingsActivity : AppCompatActivity(), UIObject {

    private val solidBackground = LauncherPreferences.theme().background() == Background.SOLID
            || LauncherPreferences.theme().colorTheme() == ColorTheme.LIGHT

    private val sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            if (solidBackground &&
                (prefKey == LauncherPreferences.theme().keys().background() ||
                        prefKey == LauncherPreferences.theme().keys().colorTheme())
            ) {
                // Switching from solid background to a transparent background using `recreate()`
                // causes a very ugly glitch, making the settings unreadable.
                // This ugly workaround causes a jump to the top of the list, but at least
                // the text stays readable.
                val i = Intent(this, SettingsActivity::class.java)
                    .also { it.putExtra("tab", 1) }
                finish()
                startActivity(i)
            } else
            if (prefKey?.startsWith("theme.") == true ||
                prefKey?.startsWith("display.") == true
            ) {
                recreate()
            }
        }
    private lateinit var binding: SettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()


        // Initialise layout
        binding = SettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set up tabs and swiping in settings
        val sectionsPagerAdapter = SettingsSectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.settings_viewpager)
        viewPager.adapter = sectionsPagerAdapter

        val tabs: TabLayout = findViewById(R.id.settings_tabs)
        tabs.setupWithViewPager(viewPager)
        if (intent.hasExtra("tab")) {
            tabs.getTabAt(intent.getIntExtra("tab", 0))?.select()
        }
    }

    override fun onStart() {
        super<AppCompatActivity>.onStart()
        super<UIObject>.onStart()
        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    override fun onPause() {
        LauncherPreferences.getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
        super.onPause()
    }

    override fun getTheme(): Resources.Theme {
        return modifyTheme(super.getTheme())
    }

    override fun setOnClicks() {
        // As older APIs somehow do not recognize the xml defined onClick
        binding.settingsClose.setOnClickListener { finish() }
        // open device settings (see https://stackoverflow.com/a/62092663/12787264)
        binding.settingsSystem.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CHOOSE_APP -> saveListActivityChoice(data)
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }
}

private val TAB_TITLES = arrayOf(
    R.string.settings_tab_app,
    R.string.settings_tab_launcher,
    R.string.settings_tab_meta
)

class SettingsSectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> SettingsFragmentActions()
            1 -> SettingsFragmentLauncher()
            2 -> SettingsFragmentMeta()
            else -> Fragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return 3
    }
}
