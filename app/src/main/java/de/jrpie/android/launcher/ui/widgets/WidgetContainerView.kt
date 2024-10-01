package de.jrpie.android.launcher.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

// TODO: implement layout logic instead of linear layout
class WidgetContainerView(context: Context, attrs: AttributeSet?): LinearLayout(context, attrs) {
    init {
        orientation = VERTICAL
    }
}