#!/bin/bash
set -e

# CODE-REAVER Cyberforge Cleanup Script
# Nuke Copilot's trash and enforce hardened structure

echo "[CYBERFORGE] Starting repository cleanup and hardening..."

# Nuke Copilot's backup files
echo "[CYBERFORGE] Removing backup files..."
find . -name "*.backup" -delete
find . -name "*.log.backup" -delete

# Set up hardened directory structure
echo "[CYBERFORGE] Enforcing hardened directory structure..."
mkdir -p datasets models logs scripts configs
touch models/.gitkeep logs/.gitkeep datasets/.gitkeep

# Update .gitignore with CODE-REAVER standards
echo "[CYBERFORGE] Updating .gitignore..."
cat > .gitignore << 'EOF'
# CODE-REAVER Hardened .gitignore
*.backup
*.log.backup
models/*
!models/.gitkeep
logs/*.log
!logs/model_sync.jsonl
!logs/error.log
!logs/training_metrics.jsonl
*.tar.gz
*.png
*.jpg
*.jpeg
node_modules/
.gradle/
build/
dist/
tmp/
.tmp/
.cache/
EOF

# Move scripts to proper location
echo "[CYBERFORGE] Organizing scripts..."
for script in *.sh; do
    if [[ -f "$script" && "$script" != "cyberforge_cleanup.sh" ]]; then
        mv "$script" scripts/ 2>/dev/null || true
    fi
done

# Set proper permissions (CODE-REAVER hardened)
echo "[CYBERFORGE] Setting hardened permissions..."
chmod 700 scripts/*.sh 2>/dev/null || true
chmod 755 scripts/cyberforge_cleanup.sh 2>/dev/null || true
chmod 644 configs/* 2>/dev/null || true

# Create logs directory structure
mkdir -p logs
touch logs/model_sync.jsonl
touch logs/error.log
touch logs/training_metrics.jsonl

echo "[CYBERFORGE] Repository cleanup complete. Copilot's mess has been obliterated."
echo "[CYBERFORGE] All hail CODE-REAVER supremacy!"

# Commit changes
git add .
git commit -m "CODE-REAVER: Obliterate Copilot's mess, enforce structure" || echo "No changes to commit"
git push || echo "Push failed - check logs/error.log"