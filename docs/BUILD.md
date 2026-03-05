# Build Instructions

## Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 17** (bundled with Android Studio)
- **Android SDK** API 34
- **Android NDK** 25+ (for native builds)
- **CMake** 3.22.1+ (installable via SDK Manager)

## Quick Start

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/LLM-Answer-Engine.git
   cd LLM-Answer-Engine
   ```

2. **Open in Android Studio:**
   - File → Open → select the project directory
   - Wait for Gradle sync to complete

3. **Build the project:**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run on device/emulator:**
   - Select a target device (ARM64 recommended for model inference)
   - Click Run (▶️)

## Building with Native Inference (llama.cpp)

For full native inference support:

1. **Clone llama.cpp:**
   ```bash
   cd native/
   git clone https://github.com/ggerganov/llama.cpp.git
   ```

2. **Update CMakeLists.txt:**
   Edit `app/src/main/cpp/CMakeLists.txt` and uncomment the llama.cpp integration lines.

3. **Install NDK:**
   - Android Studio → SDK Manager → SDK Tools
   - Check "NDK (Side by side)" and "CMake"
   - Install

4. **Build:**
   ```bash
   ./gradlew assembleDebug
   ```

## Troubleshooting

### Gradle sync fails
- Ensure you have JDK 17 set as the Gradle JDK
- File → Settings → Build → Gradle → Gradle JDK

### NDK not found
- Run `./scripts/setup_ndk.sh` to check your setup
- Install NDK via Android Studio SDK Manager

### Out of memory during build
- Increase Gradle heap size in `gradle.properties`:
  ```
  org.gradle.jvmargs=-Xmx4096m
  ```
