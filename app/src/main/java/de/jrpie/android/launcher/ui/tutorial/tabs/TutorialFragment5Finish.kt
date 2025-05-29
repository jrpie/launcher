package de.jrpie.android.launcher.ui.tutorial.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.databinding.Tutorial5FinishBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.requestNotificationPermission
import de.jrpie.android.launcher.setDefaultHomeScreen
import de.jrpie.android.launcher.ui.UIObject

/**
 * The [TutorialFragment5Finish] is a used as a tab in the TutorialActivity.
 *
 * It is used to display further resources and let the user start Launcher
 */
class TutorialFragment5Finish : Fragment(), UIObject {

    private lateinit var binding: Tutorial5FinishBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = Tutorial5FinishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super<Fragment>.onStart()
        super<UIObject>.onStart()
        requestNotificationPermission(requireActivity())
    }


    override fun setOnClicks() {
        super.setOnClicks()
        binding.tutorialFinishButtonStart.setOnClickListener { finishTutorial() }
    }

    private fun finishTutorial() {
        if (!LauncherPreferences.internal().started()) {
            LauncherPreferences.internal().started(true)
            LauncherPreferences.internal().startedTime(System.currentTimeMillis() / 1000L)
        }
        context?.let { setDefaultHomeScreen(it, checkDefault = true) }

        activity?.finish()
    }
}
