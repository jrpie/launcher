+++
  title = 'Widgets'
  weight = 11
+++

# Widgets

&mu;Launcher allows to add [app widgets](https://developer.android.com/develop/ui/views/appwidgets/overview) to the home screen and to widget panels.

Widgets can be added, moved, removed, and configured in `Settings > Manage Widgets`.

When adding a widget, the list of available widgets can be searched
(matching the widget's name, description, and app name)
and sorted alphabetically using the button in the top left corner.

Widgets of apps from the [work profile](/docs/profiles/) are only available
if the app managing the work profile explicitly allows them
(using [`addCrossProfileWidgetProvider`](https://developer.android.com/reference/android/app/admin/DevicePolicyManager#addCrossProfileWidgetProvider(android.content.ComponentName,%20java.lang.String))).
This is an Android restriction that applies to all launchers.

It is configurable whether or not interaction with a widget should be enabled.

* If interaction is enabled, touch events are forwarded to the widget as usual.
However, &mu;Launcher [gestures](/docs/actions-and-gestures/) can not be executed in areas where such a widget is present.

* If interaction is disabled, the widget does not respond to any touch events.
    This is recommended when using a widget only to display information.

&mu;Launcher's clock behaves similarly to an app widget and can be managed in the same way.[^1]

[^1]: However, it is technically not an app widget and cannot be used with other launchers.

# Widget Panels

Widget panels can contain widgets that are not needed on the home screen.
They can be managed in `Settings > Manage Widget Panels`.
Widget panels can be opened by using the [Open Widget Panel](/docs/actions-and-gestures/#available-actions) action.
