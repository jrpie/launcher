package de.jrpie.android.launcher.ui.widgets.manage

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.databinding.ActivitySelectWidgetBinding
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.ui.UIObject
import de.jrpie.android.launcher.widgets.bindAppWidgetOrRequestPermission
import de.jrpie.android.launcher.widgets.getAppWidgetProviders


private const val REQUEST_WIDGET_PERMISSION = 29

/**
 *  This activity lets the user pick an app widget to add.
 *  It provides an interface similar to [android.appwidget.AppWidgetManager.ACTION_APPWIDGET_PICK],
 *  but shows more information and also shows widgets from other user profiles.
 */
class SelectWidgetActivity : AppCompatActivity(), UIObject {
    lateinit var binding: ActivitySelectWidgetBinding
    var widgetId: Int = -1

    private fun tryBindWidget(info: AppWidgetProviderInfo) {
        if(bindAppWidgetOrRequestPermission(this, info, widgetId, REQUEST_WIDGET_PERMISSION)) {
            setResult(
                RESULT_OK,
                Intent().also {
                    it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                }
            )
            finish()
        }
    }

    override fun onStart() {
        super<AppCompatActivity>.onStart()
        super<UIObject>.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()

        binding = ActivitySelectWidgetBinding.inflate(layoutInflater)
        setContentView(binding.root)


        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)

        val viewManager = LinearLayoutManager(this)
        val viewAdapter = SelectWidgetRecyclerAdapter()

        binding.selectWidgetRecycler.apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun getTheme(): Resources.Theme {
        return modifyTheme(super.getTheme())
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_WIDGET_PERMISSION && resultCode == RESULT_OK) {
            data ?: return
            Log.i("SelectWidget", "permission granted")
            val provider = (data.getSerializableExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER) as? AppWidgetProviderInfo) ?: return
            tryBindWidget(provider)
        }
    }

    inner class SelectWidgetRecyclerAdapter() :
        RecyclerView.Adapter<SelectWidgetRecyclerAdapter.ViewHolder>() {

        private val widgets = getAppWidgetProviders(this@SelectWidgetActivity).toTypedArray()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            var textView: TextView = itemView.findViewById(R.id.list_widgets_row_name)
            var descriptionView: TextView = itemView.findViewById(R.id.list_widgets_row_description)
            var iconView: ImageView = itemView.findViewById(R.id.list_widgets_row_icon)
            var previewView: ImageView = itemView.findViewById(R.id.list_widgets_row_preview)


            override fun onClick(v: View) {
                tryBindWidget(widgets[bindingAdapterPosition])
            }

            init {
                itemView.setOnClickListener(this)
            }
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
            val label = widgets[i].label
            val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                widgets[i].loadDescription(this@SelectWidgetActivity)
            } else {
                ""
            }
            val preview =
                widgets[i].loadPreviewImage(this@SelectWidgetActivity, DisplayMetrics.DENSITY_DEFAULT )
            val icon =
                widgets[i].loadIcon(this@SelectWidgetActivity, DisplayMetrics.DENSITY_DEFAULT)

            viewHolder.textView.text = label
            viewHolder.descriptionView.text = description
            viewHolder.descriptionView.visibility =
                if (description?.isEmpty() == false) { View.VISIBLE } else { View.GONE }
            viewHolder.iconView.setImageDrawable(icon)

            viewHolder.previewView.setImageDrawable(preview)
            viewHolder.previewView.visibility =
                if (preview != null) { View.VISIBLE } else { View.GONE }

            viewHolder.previewView.requestLayout()
        }

        override fun getItemCount(): Int {
            return widgets.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view: View = inflater.inflate(R.layout.list_widgets_row, parent, false)
            return ViewHolder(view)
        }
    }
}