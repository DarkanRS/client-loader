#!/bin/bash

set -e

JDK_VER="17.0.5"
JDK_BUILD="8"
PACKR_VERSION="runelite-1.4"

# Check if there's a client jar file - If there's no file the AppImage will not work but will still be built.
if ! [ -e build/libs/darkan-shaded.jar ]
then
  echo "build/libs/darkan-shaded.jar not found, exiting"
  exit 1
fi
echo OpenJDK17U-jre_x64_linux_hotspot_${JDK_VER}_${JDK_BUILD}.tar.gz
if ! [ -f OpenJDK17U-jre_x64_linux_hotspot_${JDK_VER}_${JDK_BUILD}.tar.gz   ] ; then
    curl -Lo OpenJDK17U-jre_x64_linux_hotspot_${JDK_VER}_${JDK_BUILD}.tar.gz  \
        https://github.com/adoptium/temurin17-binaries/releases/download/jdk-${JDK_VER}%2B${JDK_BUILD}/OpenJDK17U-jre_x64_linux_hotspot_${JDK_VER}_${JDK_BUILD}.tar.gz 
fi

#rm -f packr.jar
#curl -o packr.jar https://libgdx.badlogicgames.com/ci/packr/packr.jar

# packr requires a "jdk" and pulls the jre from it - so we have to place it inside
# the jdk folder at jre/
if ! [ -d linux-jdk ] ; then
    tar zxf OpenJDK17U-jre_x64_linux_hotspot_${JDK_VER}_${JDK_BUILD}.tar.gz
    mkdir linux-jdk
    mv jdk-${JDK_VER}+${JDK_BUILD}-jre jre
    mv jre linux-jdk
    cd linux-jdk
fi

if ! [ -f packr_${PACKR_VERSION}.jar ] ; then
    curl -Lo packr_${PACKR_VERSION}.jar \
        https://github.com/runelite/packr/releases/download/${PACKR_VERSION}/packr.jar
fi

echo "f51577b005a51331b822a18122ce08fca58cf6fee91f071d5a16354815bbe1e3  packr_${PACKR_VERSION}.jar" | sha256sum -c

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
