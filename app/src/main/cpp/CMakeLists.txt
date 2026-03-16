cmake_minimum_required(VERSION 3.22.1)

project("llama_jni")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Optimization flags for release builds
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -DNDEBUG")

# Add the JNI wrapper library
add_library(llama_jni SHARED
    llama_jni.cpp
)

# Link Android log library
find_library(log-lib log)

target_link_libraries(llama_jni
    ${log-lib}
)

# NOTE: To build with full llama.cpp support:
# 1. Clone llama.cpp into native/llama.cpp/
# 2. Uncomment the following lines:
#
set(LLAMA_BUILD_COMMON OFF CACHE BOOL "Disable llama.cpp common library" FORCE)

add_subdirectory(${CMAKE_SOURCE_DIR}/../../../../native/llama.cpp llama.cpp)
target_include_directories(llama_jni PRIVATE ${CMAKE_SOURCE_DIR}/../../../../native/llama.cpp/include)
target_link_libraries(llama_jni llama ggml)
target_compile_definitions(llama_jni PRIVATE LLAMA_AVAILABLE=1)

# Provide mock symbols to bypass common.h dependency missing issues since we don't build it
target_compile_definitions(llama_jni PRIVATE
    LLAMA_BUILD_NUMBER=1
    LLAMA_COMMIT="unknown"
    LLAMA_COMPILER="unknown"
    LLAMA_BUILD_TARGET="unknown"
)
