package de.jrpie.android.launcher.preferences;

import java.util.HashMap;
import java.util.Set;

import de.jrpie.android.launcher.R;
import de.jrpie.android.launcher.actions.lock.LockMethod;
import de.jrpie.android.launcher.preferences.list.AppNameFormat;
import de.jrpie.android.launcher.preferences.list.ListLayout;
import de.jrpie.android.launcher.preferences.serialization.MapAbstractAppInfoStringExportSerializer;
import de.jrpie.android.launcher.preferences.serialization.MapAbstractAppInfoStringPreferenceSerializer;
import de.jrpie.android.launcher.preferences.serialization.SetAbstractAppInfoExportSerializer;
import de.jrpie.android.launcher.preferences.serialization.SetAbstractAppInfoPreferenceSerializer;
import de.jrpie.android.launcher.preferences.serialization.SetPinnedShortcutInfoExportSerializer;
import de.jrpie.android.launcher.preferences.serialization.SetPinnedShortcutInfoPreferenceSerializer;
import de.jrpie.android.launcher.preferences.serialization.SetWidgetExportSerializer;
import de.jrpie.android.launcher.preferences.serialization.SetWidgetPanelExportSerializer;
import de.jrpie.android.launcher.preferences.serialization.SetWidgetPanelSerializer;
import de.jrpie.android.launcher.preferences.serialization.SetWidgetSerializer;
import de.jrpie.android.launcher.preferences.theme.Background;
import de.jrpie.android.launcher.preferences.theme.ColorTheme;
import de.jrpie.android.launcher.preferences.theme.Font;
import eu.jonahbauer.android.preference.annotations.Preference;
import eu.jonahbauer.android.preference.annotations.PreferenceGroup;
import eu.jonahbauer.android.preference.annotations.Preferences;

