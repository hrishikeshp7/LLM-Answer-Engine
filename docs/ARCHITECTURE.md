# Architecture

## Overview

LLM Answer Engine is an Android application that runs small language models completely offline on device, with optional web search augmentation via the Brave Search API.

## Components

### 1. UI Layer (Jetpack Compose)
- **ChatScreen** - Main chat interface with streaming responses
- **SetupScreen** - First-run model download wizard
- **ModelManagerScreen** - Manage installed models
- **SettingsScreen** - Configure API keys, model parameters, performance

### 2. Inference Layer
- **LlamaInference** - JNI bridge to the native C++ inference engine
- **InferenceManager** - High-level Kotlin wrapper for model loading and generation
- **llama_jni.cpp** - Native C++ implementation using llama.cpp

### 3. Data Layer
- **ModelDownloadManager** - Handles model downloads with pause/resume
- **SecurePreferences** - Encrypted storage for API keys
- **BraveSearchClient** - Web search API integration
- **ModelRegistry** - Catalog of available models

### 4. Native Layer
- **llama.cpp** - GGML/GGUF inference runtime (external dependency)
- **JNI bindings** - Bridge between Kotlin and C++

## Data Flow

```
User Input
    │
    ▼
ChatViewModel
    │
    ├──► BraveSearchClient (optional web search)
    │         │
    │         ▼
    │    Brave Search API
    │         │
    │         ▼
    ├──► Format Prompt (with search context if available)
    │
    ▼
InferenceManager
    │
    ▼
LlamaInference (JNI)
    │
    ▼
llama_jni.cpp (Native C++)
    │
    ▼
llama.cpp Runtime
    │
    ▼
Streaming Token Output
    │
    ▼
ChatScreen UI
```

## Security

- No API keys stored in source code
- Brave API key stored in EncryptedSharedPreferences
- All inference runs locally on device
- Web search is optional and user-controlled
- CI checks prevent committing secrets

## Model Management

Models are downloaded on demand and stored in the app's internal storage:
- `/data/data/com.llmengine.app/files/models/`
- Downloads support pause/resume via HTTP Range headers
- SHA-256 checksum verification after download
- Users can delete and re-download models
