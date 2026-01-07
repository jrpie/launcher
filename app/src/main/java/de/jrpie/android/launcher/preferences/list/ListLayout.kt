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
    val updateLayoutManager: (context: Context, layoutManager: RecyclerView.LayoutManager) -> Unit,
    val layoutResource: Int,
    val useBadgedText: Boolean,
) {
    DEFAULT(
        { c -> LinearLayoutManager(c) },
        { _,_ -> },
        R.layout.list_apps_row,
        false
    ),
    TEXT(
        { c -> LinearLayoutManager(c) },
        { _,_ -> },
        R.layout.list_apps_row_variant_text,
        true
    ),
    GRID(
        { c ->
            GridLayoutManager(c, getNumColumns(c))
        },
        { c,l ->
           (l as? GridLayoutManager)?.spanCount = getNumColumns(c)
        },
        R.layout.list_apps_row_variant_grid,
        false
    ),
    GRID_ONLY_ICONS(
        { c ->
            GridLayoutManager(c, getNumColumns(c, 55f))
        },
        { c,l ->
            (l as? GridLayoutManager)?.spanCount = getNumColumns(c, 55f)
        },
        R.layout.list_apps_row_variant_grid_only_icons,
        false
    ),
}

private fun getNumColumns(context: Context, columnWidthSP: Float = 90f): Int {
    val displayMetrics = context.resources.displayMetrics
    val widthColumnPx =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, columnWidthSP, displayMetrics)
    return (displayMetrics.widthPixels / widthColumnPx).toInt()
}