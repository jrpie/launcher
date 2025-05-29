+++
  title = 'Widgets'
  weight = 11
+++

# Widgets

&mu;Launcher allows to add [app widgets](https://developer.android.com/develop/ui/views/appwidgets/overview) to the home screen and to widget panels.

Widgets can be added, moved, removed, and configured in `Settings > Manage Widgets`.

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
