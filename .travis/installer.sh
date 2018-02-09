#!/usr/bin/env bash

set -ev
case "$1" in
    osx)    COOKIE1="gpw_e24=http%3A%2F%2Fwww.oracle.com%2F"
            COOKIE2="oraclelicense=accept-securebackup-cookie"
            BASEURL=http://download.oracle.com/otn-pub/java/jdk
            JDK=9.0.4+11/c2514751926b4512b076cc82f959763f/jdk-9.0.4_osx-x64_bin.dmg
            wget --no-cookies --no-check-certificate \
                --header "Cookie: $COOKIE1; $COOKIE2" "$BASEURL/$JDK"
            hdiutil mount jdk-*.dmg
            sudo installer -pkg /Volumes/JDK*/JDK*.pkg -target LocalSystem
            # We should unmount the image, but we don't care...
            echo "Completed JDK installation..."
            ;;
esac
exit 0
