package de.jrpie.android.launcher.ui.widgets

import android.app.Activity
import android.os.Build
import android.os.Bundle
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
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.actions.LauncherAction
import de.jrpie.android.launcher.databinding.ActivitySelectWidgetBinding
import de.jrpie.android.launcher.databinding.HomeBinding
import de.jrpie.android.launcher.ui.list.ListActivity
import de.jrpie.android.launcher.ui.list.other.OtherRecyclerAdapter
import de.jrpie.android.launcher.widgets.bindAppWidget
import de.jrpie.android.launcher.widgets.getAppWidgetProviders

class SelectWidgetActivity : AppCompatActivity() {
    lateinit var binding: ActivitySelectWidgetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        // Initialise layout
        binding = ActivitySelectWidgetBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val viewManager = LinearLayoutManager(this)
        val viewAdapter = SelectWidgetRecyclerAdapter(this)

        binding.selectWidgetRecycler.apply {
            // improve performance (since content changes don't change the layout size)
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }
}

class SelectWidgetRecyclerAdapter(val activity: Activity) :
    RecyclerView.Adapter<SelectWidgetRecyclerAdapter.ViewHolder>() {

    private val widgets = getAppWidgetProviders(activity).toTypedArray()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        var textView: TextView = itemView.findViewById(R.id.list_widgets_row_name)
        var iconView: ImageView = itemView.findViewById(R.id.list_widgets_row_icon)
        var previewView: ImageView = itemView.findViewById(R.id.list_widgets_row_preview)


        override fun onClick(v: View) {
            val pos = bindingAdapterPosition
            val content = widgets[pos]

            bindAppWidget(activity, content)
            activity.finish()
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val label = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            "${widgets[i].activityInfo.loadLabel(activity.packageManager)} ${widgets[i].loadDescription(activity)}"
        } else {
            widgets[i].label
        }
        val preview = widgets[i].loadPreviewImage(activity, 100)
        val icon = widgets[i].loadIcon(activity, 100)

        viewHolder.textView.text = label
        viewHolder.iconView.setImageDrawable(icon)
        viewHolder.previewView.setImageDrawable(preview)
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
