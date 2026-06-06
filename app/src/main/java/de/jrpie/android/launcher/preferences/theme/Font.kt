package de.jrpie.android.launcher.preferences.theme

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import de.jrpie.android.launcher.R

/**
 * Changes here must also be added to @array/settings_theme_font_values
 */

@Suppress("unused")
enum class Font(val id: Int, val getTypeface: (Context) -> Typeface?) {
    HACK(
        R.style.fontHack,
        { c -> ResourcesCompat.getFont(c, R.font.hack) }),
    SYSTEM_DEFAULT(
        R.style.fontSystemDefault,
        { _ -> Typeface.DEFAULT }),
    SANS_SERIF(
        R.style.fontSansSerif,
        { _ -> Typeface.SANS_SERIF }),
    SERIF(
        R.style.fontSerif,
        { _ -> Typeface.SERIF }),
    MONOSPACE(
        R.style.fontMonospace,
        { _ -> Typeface.MONOSPACE }),
    SERIF_MONOSPACE(
        R.style.fontSerifMonospace,
        { _ -> Typeface.create("serif-monospace", Typeface.NORMAL) }),
    ;

    fun applyToTheme(theme: Resources.Theme) {
        theme.applyStyle(id, true)
    }
}