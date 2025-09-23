+++
title = 'Declined Features'
+++

# Declined Features

&mu;Launcher aims to be minimal software.
This necessitates to decline certain feature suggestions (which is of course always a hard decision).
The following is a list of features suggested to me, which I ultimately decided not to implement.
Please do not request these features again.
If these are essential features for you, check out the plethora of other [FOSS launchers](/docs/alternatives/) available.


## Daily Wallpapers

This is possible using a [live wallpaper](/docs/examples/wallpapers/).

## Apps on the Home Screen

This doesn't fit &mu;Launcher's concept well.
Moreover, it is possible to achieve this using [widgets](/docs/examples/apps-on-home-screen/).

## Fancy Clock Faces

Use widgets instead.

## Protect Apps With a Password
While this might sound like a reasonable security feature at first, it is actually not something a launcher (or any non-system app for that matter) can do.
For example, one can always go to `System Settings > Apps` and launch apps from there
or just install a different home screen without any artificial restrictions.
Thus such a feature would only give a false sense of security.
If some of your apps need additional protection, use Android's [private space](https://source.android.com/docs/security/features/private-space) instead.

## Termux Action

Use [Termux:Widget](/docs/examples/wallpapers/) instead.

## Search Contacts

 * Accessing contacts is a sensitive permission. I don't want my home screen to access my contact book.
 * It feels like reinventing the wheel. There already is a contacts app. I think the launcher acting as a second contact app is out of the scope of "do one thing and do it well"
 * It would only work for the contacts of the profile where &mu;Launcher is installed. &mu;Launcher can list apps from all profiles through the LauncherApps API, but there is no way to access contacts from other profiles (for a very good reason).

See [#119](https://github.com/jrpie/launcher/issues/119).
