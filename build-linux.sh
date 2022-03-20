#!/bin/bash

set -e

JDK_VER="17.0.2"
JDK_BUILD="17"
PACKR_VERSION="runelite-1.0"

# Check if there's a client jar file - If there's no file the AppImage will not work but will still be built.
if ! [ -e build/libs/darkan-shaded.jar ]
then
  echo "build/libs/darkan-shaded.jar not found, exiting"
  exit 1
fi

if ! [ -f openjdk-17.0.2_linux-x64_bin.tar.gz ] ; then
    curl -Lo openjdk-17.0.2_linux-x64_bin.tar.gz \
        https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz
fi

rm -f packr.jar
curl -o packr.jar https://libgdx.badlogicgames.com/ci/packr/packr.jar

# packr requires a "jdk" and pulls the jre from it - so we have to place it inside
# the jdk folder at jre/
if ! [ -d linux-jdk ] ; then
    tar zxf openjdk-17.0.2_linux-x64_bin.tar.gz
    mkdir linux-jdk
    mv jdk-17.0.2 linux-jdk
fi

if ! [ -f packr_${PACKR_VERSION}.jar ] ; then
    curl -Lo packr_${PACKR_VERSION}.jar \
        https://github.com/runelite/packr/releases/download/${PACKR_VERSION}/packr.jar
fi

echo "18b7cbaab4c3f9ea556f621ca42fbd0dc745a4d11e2a08f496e2c3196580cd53  packr_${PACKR_VERSION}.jar" | sha256sum -c

java -jar packr_${PACKR_VERSION}.jar \
    --platform \
    linux64 \
    --jdk \
    linux-jdk \
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
    native-linux/Darkan.AppDir/ \
    --resources \
    build/filtered-resources/darkan.desktop \
    appimage/darkan.png

pushd native-linux/Darkan.AppDir
mkdir -p jre/lib/amd64/server/
ln -s ../../server/libjvm.so jre/lib/amd64/server/ # packr looks for libjvm at this hardcoded path
popd

# Symlink AppRun -> RuneLite
pushd native-linux/Darkan.AppDir/
ln -s Darkan AppRun
popd

curl -Lo appimagetool-x86_64.AppImage https://github.com/AppImage/AppImageKit/releases/download/12/appimagetool-x86_64.AppImage
chmod 755 appimagetool-x86_64.AppImage

./appimagetool-x86_64.AppImage \
	native-linux/Darkan.AppDir/ \
	native-linux/Darkan.AppImage
