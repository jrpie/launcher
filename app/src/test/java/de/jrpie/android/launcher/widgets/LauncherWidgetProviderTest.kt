package de.jrpie.android.launcher.widgets

import android.appwidget.AppWidgetProviderInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherWidgetProviderTest {

    companion object {
        private const val ALPHA = "Alpha"
        private const val ANALOG_CLOCK = "Analog Clock"
    }

    private fun provider(
        label: String?,
        appName: String? = null,
        description: String? = null
    ) = LauncherAppWidgetProvider(
        AppWidgetProviderInfo(),
        label,
        description,
        appName,
        null,
        null
    )

    @Test
    fun `matchesSearch matches label case-insensitively`() {
        val widget = provider(label = ANALOG_CLOCK)
        assertTrue(widget.matchesSearch("analog"))
        assertTrue(widget.matchesSearch("CLOCK"))
        assertFalse(widget.matchesSearch("calendar"))
    }

    @Test
    fun `matchesSearch matches app name and description`() {
        val widget = provider(
            label = "Inbox",
            appName = "Fairmail",
            description = "Shows unread mail"
        )
        assertTrue(widget.matchesSearch("fairmail"))
        assertTrue(widget.matchesSearch("unread"))
        assertFalse(widget.matchesSearch("calendar"))
    }

    @Test
    fun `matchesSearch handles null fields`() {
        val widget = provider(label = null, appName = null, description = null)
        assertFalse(widget.matchesSearch("anything"))
    }

    @Test
    fun `filterAndSort with empty query keeps original order`() {
        val widgets = listOf(provider("Zulu"), provider(ALPHA), provider("Mike"))
        assertEquals(widgets, widgets.filterAndSort("", sortAlphabetical = false))
    }

    @Test
    fun `filterAndSort sorts alphabetically ignoring case`() {
        val widgets = listOf(provider("zulu"), provider(ALPHA), provider("mike"))
        assertEquals(
            listOf(ALPHA, "mike", "zulu"),
            widgets.filterAndSort("", sortAlphabetical = true).map { it.label }
        )
    }

    @Test
    fun `filterAndSort sorts widgets without label first`() {
        val widgets = listOf(provider(ALPHA), provider(null))
        assertEquals(
            listOf(null, ALPHA),
            widgets.filterAndSort("", sortAlphabetical = true).map { it.label }
        )
    }

    @Test
    fun `filterAndSort filters and sorts combined`() {
        val widgets = listOf(
            provider("Digital Clock", appName = "Clock"),
            provider("Calendar"),
            provider(ANALOG_CLOCK, appName = "Clock")
        )
        assertEquals(
            listOf(ANALOG_CLOCK, "Digital Clock"),
            widgets.filterAndSort("clock", sortAlphabetical = true).map { it.label }
        )
    }
}
