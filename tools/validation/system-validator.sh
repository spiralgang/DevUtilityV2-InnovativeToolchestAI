#!/bin/bash
#
# System and repository component validator.
# Ensures that the repository's core components and configurations are sound.
#
# Rationale:
# Centralizes validation logic. Moving this script to a `tools/` directory
# establishes a clear, organized structure for all operational tooling.
#
set -euo pipefail

# --- Configuration ---
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

# --- Main Validation Logic ---
echo -e "${BLUE}🧪 Running System Validation Suite...${NC}"
printf -- '-%.0s' {1..60}; echo

# Test 1: Core Dependencies
echo -e "\n${BLUE}[TEST 1]${NC} Checking core runtime dependencies..."
command -v python3 >/dev/null || { echo "❌ python3 not found"; exit 1; }
command -v bash >/dev/null || { echo "❌ bash not found"; exit 1; }
echo -e "${GREEN}✅ Core dependencies are present.${NC}"

# Test 2: GitHub Actions Workflow Integrity
echo -e "\n${BLUE}[TEST 2]${NC} Validating GitHub Actions workflows with actionlint..."
if command -v actionlint &> /dev/null; then
    # Lint all YAML files in the workflows directory for correctness.
    actionlint .github/workflows/*.yml
    echo -e "${GREEN}✅ All GitHub Actions workflows passed validation.${NC}"
else
    # This block should never be reached in a correctly configured CI run.
    echo -e "${YELLOW}⚠️ actionlint not found. Critical validation skipped.${NC}" >&2
    exit 1
fi

# Test 3: Android Project Sanity Check
echo -e "\n${BLUE}[TEST 3]${NC} Performing Android project sanity check..."
if [[ -f "build.gradle" ]] && [[ -f "gradlew" ]]; then
    echo -e "${GREEN}✅ Gradle wrapper and build script found. Project appears to be a valid Android project.${NC}"
else
    echo -e "${YELLOW}⚠️ build.gradle or gradlew not found. This may not be a functional Android project.${NC}"
fi

printf -- '-%.0s' {1..60}; echo
echo -e "\n${GREEN}🎉 System Validation Suite Passed!${NC}\n"