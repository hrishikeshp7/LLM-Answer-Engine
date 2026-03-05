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
# add_subdirectory(${CMAKE_SOURCE_DIR}/../../../../native/llama.cpp llama.cpp)
# target_include_directories(llama_jni PRIVATE ${CMAKE_SOURCE_DIR}/../../../../native/llama.cpp)
# target_link_libraries(llama_jni llama ggml)
# target_compile_definitions(llama_jni PRIVATE LLAMA_AVAILABLE=1)
