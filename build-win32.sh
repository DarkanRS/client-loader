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

if ! [ -f OpenJDK17U-jre_x86-32_windows_hotspot_${JDK_VER}_${JDK_BUILD}.zip ] ; then
    curl -Lo OpenJDK17U-jre_x86-32_windows_hotspot_${JDK_VER}_${JDK_BUILD}.zip \
        https://github.com/adoptium/temurin17-binaries/releases/download/jdk-${JDK_VER}%2B${JDK_BUILD}/OpenJDK17U-jre_x86-32_windows_hotspot_${JDK_VER}_${JDK_BUILD}.zip
fi

# rm -f packr.jar
# curl -o packr.jar https://libgdx.badlogicgames.com/ci/packr/packr.jar

echo "1db2a4f14161524c43977b441f0b78a2a547bcb5fe4a182b03bf52650d64730a OpenJDK17U-jre_x86-32_windows_hotspot_${JDK_VER}_${JDK_BUILD}.zip" | sha256sum -c

# packr requires a "jdk" and pulls the jre from it - so we have to place it inside
# the jdk folder at jre/
if ! [ -d win32-jdk ] ; then
    unzip OpenJDK17U-jre_x86-32_windows_hotspot_${JDK_VER}_${JDK_BUILD}.zip
    mkdir win32-jdk
    mv jdk-${JDK_VER}+${JDK_BUILD}-jre win32-jdk/jre
fi

if ! [ -f packr_${PACKR_VERSION}.jar ] ; then
    curl -Lo packr_${PACKR_VERSION}.jar \
        https://github.com/runelite/packr/releases/download/${PACKR_VERSION}/packr.jar
fi

echo "f51577b005a51331b822a18122ce08fca58cf6fee91f071d5a16354815bbe1e3  packr_${PACKR_VERSION}.jar" | sha256sum -c

java -jar packr_${PACKR_VERSION}.jar \
    --platform \
    windows32 \
    --jdk \
    win32-jdk \
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
    native-win32

# modify packr exe manifest to enable Windows dpi scaling
"C:\Program Files (x86)\Resource Hacker\ResourceHacker.exe" \
    -open native-win32/Darkan.exe \
    -save native-win32/Darkan.exe \
    -action addoverwrite \
    -res packr/darkan.manifest \
    -mask MANIFEST,1,

# packr on Windows doesn't support icons, so we use resourcehacker to include it
"C:\Program Files (x86)\Resource Hacker\ResourceHacker.exe" \
    -open native-win32/Darkan.exe \
    -save native-win32/Darkan.exe \
    -action add \
    -res darkan.ico \
    -mask ICONGROUP,MAINICON,

if ! [ -f vcredist_x86.exe ] ; then
    # Visual C++ Redistributable Packages for Visual Studio 2013
    curl -Lo vcredist_x86.exe https://download.microsoft.com/download/2/E/6/2E61CFA4-993B-4DD4-91DA-3737CD5CD6E3/vcredist_x86.exe
fi

echo "a22895e55b26202eae166838edbe2ea6aad00d7ea600c11f8a31ede5cbce2048 *vcredist_x86.exe" | sha256sum -c

# We use the filtered iss file
"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" build/filtered-resources/darkan32.iss