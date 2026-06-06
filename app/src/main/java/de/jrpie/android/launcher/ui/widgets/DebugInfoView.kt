package de.jrpie.android.launcher.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import de.jrpie.android.launcher.databinding.WidgetDebugInfoBinding
import de.jrpie.android.launcher.getDeviceInfo
import de.jrpie.android.launcher.preferences.LauncherPreferences

class DebugInfoView(context: Context, attrs: AttributeSet? = null, val appWidgetId: Int) :
    ConstraintLayout(context, attrs) {

    val binding: WidgetDebugInfoBinding =
        WidgetDebugInfoBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.debugInfoText.text = getDeviceInfo()
        LauncherPreferences.clock().font().getTypeface(context)?.let {
            binding.debugInfoText.setTypeface(it)
        }
    }
}