#!/usr/bin/env bash
#
# Robust repository & environment validation script
# - Fast by default, deeper checks optional via flags
# - Designed to be safe to run from any CWD (resolves repo root)
# - Helpful, actionable error messages and machine-friendly exit codes
#
# Usage:
#   .ops/validate-system.sh [--fix] [--strict] [--verbose] [--no-json-check]
#
# Flags:
#   --fix            Create missing optional directories (logs) and set sane perms
#   --strict         Turn on stricter checks (git working tree clean, minimal versions)
#   --verbose        Verbose output
#   --no-json-check  Skip JSON validation (useful when jq not available)
#
# Exit codes:
#   0  - all checks passed
#   1  - usage / runtime error
#   2  - required check failed (missing tool/file/dir, invalid JSON when required)
#   3  - strict-mode failure (clean working tree, versions)
#
set -euo pipefail

# -------------------------
# Basic helpers & settings
# -------------------------
SELF="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/$(basename "${BASH_SOURCE[0]}")"
start_ts=$(date -u +%s)

# CLI
fix=false
strict=false
verbose=false
json_check=true

while [ "${#:-}" -gt 0 ]; do
  case "$1" in
    --fix) fix=true; shift ;;
    --strict) strict=true; shift ;;
    --verbose) verbose=true; shift ;;
    --no-json-check) json_check=false; shift ;;
    -h|--help) sed -n '1,120p' "$SELF"; exit 0 ;;
    --) shift; break ;;
    -*)
      echo "Unknown flag: $1" >&2
      exit 1
      ;;
    *) break ;;
  esac
done

