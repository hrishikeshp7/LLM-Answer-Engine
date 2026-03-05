# Models Directory

This directory is a placeholder for model files.

**Models are NOT included in this repository.**

Models must be downloaded from within the app or manually placed here for development.

## Supported Models

| Model | Type | Size | Format |
|-------|------|------|--------|
| Qwen 2.5 0.5B Instruct | LLM | ~397 MB | GGUF Q4_K_M |
| All-MiniLM-L6-v2 | Embedding | ~23 MB | GGUF Q4_K_M |

## Manual Download

For development, you can manually download models:

```bash
# Main LLM model
wget https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf

# Embedding model
wget https://huggingface.co/leliuga/all-MiniLM-L6-v2-GGUF/resolve/main/all-MiniLM-L6-v2.Q4_K_M.gguf
```

Place downloaded files in `app/src/main/assets/models/` or let the app
download them to its internal storage.
