package de.jrpie.android.launcher.widgets

import de.jrpie.android.launcher.FilterHelper
import java.util.Locale

class WidgetProviderFilter(
    var query: String,
    var sortAlphabetical: Boolean = true
) : FilterHelper() {

    operator fun invoke(widgetProviders: List<LauncherWidgetProvider>): List<LauncherWidgetProvider> {
        val widgetProviders =
            if (sortAlphabetical) {
                widgetProviders.sortedBy { widgetProvider ->
                    (widgetProvider.label ?: "").toString().lowercase(Locale.ROOT)
                }
            } else {
                widgetProviders.sortedBy { widgetProvider ->
                    ((widgetProvider.appName ?: "").toString()
                            + (widgetProvider.label ?: "").toString())
                        .lowercase(Locale.ROOT)
                }
            }

        return if (query.isEmpty()) {
            widgetProviders
        } else {
            widgetProviders.filterByQuery(query) { widgetProvider ->
                listOf(
                    (widgetProvider.label ?: "").toString(),
                    (widgetProvider.appName ?: "").toString(),
                    (widgetProvider.description ?: "").toString()
                )
            }
        }
    }
}