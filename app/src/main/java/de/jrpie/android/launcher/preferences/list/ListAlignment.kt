package de.jrpie.android.launcher.preferences.list

import android.view.Gravity

@Suppress("unused")
enum class ListAlignment(val gravity: Int) {
    LEFT(Gravity.START or Gravity.CENTER_VERTICAL),
    CENTER(Gravity.CENTER),
    RIGHT(Gravity.END or Gravity.CENTER_VERTICAL)
}
