package de.jrpie.android.launcher.preferences.theme

import android.content.Context
import android.content.res.Resources
import android.graphics.ColorMatrix
import com.google.android.material.color.DynamicColors
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.preferences.LauncherPreferences

private val grayscaleMatrix = ColorMatrix().apply { setSaturation(0f) }

enum class ColorTheme(
    private val id: Int,
    private val labelResource: Int,
    private val shadowId: Int,
    val isAvailable: () -> Boolean,
    val monochromeMatrix: ColorMatrix,
    val forceMonochrome: Boolean = false
) {
    DEFAULT(
        R.style.colorThemeDefault,
        R.string.settings_theme_color_theme_item_default,
        R.style.textShadow,
        { true },
        grayscaleMatrix
    ),
    DARK(
        R.style.colorThemeDark,
        R.string.settings_theme_color_theme_item_dark,
        R.style.textShadow,
        { true },
        grayscaleMatrix
    ),
    LIGHT(
        R.style.colorThemeLight,
        R.string.settings_theme_color_theme_item_light,
        R.style.textShadowLight,
        { true },
        grayscaleMatrix
    ),
    GREEN(
        R.style.colorThemeGreen,
        R.string.settings_theme_color_theme_item_green,
        R.style.textShadow,
        { true },
        ColorMatrix().apply {
            setSaturation(0f)
            postConcat(
                ColorMatrix(
                    floatArrayOf(
                        0f, 0f, 0f, 0f, 0f,
                        0f, 1f, 0f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        },
        true
    ),
    AMBER(
        R.style.colorThemeAmber,
        R.string.settings_theme_color_theme_item_amber,
        R.style.textShadow,
        { true },
        ColorMatrix().apply {
            setSaturation(0f)
            postConcat(
                ColorMatrix(
                    floatArrayOf(
                        1f, 0f, 0f, 0f, 0f,
                        0f, (0xb0 / 255f), 0f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        },
        true
    ),
    DYNAMIC(
        R.style.colorThemeDynamic,
        R.string.settings_theme_color_theme_item_dynamic,
        R.style.textShadow,
        { DynamicColors.isDynamicColorAvailable() },
        grayscaleMatrix
    ),
    ;

    fun applyToTheme(theme: Resources.Theme, shadow: Boolean) {
        val colorTheme = if (this.isAvailable()) this else DEFAULT
        theme.applyStyle(colorTheme.id, true)

        if (shadow) {
            theme.applyStyle(colorTheme.shadowId, true)
        }
    }

    fun monochromeIcons(): Boolean {
        if (forceMonochrome) {
            return true
        }
        return LauncherPreferences.theme().monochromeIcons()
    }

    fun getLabel(context: Context): String {
        return context.getString(labelResource)
    }
}