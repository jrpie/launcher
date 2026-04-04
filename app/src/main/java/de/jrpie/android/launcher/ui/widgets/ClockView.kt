package de.jrpie.android.launcher.ui.widgets

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.format.DateFormat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
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
        val modernClock = LauncherPreferences.clock().modern()
        val showSeconds = LauncherPreferences.clock().showSeconds()
        val dateBold = LauncherPreferences.clock().dateBold()
        val timeBold = LauncherPreferences.clock().timeBold()

        var defaultDateFMT = "yyyy-MM-dd"
        var modernDateFMT = "dd | MMMM yyyy"
        var defaultTimeFMT = if (use24hFormat) {
            "HH:mm"
        } else {
            "hh:mm"
        }
        if (showSeconds) {
            defaultTimeFMT += ":ss"
        }
        if (!use24hFormat) {
            defaultTimeFMT += " a"
        }
        var modernTimeFMT = if (use24hFormat) {
            "HH '|' mm"
        } else {
            "hh '|' mm"
        }
        if (showSeconds) {
            modernTimeFMT += " '|' ss"
        }
        if (!use24hFormat) {
            modernTimeFMT += " a"
        }

        if (LauncherPreferences.clock().localized()) {
            defaultDateFMT = DateFormat.getBestDateTimePattern(locale, defaultDateFMT)
            modernDateFMT = DateFormat.getBestDateTimePattern(locale, modernDateFMT)
            defaultTimeFMT = DateFormat.getBestDateTimePattern(locale, defaultTimeFMT)
            modernTimeFMT = DateFormat.getBestDateTimePattern(locale, modernTimeFMT)
        }

        var upperFormat: String
        var lowerFormat: String
        var upperVisible: Boolean
        var lowerVisible: Boolean
        var upperIsTime: Boolean

        if (modernClock) {
            upperFormat = modernDateFMT
            lowerFormat = modernTimeFMT
            upperVisible = dateVisible
            lowerVisible = timeVisible
            upperIsTime = false
        } else {
            upperFormat = defaultDateFMT
            lowerFormat = defaultTimeFMT
            upperVisible = dateVisible
            lowerVisible = timeVisible
            upperIsTime = false
        }

        if (LauncherPreferences.clock().flipDateTime()) {
            upperFormat = lowerFormat.also { lowerFormat = upperFormat }
            upperVisible = lowerVisible.also { lowerVisible = upperVisible }
            upperIsTime = !upperIsTime
        }

        binding.clockUpperView.isVisible = upperVisible
        binding.clockLowerView.isVisible = lowerVisible
        binding.clockPanel.isVisible = upperVisible || lowerVisible

        val clockColor = LauncherPreferences.clock().color()
        binding.clockUpperView.setTextColor(clockColor)
        binding.clockLowerView.setTextColor(clockColor)
        applyClockStyle(clockColor, modernClock, upperIsTime, showSeconds)

        binding.clockLowerView.format24Hour = lowerFormat
        binding.clockUpperView.format24Hour = upperFormat
        binding.clockLowerView.format12Hour = lowerFormat
        binding.clockUpperView.format12Hour = upperFormat

        if (upperIsTime) {
            applyBold(binding.clockUpperView, timeBold)
            applyBold(binding.clockLowerView, dateBold)
        } else {
            applyBold(binding.clockUpperView, dateBold)
            applyBold(binding.clockLowerView, timeBold)
        }
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

    private fun createClockPanelBackground(clockColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 36f.dp
            setColor(ColorUtils.setAlphaComponent(0xFFFFFFFF.toInt(), 20))
            setStroke(1f.dp.toInt(), ColorUtils.blendARGB(
                ColorUtils.setAlphaComponent(0xFFFFFFFF.toInt(), 120),
                ColorUtils.setAlphaComponent(clockColor, 72),
                0.35f
            ))
        }
    }

    private fun createModernTimeBackground(clockColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 0f
            setColor(ColorUtils.setAlphaComponent(0xFFFFFFFF.toInt(), 0))
            setStroke(3f.dp.toInt(), ColorUtils.blendARGB(
                ColorUtils.setAlphaComponent(0xFFFFFFFF.toInt(), 230),
                ColorUtils.setAlphaComponent(clockColor, 140),
                0.25f
            ))
        }
    }

    private fun applyClockStyle(
        clockColor: Int,
        modernClock: Boolean,
        upperIsTime: Boolean,
        showSeconds: Boolean
    ) {
        val panelLayoutParams = binding.clockPanel.layoutParams as LayoutParams
        binding.clockPanel.layoutParams = panelLayoutParams

        val upperParams = binding.clockUpperView.layoutParams as LinearLayout.LayoutParams
        val lowerParams = binding.clockLowerView.layoutParams as LinearLayout.LayoutParams

        fun setAutoSize(textView: TextView, minSp: Int, maxSp: Int) {
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                textView,
                minSp,
                maxSp,
                1,
                TypedValue.COMPLEX_UNIT_SP
            )
        }

        if (modernClock) {
            val timeMaxSp = if (showSeconds) 38 else 64
            val timeMinSp = if (showSeconds) 16 else 34
            val dateMaxSp = if (showSeconds) 17 else 19
            val timeHorizontalPadding = if (showSeconds) 14f.dp.toInt() else 10f.dp.toInt()
            val timeVerticalPadding = if (showSeconds) 26f.dp.toInt() else 24f.dp.toInt()
            val sideMargin = if (showSeconds) 18f.dp.toInt() else 44f.dp.toInt()

            panelLayoutParams.width = 0
            panelLayoutParams.marginStart = sideMargin
            panelLayoutParams.marginEnd = sideMargin
            binding.clockPanel.background = null
            binding.clockPanel.elevation = 0f
            binding.clockPanel.minimumWidth = 0
            binding.clockPanel.setPadding(0, 0, 0, 0)

            upperParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            lowerParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            lowerParams.topMargin = if (showSeconds) 16f.dp.toInt() else 22f.dp.toInt()

            binding.clockUpperView.gravity = Gravity.CENTER
            binding.clockLowerView.gravity = Gravity.CENTER
            binding.clockUpperView.textAlignment = TEXT_ALIGNMENT_CENTER
            binding.clockLowerView.textAlignment = TEXT_ALIGNMENT_CENTER

            if (upperIsTime) {
                setAutoSize(binding.clockUpperView, timeMinSp, timeMaxSp)
                setAutoSize(binding.clockLowerView, 10, dateMaxSp)
                binding.clockUpperView.letterSpacing = 0.01f
                binding.clockLowerView.letterSpacing = if (showSeconds) 0.14f else 0.16f
                binding.clockUpperView.setShadowLayer(8f.dp, 0f, 2f.dp, ColorUtils.setAlphaComponent(0xFFFFFFFF.toInt(), 16))
                binding.clockLowerView.setShadowLayer(0f, 0f, 0f, 0)
                binding.clockUpperView.setAllCaps(false)
                binding.clockLowerView.setAllCaps(true)
                binding.clockUpperView.background = createModernTimeBackground(clockColor)
                binding.clockUpperView.setPadding(
                    timeHorizontalPadding,
                    timeVerticalPadding,
                    timeHorizontalPadding,
                    timeVerticalPadding
                )
                binding.clockLowerView.background = null
                binding.clockLowerView.setPadding(0, 0, 0, 0)
            } else {
                setAutoSize(binding.clockUpperView, 10, dateMaxSp)
                setAutoSize(binding.clockLowerView, timeMinSp, timeMaxSp)
                binding.clockUpperView.letterSpacing = if (showSeconds) 0.14f else 0.16f
                binding.clockLowerView.letterSpacing = 0.01f
                binding.clockUpperView.setShadowLayer(0f, 0f, 0f, 0)
                binding.clockLowerView.setShadowLayer(8f.dp, 0f, 2f.dp, ColorUtils.setAlphaComponent(0xFFFFFFFF.toInt(), 16))
                binding.clockUpperView.setAllCaps(true)
                binding.clockLowerView.setAllCaps(false)
                binding.clockLowerView.background = createModernTimeBackground(clockColor)
                binding.clockLowerView.setPadding(
                    timeHorizontalPadding,
                    timeVerticalPadding,
                    timeHorizontalPadding,
                    timeVerticalPadding
                )
                binding.clockUpperView.background = null
                binding.clockUpperView.setPadding(0, 0, 0, 0)
            }
        } else {
            panelLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            panelLayoutParams.marginStart = 0
            panelLayoutParams.marginEnd = 0
            binding.clockPanel.background = null
            binding.clockPanel.elevation = 0f
            binding.clockPanel.minimumWidth = 0
            binding.clockPanel.setPadding(0, 0, 0, 0)

            upperParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            lowerParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            lowerParams.topMargin = 0

            binding.clockUpperView.gravity = Gravity.CENTER
            binding.clockLowerView.gravity = Gravity.CENTER
            binding.clockUpperView.textAlignment = TEXT_ALIGNMENT_CENTER
            binding.clockLowerView.textAlignment = TEXT_ALIGNMENT_CENTER
            setAutoSize(binding.clockUpperView, 18, 30)
            setAutoSize(binding.clockLowerView, 10, 18)
            binding.clockUpperView.letterSpacing = 0f
            binding.clockLowerView.letterSpacing = 0f
            binding.clockUpperView.setShadowLayer(0f, 0f, 0f, 0)
            binding.clockLowerView.setShadowLayer(0f, 0f, 0f, 0)
            binding.clockUpperView.background = null
            binding.clockLowerView.background = null
            binding.clockUpperView.setPadding(0, 0, 0, 0)
            binding.clockLowerView.setPadding(0, 0, 0, 0)
            binding.clockUpperView.setAllCaps(false)
            binding.clockLowerView.setAllCaps(false)
        }

        binding.clockUpperView.layoutParams = upperParams
        binding.clockLowerView.layoutParams = lowerParams
    }

    private fun applyBold(textView: TextView, enabled: Boolean) {
        textView.paint.isFakeBoldText = enabled
        textView.invalidate()
    }

    private val Float.dp: Float
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            resources.displayMetrics
        )
}