@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")

package de.jrpie.android.launcher.preferences.serialization

import de.jrpie.android.launcher.apps.AbstractAppInfo
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.apps.PinnedShortcutInfo
import de.jrpie.android.launcher.widgets.AppWidget
import de.jrpie.android.launcher.widgets.BrokenWidget
import de.jrpie.android.launcher.widgets.ClockWidget
import de.jrpie.android.launcher.widgets.Widget
import de.jrpie.android.launcher.widgets.WidgetPanel
import de.jrpie.android.launcher.widgets.WidgetPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportSerializersTest {

    @Test
    fun `app info sets export as json objects and round trip`() {
        val serializer = SetAbstractAppInfoExportSerializer()
        val set = hashSetOf<AbstractAppInfo>(
            AppInfo("com.example.app", "com.example.app.MainActivity", 0),
            PinnedShortcutInfo("id1", "com.example.shortcut", "com.example.shortcut.Activity", 10)
        )

        val json = serializer.export(set as java.util.Set<AbstractAppInfo>)!!

        assertEquals(2, json.length())
        // real nested JSON, not strings containing JSON
        (0 until json.length()).forEach { i -> json.getJSONObject(i) }

        assertEquals(set, serializer.restore(json))
    }

    @Test
    fun `custom name map exports keys as json objects and round trips`() {
        val serializer = MapAbstractAppInfoStringExportSerializer()
        val map = hashMapOf<AbstractAppInfo, String>(
            AppInfo("com.example.app", null, 0) as AbstractAppInfo to "Custom Name"
        )

        val json = serializer.export(map)!!

        assertEquals(1, json.length())
        assertEquals("Custom Name", json.getJSONObject(0).getString("value"))
        assertEquals("com.example.app", json.getJSONObject(0).getJSONObject("key").getString("packageName"))

        assertEquals(map, serializer.restore(json))
    }

    @Test
    fun `app widgets are exported as broken widgets`() {
        val serializer = SetWidgetExportSerializer()
        val set = hashSetOf<Widget>(
            AppWidget(
                42, WidgetPosition(1, 2, 3, 4), WidgetPanel.HOME.id, false,
                "com.example.app", "com.example.app.WidgetProvider", 0
            ),
            ClockWidget(-5, WidgetPosition(0, 0, 10, 4), WidgetPanel.HOME.id)
        )

        val json = serializer.export(set as java.util.Set<Widget>)!!
        val types = (0 until json.length()).map { json.getJSONObject(it).getString("type") }

        assertTrue("AppWidget must be exported as BrokenWidget", "widget:broken" in types)
        assertTrue("internal widgets must be exported unchanged", "widget:clock" in types)

        val restored = serializer.restore(json)!!
        val broken = restored.filterIsInstance<BrokenWidget>().single()
        assertEquals(42, broken.id)
        assertEquals("com.example.app", broken.packageName)
        assertEquals("com.example.app.WidgetProvider", broken.className)
        assertEquals(0, broken.user)
        assertTrue(restored.filterIsInstance<ClockWidget>().single().id == -5)
    }

    @Test
    fun `widget panels round trip`() {
        val serializer = SetWidgetPanelExportSerializer()
        val set = hashSetOf(WidgetPanel(3, "My Panel"))

        val json = serializer.export(set as java.util.Set<WidgetPanel>)!!

        assertEquals("My Panel", json.getJSONObject(0).getString("label"))
        assertEquals(set, serializer.restore(json))
    }

    @Test
    fun `export serializers pass null through`() {
        assertNull(SetAbstractAppInfoExportSerializer().export(null))
        assertNull(SetAbstractAppInfoExportSerializer().restore(null))
        assertNull(MapAbstractAppInfoStringExportSerializer().export(null))
        assertNull(MapAbstractAppInfoStringExportSerializer().restore(null))
        assertNull(SetWidgetExportSerializer().export(null))
        assertNull(SetWidgetExportSerializer().restore(null))
        assertNull(SetWidgetPanelExportSerializer().export(null))
        assertNull(SetWidgetPanelExportSerializer().restore(null))
        assertNull(SetPinnedShortcutInfoExportSerializer().export(null))
        assertNull(SetPinnedShortcutInfoExportSerializer().restore(null))
    }

    @Test
    fun `pinned shortcut sets round trip`() {
        val serializer = SetPinnedShortcutInfoExportSerializer()
        val set = hashSetOf(
            PinnedShortcutInfo("shortcut1", "com.example.app", "com.example.app.Activity", 10)
        )

        val json = serializer.export(set as java.util.Set<PinnedShortcutInfo>)!!

        assertEquals("shortcut1", json.getJSONObject(0).getString("id"))
        assertEquals(set, serializer.restore(json))
    }
}
