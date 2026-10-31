package de.jrpie.android.launcher.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.databinding.WidgetBrokenBinding

class BrokenWidgetView(
    context: Context,
    attrs: AttributeSet? = null,
    val appWidgetId: Int,
    packageName: String?
) : ConstraintLayout(context, attrs) {

    val binding: WidgetBrokenBinding =
        WidgetBrokenBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        packageName?.let {
            binding.brokenWidgetText.text =
                context.getString(R.string.widget_broken_text_package, it)
        }
    }
}
