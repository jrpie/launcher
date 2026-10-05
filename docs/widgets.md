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

Note that widgets of apps from the [work profile](/docs/profiles/) are only available
if the app managing the work profile explicitly allows this.[^cross-profile-widgets]
For example, when using shelter, this needs to be enabled per app.

Since widgets on the home screen might interfere with gestures,
it is configurable whether interaction with a widget should be possible.

* If interaction is disabled (default),
    the widget does not respond to any touch events.
    This is recommended when using a widget that only displays information.

* If interaction is enabled, touch events are forwarded to the widget as usual.
    However, &mu;Launcher's [gestures](/docs/actions-and-gestures/) can not be executed in areas where such a widget is present.

&mu;Launcher's clock behaves similarly to an app widget and can be managed in the same way.[^widget-clock]

[^widget-clock]: However, it is technically not an app widget and cannot be used with other launchers.
[^cross-profile-widgets]: Using [`addCrossProfileWidgetProvider`](https://developer.android.com/reference/android/app/admin/DevicePolicyManager#addCrossProfileWidgetProvider(android.content.ComponentName,%20java.lang.String))

# Widget Panels

Widget panels can contain widgets that are not needed on the home screen.
They can be managed in `Settings > Manage Widget Panels`.
Widget panels can be opened by using the [Open Widget Panel](/docs/actions-and-gestures/#available-actions) action.
