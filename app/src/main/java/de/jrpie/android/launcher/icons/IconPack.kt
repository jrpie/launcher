package de.jrpie.android.launcher.icons

import android.content.Context
import android.graphics.drawable.Drawable
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.apps.AppInfo
import kotlinx.serialization.Serializable

@Serializable
sealed interface IconPackHandle {
    fun getLabel(context: Context): String
    fun getIconPack(context: Context): IconPack?
}

interface IconPack {

    fun getLabel(context: Context): String

    fun loadIcon(context: Context, info: AppInfo): Drawable?
    fun loadBadgedIcon(context: Context, info: AppInfo): Drawable?

    companion object {
        fun getSelectedIconPack(context: Context?): IconPack {
            return (context?.applicationContext as? Application)?.iconPack ?: DefaultIconPack
        }
    }
}
