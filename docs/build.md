+++
  weight = 50
+++


# Building from Source

## Using the command line

Install JDK 17 and the Android SDK.
Make sure that `JAVA_HOME` and `ANDROID_HOME` are set correctly.

```bash
git clone https://github.com/jrpie/Launcher
cd Launcher

./gradlew assembleDefaultRelease
```

This will create an APK file at `app/build/outputs/apk/default/release/app-default-release-unsigned.apk`.

Note that you need to sign it:

```bash
apksigner sign --ks "$YOUR_KEYSTORE" \
    --ks-key-alias "$YOUR_ALIAS" \
    --ks-pass="pass:$YOUR_PASSWORD" \
    --key-pass="pass:$YOUR_PASSWORD" \
    --alignment-preserved \
    --v1-signing-enabled=true \
    --v2-signing-enabled=true \
    --v3-signing-enabled=true \
    --v4-signing-enabled=true \
    app-default-release-unsigned.apk
```

See [this guide](https://developer.android.com/build/building-cmdline)
for further instructions.

## Using Android Studio

Install [Android Studio](https://developer.android.com/studio), import this project and build it.

See [this guide](https://developer.android.com/studio/run)
for further instructions.

## CI Pipeline

The [CI pipeline](https://github.com/jrpie/Launcher/actions) automatically creates debug builds.

{{% hint warning %}}
Note: These builds are not signed.
They are built in debug mode and are only suitable for testing.
{{% /hint %}}
