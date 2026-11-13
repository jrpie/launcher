#!/bin/bash

# This script builds all variants of µLauncher to create a release, namely:
# - app-release.apk (GitHub release; used by F-Droid for reproducible builds)
# - launcher-accrescent.apks (Accrescent)
# - app-release.aab (Play Store)

# This is only intended to work on my (@jrpie) computer.
# To use this script for building a fork you need to:
#   - install bundletool.jar and
#   - create a keystore and modify the variables below accordingly

export JAVA_HOME="/usr/lib/jvm/java-21-openjdk/"
OUTPUT_DIR="$HOME/launcher-release"
BUILD_TOOLS_DIR="$HOME/Android/Sdk/build-tools/35.0.0"

# keystore for the default release
KEYSTORE="$HOME/data/keys/launcher_jrpie.jks"
# keystore for the default accrescent release
KEYSTORE_ACCRESCENT="$HOME/data/keys/launcher_jrpie_accrescent.jks"

# keepassxc-password is a custom script to fetch passwords from my password manager
KEYSTORE_PASS=$(keepassxc-password "android_keys/launcher")
KEYSTORE_ACCRESCENT_PASS=$(keepassxc-password "android_keys/launcher-accrescent")

if [[ $(git status --porcelain) ]]; then
    echo "There are uncommitted changes."

    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if ! [[ $REPLY =~ ^[Yy]$ ]]
    then
        exit 1
    fi
fi

rm -rf "$OUTPUT_DIR"
mkdir "$OUTPUT_DIR"


echo
echo "======================="
echo " Default Release (apk) "
echo "======================="

./gradlew clean
./gradlew assembleDefaultRelease
mv app/build/outputs/apk/default/release/app-default-release-unsigned.apk "$OUTPUT_DIR/app-release.apk"
"$BUILD_TOOLS_DIR/apksigner" sign --ks "$KEYSTORE" \
    --ks-key-alias key0 \
    --ks-pass="pass:$KEYSTORE_PASS" \
    --key-pass="pass:$KEYSTORE_PASS" \
    --alignment-preserved \
    --v1-signing-enabled=true \
    --v2-signing-enabled=true \
    --v3-signing-enabled=true \
    --v4-signing-enabled=true \
    "$OUTPUT_DIR/app-release.apk"

echo
echo "======================="
echo " Default Release (aab) "
echo "======================="

./gradlew clean
./gradlew bundleDefaultRelease
mv app/build/outputs/bundle/defaultRelease/app-default-release.aab "$OUTPUT_DIR/app-release.aab"
"$BUILD_TOOLS_DIR/apksigner" sign --ks "$KEYSTORE" \
    --ks-key-alias key0 \
    --ks-pass="pass:$KEYSTORE_PASS" \
    --key-pass="pass:$KEYSTORE_PASS" \
    --v1-signing-enabled=true --v2-signing-enabled=true --v3-signing-enabled=true --v4-signing-enabled=true \
    --min-sdk-version=21 \
    "$OUTPUT_DIR/app-release.aab"

echo
echo "======================="
echo " Accrescent (apks) "
echo "======================="

./gradlew clean
./gradlew bundleAccrescentRelease
mv app/build/outputs/bundle/accrescentRelease/app-accrescent-release.aab "$OUTPUT_DIR/app-accrescent-release.aab"

# build apks using bundletool from https://github.com/google/bundletool/releases
"$JAVA_HOME/bin/java" -jar /opt/android/bundletool.jar build-apks \
    --bundle="$OUTPUT_DIR/app-accrescent-release.aab" --output="$OUTPUT_DIR/launcher-accrescent.apks" \
    --ks="$KEYSTORE_ACCRESCENT" \
    --ks-pass="pass:$KEYSTORE_ACCRESCENT_PASS" \
    --ks-key-alias="key0" \
    --key-pass="pass:$KEYSTORE_ACCRESCENT_PASS" \
    --mode="universal"
