+++
  weight = 10
+++

# Settings

Tweaks and customizations can be made from within the settings page.
The settings can be opened by binding the Settings action to a gesture (this is especially useful when configuring &mu;Launcher for the first time) or from the settings icon in the app drawer.[^1]

[^1]: i.e., the 'All Apps', 'Favorite Apps', and 'Private Space' views.

## Appearance

### Choose a wallpaper

This triggers Android's mechanism to change the wallpaper using a photo app, file explorer, or native wallpaper setting app.
µLauncher uses the system-wide wallpaper, i.e., this change also affects other launchers.

### Color Theme

Several color themes are available.
The light them enforces a solid [background](#background-app-list-and-settings) to keep the text readable. It is mainly intended for devices with a monochrome screen.
The dynamic theme uses colors from Material You and is only available on devices supporting this feature.


**type:**&nbsp;`dropdown`
**options:**&nbsp;`Default`, `Dark`, `Light`, `Dynamic`



### Font (in-app font)

Set the font used within the app settings. This setting does not affect the date/time home screen font.

**type:**&nbsp;`dropdown`

**options:**&nbsp;`Hack`,`System default`,`Sans serif`,`Serif`,`Monospace`,`Serif monospace`

### Text Shadow

**type:**&nbsp;`toggle`

### Background (app list and settings)

Defines which background should be used in app drawers, settings, etc.
to increase legibility.
* `Transparent` does not change the wallpaper.
* `Dim` dims the wallpaper.
* `Blur` tries to blur the wallpaper. This is not possible on all devices. Some older devices don't support the operation. Also blur can be temporarily unavailable when the device is in power saving mode. In these case, `Dim` is used as a fallback.
* `Solid` sets the background to a solid color (depending on the color theme). For the light theme only this option is available.

On the home screen and widget panels, the wallpaper is always shown unmodified.

**type:**&nbsp;`dropdown`
**options:**&nbsp;`Transparent`,`Dim`,`Blur`,`Solid`

### Monochrome app icons

Remove coloring from all app icons. Can help decrease visual stimulus when enabled.

**type:**&nbsp;`toggle`

## Date & Time

These settings affect the clock shown on the home screen (or on widget panels).
If the clock is removed, the settings are not used.

### Font (home screen)

Set the home screen font for date and time. This setting does not affect the font of other components.

**type:**&nbsp;`dropdown`

**options:**&nbsp;`Hack`,`System default`,`Sans serif`,`Serif`,`Monospace`,`Serif monospace`

### Color

Set the color for the home screen date and time.

Accepts a 6-digit RGB or 8-digit ARGB color code characters.[^2]
Note that on Android, the ARGB color format is used, i.e., the alpha component is specified first.
This differs from the more common RGBA, which is used in web development.


[^2]: More precisely, everything that is valid input for [parseColor](https://developer.android.com/reference/android/graphics/Color#parseColor(java.lang.String)) can be used.


**type:**&nbsp;`ARGB`

### Use localized date format

Adapt the display of dates and times to the specific conventions of a particular locale or region as set by the system. Different locales use different date orders (e.g., MM/DD/YYYY in the US, DD/MM/YYYY in Europe).

**type:**&nbsp;`toggle`

### Show time

Show the current time on the home screen.

**type:**&nbsp;`toggle`

### Show seconds

Show the current time down to the second on the home screen.

**type:**&nbsp;`toggle`

### Show date

Show the current date on the home screen.

**type:**&nbsp;`toggle`

### Flip date and time

Place the current time above the current date on the home screen.

**type:**&nbsp;`toggle`

## Functionality

### Launch search results

Launches any app that matches the user's keyboard input when no other apps match.

As you type inside the app drawer, the app narrows down the list of apps shown based on the app title matching your text input.
With the 'launch search results' setting, once only one matching app remains, it is launched immediately.
Usually it suffices to type two or three characters the single out the desired app.

This feature becomes more powerful when combined with [renaming](#additional-settings) apps, effectively letting you define custom app names that could be considered 'aliases' or shortcuts.
For instance, if you want the keyboard input `gh` to open your `GitHub` app, you could rename `GitHub` to `GitHub gh`, `gh GitHub`, or simply `gh`.
Assuming no other installed apps have the `gh` combination of letters in them, opening the app drawer and typing `gh` would immediately launch your `GitHub` app.

Press space to temporarily disable this feature and allow text entry without prematurely launching an app. Useful when combined with the [Search the web](#search-the-web) feature.

**type:**&nbsp;`toggle`

### Search the web

Press return while searching the app list to launch a web search.

This launches an [`Intent.ACTION_WEB_SEARCH`](https://developer.android.com/reference/android/content/Intent#ACTION_WEB_SEARCH). You should be asked which app you want to use.
If you want to change the default later, go to Android Settings > Apps > select the app > Open by default.
Note that this is independent of the default browser.

You can also use an app like [&mu;Search](https://git.jrpie.de/jrpie/usearch)
to switch between multiple search engines.

{{% hint warning %}}
Note that &mu;Search is still work in progress. Currently, it is just a proof of concept.
{{% /hint %}}


**type:**&nbsp;`toggle`

### Start keyboard for search

Automatically open the keyboard when the app drawer is opened.

**type:**&nbsp;`toggle`

### Double swipe gestures

Enable double swipe (two finger) gestures in launcher settings. Does not erase gesture bindings if accidentally turned off.

**type:**&nbsp;`toggle`

### Edge swipe gestures

Enable edge swipe (near edges of screen) gestures in launcher settings. Does not erase gesture bindings if accidentally turned off.

**type:**&nbsp;`toggle`

### Edge width

Change how large a margin is used for detecting edge gestures. Shows the edge margin preview when using the slider.

**type:**&nbsp;`slider`

### Choose method for locking the screen

There are two methods to lock the screen, and unfortunately, both have downsides.

1. **`Device Admin`**

    - Doesn't work with unlocking by [fingerprint or face recognition](https://developer.android.com/reference/android/app/admin/DevicePolicyManager#lockNow()).

2. **`Accessibility Service`**

    - Requires excessive privileges.
      &mu;Launcher will use those privileges *only* for locking the screen.
      As a rule of thumb, it is [not recommended](https://android.stackexchange.com/questions/248171/is-it-safe-to-give-accessibility-permission-to-an-app)
      to grant access to accessibility services to a random app.
      Always review the [source code](https://github.com/jrpie/launcher/blob/master/app/src/main/java/de/jrpie/android/launcher/actions/lock/LauncherAccessibilityService.kt) before granting accessibility permissions so you can familiarize yourself with what the code might do.
    - On some devices, the start-up PIN will no longer be used for encrypting data after activating an accessibility service. This can be [reactivated](https://issuetracker.google.com/issues/37010136#comment36) afterwards.

   **type:**&nbsp;`text buttons`

   **options:**&nbsp;`USE DEVICE ADMIN`,`USE ACCESSIBILITY SERVICE`

{{% hint warning %}}
Due to [Accrescent's policy on accessibility services](https://accrescent.app/docs/guide/publish/requirements.html#androidaccessibilityserviceaccessibilityservice) the version of &mu;Launcher distributed via Accrescent doesn't include an accessibility service. Device admin is always used and this setting is disabled.
{{% /hint %}}

## Apps

### Hidden apps

Open an app drawer containing only hidden apps.

### Don't show apps that are bound to a gesture in the app list

Remove certain apps from the app drawer if they are already accessible via a gesture.

Reduces redundancy and tidies up the app drawer.

**type:**&nbsp;`toggle`

### Hide paused apps

Remove paused apps from the app drawer.
For example, an app belonging to the work profile is paused when the work profile is inactive.

**type:**&nbsp;`toggle`

### Hide private space from app list

Remove private space from the app drawer.
Private space apps can be accessed using a separate app drawer, which can be opened with the Private Space action.

**type:**&nbsp;`toggle`

### Layout of app list

Changes how the apps are displayed when accessing the app drawer.

- `Default`: All apps in the drawer will show in a vertically-scrolled list with their app icon and title.
- `Text`: Removes the app icons, shows only app titles in the drawer as a vertically-scrolled list.
    Work profile and private space apps are distinguished by a different label instead of a badge.
- `Grid`: Shows apps with their app icon and title in a grid layout.

**type:**&nbsp;`dropdown`

**options:**&nbsp;`Default`,`Text`,`Grid`

### Reverse the app list

Enable reverse alphabetical sorting of apps in the app drawer.
Useful for keeping apps within easier reach from the keyboard.

**type:**&nbsp;`toggle`

## Display

### Rotate screen

**type:**&nbsp;`toggle`

### Keep screen on

**type:**&nbsp;`toggle`

### Hide status bar

Remove the top status bar from the home screen.

**type:**&nbsp;`toggle`

### Hide navigation bar

Remove the navigation bar from the home screen. Enabling this setting may make it difficult to use the device if gestures are not set up properly.

**type:**&nbsp;`toggle`
