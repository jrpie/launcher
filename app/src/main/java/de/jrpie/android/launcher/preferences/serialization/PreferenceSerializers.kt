@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package de.jrpie.android.launcher.preferences.serialization

import de.jrpie.android.launcher.apps.AbstractAppInfo
import de.jrpie.android.launcher.apps.PinnedShortcutInfo
import de.jrpie.android.launcher.widgets.Widget
import eu.jonahbauer.android.preference.annotations.serializer.PreferenceSerializationException
import eu.jonahbauer.android.preference.annotations.serializer.PreferenceSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json



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

