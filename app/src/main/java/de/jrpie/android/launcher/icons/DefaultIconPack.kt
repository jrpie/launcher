package de.jrpie.android.launcher.icons

import android.content.Context
import android.graphics.drawable.Drawable
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.apps.AppInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("icons:default")
class DefaultIconPackHandle : IconPackHandle {
    override fun getLabel(context: Context): String {
        return context.getString(R.string.settings_theme_icon_pack_default)
    }
    override fun getIconPack(context: Context): IconPack {
        return DefaultIconPack
    }
    override fun toString(): String {
        return "default"
    }

    override fun equals(other: Any?): Boolean {
        return other is DefaultIconPackHandle
    }

    override fun hashCode(): Int {
        return 0
    }

}

data object DefaultIconPack : IconPack {
    override fun getLabel(context: Context): String {
        return context.getString(R.string.settings_theme_icon_pack_default)
    }

    override fun loadIcon(context: Context, info: AppInfo): Drawable? {
        return info.getLauncherActivityInfo(context)?.getIcon(0)
    }

    override fun loadBadgedIcon(context: Context, info: AppInfo): Drawable? {
        return info.getLauncherActivityInfo(context)?.getBadgedIcon(0)
    }
}

