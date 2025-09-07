# Hugging Face Model Clone Workflow

This workflow automatically downloads and caches Hugging Face models for build assistance.

## Features

- ✅ **Automatic caching** - Models are cached between workflow runs
- ✅ **Build-first approach** - Designed for build assistance, not repository bloat
- ✅ **Optional commits** - Models can optionally be committed to repo
- ✅ **Forensic logging** - Detailed logs for debugging and auditing
- ✅ **Error handling** - Graceful failure with clear error messages

## Usage

### For Build Assistance (Recommended)

Run the workflow manually without committing models:

1. Go to Actions → "Clone and Cache Hugging Face Models"
2. Click "Run workflow" 
3. Leave "Commit models to repository" **unchecked** (default)
4. Models will be cached and available for subsequent build steps

### To Commit Models to Repository

Run the workflow with commit enabled:

1. Go to Actions → "Clone and Cache Hugging Face Models"  
2. Click "Run workflow"
3. **Check** "Commit models to repository"
4. Models will be downloaded and committed to the repo

## Configuration

Edit `configs/model_manifest.json` to add/remove models:

```json
{
  "models": [
    { "repo": "https://huggingface.co/Qwen/Qwen2.5-0.5B", "dir": "Qwen2.5-0.5B" },
    { "repo": "https://huggingface.co/microsoft/phi-2", "dir": "Phi-2" }
  ]
}
```

## Workflow Triggers

- **Manual**: Via workflow_dispatch (recommended)
- **Automatic**: When `clone-hf-models.yml` or `model_manifest.json` changes

## Output

- **Models**: Downloaded to `models/` directory
- **Logs**: Forensic logs in `logs/model_sync.jsonl`
- **Cache**: GitHub Actions cache for faster subsequent runs

## Troubleshooting

### Permission Issues
The workflow has `contents: write` permissions for optional commits.

### Large Models
Models are cloned with `--depth 1` for efficiency.

### Cache Misses
Cache key includes hash of `model_manifest.json` - changes invalidate cache.

## Build Integration

Use models in subsequent workflow steps:

```yaml
- name: Use Cached Models
  run: |
    echo "Available models:"
    ls -la models/
    
    # Use specific model
    python inference.py --model models/Qwen2.5-0.5B
```