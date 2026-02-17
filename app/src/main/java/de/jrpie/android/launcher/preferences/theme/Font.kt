package de.jrpie.android.launcher.preferences.theme

import android.content.res.Resources
import de.jrpie.android.launcher.R

/**
 * Changes here must also be added to @array/settings_theme_font_values
 */

@Suppress("unused")
enum class Font(val id: Int) {
    HACK(R.style.fontHack),
    SYSTEM_DEFAULT(R.style.fontSystemDefault),
    SANS_SERIF(R.style.fontSansSerif),
    SERIF(R.style.fontSerif),
    MONOSPACE(R.style.fontMonospace),
    SERIF_MONOSPACE(R.style.fontSerifMonospace),
    ;

    fun applyToTheme(theme: Resources.Theme) {
        theme.applyStyle(id, true)
    }
}