@Preferences(
        name = "de.jrpie.android.launcher.preferences.LauncherPreferences",
        makeFile = true,
        r = R.class,
        value = {
                @PreferenceGroup(name = "internal", prefix = "settings_internal_", suffix = "_key", value = {
                        // set after the user finished the tutorial
                        @Preference(name = "started", type = boolean.class, defaultValue = "false"),
                        @Preference(name = "started_time", type = long.class),
                        // see PREFERENCE_VERSION in de.jrpie.android.launcher.preferences.Preferences.kt
                        @Preference(name = "version_code", type = int.class, defaultValue = "-1"),
                }),
                @PreferenceGroup(name = "apps", prefix = "settings_apps_", suffix = "_key", value = {
                        @Preference(name = "favorites", type = Set.class, serializer = SetAbstractAppInfoPreferenceSerializer.class,
                                exportSerializer = SetAbstractAppInfoExportSerializer.class, export = true),
                        @Preference(name = "hidden", type = Set.class, serializer = SetAbstractAppInfoPreferenceSerializer.class,
                                exportSerializer = SetAbstractAppInfoExportSerializer.class, export = true),
                        @Preference(name = "pinned_shortcuts", type = Set.class, serializer = SetPinnedShortcutInfoPreferenceSerializer.class,
                                exportSerializer = SetPinnedShortcutInfoExportSerializer.class, export = true),
                        @Preference(name = "custom_names", type = HashMap.class, serializer = MapAbstractAppInfoStringPreferenceSerializer.class,
                                exportSerializer = MapAbstractAppInfoStringExportSerializer.class, export = true),
                        @Preference(name = "hide_bound_apps", type = boolean.class, defaultValue = "false", export = true),
                        @Preference(name = "hide_paused_apps", type = boolean.class, defaultValue = "false", export = true),
                        @Preference(name = "hide_private_space_apps", type = boolean.class, defaultValue = "false", export = true),
                }),
                @PreferenceGroup(name = "list", prefix = "settings_list_", suffix = "_key", value = {
                        @Preference(name = "layout", type = ListLayout.class, defaultValue = "DEFAULT", export = true),
                        @Preference(name = "reverse_layout", type = boolean.class, defaultValue = "false", export = true),
                        @Preference(name = "app_name_format", type = AppNameFormat.class, defaultValue = "DEFAULT", export = true)
                }),
                @PreferenceGroup(name = "gestures", prefix = "settings_gesture_", suffix = "_key", value = {
                }),
                @PreferenceGroup(name = "general", prefix = "settings_general_", suffix = "_key", value = {
                        @Preference(name = "choose_home_screen", type = void.class)
                }),
                @PreferenceGroup(name = "theme", prefix = "settings_theme_", suffix = "_key", value = {
                        @Preference(name = "wallpaper", type = void.class),
                        @Preference(name = "color_theme", type = ColorTheme.class, defaultValue = "DEFAULT", export = true),
                        @Preference(name = "background", type = Background.class, defaultValue = "DIM", export = true),
                        @Preference(name = "font", type = Font.class, defaultValue = "HACK", export = true),
                        @Preference(name = "text_shadow", type = boolean.class, defaultValue = "false", export = true),
                        @Preference(name = "monochrome_icons", type = boolean.class, defaultValue = "false", export = true),
                        @Preference(name = "animations", type = boolean.class, defaultValue = "true", export = true),
                }),
                @PreferenceGroup(name = "clock", prefix = "settings_clock_", suffix = "_key", value = {
                        @Preference(name = "font", type = Font.class, defaultValue = "HACK", export = true),
                        @Preference(name = "color", type = int.class, defaultValue = "0xffffffff", export = true),
                        @Preference(name = "date_visible", type = boolean.class, defaultValue = "true", export = true),
                        @Preference(name = "time_visible", type = boolean.class, defaultValue = "true", export = true),
                        @Preference(name = "flip_date_time", type = boolean.class, defaultValue = "false", export = true),
                        @Preference(name = "localized", type = boolean.class, defaultValue = "false", export = true),
                        @Preference(name = "show_seconds", type = boolean.class, defaultValue = "true", export = true),
                        @Preference(name = "font_size", type = int.class, defaultValue = "30", export = true)
                }),
                @PreferenceGroup(name = "display", prefix = "settings_display_", suffix = "_key", value = {
                        @Preference(name = "screen_timeout_disabled", type = boolean.class, defaultValue = "false", export = true),
                        @Preference(name = "hide_status_bar", type = boolean.class, defaultValue = "true", export = true),
                        @Preference(name = "hide_navigation_bar", type = boolean.class, defaultValue = "false", export = true),
                        @Preference(name = "rotate_screen", type = boolean.class, defaultValue = "true", export = true),
                }),
                @PreferenceGroup(name = "functionality", prefix = "settings_functionality_", suffix = "_key", value = {
                        @Preference(name = "search_auto_launch", type = boolean.class, defaultValue = "true", export = true),
                        @Preference(name = "search_web", type = boolean.class, description = "false", export = true),
                        @Preference(name = "search_auto_open_keyboard", type = boolean.class, defaultValue = "true", export = true),
                        @Preference(name = "search_auto_close_keyboard", type = boolean.class, defaultValue = "false", export = true),
                }),
                @PreferenceGroup(name = "enabled_gestures", prefix = "settings_enabled_gestures_", suffix = "_key", value = {
                        @Preference(name = "double_swipe", type = boolean.class, defaultValue = "true", export = true),
                        @Preference(name = "edge_swipe", type = boolean.class, defaultValue = "true", export = true),
                        @Preference(name = "edge_swipe_edge_width", type = int.class, defaultValue = "15", export = true),
                        @Preference(name = "diagonal_swipe", type = boolean.class, defaultValue = "false", export = true),
                }),
                @PreferenceGroup(name = "actions", prefix = "settings_actions_", suffix = "_key", value = {
                        @Preference(name = "lock_method", type = LockMethod.class, defaultValue = "DEVICE_ADMIN", export = true),
                }),
                @PreferenceGroup(name = "widgets", prefix = "settings_widgets_", suffix = "_key", value = {
                        @Preference(name = "widgets", type = Set.class, serializer = SetWidgetSerializer.class,
                                exportSerializer = SetWidgetExportSerializer.class, export = true),
                        @Preference(name = "custom_panels", type = Set.class, serializer = SetWidgetPanelSerializer.class,
                                exportSerializer = SetWidgetPanelExportSerializer.class, export = true)
                }),
        })
public final class LauncherPreferences$Config {
}
