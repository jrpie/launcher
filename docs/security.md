+++
  weight = 30
+++

# Security Considerations

In order to launch apps, &mu;Launcher obtains a list of all apps installed on the device.
This includes apps from other profiles such as the [private space](/docs/profiles/#private-space)
and the [work profile](/docs/profiles/#work-profile).

&mu;Launcher aims to be minimal software. Functionality that can be provided
by other apps[^1] is not integrated into &mu;Launcher itself,
thus allowing user to install only what they need.

[^1]: For example [daily wallpapers](/docs/examples/wallpapers/)

{{% hint info %}}
&mu;Launcher does **not connect to the internet**.
Certain functions, such as the buttons in the meta section may prompt the browser
to open a website, but &mu;Launcher itself does not open internet connections.
Functionality that would require an internet connection will not be implemented.
{{% /hint %}}

## Requested Permissions

&mu;Launcher requests several permissions:

 *  [`android.permission.REQUEST_DELETE_PACKAGES`](https://developer.android.com/reference/android/Manifest.permission#REQUEST_DELETE_PACKAGES)
 *  [`android.permission.QUERY_ALL_PACKAGES`](https://developer.android.com/reference/android/Manifest.permission#QUERY_ALL_PACKAGES)
 *  [`android.permission.ACCESS_HIDDEN_PROFILES`](https://developer.android.com/reference/android/Manifest.permission#ACCESS_HIDDEN_PROFILES)
 *  [`android.permission.EXPAND_STATUS_BAR`](https://developer.android.com/reference/android/Manifest.permission#EXPAND_STATUS_BAR)
 *  [`android.permission.POST_NOTIFICATIONS`](https://developer.android.com/reference/android/Manifest.permission#POST_NOTIFICATIONS)
 *  [`android.permission.BIND_ACCESSIBILITY_SERVICE`](https://developer.android.com/reference/android/Manifest.permission#BIND_ACCESSIBILITY_SERVICE)
 *  [`android.permission.BIND_DEVICE_ADMIN`](https://developer.android.com/reference/android/Manifest.permission#BIND_DEVICE_ADMIN)


### Accessibility Service

&mu;Launcher's accessibility service can be used to lock the screen and
to open the list of recent apps.

{{% hint danger %}}
Enabling &mu;Launcher's accessibility service grants excessive permissions to the app.
Do not enable the accessibility service if you don't need it.
Before enabling, make sure that you obtained your copy of &mu;Launcher from a source you trust.
The official sources can be found [here](https://launcher.jrpie.de/).
{{% /hint %}}

Due to [Accrescent's policy](https://accrescent.app/docs/guide/publish/requirements.html#androidaccessibilityserviceaccessibilityservice) on accessibility services,
the version of &mu;Launcher published on Accrescent does not contain an accessibility service.


### Device Administrator Permissions

Device Administrator permissions can be used for locking the device as an alternative to using the accessibility service.
This is the preferable option, as the required permissions are far less intrusive.
However, this method is (ab)using an API intended for emergency situations,
hence unlocking using weak authentication methods (fingerprint, face detection)
is not possible.

## Crash Reports

For privacy reasons, &mu;Launcher does not collect crash reports automatically.

## Reporting Security Issues

For security related issues, please use the contact information
from the [security.txt](https://jrpie.de/.well-known/security.txt) on my website
or [report a vulnerability](https://github.com/jrpie/Launcher/security/advisories/new) on github.

{{% hint danger %}}
Please do not report security issues using github's issue feature!
{{% /hint %}}
