#!/bin/bash
# setup_ndk.sh - Helper script to set up the Android NDK for native builds.
#
# Usage: ./scripts/setup_ndk.sh
#
# This script checks for NDK installation and provides guidance.

set -e

echo "=== LLM Answer Engine - NDK Setup ==="
echo ""

# Check for ANDROID_HOME or ANDROID_SDK_ROOT
SDK_DIR="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"

if [ -z "$SDK_DIR" ]; then
    echo "ERROR: ANDROID_HOME or ANDROID_SDK_ROOT is not set."
    echo ""
    echo "Please set one of these environment variables to your Android SDK path."
    echo "Example: export ANDROID_HOME=\$HOME/Android/Sdk"
    exit 1
fi

echo "Android SDK found at: $SDK_DIR"

# Check for NDK
NDK_DIR="$SDK_DIR/ndk"
if [ -d "$NDK_DIR" ]; then
    echo "NDK installations found:"
    ls -1 "$NDK_DIR"
else
    echo "WARNING: No NDK found. Install via Android Studio SDK Manager."
    echo "  - Open Android Studio → SDK Manager → SDK Tools"
    echo "  - Check 'NDK (Side by side)' and install"
fi

# Check for CMake
CMAKE_DIR="$SDK_DIR/cmake"
if [ -d "$CMAKE_DIR" ]; then
    echo "CMake installations found:"
    ls -1 "$CMAKE_DIR"
else
    echo "WARNING: CMake not found. Install via Android Studio SDK Manager."
fi

echo ""
echo "=== Setup Complete ==="
