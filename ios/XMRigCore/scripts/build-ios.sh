#!/bin/bash
# Build XMRig for iOS (arm64)
# Produces: XMRigCore.xcframework

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
BUILD_DIR="$ROOT_DIR/build"
OUTPUT_DIR="$ROOT_DIR/output"

XMRIG_VERSION="6.25.0"
XMRIG_URL="https://github.com/xmrig/xmrig/archive/refs/tags/v${XMRIG_VERSION}.tar.gz"

echo "=== Building XMRig $XMRIG_VERSION for iOS ==="

# Create directories
mkdir -p "$BUILD_DIR" "$OUTPUT_DIR"

# Download XMRig if not exists
if [ ! -d "$BUILD_DIR/xmrig-$XMRIG_VERSION" ]; then
    echo "Downloading XMRig $XMRIG_VERSION..."
    curl -L "$XMRIG_URL" -o "$BUILD_DIR/xmrig.tar.gz"
    tar -xzf "$BUILD_DIR/xmrig.tar.gz" -C "$BUILD_DIR"
    rm "$BUILD_DIR/xmrig.tar.gz"
fi

XMRIG_SRC="$BUILD_DIR/xmrig-$XMRIG_VERSION"

# iOS arm64 build
echo "Building for iOS arm64..."
mkdir -p "$BUILD_DIR/ios-arm64"
cd "$BUILD_DIR/ios-arm64"

cmake "$XMRIG_SRC" \
    -DCMAKE_SYSTEM_NAME=iOS \
    -DCMAKE_OSX_ARCHITECTURES=arm64 \
    -DCMAKE_OSX_DEPLOYMENT_TARGET=14.0 \
    -DCMAKE_BUILD_TYPE=Release \
    -DWITH_OPENCL=OFF \
    -DWITH_CUDA=OFF \
    -DWITH_HWLOC=OFF \
    -DWITH_HTTP=OFF \
    -DWITH_TLS=OFF \
    -DWITH_ASM=ON \
    -DBUILD_STATIC=ON \
    -DCMAKE_C_FLAGS="-fembed-bitcode" \
    -DCMAKE_CXX_FLAGS="-fembed-bitcode"

make -j$(sysctl -n hw.ncpu)

# Create static library
echo "Creating static library..."
ar rcs "$OUTPUT_DIR/libxmrig-ios-arm64.a" \
    src/CMakeFiles/xmrig.dir/**/*.o \
    src/crypto/CMakeFiles/*.dir/**/*.o 2>/dev/null || true

echo "=== Build Complete ==="
echo "Output: $OUTPUT_DIR/libxmrig-ios-arm64.a"
echo ""
echo "Next steps:"
echo "1. Create xcframework with: xcodebuild -create-xcframework ..."
echo "2. Add to Xcode project"
