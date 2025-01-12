#!/bin/bash
export JAVA_HOME="/usr/lib/jvm/java-23-openjdk/"
OUTPUT_DIR="$HOME/launcher-release"
BUILD_TOOLS_DIR="$HOME/Android/Sdk/build-tools/35.0.0"
KEYSTORE="$HOME/data/keys/launcher_jrpie.jks"
KEYSTORE_ACCRESCENT="$HOME/data/keys/launcher_jrpie_accrescent.jks"
# KEYSTORE_PASS=$(keepassxc-password "android_keys/launcher")
# KEYSTORE_ACCRESCENT_PASS=$(keepassxc-password "android_keys/launcher-accrescent")

if [[ $(git status --porcelain) ]]; then
    echo "There are uncommitted changes."
    exit 1
fi
VERSION_INFO=$(./gradlew -q Version --rerun)
echo $VERSION_INFO


IFS="
" read -r VERSION_NAME VERSION_CODE MIN_SDK TARGET_SDK <<< $(./gradlew -q Version --rerun)


echo "Building Release $VERSION_NAME ($VERSION_CODE)"
echo "Target SDK: $TARGET_SDK,  Min SDK: $MIN_SDK"

rm -rf "$OUTPUT_DIR"
mkdir "$OUTPUT_DIR"


echo
echo "======================="
echo " Default Release (apk) "
echo "======================="

./gradlew clean
./gradlew assembleDefaultRelease
mv app/build/outputs/apk/default/release/app-default-release-unsigned.apk "$OUTPUT_DIR/unsigned.apk"
$BUILD_TOOLS_DIR/apksigner sign --ks "$KEYSTORE" \
    --ks-key-alias key0 \
    --ks-pass="pass:$KEYSTORE_PASS" \
    --key-pass="pass:$KEYSTORE_PASS" \
    --v1-signing-enabled=true --v2-signing-enabled=true --v3-signing-enabled=true --v4-signing-enabled=true \
    "$OUTPUT_DIR/unsigned.apk"

echo
echo "======================="
echo " Default Release (aab) "
echo "======================="

./gradlew clean
./gradlew bundleDefaultRelease
mv app/build/outputs/bundle/defaultRelease/app-default-release.aab $OUTPUT_DIR/app-release.aab
$BUILD_TOOLS_DIR/apksigner sign --ks "$KEYSTORE" \
    --ks-key-alias key0 \
    --ks-pass="pass:$KEYSTORE_PASS" \
    --key-pass="pass:$KEYSTORE_PASS" \
    --v1-signing-enabled=true --v2-signing-enabled=true --v3-signing-enabled=true --v4-signing-enabled=true \
    --min-sdk-version=21 \
    --target-sdk-version=35 \
    "$OUTPUT_DIR/app-release.aab"


echo
echo "======================="
echo " Accrescent (apks) "
echo "======================="

./gradlew clean
./gradlew bundleAccrescentRelease
mv app/build/outputs/bundle/accrescentRelease/app-accrescent-release.aab $OUTPUT_DIR/app-accrescent-release.aab
$JAVA_HOME/bin/java -jar /opt/android/bundletool.jar build-apks \
    --bundle="$OUTPUT_DIR/app-accrescent-release.aab" --output="$OUTPUT_DIR/launcher-accrescent.apks" \
    --ks="$KEYSTORE_ACCRESCENT" \
    --ks-pass="pass:$KEYSTORE_ACCRESCENT_PASS" \
    --ks-key-alias="key0" \
    --key-pass="pass:$KEYSTORE_ACCRESCENT_PASS"
