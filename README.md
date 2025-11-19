<!-- Shields from shields.io -->
[![][shield-release]][latest-release]
[![Android CI](https://github.com/jrpie/Launcher/actions/workflows/android.yml/badge.svg)](https://github.com/jrpie/Launcher/actions/workflows/android.yml)
[![][shield-license]][license]
[![Chat on Matrix](https://matrix.to/img/matrix-badge.svg)][matrix]
[![Chat on Discord](https://img.shields.io/badge/discord-join%20chat-007ec6.svg?style=flat)][discord]




# μLauncher


µLauncher is an Android home screen that lets you launch apps using swipe gestures and button presses.
It is *minimal, efficient and free of distraction*.


<a href="https://f-droid.org/packages/de.jrpie.android.launcher/"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80"></a>
<a href="https://accrescent.app/app/de.jrpie.android.launcher.accrescent"><img alt="Get it on Accrescent" src="https://accrescent.app/badges/get-it-on.png" height="80"></a>
<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://app/{%22id%22:%22de.jrpie.android.launcher%22,%22url%22:%22https://github.com/jrpie/Launcher%22,%22author%22:%22jrpie%22,%22name%22:%22%c2%b5Launcher%22,%22additionalSettings%22:%22{\%22apkFilterRegEx\%22:\%22release\%22,\%22invertAPKFilter\%22:false,\%22about\%22:\%22%c2%b5Launcher%20is%20a%20minimal%20home%20screen.\%22}%22}"><img src="https://raw.githubusercontent.com/ImranR98/Obtainium/b1c8ac6f2ab08497189721a788a5763e28ff64cd/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="80"></a>
<a href="https://github.com/jrpie/launcher/releases/latest"><img src="https://raw.githubusercontent.com/NeoApplications/Neo-Backup/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" alt="Get it on GitHub" height="80"></a>

You can also [get it on Google Play](https://play.google.com/store/apps/details?id=de.jrpie.android.launcher), but I don't recommend that.

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg"
     alt="screenshot"
     height="400">
     <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg"
     alt="screenshot"
     height="400">
     <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.jpg"
     alt="screenshot"
     height="400">
     <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg"
     alt="screenshot"
     height="400">
     <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.jpg"
     alt="screenshot"
     height="400">
     <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/7.jpg"
     alt="screenshot"
     height="400">
     <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/8.jpg"
     alt="screenshot"
     height="400">


µLauncher is a fork of [finnmglas's app Launcher][original-repo].
An incomplete list of changes can be found [here](docs/launcher.md).

## Features

µLauncher only displays the date, time and a wallpaper.
Pressing back or swiping up (this can be configured) opens a list
of all installed apps, which can be searched efficiently.

The following gestures are available:
 - volume up / down,
 - swipe up / down / left / right,
 - swipe with two fingers,
 - swipe on the left / right resp. top / bottom edge,
 - tap, then swipe up / down / left / right,
 - draw < / > / V / Λ
 - click on date / time,
 - double click,
 - long click,
 - back button.

To every gesture you can bind one of the following actions:
 - launch an app,
 - open a list of all / favorite / private apps,
 - open µLauncher settings,
 - toggle private space lock,
 - lock the screen,
 - toggle the torch,
 - volume up / down,
 - go to previous / next audio track.



µLauncher is compatible with [work profile](https://www.android.com/enterprise/work-profile/),
so apps like [Shelter](https://gitea.angry.im/PeterCxy/Shelter) can be used.

By default the font is set to [Hack][hack-font], but other fonts can be selected.



## Contributing

There are several ways to contribute to this app:
* You can add or improve [translations][toolate].
     <br><img src="https://toolate.othing.xyz/widget/jrpie-launcher/launcher/horizontal-auto.svg" alt="translation status">
* If you find a bug or have an idea for a new feature you can [join the chat][chat] or open an [issue][issues]. Please note that I work on this project in my free time. Thus I might not respond immediately and not all ideas will be implemented.
* You can implement a new feature yourself:
  - Create a fork of this repository: [![][shield-gh-fork]][fork]
  - Create a new branch named `feature/<your feature>` or `fix/<your fix>` and commit your changes.
  - Open a new pull request.


See [build.md](docs/build.md) for instructions how to build this project.
The [CI pipeline](https://github.com/jrpie/Launcher/actions) automatically creates debug builds.
Note that those are not signed and not suitable for everyday use!
The latest debug build can be found [here](https://github.com/jrpie/launcher/releases/tag/pre-release).
You can also [add it to Obtainium](https://apps.obtainium.imranr.dev/redirect?r=obtainium://app/%7B%22id%22%3A%22de.jrpie.android.launcher.debug%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fjrpie%2Flauncher%22%2C%22author%22%3A%22jrpie%22%2C%22name%22%3A%22%CE%BCLauncher%20%5Bdebug%5D%22%2C%22preferredApkIndex%22%3A0%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Atrue%2C%5C%22fallbackToOlderReleases%5C%22%3Afalse%2C%5C%22filterReleaseTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22filterReleaseNotesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22verifyLatestTag%5C%22%3Afalse%2C%5C%22sortMethodChoice%5C%22%3A%5C%22date%5C%22%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Atrue%2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22trackOnly%5C%22%3Afalse%2C%5C%22versionExtractionRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22matchGroupToUse%5C%22%3A%5C%22%5C%22%2C%5C%22versionDetection%5C%22%3Afalse%2C%5C%22releaseDateAsVersion%5C%22%3Afalse%2C%5C%22useVersionCodeAsOSVersion%5C%22%3Afalse%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22invertAPKFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C%22appAuthor%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGooglePlay%5C%22%3Afalse%2C%5C%22allowInsecure%5C%22%3Afalse%2C%5C%22exemptFromBackgroundUpdates%5C%22%3Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22about%5C%22%3A%5C%22%5C%22%2C%5C%22refreshBeforeDownload%5C%22%3Afalse%7D%22%2C%22overrideSource%22%3Anull%7D).

---
  [hack-font]: https://sourcefoundry.org/hack/
  [original-repo]: https://github.com/finnmglas/Launcher
  [toolate]: https://toolate.othing.xyz/projects/jrpie-launcher/
  [issues]: https://github.com/jrpie/Launcher/issues/
  [fork]: https://github.com/jrpie/Launcher/fork/


<!-- Download links / stores -->

  [store-googleplay]: https://play.google.com/store/apps/details?id=de.jrpie.android.launcher
  [store-googleplay-badgecampain]: https://play.google.com/store/apps/details?id=de.jrpie.android.launcher&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1
  [store-fdroid]: https://f-droid.org/packages/de.jrpie.android.launcher/

<!-- Shields and Badges -->

  [shield-release]: https://img.shields.io/github/v/release/jrpie/Launcher?style=flat
  [latest-release]: https://github.com/jrpie/Launcher/releases/latest
  [shield-contribute]: https://img.shields.io/badge/contributions-welcome-007ec6.svg?style=flat
  [shield-license]: https://img.shields.io/badge/license-MIT-007ec6?style=flat

  [shield-gh-watch]: https://img.shields.io/github/watchers/jrpie/Launcher?label=Watch&style=social
  [shield-gh-star]: https://img.shields.io/github/stars/jrpie/Launcher?label=Star&style=social
  [shield-gh-fork]: https://img.shields.io/github/forks/jrpie/Launcher?label=Fork&style=social
  [matrix]: https://s.jrpie.de/launcher-matrix
  [discord]: https://s.jrpie.de/launcher-discord
  [chat]: https://s.jrpie.de/launcher-chat

<!-- Star History -->

## Star History

<a href="https://www.star-history.com/#jrpie/launcher&type=date&legend=top-left">
 <picture>
   <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/svg?repos=jrpie/launcher&type=date&theme=dark&legend=top-left" />
   <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/svg?repos=jrpie/launcher&type=date&legend=top-left" />
   <img alt="Star History Chart" src="https://api.star-history.com/svg?repos=jrpie/launcher&type=date&legend=top-left" />
 </picture>
</a>

<!-- Helpful resources -->

  [license]: https://github.com/jrpie/Launcher/blob/master/LICENSE
