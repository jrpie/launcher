package de.jrpie.android.launcher.preferences

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.preferences.list.ListAlignment

class ListAlignmentPreference(context: Context, attrs: AttributeSet?) :
    Preference(context, attrs) {

    @Suppress("unused")
    constructor(context: Context) : this(context, null)

    init {
        layoutResource = R.layout.preference_list_alignment
        isSelectable = false
    }

    private fun currentAlignment(): ListAlignment =
        ListAlignment.entries.firstOrNull { it.name == getPersistedString(ListAlignment.LEFT.name) }
            ?: ListAlignment.LEFT

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val buttons = listOf(
            ListAlignment.LEFT to holder.findViewById(R.id.settings_list_alignment_left),
            ListAlignment.CENTER to holder.findViewById(R.id.settings_list_alignment_center),
            ListAlignment.RIGHT to holder.findViewById(R.id.settings_list_alignment_right)
        ).map { (alignment, view) -> alignment to (view as TextView) }

        fun update() {
            val selected = currentAlignment()
            buttons.forEachIndexed { i, (alignment, button) ->
                styleButton(button, i, buttons.size, alignment == selected)
            }
        }

        buttons.forEach { (alignment, button) ->
            button.setOnClickListener {
                if (callChangeListener(alignment.name)) {
                    persistString(alignment.name)
                    update()
                }
            }
        }
        update()
    }

    private fun styleButton(button: TextView, index: Int, count: Int, checked: Boolean) {
        val context = button.context
        val accent = resolveColor(context, androidx.appcompat.R.attr.colorAccent) ?: 0
        val textColor = resolveColor(context, android.R.attr.textColor) ?: 0
        val selectedTextColor =
            resolveColor(context, R.attr.listAlignmentSelectedTextColor) ?: textColor
        val density = context.resources.displayMetrics.density
        val r = 8 * density
        val left = if (index == 0) r else 0f
        val right = if (index == count - 1) r else 0f

        val stroke = (1 * density).toInt().coerceAtLeast(1)
        (button.layoutParams as? ViewGroup.MarginLayoutParams)?.marginStart =
            if (index == 0) 0 else -stroke

        button.background = GradientDrawable().apply {
            cornerRadii = floatArrayOf(left, left, right, right, right, right, left, left)
            setStroke(stroke, accent)
            setColor(if (checked) accent else 0)
        }
        button.setTextColor(if (checked) selectedTextColor else textColor)
        button.isSelected = checked
    }

    private fun resolveColor(context: Context, attr: Int): Int? {
        val value = TypedValue()
        return if (context.theme.resolveAttribute(attr, value, true)) value.data else null
    }
}
