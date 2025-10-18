package de.jrpie.android.launcher.ui.widgets.manage

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.databinding.ActivityManageWidgetPanelsBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.ui.UIObjectActivity
import de.jrpie.android.launcher.widgets.WidgetPanel
import de.jrpie.android.launcher.widgets.updateWidgetPanel

class ManageWidgetPanelsActivity : UIObjectActivity() {

    @SuppressLint("NotifyDataSetChanged")
    private val sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            if (
                prefKey == LauncherPreferences.widgets().keys().customPanels()
                || prefKey == LauncherPreferences.widgets().keys().widgets()
            ) {
                viewAdapter.widgetPanels =
                    (LauncherPreferences.widgets().customPanels() ?: setOf()).toTypedArray()

                viewAdapter.notifyDataSetChanged()
            }
        }
    private lateinit var binding: ActivityManageWidgetPanelsBinding
    private lateinit var viewAdapter: WidgetPanelsRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageWidgetPanelsBinding.inflate(layoutInflater)
        setContentView(binding.main)

        val viewManager = LinearLayoutManager(this)
        viewAdapter = WidgetPanelsRecyclerAdapter(this, true) { widgetPanel ->
            startActivity(
                Intent(
                    this@ManageWidgetPanelsActivity,
                    ManageWidgetsActivity::class.java
                ).also {
                    it.putExtra(EXTRA_PANEL_ID, widgetPanel.id)
                })
        }
        binding.manageWidgetPanelsRecycler.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        binding.manageWidgetPanelsClose.setOnClickListener { finish() }
        binding.manageWidgetPanelsAddPanel.setOnClickListener {
            AlertDialog.Builder(this@ManageWidgetPanelsActivity, R.style.AlertDialogCustom).apply {
                setTitle(R.string.dialog_create_widget_panel_title)
                setNegativeButton(R.string.dialog_cancel) { _, _ -> }
                setPositiveButton(R.string.dialog_ok) { dialogInterface, _ ->
                    val panelId = WidgetPanel.allocateId()
                    val label = (dialogInterface as? AlertDialog)
                        ?.findViewById<EditText>(R.id.dialog_create_widget_panel_edit_text)?.text?.toString()
                        ?: (getString(R.string.widget_panel_default_name, panelId))

                    updateWidgetPanel(WidgetPanel(panelId, label))
                }
                setView(R.layout.dialog_create_widget_panel)
            }.create().also { it.show() }.apply {
                findViewById<EditText>(R.id.dialog_create_widget_panel_edit_text)
                    ?.setText(
                        getString(
                            R.string.widget_panel_default_name,
                            WidgetPanel.allocateId()
                        )
                    )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    override fun onPause() {
        LauncherPreferences.getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
        super.onPause()
    }

    override fun setOnClicks() {
        binding.manageWidgetPanelsClose.setOnClickListener { finish() }
    }
}