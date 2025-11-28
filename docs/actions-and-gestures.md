+++
  weight = 10
+++

# Actions and Gestures

µLauncher's central mechanism for accessing important functionality quickly
is to bind actions (e.g., launching an app) to gestures (e.g., swiping up).
These bindings can be configured in µLauncher Settings > ACTIONS.


## Available Gestures

### Swipes

- Basic swipes: Swipe up, down, left, or right
- Double swipes: Swipe up, down, left, or right with two fingers
- Edge swipes:
  - Swipe up or down on the left or right edge
  - Swipe left or right on the top or bottom edge

  The size of the edges is configurable in settings.
  For a swipe to be detected as an edge swipe, the finger must not leave the respective edge region while swiping.

### Taps

- Tap on date or time
- Double tap
- Long click

### Tap-then-Swipes

- Tap then swipe up, down, left, or right

    To execute these gestures consistently, it is helpful to think of them as double taps,
    where the finger stays on the screen after the second tap and then does a swipe.
    The swipe must start very shortly after the tap ended.

### Complex Gestures

- Draw <, >, V, or Λ
- Draw <, >, V, or Λ in reverse direction

### Hardware Buttons as Gestures

- Back button (or back gesture if gesture navigation is enabled)
- Volume buttons

***

## Available Actions

To any of the available gestures, one of the following actions can be bound:

- Launch an app (or a pinned shortcut)
- Open a widget panel.
    Widget panels can hold widgets that are not needed on the home screen itself.
    They can be created and managed in µLauncher Settings > Manage Widget Panels
- Open a list of all, favorite, or private apps (hidden apps are excluded).
    Actions related to private space are only shown if private space is set up on the device.
    µLauncher's settings can be accessed from those lists.
    If private space is set up, an icon to (un)lock it is shown on the top right.
- Open µLauncher's settings
- Toggle private space lock
- Lock the screen: This allows you to lock the screen.
    There are two mechanisms by which the screen can be locked,
    [accessibility service and device admin](/docs/settings#choose-method-for-locking-the-screen).
    Note that device admin doesn't work with unlocking by fingerprint or face recognition.
- Toggle the flashlight
- Raise, lower, or adjust volume
- Play or pause media playback
- Skip to previous or next audio track
- Open notifications panel: Might be useful if the top of your screen is broken.
- Open quick settings panel: Why swipe down twice?
- Open [recent apps](https://developer.android.com/guide/components/activities/recents): Requires accessibility service. It can be used as a workaround for an Android bug.
- Launch another home screen: Allows using another installed home screen temporarily.
- Do nothing: Simply prevents showing the message that no action is bound to this gesture.
