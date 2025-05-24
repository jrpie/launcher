package de.jrpie.android.launcher.preferences

import android.content.Context
import de.jrpie.android.launcher.actions.Action
import de.jrpie.android.launcher.actions.Gesture
import de.jrpie.android.launcher.actions.ShortcutAction
import de.jrpie.android.launcher.actions.lock.LockMethod
import de.jrpie.android.launcher.apps.AbstractAppInfo
import de.jrpie.android.launcher.apps.PinnedShortcutInfo
import de.jrpie.android.launcher.preferences.theme.Background
import de.jrpie.android.launcher.preferences.theme.ColorTheme
import de.jrpie.android.launcher.preferences.theme.Font
import de.jrpie.android.launcher.widgets.AppWidget
import de.jrpie.android.launcher.widgets.BrokenAppWidget
import de.jrpie.android.launcher.widgets.Widget
import de.jrpie.android.launcher.widgets.WidgetPanel
import de.jrpie.android.launcher.widgets.generateInternalId
import de.jrpie.android.launcher.widgets.updateWidget
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("launcher:config:apps:v100")
data class ExportedAppConfig(
    val favorites: Set<AbstractAppInfo>?,
    val hidden: Set<AbstractAppInfo>?,
    val pinnedShortcuts: Set<PinnedShortcutInfo>?,
    val customNames: HashMap<AbstractAppInfo, String>?,
    val hideBoundApps: Boolean,
    val hidePausedApps: Boolean,
    val hidePrivateSpaceApps: Boolean
) {
    fun restore() {
        LauncherPreferences.apps().favorites(favorites)
        LauncherPreferences.apps().hidden(hidden)
        LauncherPreferences.apps().pinnedShortcuts(pinnedShortcuts)
        LauncherPreferences.apps().customNames(customNames)
        LauncherPreferences.apps().hideBoundApps(hideBoundApps)
        LauncherPreferences.apps().hidePausedApps(hidePausedApps)
        LauncherPreferences.apps().hidePrivateSpaceApps(hidePrivateSpaceApps)
    }

    companion object {
        fun export(): ExportedAppConfig {
            return ExportedAppConfig(
                LauncherPreferences.apps().favorites(),
                LauncherPreferences.apps().hidden(),
                LauncherPreferences.apps().pinnedShortcuts(),
                LauncherPreferences.apps().customNames(),
                LauncherPreferences.apps().hideBoundApps(),
                LauncherPreferences.apps().hidePausedApps(),
                LauncherPreferences.apps().hidePausedApps(),
            )
        }
    }
}

@Serializable
@SerialName("launcher:config:list:v100")
data class ExportedListConfig(
    val layout: ListLayout,
    val reverseLayout: Boolean
) {
    fun restore() {
        LauncherPreferences.list().layout(layout)
        LauncherPreferences.list().reverseLayout(reverseLayout)
    }

    companion object {
        fun export(): ExportedListConfig {
            return ExportedListConfig(
                LauncherPreferences.list().layout(),
                LauncherPreferences.list().reverseLayout()
            )
        }
    }
}

@Serializable
@SerialName("launcher:config:theme:v100")
data class ExportedThemeConfig(
    val colorTheme: ColorTheme,
    val background: Background,
    val font: Font,
    val textShadow: Boolean,
    val monochromeIcons: Boolean
) {
    fun restore() {
        LauncherPreferences.theme().colorTheme(colorTheme)
        LauncherPreferences.theme().background(background)
        LauncherPreferences.theme().font(font)
        LauncherPreferences.theme().textShadow(textShadow)
        LauncherPreferences.theme().monochromeIcons(monochromeIcons)
    }

    companion object {
        fun export(): ExportedThemeConfig {
            return ExportedThemeConfig(
                LauncherPreferences.theme().colorTheme(),
                LauncherPreferences.theme().background(),
                LauncherPreferences.theme().font(),
                LauncherPreferences.theme().textShadow(),
                LauncherPreferences.theme().monochromeIcons()
            )
        }
    }
}

@Serializable
@SerialName("launcher:config:clock:v100")
data class ExportedClockConfig(
    val font: Font,
    val color: Int,
    val dateVisible: Boolean,
    val timeVisible: Boolean,
    val flipDateTime: Boolean,
    val localized: Boolean,
    val showSeconds: Boolean
) {
    fun restore() {
        LauncherPreferences.clock().font(font)
        LauncherPreferences.clock().color(color)
        LauncherPreferences.clock().dateVisible(dateVisible)
        LauncherPreferences.clock().timeVisible(timeVisible)
        LauncherPreferences.clock().flipDateTime(flipDateTime)
        LauncherPreferences.clock().localized(localized)
        LauncherPreferences.clock().showSeconds(showSeconds)
    }

    companion object {
        fun export(): ExportedClockConfig {
            return ExportedClockConfig(
                LauncherPreferences.clock().font(),
                LauncherPreferences.clock().color(),
                LauncherPreferences.clock().dateVisible(),
                LauncherPreferences.clock().timeVisible(),
                LauncherPreferences.clock().flipDateTime(),
                LauncherPreferences.clock().localized(),
                LauncherPreferences.clock().showSeconds()
            )
        }
    }
}

