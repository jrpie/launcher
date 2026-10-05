package de.jrpie.android.launcher.apps

import android.content.Context
import de.jrpie.android.launcher.FilterHelper
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.AppAction
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.actions.ShortcutAction
import de.jrpie.android.launcher.preferences.LauncherPreferences
import java.util.Locale

class AppFilter(
    var context: Context,
    var query: String,
    var favoritesVisibility: AppSetVisibility = AppSetVisibility.VISIBLE,
    var hiddenVisibility: AppSetVisibility = AppSetVisibility.HIDDEN,
    var privateSpaceVisibility: AppSetVisibility = AppSetVisibility.VISIBLE
): FilterHelper() {

    operator fun invoke(apps: List<AbstractDetailedAppInfo>): List<AbstractDetailedAppInfo> {
        var apps =
            apps.sortedBy { app -> app.getCustomLabel(context).lowercase(Locale.ROOT) }

        val hidden = LauncherPreferences.apps().hidden() ?: setOf()
        val favorites = LauncherPreferences.apps().favorites() ?: setOf()
        val private = apps.filter { it.isPrivate() }
            .map { it.getRawInfo() }.toSet()

        apps = apps.filter { info ->
            favoritesVisibility.predicate(favorites, info)
                    && hiddenVisibility.predicate(hidden, info)
                    && privateSpaceVisibility.predicate(private, info)
        }

        if (LauncherPreferences.apps().hideBoundApps()) {
            val boundApps = Gesture.entries
                .filter(Gesture::isEnabled)
                .mapNotNull { g -> Action.forGesture(g) }
                .mapNotNull {
                    (it as? AppAction)?.app
                        ?: (it as? ShortcutAction)?.shortcut
                }
                .toSet()
            apps = apps.filterNot { info -> boundApps.contains(info.getRawInfo()) }
        }

        return if (query.isEmpty()) {
            apps
        } else {
            apps.filterByQuery(query) { app ->
                listOf(app.getCustomLabel(context))
            }
        }
    }

    companion object {
        enum class AppSetVisibility(
            val predicate: (set: Set<AbstractAppInfo>, AbstractDetailedAppInfo) -> Boolean
        ) {
            VISIBLE({ _, _ -> true }),
            HIDDEN({ set, appInfo -> !set.contains(appInfo.getRawInfo()) }),
            EXCLUSIVE({ set, appInfo -> set.contains(appInfo.getRawInfo()) }),
            ;
        }
    }
}