# Color helpers if output is a TTY
if [ -t 1 ]; then
  RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[0;33m'; CYAN='\033[0;36m'; NC='\033[0m'
else
  RED=''; GREEN=''; YELLOW=''; CYAN=''; NC=''
fi

log() { [ "$verbose" = true ] && echo -e "${CYAN}[info]${NC} $*"; }
warn() { echo -e "${YELLOW}[warn]${NC} $*" >&2; }
err() { echo -e "${RED}[error]${NC} $*" >&2; }
ok() { echo -e "${GREEN}[ok]${NC} $*"; }

# track failures for an aggregated exit
failures=0
failure_msgs=()

# -------------------------
# Resolve repository root
# -------------------------
repo_root=""
if command -v git >/dev/null 2>&1; then
  # git rev-parse will fail in non-repo; silence and fallback
  repo_root="$(git rev-parse --show-toplevel 2>/dev/null || true)"
fi

if [ -z "$repo_root" ]; then
  # Fallback: parent of .ops/ (this script's parent)
  script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  repo_root="$(cd "$script_dir/.." && pwd)"
  log "git not available or not inside a repo; inferred repo root as $repo_root"
fi

cd "$repo_root" || { err "cannot cd to repo root: $repo_root"; exit 1; }

# -------------------------
# Required commands & tools
# -------------------------
required_cmds=(bash git sh)
optional_cmds=(jq yq shellcheck)

for cmd in "${required_cmds[@]}"; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    err "required command missing: $cmd"
    failures=$((failures+1))
    failure_msgs+=("missing command: $cmd")
  else
    log "found command: $cmd ($(command -v "$cmd"))"
  fi
done

# detect availability of optional utilities
have_jq=false; have_yq=false; have_shellcheck=false
if command -v jq >/dev/null 2>&1; then have_jq=true; fi
if command -v yq >/dev/null 2>&1; then have_yq=true; fi
if command -v shellcheck >/dev/null 2>&1; then have_shellcheck=true; fi

# honor user's --no-json-check flag
if [ "$json_check" = false ]; then have_jq=false; fi

# -------------------------
# Configurable expected paths
# -------------------------
# required_files: must exist
required_files=(
  ".git"                          # repo marker (dir or file for worktrees)
  "configs/activation_rules.json" # critical config file
)

# required_dirs: must exist and be directories
required_dirs=(
  ".github/workflows"
  "scripts"
)

# optional_dirs: expected but can be auto-created in --fix mode
optional_dirs=(
  "logs"
  "configs"
)

# loose expectations (warnings only)
recommended_files=(
  "README.md"
  "LICENSE"
)

# -------------------------
# Check files & directories
# -------------------------
# Accept .git as either dir or file (worktrees use a file pointing to gitdir)
if [ -e ".git" ]; then
  log ".git exists (ok)"
else
  err ".git not found in repository root ($repo_root)"
  failures=$((failures+1))
  failure_msgs+=(".git missing")
fi

for f in "${required_files[@]}"; do
  if [ ! -e "$f" ]; then
    err "required file missing: $f"
    failures=$((failures+1))
    failure_msgs+=("missing file: $f")
  else
    if [ -f "$f" ]; then log "found file: $f"; else log "found path: $f"; fi
  fi
done

for d in "${required_dirs[@]}"; do
  if [ ! -d "$d" ]; then
    err "required directory missing: $d"
    failures=$((failures+1))
    failure_msgs+=("missing dir: $d")
  else
    log "found directory: $d"
  fi
done

for d in "${optional_dirs[@]}"; do
  if [ ! -d "$d" ]; then
    if [ "$fix" = true ]; then
      log "creating optional directory: $d"
      mkdir -p "$d"
      ok "created $d"
    else
      warn "optional directory missing: $d"
      failure_msgs+=("optional missing: $d")
    fi
  else
    log "optional directory present: $d"
  fi
done

for r in "${recommended_files[@]}"; do
  if [ ! -e "$r" ]; then
    warn "recommended file missing: $r"
  else
    log "recommended file present: $r"
  fi
done

# -------------------------
# Basic JSON validation
# -------------------------
if [ -f "configs/activation_rules.json" ]; then
  if [ "$have_jq" = true ]; then
    if ! jq empty "configs/activation_rules.json" >/dev/null 2>&1; then
      err "configs/activation_rules.json is not valid JSON"
      failures=$((failures+1))
      failure_msgs+=("invalid JSON: configs/activation_rules.json")
    else
      log "configs/activation_rules.json: valid JSON"
      # Basic sanity: ensure non-empty object/array
      size=$(jq 'if type=="object" then keys | length elif type=="array" then length else 0 end' configs/activation_rules.json)
      if [ "$size" -eq 0 ]; then
        warn "configs/activation_rules.json parsed but appears empty (0 keys/elements)"
      fi
    fi
  else
    warn "jq not available; skipping JSON validation for configs/activation_rules.json"
  fi
fi

# -------------------------
# Optional YAML validation (if yq present)
# -------------------------
if [ "$have_yq" = true ]; then
  # validate any .yml/.yaml in configs
  if [ -d "configs" ]; then
    shopt_set=false
    if command -v bash >/dev/null 2>&1; then
      # shellcheck disable=SC2034
      shopt_set=true
    fi
    found_yaml=false
    while IFS= read -r -d '' file; do
      found_yaml=true
      if ! yq eval '.' "$file" >/dev/null 2>&1; then
        err "invalid YAML: $file"
        failures=$((failures+1))
        failure_msgs+=("invalid YAML: $file")
      else
        log "valid YAML: $file"
      fi
    done < <(find configs -type f \( -iname '*.yml' -o -iname '*.yaml' \) -print0 || true)
    [ "$found_yaml" = true ] || log "no YAML files in configs to validate"
  fi
fi

# -------------------------
# Permissions & executables
# -------------------------
# Ensure scripts dir files are executable and have a shebang
if [ -d "scripts" ]; then
  while IFS= read -r -d '' file; do
    # Only inspect regular files
    if [ -f "$file" ]; then
      # check for shebang
      if ! head -n1 "$file" | grep -q '^#!'; then
        warn "script missing shebang: $file"
        failure_msgs+=("no-shebang: $file")
      fi
      # ensure executable bit set for owner
      if [ ! -x "$file" ]; then
        warn "script not executable: $file"
        if [ "$fix" = true ]; then
          chmod +x "$file" && ok "made executable: $file" || { err "failed to chmod +x $file"; failures=$((failures+1)); }
        else
          failure_msgs+=("not-exec: $file")
        fi
      fi
    fi
  done < <(find scripts -maxdepth 2 -type f -print0 || true)
fi

# -------------------------
# Strict mode checks
# -------------------------
if [ "$strict" = true ]; then
  # Check git working tree clean
  if command -v git >/dev/null 2>&1; then
    if ! git diff --quiet --ignore-submodules --cached; then
      err "git index has staged changes"
      failures=$((failures+1))
      failure_msgs+=("git index dirty")
    fi
    if ! git diff --quiet --ignore-submodules; then
      err "git working tree has unstaged changes"
      failures=$((failures+1))
      failure_msgs+=("git working tree dirty")
    fi
    # warn if repository is shallow
    if git rev-parse --is-shallow-repository >/dev/null 2>&1 && [ "$(git rev-parse --is-shallow-repository 2>/dev/null)" = "true" ]; then
      warn "repository is a shallow clone; CI runs may be affected"
    fi
  fi

  # Minimal versions (warn/fail)
  # Define minimums here as needed, but do not attempt heavy parsing if commands missing
  min_git_major=2
  min_bash_major=4

  if command -v git >/dev/null 2>&1; then
    git_ver="$(git --version | awk '{print $3}')"
    git_major="${git_ver%%.*}"
    if [ "$git_major" -lt "$min_git_major" ] 2>/dev/null; then
      warn "git version is older than recommended ($git_ver < ${min_git_major}.x)"
      failure_msgs+=("git-version:$git_ver")
      failures=$((failures+1))
    else
      log "git version OK: $git_ver"
    fi
  fi

  if command -v bash >/dev/null 2>&1; then
    bash_ver="${BASH_VERSION%%[^0-9.]*}"
    bash_major="${bash_ver%%.*}"
    if [ "$bash_major" -lt "$min_bash_major" ] 2>/dev/null; then
      warn "bash version is older than recommended ($bash_ver < ${min_bash_major}.x)"
      failure_msgs+=("bash-version:$bash_ver")
      failures=$((failures+1))
    else
      log "bash version OK: $bash_ver"
    fi
  fi
fi

# -------------------------
# Optional linting
# -------------------------
if [ "$have_shellcheck" = true ]; then
  # only lint a few key scripts to keep runtime small
  lint_targets=(.ops/validate-system.sh scripts/*.sh)
  for t in "${lint_targets[@]}"; do
    for f in $t; do
      [ -f "$f" ] || continue
      if ! shellcheck --external-sources "$f" >/dev/null 2>&1; then
        warn "shellcheck reported issues in $f (run shellcheck $f for details)"
      else
        log "shellcheck OK: $f"
      fi
    done
  done
fi

# -------------------------
# Report & exit
# -------------------------
elapsed=$(( $(date -u +%s) - start_ts ))

if [ "$failures" -eq 0 ]; then
  ok "All validation tests pass in ~${elapsed}s"
  exit 0
else
  err "Validation failed: $failures problem(s) detected in ~${elapsed}s"
  for m in "${failure_msgs[@]}"; do
    echo "- $m" >&2
  done
  # For strict-mode failures, use exit 3
  if [ "$strict" = true ]; then
    exit 3
  fi
  exit 2
fi

# -------------------------
# References (in-file)
# -------------------------
# - See /reference for project standards and canonical links.
# - jq: https://stedolan.github.io/jq/
# - yq: https://mikefarah.gitbook.io/yq/
# - shellcheck: https://www.shellcheck.net/
# - git rev-parse: https://git-scm.com/docs/git-rev-parse
# - Bash best practices: https://tldp.org/LDP/abs/html/
#
# Rationale:
# - Resolve repo root reliably so script can be executed from subdirectories or CI checkouts.
# - Fail fast for required checks but offer --fix to remedy simple, safe issues (like creating logs/).
# - Use optional tooling (jq/yq/shellcheck) when available to keep runtime small on minimal CI images.
# - Provide clear exit codes for automation to react differently to strict-mode vs required failures.
#
# End
