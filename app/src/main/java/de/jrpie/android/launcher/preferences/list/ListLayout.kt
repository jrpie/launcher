package de.jrpie.android.launcher.preferences.list

import android.content.Context
import android.util.TypedValue
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.R

// TODO: move this to de.jrpie.android.launcher.ui.list.apps ?
@Suppress("unused")
enum class ListLayout(
    val layoutManager: (context: Context) -> RecyclerView.LayoutManager,
    val layoutResource: Int,
    val useBadgedText: Boolean,
) {
    DEFAULT(
        { c -> LinearLayoutManager(c) },
        R.layout.list_apps_row,
        false
    ),
    TEXT(
        { c -> LinearLayoutManager(c) },
        R.layout.list_apps_row_variant_text,
        true
    ),
    GRID(
        { c ->
            val displayMetrics = c.resources.displayMetrics
            val widthColumnPx =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 90f, displayMetrics)
            val numColumns = (displayMetrics.widthPixels / widthColumnPx).toInt()
            GridLayoutManager(c, numColumns)
        },
        R.layout.list_apps_row_variant_grid,
        false
    ),
}