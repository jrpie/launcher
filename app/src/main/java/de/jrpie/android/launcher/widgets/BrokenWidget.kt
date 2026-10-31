package de.jrpie.android.launcher.widgets

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import de.jrpie.android.launcher.ui.widgets.BrokenWidgetView
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A placeholder for an [AppWidget] that could not be restored from a settings backup.
 * The appWidgetId of the original widget is useless on restore (the app widget host
 * allocation does not survive a backup), so the original provider is remembered instead,
 * allowing the user to see what was there and bind a new widget in its place.
 */
@Serializable
@SerialName("widget:broken")
class BrokenWidget(
    override var id: Int,
    override var position: WidgetPosition,
    override val panelId: Int,
    override var allowInteraction: Boolean = false,
    val packageName: String? = null,
    val className: String? = null,
    val user: Int? = null
) : Widget() {

    constructor(widget: AppWidget) : this(
        widget.id,
        widget.position,
        widget.panelId,
        false,
        widget.packageName,
        widget.className,
        widget.user
    )

    override fun createView(activity: Activity): View {
        return BrokenWidgetView(activity, null, id, packageName)
    }

    override fun findView(views: Sequence<View>): BrokenWidgetView? {
        return views.mapNotNull { it as? BrokenWidgetView }.firstOrNull { it.appWidgetId == id }
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

    override fun isReconfigurable(context: Context): Boolean {
        return false
    }

    override fun configure(activity: Activity, requestCode: Int) {}
}
