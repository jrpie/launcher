package de.jrpie.android.launcher.widgets

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import de.jrpie.android.launcher.ui.widgets.ClockView
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("widget:clock")
class ClockWidget(
    override val id: Int,
    override var position: WidgetPosition,
    override var allowInteraction: Boolean = true
) : Widget() {

    override fun createView(activity: Activity): View? {
        return ClockView(activity, null, id)
    }

    override fun findView(views: Sequence<View>): ClockView? {
        return views.mapNotNull { it as? ClockView }.firstOrNull { it.appWidgetId == id }
    }

    override fun getPreview(context: Context): Drawable? {
        return null
    }

    override fun getIcon(context: Context): Drawable? {
        return null
    }

    override fun isConfigurable(context: Context): Boolean {
        return false
    }

    override fun configure(activity: Activity, requestCode: Int) { }
}