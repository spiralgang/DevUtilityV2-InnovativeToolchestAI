#!/usr/bin/env bash
set -euo pipefail

# 1. Patch .gitmodules for missing submodule URL
if ! grep -q 'ai/ollama/qwen2.5-math-1.5b' .gitmodules 2>/dev/null; then
  cat >> .gitmodules <<EOF

[submodule "ai/ollama/qwen2.5-math-1.5b"]
	path = ai/ollama/qwen2.5-math-1.5b
	url = https://github.com/your-org/qwen2.5-math-1.5b.git
EOF
  echo "🔧 Added missing submodule entry to .gitmodules"
else
  echo "✔ .gitmodules already contains the submodule entry"
fi

# Sync and init submodules
git submodule sync
git submodule update --init --recursive
echo "🔄 Submodules synced and initialized"

# 2. Inject fallback logic into frontend_analyzer.py
ANALYZER="tools/agent_analyzers/frontend_analyzer.py"
BACKUP="${ANALYZER}.bak"

if ! grep -q "def get_issue_number" "$ANALYZER"; then
  cp "$ANALYZER" "$BACKUP"
  cat > "$ANALYZER" <<'PYCODE'
import re
import logging

def get_issue_number(context):
    """
    Try to extract '#123' from context.
    Fall back to None if not found.
    """
    match = re.search(r'#(\d+)', context or '')
    return match.group(1) if match else None

logger = logging.getLogger(__name__)

# Original file starts below
PYCODE
  # Append original content after the fallback definition
  tail -n +1 "$BACKUP" >> "$ANALYZER"
  echo "🔧 Injected issue-number fallback into $ANALYZER"
else
  echo "✔ Issue-number fallback already present in $ANALYZER"
fi

# 3. Generate requirements.txt
REQ_FILE="requirements.txt"
pip3 freeze | grep -E '^(requests|beautifulsoup4|charset_normalizer|idna|urllib3|certifi|soupsieve|typing-extensions)' > "$REQ_FILE"
echo "📝 Written Python dependencies to $REQ_FILE"

echo "✅ All fixes applied. Review changes, commit, and rerun your pipeline."
