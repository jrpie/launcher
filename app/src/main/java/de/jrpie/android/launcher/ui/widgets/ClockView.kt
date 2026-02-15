package de.jrpie.android.launcher.ui.widgets

import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.databinding.WidgetClockBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.widgets.WidgetPanel
import java.util.Locale

class ClockView(
    context: Context,
    attrs: AttributeSet? = null,
    val appWidgetId: Int,
    val panelId: Int
) : ConstraintLayout(context, attrs) {
    constructor(context: Context, attrs: AttributeSet?) : this(
        context,
        attrs,
        WidgetPanel.HOME.id,
        -1
    )

    val binding: WidgetClockBinding =
        WidgetClockBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        initClock()
        setOnClicks()
    }


    private fun initClock() {
        val locale = Locale.getDefault()

        /* use 24h format for ISO8601 (i.e., when the format is not localized)
        or when the the format is localized and the selected locale uses 24h */
        val use24hFormat =
            !LauncherPreferences.clock().localized() || DateFormat.is24HourFormat(context)

        val dateVisible = LauncherPreferences.clock().dateVisible()
        val timeVisible = LauncherPreferences.clock().timeVisible()

        var dateFMT = "yyyy-MM-dd"
        var timeFMT = if (use24hFormat) {
            "HH:mm"
        } else {
            "hh:mm"
        }
        if (LauncherPreferences.clock().showSeconds()) {
            timeFMT += ":ss"
        }
        if (!use24hFormat) {
            timeFMT += " a"
        }

        if (LauncherPreferences.clock().localized()) {
            dateFMT = DateFormat.getBestDateTimePattern(locale, dateFMT)
            timeFMT = DateFormat.getBestDateTimePattern(locale, timeFMT)
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

    private fun setOnClicks() {
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