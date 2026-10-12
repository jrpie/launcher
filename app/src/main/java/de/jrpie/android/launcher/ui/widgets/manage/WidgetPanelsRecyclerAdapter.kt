package de.jrpie.android.launcher.ui.widgets.manage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.widgets.WidgetPanel
import de.jrpie.android.launcher.widgets.updateWidgetPanel


class WidgetPanelsRecyclerAdapter(
    val context: Context,
    val showMenu: Boolean = false,
    val onSelectWidgetPanel: (WidgetPanel) -> Unit
) :
    RecyclerView.Adapter<WidgetPanelsRecyclerAdapter.ViewHolder>() {

    var widgetPanels = (LauncherPreferences.widgets().customPanels() ?: setOf()).toTypedArray()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var labelView: TextView = itemView.findViewById(R.id.list_widget_panels_label)
        var infoView: TextView = itemView.findViewById(R.id.list_widget_panels_info)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        viewHolder.labelView.text = widgetPanels[i].label
        val numWidgets = widgetPanels[i].getWidgets().size
        viewHolder.infoView.text = context.resources.getQuantityString(
            R.plurals.widget_panel_number_of_widgets,
            numWidgets, numWidgets
        )

        viewHolder.itemView.setOnClickListener {
            onSelectWidgetPanel(widgetPanels[i])
        }

        if (showMenu) {
            viewHolder.itemView.setOnLongClickListener {
                showOptionsPopup(
                    viewHolder,
                    widgetPanels[i]
                )
            }
        }
    }

    @Suppress("SameReturnValue")
    private fun showOptionsPopup(
        viewHolder: ViewHolder,
        widgetPanel: WidgetPanel
    ): Boolean {
        //create the popup menu

        val popup = PopupMenu(context, viewHolder.labelView)
        popup.menu.add(R.string.manage_widget_panels_delete).setOnMenuItemClickListener { _ ->
            widgetPanel.delete(context)
            true
        }
        popup.menu.add(R.string.manage_widget_panels_rename).setOnMenuItemClickListener { _ ->
            AlertDialog.Builder(context, R.style.AlertDialogCustom).apply {
                setNegativeButton(R.string.dialog_cancel) { _, _ -> }
                setPositiveButton(R.string.dialog_ok) { dialogInterface, _ ->
                    var newLabel = (dialogInterface as? AlertDialog)
                        ?.findViewById<EditText>(R.id.dialog_rename_widget_panel_edit_text)
                        ?.text?.toString()
                    if (newLabel.isNullOrEmpty()) {
                        newLabel =
                            (context.getString(R.string.widget_panel_default_name, widgetPanel.id))
                    }
                    widgetPanel.label = newLabel
                    updateWidgetPanel(widgetPanel)
                }
                setView(R.layout.dialog_rename_widget_panel)
            }.create().also { it.show() }.apply {
                findViewById<EditText>(R.id.dialog_rename_widget_panel_edit_text)?.let {
                    it.setText(widgetPanel.label)
                    it.hint = context.getString(R.string.widget_panel_default_name, widgetPanel.id)
                }
            }
            true
        }

        popup.show()
        return true
    }

    override fun getItemCount(): Int {
        return widgetPanels.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.list_widget_panels_row, parent, false)
        val viewHolder = ViewHolder(view)
        return viewHolder
    }
}