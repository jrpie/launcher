package de.jrpie.android.launcher.widgets

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import de.jrpie.android.launcher.preferences.LauncherPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
sealed class Widget {
    abstract val id: Int
    abstract var position: WidgetPosition
    abstract val panelId: Int
    abstract var allowInteraction: Boolean

    /**
     * @param activity The activity where the view will be used. Must not be an AppCompatActivity.
     */
    abstract fun createView(activity: Activity): View?
    abstract fun findView(views: Sequence<View>): View?
    abstract fun getPreview(context: Context): Drawable?
    abstract fun getIcon(context: Context): Drawable?
    abstract fun isConfigurable(context: Context): Boolean
    abstract fun configure(activity: Activity, requestCode: Int)

    fun delete(context: Context) {
        if (id >= 0) {
            context.getAppWidgetHost().deleteAppWidgetId(id)
        }

        LauncherPreferences.widgets().widgets(
            LauncherPreferences.widgets().widgets()?.also {
                it.remove(this)
            }
        )
    }

    override fun hashCode(): Int {
        return id
    }

    override fun equals(other: Any?): Boolean {
        return (other as? Widget)?.id == id
    }

    fun serialize(): String {
        return Json.encodeToString(serializer(), this)
    }
    companion object {
        fun deserialize(serialized: String): Widget {
            return Json.decodeFromString(serialized)
        }
        fun byId(id: Int): Widget? {
            // TODO: do some caching
            return LauncherPreferences.widgets().widgets().firstOrNull {
                 it.id == id
            }
        }
    }
}
