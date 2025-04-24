package de.jrpie.android.launcher.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.databinding.ClockBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import java.util.Locale

class ClockView(context: Context, attrs: AttributeSet? = null, val appWidgetId: Int): ConstraintLayout(context, attrs) {

    val binding: ClockBinding = ClockBinding.inflate(LayoutInflater.from(context), this, true)
    init {
        initClock()
        setOnClicks()
    }


    private fun initClock() {
        val locale = Locale.getDefault()
        val dateVisible = LauncherPreferences.clock().dateVisible()
        val timeVisible = LauncherPreferences.clock().timeVisible()

        var dateFMT = "yyyy-MM-dd"
        var timeFMT = "HH:mm"
        if (LauncherPreferences.clock().showSeconds()) {
            timeFMT += ":ss"
        }

        if (LauncherPreferences.clock().localized()) {
            dateFMT = android.text.format.DateFormat.getBestDateTimePattern(locale, dateFMT)
            timeFMT = android.text.format.DateFormat.getBestDateTimePattern(locale, timeFMT)
        }

        var upperFormat = dateFMT
        var lowerFormat = timeFMT
        var upperVisible = dateVisible
        var lowerVisible = timeVisible

        if (LauncherPreferences.clock().flipDateTime()) {
            upperFormat = lowerFormat.also { lowerFormat = upperFormat }
            upperVisible = lowerVisible.also { lowerVisible = upperVisible }
        }

        binding.clockUpperView.isVisible = upperVisible
        binding.clockLowerView.isVisible = lowerVisible

        binding.clockUpperView.setTextColor(LauncherPreferences.clock().color())
        binding.clockLowerView.setTextColor(LauncherPreferences.clock().color())

        binding.clockLowerView.format24Hour = lowerFormat
        binding.clockUpperView.format24Hour = upperFormat
        binding.clockLowerView.format12Hour = lowerFormat
        binding.clockUpperView.format12Hour = upperFormat
    }

    fun setOnClicks() {
        binding.clockUpperView.setOnClickListener {
            if (LauncherPreferences.clock().flipDateTime()) {
                Gesture.TIME(context)
            } else {
                Gesture.DATE(context)
            }
        }

        binding.clockLowerView.setOnClickListener {
            if (LauncherPreferences.clock().flipDateTime()) {
                Gesture.DATE(context)
            } else {
                Gesture.TIME(context)
            }
        }
    }



}