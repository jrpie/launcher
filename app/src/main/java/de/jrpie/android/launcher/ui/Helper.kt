package de.jrpie.android.launcher.ui

import android.app.Activity
import android.content.Context
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.preferences.theme.ColorTheme
import kotlin.math.absoluteValue

// Taken from https://stackoverflow.com/questions/47293269
fun View.blink(
    times: Int = Animation.INFINITE,
    duration: Long = 1000L,
    offset: Long = 20L,
    minAlpha: Float = 0.2f,
    maxAlpha: Float = 1.0f,
    repeatMode: Int = Animation.REVERSE
) {
    startAnimation(AlphaAnimation(minAlpha, maxAlpha).also {
        it.duration = duration
        it.startOffset = offset
        it.repeatMode = repeatMode
        it.repeatCount = times
    })
}

// Taken from: https://stackoverflow.com/a/30340794/12787264
fun ImageView.transformMonochrome(grayscale: Boolean, theme: ColorTheme) {
    this.colorFilter = if (grayscale) {
        ColorMatrixColorFilter(theme.monochromeMatrix)
    } else {
        null
    }
}

fun Drawable.transformMonochrome(grayscale: Boolean, theme: ColorTheme) {
    this.colorFilter = if (grayscale) {
        ColorMatrixColorFilter(theme.monochromeMatrix)
    } else {
        null
    }
}


// Taken from https://stackoverflow.com/a/50743764
private fun View.openSoftKeyboard(context: Context) {
    this.requestFocus()
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

// https://stackoverflow.com/a/17789187
private fun closeSoftKeyboard(activity: Activity) {
    activity.currentFocus?.let { focus ->
        (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(focus.windowToken, 0)
    }
}

/**
 * Applies the settings [LauncherPreferences.functionality.searchAutoOpenKeyboard]
 * and [LauncherPreferences.functionality.searchAutoCloseKeyboard]
 * to a combination of [RecyclerView] and [SearchView]
 *
 * @param openKeyboard false: the keyboard is not opened; true: searchAutoOpenKeyboard setting is used.
 */
fun applyKeyboardSettings(activity: Activity, recyclerView: RecyclerView, searchView: SearchView, openKeyboard: Boolean = true) {
    if (LauncherPreferences.functionality().searchAutoCloseKeyboard()) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var totalDy: Int = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                totalDy += dy
                if (totalDy.absoluteValue > 100) {
                    totalDy = 0
                    closeSoftKeyboard(activity)
                }
            }
        })
    }
    if (openKeyboard && LauncherPreferences.functionality().searchAutoOpenKeyboard()) {
        searchView.openSoftKeyboard(activity)
    }
}
