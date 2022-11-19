#!/bin/bash

# REQUIREMENTS
# jdk-11.0.17_macos-x64_bin.dmg
# npm install Adam-/create-dmg
# brew install gradle

set -e

JDK_VER="17.0.5"
JDK_BUILD="8"
PACKR_VERSION="runelite-1.4"

SIGNING_IDENTITY="Developer ID Application"

if ! [ -f OpenJDK11U-jre_x64_mac_hotspot_${JDK_VER}_${JDK_BUILD}.tar.gz ] ; then
    curl -Lo OpenJDK11U-jre_x64_mac_hotspot_${JDK_VER}_${JDK_BUILD}.tar.gz \
        https://github.com/adoptium/temurin17-binaries/releases/download/jdk-${JDK_VER}%2B${JDK_BUILD}/OpenJDK17U-jre_x64_mac_hotspot_${JDK_VER}_${JDK_BUILD}.tar.gz
fi

echo "d7da2b16f23371d924ca241f55baadcd1ed9f864  OpenJDK11U-jre_x64_mac_hotspot_${JDK_VER}_${JDK_BUILD}.tar.gz" | shasum -c

# packr requires a "jdk" and pulls the jre from it - so we have to place it inside
# the jdk folder at jre/


if ! [ -d osx-jdk ] ; then
    tar zxf OpenJDK11U-jre_x64_mac_hotspot_${JDK_VER}_${JDK_BUILD}.tar.gz
    mkdir osx-jdk
    mv jdk-${JDK_VER}+${JDK_BUILD}-jre osx-jdk/jre

    # Move JRE out of Contents/Home/
    pushd osx-jdk/jre
    cp -r Contents/Home/* .
    popd
fi

if ! [ -f packr_${PACKR_VERSION}.jar ] ; then
    curl -Lo packr_${PACKR_VERSION}.jar \
        https://github.com/runelite/packr/releases/download/${PACKR_VERSION}/packr.jar
fi

echo "f51577b005a51331b822a18122ce08fca58cf6fee91f071d5a16354815bbe1e3  packr_${PACKR_VERSION}.jar" | shasum -c

java -jar packr_${PACKR_VERSION}.jar packr/macOS-config.json

cp packr/Info.plist native-osx/Darkan.app/Contents

echo Setting world execute permissions on Darkan
pushd native-osx/Darkan.app
chmod g+x,o+x Contents/MacOS/Darkan
popd

codesign -f -s "${SIGNING_IDENTITY}" --entitlements osx/signing.entitlements --options runtime native-osx/Darkan.app || true


# create-dmg exits with an error code due to no code signing, but is still okay
# note we use Adam-/create-dmg as upstream does not support UDBZ
# npm install --global Adam-/create-dmg
create-dmg --volname Darkan --format UDBZ native-osx/Darkan.app native-osx/ || true

mv native-osx/Darkan\ *.dmg native-osx/Darkan.dmg

xcrun altool --notarize-app --username "${ALTOOL_USER}" --password "${ALTOOL_PASS}" --primary-bundle-id darkan --file native-osx/Darkan.dmg || true
