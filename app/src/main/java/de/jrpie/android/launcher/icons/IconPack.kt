package de.jrpie.android.launcher.icons

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.getUserFromId
import org.xmlpull.v1.XmlPullParser

interface IconPack {

    fun getLabel(context: Context): String

    // TODO: should work with AbstractAppInfo
    fun loadIcon(context: Context, info: AppInfo): Drawable?;
    fun loadBadgedIcon(context: Context, info: AppInfo): Drawable?;

    companion object {
        fun getSelectedIconPack(context: Context?): IconPack {
            return (context?.applicationContext as? Application)?.iconPack ?: DefaultIconPack()
        }
    }
}

class DefaultIconPack : IconPack {
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

class CustomIconPackHandle(val packageName: String, val label: String) {
    override fun toString(): String {
        return packageName
    }

    override fun equals(other: Any?): Boolean {
        return (other as? CustomIconPackHandle)?.packageName == packageName
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }
}

class CustomIconPack(val iconPackHandle: CustomIconPackHandle, val resources: Resources) :
    IconPack {
    val appfilter: MutableMap<String, String> = mutableMapOf()
    val drawableIds: MutableMap<String, Int> = mutableMapOf()

    private fun getDrawable(drawableName: String, context: Context): Drawable?
    {
        val drawableId = drawableIds.getOrPut(drawableName) {
            @SuppressLint("DiscouragedApi")
            resources.getIdentifier(drawableName, "drawable", iconPackHandle.packageName)
        }
        if (drawableId == 0) {
            return null
        }
        return ResourcesCompat.getDrawable(resources, drawableId, context.theme)
    }

    override fun getLabel(context: Context): String {
        return iconPackHandle.label
    }

    init {
        getAppfilterXML()?.let {
            while (it.eventType != XmlPullParser.END_DOCUMENT) {
                var drawable = ""
                var component = ""
                if (it.eventType == XmlPullParser.START_TAG && it.name == "item") {
                    for (i in 0..<it.attributeCount) {
                        if (it.getAttributeName(i) == "drawable") {
                            drawable = it.getAttributeValue(i)
                        } else if (it.getAttributeName(i) == "component") {
                            component = it.getAttributeValue(i)
                        }
                    }
                    appfilter[component] = drawable
                }
                it.next()
            }
        }
    }
    override fun loadIcon(context: Context, info: AppInfo): Drawable? {
        return appfilter["ComponentInfo{${info.packageName}/${info.activityName}}"]?.let { drawableName ->
            getDrawable(drawableName, context)
        } ?: info.getLauncherActivityInfo(context)?.getBadgedIcon(0)
    }

    override fun loadBadgedIcon(context: Context, info: AppInfo): Drawable? {
        return loadIcon(context, info)?.let { icon ->
            context.packageManager.getUserBadgedIcon(icon, getUserFromId(info.user, context))
        }
    }

    /**
     * List of available drawables (to select custom icons) in the form
     * ```xml
     * <item drawable="my_drawable" />
     * ```
     */
    private fun getDrawableXML(): XmlResourceParser? {
        val id = resources.getIdentifier("drawable", "xml", iconPackHandle.packageName)
        if (id == 0) {
            Log.e("Launcher", "Could not find drawable.xml in icon theme ${iconPackHandle.label}")
            return null
        }
        return resources.getXml(id)
    }

    /**
     *  appfilter.xml contains a map CompenentInfo -> drawable in the form:
     *  ```xml
     *  <item component="ComponentInfo{com.android.chrome/com.google.android.apps.chrome.Main}" drawable="ic_browser_green" />
     *  ```
     *  TODO: handle special keyword: https://github.com/teslacoil/Example_NovaTheme/tree/master#:~:text=The%20keywords%20supported%20are
     */
    private fun getAppfilterXML(): XmlResourceParser? {
        val id = resources.getIdentifier("appfilter", "xml", iconPackHandle.packageName)
        if (id == 0) {
            Log.e("Launcher", "Could not find appfilter.xml in icon theme ${iconPackHandle.label}")
            return null
        }
        return resources.getXml(id)
    }

    companion object {
        fun fromHandle(handle: CustomIconPackHandle, context: Context): CustomIconPack? {
            val resources = try {
                context.packageManager.getResourcesForApplication(handle.packageName)
            } catch (_: PackageManager.NameNotFoundException) {
                Log.w("Launcher", "Could not get resources for icon pack ${handle}")
                return null
            }
            return CustomIconPack(handle, resources)
        }
    }
}