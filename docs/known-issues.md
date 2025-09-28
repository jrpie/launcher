+++
title = 'Known Bugs'
+++

# Known Bugs


Before reporting a bug, please check whether it has already been filed in the [issue tracker](https://github.com/jrpie/launcher/issues).

I'll try to fix bug related to &mu;Launcher, however some problems
are caused by Android itself (or a vendor's modified version of Android)
and can't be fixed on &mu;Launcher's side.
If there is a ___reasonable___ workaround, I might still add it.
However, note that I don't own a Samsung / Huawei / Sony / ... device,
hence I have no means to debug vendor specific problems.
Moreover I don't have the time to clean up after every random vendor who messes up their implementation, especially when that custom implementation is not open source.

## Android Issues

See also the relevant [list of issues](https://github.com/jrpie/launcher/issues?q=is%3Aissue%20state%3Aopen%20label%3A%22android%20issue%22) in the issue tracker.

Please upvote the following issues in Android's issue tracker:
* [401872146](https://issuetracker.google.com/issues/401872146) - a bug related to touch detection.
* [352276244](https://issuetracker.google.com/issues/352276244) - some private space features can not be accessed by 3rd party launchers.



## Vendor specific problems

### Huawei
#### Huawei's widgets don't work

See [#212](https://github.com/jrpie/launcher/issues/212) in the issue tracker.
[Huawei's implementation is broken](https://www.reddit.com/r/NovaLauncher/comments/8l0fbb/nova_launcher_crashing_when_trying_to_add_emui/),
and only works with Huawei's own home screen.
There is nothing I can do about that.
Widgets provided by other apps seem to work fine.
