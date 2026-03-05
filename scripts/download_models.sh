#!/bin/bash
# download_models.sh - Download model files for development/testing.
#
# Usage: ./scripts/download_models.sh [output_dir]
#
# Downloads the GGUF model files used by the app.
# By default, downloads to the models/ directory.

set -e

OUTPUT_DIR="${1:-models}"
mkdir -p "$OUTPUT_DIR"

echo "=== LLM Answer Engine - Model Downloader ==="
echo ""
echo "Output directory: $OUTPUT_DIR"
echo ""

# Main LLM model
LLM_URL="https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf"
LLM_FILE="$OUTPUT_DIR/qwen3.5-0.8b-q4_k_m.gguf"

# Embedding model
EMB_URL="https://huggingface.co/leliuga/all-MiniLM-L6-v2-GGUF/resolve/main/all-MiniLM-L6-v2.Q4_K_M.gguf"
EMB_FILE="$OUTPUT_DIR/all-minilm-l6-v2-q4_k_m.gguf"

echo "Downloading Qwen 3.5 0.8B (Q4_K_M)..."
if [ -f "$LLM_FILE" ]; then
    echo "  Already exists, skipping."
else
    wget -c -O "$LLM_FILE" "$LLM_URL" || curl -L -C - -o "$LLM_FILE" "$LLM_URL"
fi

echo ""
echo "Downloading All-MiniLM-L6-v2 (Q4_K_M)..."
if [ -f "$EMB_FILE" ]; then
    echo "  Already exists, skipping."
else
    wget -c -O "$EMB_FILE" "$EMB_URL" || curl -L -C - -o "$EMB_FILE" "$EMB_URL"
fi

echo ""
echo "=== Downloads Complete ==="
echo "Models saved to: $OUTPUT_DIR"
ls -lh "$OUTPUT_DIR"
