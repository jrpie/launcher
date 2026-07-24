package de.jrpie.android.launcher.preferences

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupTest {

    @Test
    fun `remapUserIds rewrites user fields in nested objects`() {
        val json = JSONObject(
            """
            {
                "apps.favorites": [
                    {"type": "app", "packageName": "com.example.a", "activityName": null, "user": 10},
                    {"type": "app", "packageName": "com.example.b", "activityName": null, "user": 0}
                ],
                "action.up": {"type": "action:app", "app": {"packageName": "com.example.c", "user": 10}}
            }
            """
        )

        remapUserIds(json, mapOf(10 to 11, 0 to 0))

        val favorites = json.getJSONArray("apps.favorites")
        assertEquals(11, favorites.getJSONObject(0).getInt("user"))
        assertEquals(0, favorites.getJSONObject(1).getInt("user"))
        assertEquals(11, json.getJSONObject("action.up").getJSONObject("app").getInt("user"))
    }

    @Test
    fun `remapUserIds rewrites user fields in map entry keys`() {
        val json = JSONObject(
            """
            {
                "apps.custom_names": [
                    {"key": {"type": "app", "packageName": "com.example.a", "user": 10}, "value": "Custom"}
                ]
            }
            """
        )

        remapUserIds(json, mapOf(10 to 12))

        val entry = json.getJSONArray("apps.custom_names").getJSONObject(0)
        assertEquals(12, entry.getJSONObject("key").getInt("user"))
        assertEquals("Custom", entry.getString("value"))
    }

    @Test
    fun `remapUserIds leaves unmapped user ids unchanged`() {
        val json = JSONObject("""{"widget": {"user": 42, "id": 7}}""")

        remapUserIds(json, mapOf(10 to 11))

        assertEquals(42, json.getJSONObject("widget").getInt("user"))
        assertEquals(7, json.getJSONObject("widget").getInt("id"))
    }

    @Test
    fun `remapUserIds ignores non integer user fields`() {
        val json = JSONObject("""{"entry": {"user": "not an id"}}""")

        remapUserIds(json, mapOf(10 to 11))

        assertEquals("not an id", json.getJSONObject("entry").getString("user"))
    }

    @Test
    fun `remapUserIds does not rewrite values of fields not named user`() {
        val json = JSONObject("""{"entry": {"id": 10, "panelId": 10}}""")

        remapUserIds(json, mapOf(10 to 11))

        assertEquals(10, json.getJSONObject("entry").getInt("id"))
        assertEquals(10, json.getJSONObject("entry").getInt("panelId"))
    }

    @Test
    fun `remapUserIds descends into arrays of arrays`() {
        val json = JSONObject()
        json.put("nested", JSONArray(listOf(JSONArray(listOf(JSONObject("""{"user": 10}"""))))))

        remapUserIds(json, mapOf(10 to 11))

        assertEquals(
            11,
            json.getJSONArray("nested").getJSONArray(0).getJSONObject(0).getInt("user")
        )
    }

    @Test
    fun `migrateBackupToNewVersion rejects versions without a migration path`() {
        assertThrows(BackupVersionException::class.java) {
            migrateBackupToNewVersion(JSONObject(), UNKNOWN_PREFERENCE_VERSION)
        }
        assertThrows(BackupVersionException::class.java) {
            migrateBackupToNewVersion(JSONObject(), 100)
        }
    }
}
