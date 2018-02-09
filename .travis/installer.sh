#!/usr/bin/env bash

# Flag any non-zero exit status as script failure and abort!
set -ev

# If there is no version, FAIL!
VERSION=${VERSION:?"Must set 'VERSION' in .travis.yml!"}

case "$1" in
    # For macOS, we have to download JDK9 and install it.
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
# For both systems, we have to download the latest Gradle 4.x
# because older versions couldn't parse '9.0.4' as a Java version
# string.

# Download desired version of Gradle
wget https://services.gradle.org/distributions/gradle-$VERSION-bin.zip

# Unpack it into the current directory
unzip -qq gradle-$VERSION-bin.zip
