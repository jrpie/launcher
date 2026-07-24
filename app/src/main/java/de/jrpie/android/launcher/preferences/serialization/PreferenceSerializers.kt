@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package de.jrpie.android.launcher.preferences.serialization

import de.jrpie.android.launcher.apps.AbstractAppInfo
import de.jrpie.android.launcher.apps.PinnedShortcutInfo
import de.jrpie.android.launcher.widgets.AppWidget
import de.jrpie.android.launcher.widgets.BrokenWidget
import de.jrpie.android.launcher.widgets.Widget
import de.jrpie.android.launcher.widgets.WidgetPanel
import eu.jonahbauer.android.preference.annotations.serializer.PreferenceExportSerializer
import eu.jonahbauer.android.preference.annotations.serializer.PreferenceSerializationException
import eu.jonahbauer.android.preference.annotations.serializer.PreferenceSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject


@Suppress("UNCHECKED_CAST")
class SetAbstractAppInfoPreferenceSerializer :
    PreferenceSerializer<java.util.Set<AbstractAppInfo>?, java.util.Set<java.lang.String>?> {
    @Throws(PreferenceSerializationException::class)
    override fun serialize(value: java.util.Set<AbstractAppInfo>?): java.util.Set<java.lang.String> {
        return value?.map(AbstractAppInfo::serialize)
            ?.toHashSet() as java.util.Set<java.lang.String>
    }

    @Throws(PreferenceSerializationException::class)
    override fun deserialize(value: java.util.Set<java.lang.String>?): java.util.Set<AbstractAppInfo>? {
        return value?.map(java.lang.String::toString)?.map(AbstractAppInfo::deserialize)
            ?.toHashSet() as? java.util.Set<AbstractAppInfo>
    }
}


@Suppress("UNCHECKED_CAST")
class SetWidgetSerializer :
    PreferenceSerializer<java.util.Set<Widget>?, java.util.Set<java.lang.String>?> {
    @Throws(PreferenceSerializationException::class)
    override fun serialize(value: java.util.Set<Widget>?): java.util.Set<java.lang.String>? {
        return value?.map(Widget::serialize)
            ?.toHashSet() as? java.util.Set<java.lang.String>
    }

    @Throws(PreferenceSerializationException::class)
    override fun deserialize(value: java.util.Set<java.lang.String>?): java.util.Set<Widget>? {
        return value?.map(java.lang.String::toString)?.map(Widget::deserialize)
            ?.toHashSet() as? java.util.Set<Widget>
    }
}

@Suppress("UNCHECKED_CAST")
class SetWidgetPanelSerializer :
    PreferenceSerializer<java.util.Set<WidgetPanel>?, java.util.Set<java.lang.String>?> {
    @Throws(PreferenceSerializationException::class)
    override fun serialize(value: java.util.Set<WidgetPanel>?): java.util.Set<java.lang.String>? {
        return value?.map(WidgetPanel::serialize)
            ?.toHashSet() as? java.util.Set<java.lang.String>
    }

    @Throws(PreferenceSerializationException::class)
    override fun deserialize(value: java.util.Set<java.lang.String>?): java.util.Set<WidgetPanel>? {
        return value?.map(java.lang.String::toString)?.map(WidgetPanel::deserialize)
            ?.toHashSet() as? java.util.Set<WidgetPanel>
    }
}


@Suppress("UNCHECKED_CAST")
class SetAbstractAppInfoExportSerializer :
    PreferenceExportSerializer<java.util.Set<AbstractAppInfo>?, JSONArray?> {
    @Throws(PreferenceSerializationException::class)
    override fun export(value: java.util.Set<AbstractAppInfo>?): JSONArray? {
        return value?.let { set -> JSONArray(set.map { JSONObject(it.serialize()) }) }
    }

    @Throws(PreferenceSerializationException::class)
    override fun restore(value: JSONArray?): java.util.Set<AbstractAppInfo>? {
        if (value == null) return null
        return (0 until value.length())
            .map { AbstractAppInfo.deserialize(value.getJSONObject(it).toString()) }
            .toHashSet() as? java.util.Set<AbstractAppInfo>
    }
}


@Suppress("UNCHECKED_CAST")
class SetPinnedShortcutInfoExportSerializer :
    PreferenceExportSerializer<java.util.Set<PinnedShortcutInfo>?, JSONArray?> {
    @Throws(PreferenceSerializationException::class)
    override fun export(value: java.util.Set<PinnedShortcutInfo>?): JSONArray? {
        return value?.let { set ->
            JSONArray(set.map { JSONObject(Json.encodeToString<PinnedShortcutInfo>(it)) })
        }
    }

    @Throws(PreferenceSerializationException::class)
    override fun restore(value: JSONArray?): java.util.Set<PinnedShortcutInfo>? {
        if (value == null) return null
        return (0 until value.length())
            .map { Json.decodeFromString<PinnedShortcutInfo>(value.getJSONObject(it).toString()) }
            .toHashSet() as? java.util.Set<PinnedShortcutInfo>
    }
}


