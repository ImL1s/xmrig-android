cmake_minimum_required(VERSION 3.22.1)
project("xmrigminer")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Native bridge library
add_library(native-bridge SHARED
    src/main/cpp/native-bridge.cpp
)

# Find system libraries
find_library(log-lib log)
find_library(android-lib android)

# Link libraries
target_link_libraries(native-bridge
    ${log-lib}
    ${android-lib}
)