@Serializable
@SerialName("launcher:config:display:v100")
data class ExportedDisplayConfig(
    val screenTimeoutDisabled: Boolean,
    val hideStatusBar: Boolean,
    val hideNavigationBar: Boolean,
    val rotateScreen: Boolean
) {
    fun restore() {
        LauncherPreferences.display().screenTimeoutDisabled(screenTimeoutDisabled)
        LauncherPreferences.display().hideStatusBar(hideStatusBar)
        LauncherPreferences.display().hideNavigationBar(hideNavigationBar)
        LauncherPreferences.display().rotateScreen(rotateScreen)
    }

    companion object {
        fun export(): ExportedDisplayConfig {
            return ExportedDisplayConfig(
                LauncherPreferences.display().screenTimeoutDisabled(),
                LauncherPreferences.display().hideStatusBar(),
                LauncherPreferences.display().hideNavigationBar(),
                LauncherPreferences.display().rotateScreen()
            )
        }
    }
}

@Serializable
@SerialName("launcher:config:functionality:v100")
data class ExportedFunctionalityConfig(
    val searchAutoLaunch: Boolean,
    val searchWeb: Boolean,
    val searchAutoOpenKeyboard: Boolean,
    val searchAutoCloseKeyboard: Boolean
) {
    fun restore() {
        LauncherPreferences.functionality().searchAutoLaunch(searchAutoLaunch)
        LauncherPreferences.functionality().searchWeb(searchWeb)
        LauncherPreferences.functionality().searchAutoOpenKeyboard(searchAutoOpenKeyboard)
        LauncherPreferences.functionality().searchAutoCloseKeyboard(searchAutoCloseKeyboard)
    }

    companion object {
        fun export(): ExportedFunctionalityConfig {
            return ExportedFunctionalityConfig(
                LauncherPreferences.functionality().searchAutoLaunch(),
                LauncherPreferences.functionality().searchWeb(),
                LauncherPreferences.functionality().searchAutoOpenKeyboard(),
                LauncherPreferences.functionality().searchAutoCloseKeyboard()
            )
        }
    }
}

@Serializable
@SerialName("launcher:config:enabledgestures:v100")
data class ExportedEnabledGesturesConfig(
    val doubleSwipe: Boolean,
    val edgeSwipe: Boolean,
    val edgeSwipeEdgeWidth: Int
) {
    fun restore() {
        LauncherPreferences.enabled_gestures().doubleSwipe(doubleSwipe)
        LauncherPreferences.enabled_gestures().edgeSwipe(edgeSwipe)
        LauncherPreferences.enabled_gestures().edgeSwipeEdgeWidth(edgeSwipeEdgeWidth)
    }

    companion object {
        fun export(): ExportedEnabledGesturesConfig {
            return ExportedEnabledGesturesConfig(
                LauncherPreferences.enabled_gestures().doubleSwipe(),
                LauncherPreferences.enabled_gestures().edgeSwipe(),
                LauncherPreferences.enabled_gestures().edgeSwipeEdgeWidth()
            )
        }
    }
}

@Serializable
@SerialName("launcher:config:actions:v100")
data class ExportedActionsConfig(
    val lockMethod: LockMethod
) {
    fun restore() {
        LauncherPreferences.actions().lockMethod(lockMethod)
    }

    companion object {
        fun export(): ExportedActionsConfig {
            return ExportedActionsConfig(
                LauncherPreferences.actions().lockMethod()
            )
        }
    }
}

@Serializable
@SerialName("launcher:config:widgets:v100")
data class ExportedWidgetsConfig(
    val widgets: Set<Widget>?,
    val customPanels: Set<WidgetPanel>?
) {
    fun restore() {
        LauncherPreferences.widgets().customPanels(customPanels)
        widgets?.forEach {
            it.id = generateInternalId()
            updateWidget(it)
        }
    }

    companion object {
        fun export(): ExportedWidgetsConfig {
            return ExportedWidgetsConfig(
                (LauncherPreferences.widgets().widgets() ?: setOf()).map { w ->
                    if (w is AppWidget) {
                        return@map BrokenAppWidget(w)
                    }
                    w
                }.toSet(),
                LauncherPreferences.widgets().customPanels()
            )
        }
    }
}

@Serializable
@SerialName("launcher:config:v100")
class ExportedConfig(
    private val actions: Map<Gesture, Action>,
    private val appConfig: ExportedAppConfig,
    private val listConfig: ExportedListConfig,
    private val themeConfig: ExportedThemeConfig,
    private val clockConfig: ExportedClockConfig,
    private val displayConfig: ExportedDisplayConfig,
    private val functionalityConfig: ExportedFunctionalityConfig,
    private val enabledGesturesConfig: ExportedEnabledGesturesConfig,
    private val actionsConfig: ExportedActionsConfig,
    private val widgetsConfig: ExportedWidgetsConfig
) {
    fun restore(context: Context) {
        resetPreferences(context)
        actions.entries.forEach { entry: Map.Entry<Gesture, Action> ->
            Action.setActionForGesture(entry.key, entry.value)
        }
        appConfig.restore()
        listConfig.restore()
        themeConfig.restore()
        clockConfig.restore()
        displayConfig.restore()
        functionalityConfig.restore()
        enabledGesturesConfig.restore()
        actionsConfig.restore()
        widgetsConfig.restore()
    }


    companion object {
        fun exportConfig(): ExportedConfig {
            return ExportedConfig(
                Gesture.entries.map { g ->
                    val action = Action.forGesture(g) ?: return@map null
                    if (action is ShortcutAction) {
                        return@map null
                    }
                    Pair(g, action)
                }.filterNotNull().toMap(),
                ExportedAppConfig.export(),
                ExportedListConfig.export(),
                ExportedThemeConfig.export(),
                ExportedClockConfig.export(),
                ExportedDisplayConfig.export(),
                ExportedFunctionalityConfig.export(),
                ExportedEnabledGesturesConfig.export(),
                ExportedActionsConfig.export(),
                ExportedWidgetsConfig.export()
            )
        }
    }
}