@Suppress("UNCHECKED_CAST")
class MapAbstractAppInfoStringExportSerializer :
    PreferenceExportSerializer<java.util.HashMap<AbstractAppInfo, String>?, JSONArray?> {
    @Throws(PreferenceSerializationException::class)
    override fun export(value: java.util.HashMap<AbstractAppInfo, String>?): JSONArray? {
        return value?.let { map ->
            JSONArray(map.map { (key, name) ->
                JSONObject().put("key", JSONObject(key.serialize())).put("value", name)
            })
        }
    }

    @Throws(PreferenceSerializationException::class)
    override fun restore(value: JSONArray?): java.util.HashMap<AbstractAppInfo, String>? {
        if (value == null) return null
        val map = java.util.HashMap<AbstractAppInfo, String>()
        for (i in 0 until value.length()) {
            val entry = value.getJSONObject(i)
            map[AbstractAppInfo.deserialize(entry.getJSONObject("key").toString())] =
                entry.getString("value")
        }
        return map
    }
}


@Suppress("UNCHECKED_CAST")
class SetWidgetExportSerializer :
    PreferenceExportSerializer<java.util.Set<Widget>?, JSONArray?> {
    @Throws(PreferenceSerializationException::class)
    override fun export(value: java.util.Set<Widget>?): JSONArray? {
        // The appWidgetId of an AppWidget does not survive a backup,
        // so AppWidgets are exported as BrokenWidgets keeping the original provider.
        return value?.let { set ->
            JSONArray(set.map {
                JSONObject((if (it is AppWidget) BrokenWidget(it) else it).serialize())
            })
        }
    }

    @Throws(PreferenceSerializationException::class)
    override fun restore(value: JSONArray?): java.util.Set<Widget>? {
        if (value == null) return null
        return (0 until value.length())
            .map { Widget.deserialize(value.getJSONObject(it).toString()) }
            .toHashSet() as? java.util.Set<Widget>
    }
}


@Suppress("UNCHECKED_CAST")
class SetWidgetPanelExportSerializer :
    PreferenceExportSerializer<java.util.Set<WidgetPanel>?, JSONArray?> {
    @Throws(PreferenceSerializationException::class)
    override fun export(value: java.util.Set<WidgetPanel>?): JSONArray? {
        return value?.let { set -> JSONArray(set.map { JSONObject(it.serialize()) }) }
    }

    @Throws(PreferenceSerializationException::class)
    override fun restore(value: JSONArray?): java.util.Set<WidgetPanel>? {
        if (value == null) return null
        return (0 until value.length())
            .map { WidgetPanel.deserialize(value.getJSONObject(it).toString()) }
            .toHashSet() as? java.util.Set<WidgetPanel>
    }
}


@Suppress("UNCHECKED_CAST")
class SetPinnedShortcutInfoPreferenceSerializer :
    PreferenceSerializer<java.util.Set<PinnedShortcutInfo>?, java.util.Set<java.lang.String>?> {
    @Throws(PreferenceSerializationException::class)
    override fun serialize(value: java.util.Set<PinnedShortcutInfo>?): java.util.Set<java.lang.String> {
        return value?.map { Json.encodeToString<PinnedShortcutInfo>(it) }
            ?.toHashSet() as java.util.Set<java.lang.String>
    }

    @Throws(PreferenceSerializationException::class)
    override fun deserialize(value: java.util.Set<java.lang.String>?): java.util.Set<PinnedShortcutInfo>? {
        return value?.map(java.lang.String::toString)
            ?.map { Json.decodeFromString<PinnedShortcutInfo>(it) }
            ?.toHashSet() as? java.util.Set<PinnedShortcutInfo>
    }
}


@Suppress("UNCHECKED_CAST")
class MapAbstractAppInfoStringPreferenceSerializer :
    PreferenceSerializer<java.util.HashMap<AbstractAppInfo, String>?, java.util.Set<java.lang.String>?> {

    @Serializable
    private class MapEntry(val key: AbstractAppInfo, val value: String)

    @Throws(PreferenceSerializationException::class)
    override fun serialize(value: java.util.HashMap<AbstractAppInfo, String>?): java.util.Set<java.lang.String>? {
        return value?.map { (key, value) ->
            Json.encodeToString(MapEntry(key, value))
        }?.toHashSet() as? java.util.Set<java.lang.String>
    }

    @Throws(PreferenceSerializationException::class)
    override fun deserialize(value: java.util.Set<java.lang.String>?): java.util.HashMap<AbstractAppInfo, String>? {
        return value?.associateTo(HashMap()) {
            val entry = Json.decodeFromString<MapEntry>(it.toString())
            Pair(entry.key, entry.value)
        }
    }
}

