# Native Inference Library

This directory is reserved for the native C++ inference engine.

## Setup

To build with full llama.cpp support:

1. Clone llama.cpp:
   ```bash
   cd native/
   git clone https://github.com/ggerganov/llama.cpp.git
   ```

2. The CMakeLists.txt in `app/src/main/cpp/` includes configuration
   for building llama.cpp with the Android NDK.

3. Uncomment the llama.cpp integration lines in CMakeLists.txt.

4. Build the project with Android Studio or Gradle.

## Architecture

The native code provides:
- Model loading (GGUF format)
- Text generation with streaming callbacks
- Embedding generation for RAG
- Resource management and cleanup

The JNI bridge (`app/src/main/cpp/llama_jni.cpp`) connects the Kotlin
layer to the C++ inference engine.
