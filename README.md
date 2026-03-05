# LLM Answer Engine

A modern Android application that runs small language models **completely offline on device**, with optional web search augmentation using the Brave Search API.

> **Privacy first:** All AI inference runs locally on your device. No conversation data is ever sent to external servers.

---

## Features

### Core
- 🤖 **Local LLM Inference** — Run Qwen 3.5 0.8B (or similar) directly on your Android device
- 💬 **Chat Interface** — Clean, ChatGPT-like UI with streaming responses
- 🔍 **Web Search (Optional)** — Brave Search API integration for search-augmented generation
- 📱 **Fully Offline** — Works without internet once models are downloaded
- 🌙 **Dark Mode** — Automatic dark/light theme support

### Model Management
- 📥 **In-App Downloads** — Download models directly within the app
- ⏸️ **Pause/Resume** — Resume interrupted downloads
- ✅ **Checksum Verification** — SHA-256 verification after download
- 🗑️ **Easy Deletion** — Remove and re-download models as needed
- 📊 **Size Display** — See model sizes before downloading

### Search Augmented Generation (RAG)
- 🌐 Perform web searches via Brave Search API
- 📝 Summarize and feed results into the LLM prompt
- 🔗 Display source citations with results

### Performance Controls
- ⚙️ Configurable max tokens, temperature, CPU threads
- 💾 Memory usage modes (Low / Balanced / High Performance)
- ⚡ Token generation speed indicator

### Security
- 🔒 No hardcoded API keys
- 🔐 Encrypted storage for API keys (EncryptedSharedPreferences)
- 🛡️ CI secret scanning to prevent accidental commits

---

## Screenshots

| Setup | Chat | Settings |
|-------|------|----------|
| ![Setup Screen](docs/screenshots/setup.png) | ![Chat Screen](docs/screenshots/chat.png) | ![Settings Screen](docs/screenshots/settings.png) |

*Screenshots will be added after first build.*

---

## Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 17**
- **Android SDK** API 34
- **Android NDK** 25+ (for native inference)
- **CMake** 3.22.1+

### Build & Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/LLM-Answer-Engine.git
   cd LLM-Answer-Engine
   ```

2. **Open in Android Studio** and let Gradle sync.

3. **Build:**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run** on a physical device (ARM64 recommended) or emulator.

5. **Download models** when prompted on first launch.

### Building with Native Inference (llama.cpp)

For full on-device inference:

```bash
cd native/
git clone https://github.com/ggerganov/llama.cpp.git
```

Then uncomment the llama.cpp lines in `app/src/main/cpp/CMakeLists.txt` and rebuild.

See [docs/BUILD.md](docs/BUILD.md) for detailed instructions.

---

## How to Download Models

When you first launch the app, a setup screen will guide you through downloading:

1. **Embedding Model** (~23 MB) — Used for semantic search
2. **Main LLM Model** (~397 MB) — The language model for chat

Models are downloaded from HuggingFace and stored in the app's internal storage. They run entirely on your device.

You can also manage models later via **Settings → Model Manager**.

---

## How to Get a Brave Search API Key

Web search is **optional**. The app works fully offline without it.

To enable web search augmented generation:

1. **Visit** [https://brave.com/search/api/](https://brave.com/search/api/)
2. **Create a free account**
3. **Generate an API key** from the dashboard
4. **Open the app** → Settings → Paste your API key
5. **Enable** the "Web Search" toggle

The free tier includes 2,000 queries/month, which is sufficient for personal use.

> **Note:** Your API key is stored encrypted on your device using Android's EncryptedSharedPreferences. It is never included in the source code or sent anywhere except to the Brave Search API when you perform a search.

---

## Project Structure

```
LLM-Answer-Engine/
├── app/                          # Android application
│   ├── src/main/
│   │   ├── java/com/llmengine/app/
│   │   │   ├── ui/              # Jetpack Compose UI
│   │   │   │   ├── chat/        # Chat screen & ViewModel
│   │   │   │   ├── models/      # Model manager screen
│   │   │   │   ├── settings/    # Settings screen
│   │   │   │   ├── setup/       # First-run setup
│   │   │   │   └── theme/       # Material 3 theme
│   │   │   ├── data/            # Data layer
│   │   │   │   ├── model/       # Data models & registry
│   │   │   │   ├── search/      # Brave Search client
│   │   │   │   └── preferences/ # Secure preferences
│   │   │   ├── download/        # Model download manager
│   │   │   └── inference/       # LLM inference (JNI bridge)
│   │   ├── cpp/                 # Native C++ JNI code
│   │   └── res/                 # Android resources
│   └── src/test/                # Unit tests
├── native/                      # Native library sources
│   └── src/
├── models/                      # Model files (gitignored)
├── scripts/                     # Helper scripts
├── docs/                        # Documentation
│   ├── ARCHITECTURE.md
│   └── BUILD.md
├── .github/workflows/           # CI configuration
│   └── ci.yml
└── README.md
```

---

## Privacy

LLM Answer Engine is designed with privacy as a core principle:

- **All AI inference runs locally** on your Android device
- **No conversation data** is sent to any server
- **Models are stored locally** in the app's private storage
- **Web search** (if enabled) only sends your search query to Brave's API
- **Your API key** is stored encrypted using Android Keystore
- **No analytics or tracking** of any kind
- **No accounts required** to use the app
- **You can delete** all data and models at any time

---

## Supported Models

| Model | Type | Size | Quantization |
|-------|------|------|-------------|
| Qwen 3.5 0.8B | LLM | ~397 MB | Q4_K_M |
| All-MiniLM-L6-v2 | Embedding | ~23 MB | Q4_K_M |

Additional GGUF-format models can be supported by adding entries to the `ModelRegistry`.

---

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `./gradlew testDebugUnitTest`
5. Submit a pull request

**Important:** Never commit API keys or secrets. The CI pipeline includes secret scanning.

---

## License

This project is licensed under the GNU General Public License v3.0 — see the [LICENSE](LICENSE) file for details.

---

## Acknowledgments

- [llama.cpp](https://github.com/ggerganov/llama.cpp) — Inference runtime
- [Qwen](https://github.com/QwenLM/Qwen) — Language model
- [Brave Search API](https://brave.com/search/api/) — Web search
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — UI framework
