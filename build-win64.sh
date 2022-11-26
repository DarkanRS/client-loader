#!/bin/bash

# Requirements
# JDK 18
# GIT shell for running this script
# http://www.angusj.com/resourcehacker/
# https://jrsoftware.org/isinfo.php

set -e

JDK_VER="17.0.5"
JDK_BUILD="8"
PACKR_VERSION="runelite-1.4"

if ! [ -f OpenJDK17U-jre_x64_windows_hotspot_${JDK_VER}_${JDK_BUILD}.zip ] ; then
    curl -Lo OpenJDK17U-jre_x64_windows_hotspot_${JDK_VER}_${JDK_BUILD}.zip \
        https://github.com/adoptium/temurin17-binaries/releases/download/jdk-${JDK_VER}%2B${JDK_BUILD}/OpenJDK17U-jre_x64_windows_hotspot_${JDK_VER}_${JDK_BUILD}.zip
fi

# rm -f packr.jar
# curl -o packr.jar https://libgdx.badlogicgames.com/ci/packr/packr.jar

echo "d25a2d44c1bd9c57d49c5e98de274cd40970ab057fe304b52eb459de4ee5d8a5 OpenJDK17U-jre_x64_windows_hotspot_${JDK_VER}_${JDK_BUILD}.zip" | sha256sum -c

# packr requires a "jdk" and pulls the jre from it - so we have to place it inside
# the jdk folder at jre/
if ! [ -d win64-jdk ] ; then
    unzip OpenJDK17U-jre_x64_windows_hotspot_${JDK_VER}_${JDK_BUILD}.zip
    mkdir win64-jdk
	echo jdk-${JDK_VER}+${JDK_BUILD}-jre win64-jdk/jre
    mv jdk-${JDK_VER}+${JDK_BUILD}-jre win64-jdk/jre
fi

if ! [ -f packr_${PACKR_VERSION}.jar ] ; then
    curl -Lo packr_${PACKR_VERSION}.jar \
        https://github.com/runelite/packr/releases/download/${PACKR_VERSION}/packr.jar
fi

echo "f51577b005a51331b822a18122ce08fca58cf6fee91f071d5a16354815bbe1e3  packr_${PACKR_VERSION}.jar" | sha256sum -c

java -jar packr_${PACKR_VERSION}.jar \
    --platform \
    windows64 \
    --jdk \
    win64-jdk \
    --executable \
    Darkan \
    --classpath \
    build/libs/darkan-shaded.jar \
    --mainclass \
    com.darkan.Loader \
    --vmargs \
    Xmx1024m \
    Xss2m \
    XX:CompileThreshold=1500 \
    Djna.nosys=true \
    --output \
    native-win64

# modify packr exe manifest to enable Windows dpi scaling
"C:\Program Files (x86)\Resource Hacker\ResourceHacker.exe" \
    -open native-win64/Darkan.exe \
    -save native-win64/Darkan.exe \
    -action addoverwrite \
    -res packr/darkan.manifest \
    -mask MANIFEST,1,

# packr on Windows doesn't support icons, so we use resourcehacker to include it
"C:\Program Files (x86)\Resource Hacker\ResourceHacker.exe" \
    -open native-win64/Darkan.exe \
    -save native-win64/Darkan.exe \
    -action add \
    -res darkan.ico \
    -mask ICONGROUP,MAINICON,

if ! [ -f vcredist_x64.exe ] ; then
    # Visual C++ Redistributable for Visual Studio 2015
    curl -Lo vcredist_x64.exe https://download.microsoft.com/download/9/3/F/93FCF1E7-E6A4-478B-96E7-D4B285925B00/vc_redist.x64.exe
fi

echo "5eea714e1f22f1875c1cb7b1738b0c0b1f02aec5ecb95f0fdb1c5171c6cd93a3 *vcredist_x64.exe" | sha256sum -c

# We use the filtered iss file
"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" build/filtered-resources/darkan.iss