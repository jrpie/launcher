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

    /**
     * @param activity The activity where the view will be used. Must not be an AppCompatActivity.
     */
    abstract fun createView(activity: Activity): View?
    abstract fun findView(views: Sequence<View>): View?
    abstract fun getPreview(context: Context): Drawable?
    abstract fun getIcon(context: Context): Drawable?

    fun delete(context: Context) {
        context.getAppWidgetHost().deleteAppWidgetId(id)

        LauncherPreferences.internal().widgets(
            LauncherPreferences.internal().widgets()?.also {
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
            return (LauncherPreferences.internal().widgets() ?: setOf())
                    .firstOrNull { it.id == id }
        }
    }
}