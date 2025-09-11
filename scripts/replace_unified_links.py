#!/usr/bin/env python3
"""
Replace Ollama timidlly/unified-v1 links with Hugging Face equivalents.

Behavior:
- Replaces obvious Ollama URLs and protocols with https://huggingface.co/timidlly/unified-v1
- For "ollama pull ..." command lines, preserves the original line and inserts HF alternatives
  as a commented line right after (so semantics remain visible).
- Preserves any tag suffix (e.g. :tag) by appending it to the HF form (user can adjust later).
- Works as dry-run by default; use --apply to write files.
- Skips .git directory and common binary files.

Usage:
  ./scripts/replace_unified_links.py       # dry-run: prints proposed updates
  ./scripts/replace_unified_links.py --apply --branch "fix/replace-unified-links"
"""

from __future__ import annotations
import argparse
import re
import sys
from pathlib import Path
from typing import List, Tuple

# Patterns to find (case-insensitive where appropriate)
# The group 'model' captures timidlly/unified-v1 and optional tag in group 'tag'
URL_OLLAMA_RE = re.compile(
    r"https?://(?:www\.)?ollama\.com/(?P<model>timidlly/unified-v1)(?P<tag>:[^\s'\")]+)?",
    flags=re.IGNORECASE,
)
OLLAMA_SCHEME_RE = re.compile(
    r"(?P<prefix>\b)ollama://(?P<model>timidlly/unified-v1)(?P<tag>:[^\s'\"`]+)?",
    flags=re.IGNORECASE,
)
OLLAMA_COLON_RE = re.compile(
    r"(?P<prefix>\b)ollama:(?P<model>timidlly/unified-v1)(?P<tag>:[^\s'\"`]+)?",
    flags=re.IGNORECASE,
)
OLLAMA_PULL_RE = re.compile(
    r"(?P<leading>^|\s)(?P<cmd>ollama\s+pull\s+)(?P<model>timidlly/unified-v1)(?P<tag>:[^\s'\"`]+)?(?P<trail>.*)$",
    flags=re.IGNORECASE | re.MULTILINE,
)

# Files to skip by name or extension
SKIP_DIRS = {".git", ".venv", "venv", "node_modules", ".cache"}
SKIP_EXTS = {
    ".png", ".jpg", ".jpeg", ".gif", ".svg", ".zip", ".tar", ".gz", ".bin", ".exe", ".dll", ".so",
}

HF_BASE = "https://huggingface.co/timidlly/unified-v1"

def comment_prefix_for_path(path: Path) -> str:
    if path.suffix.lower() in {".md", ".markdown"}:
        return "<!--"
    # default to shell/python style
    return "#"

def comment_suffix_for_path(path: Path) -> str:
    if path.suffix.lower() in {".md", ".markdown"}:
        return "-->"
    return ""

def transform_text(path: Path, text: str) -> Tuple[str, List[str]]:
    """Return (new_text, list_of_change_descriptions)."""
    changes: List[str] = []
    new = text

    # 1) Replace full https://ollama.com/... URLs directly
    def repl_url(m: re.Match) -> str:
        model = m.group("model")
        tag = m.group("tag") or ""
        replacement = HF_BASE + (tag or "")
        changes.append(f"URL -> {replacement}")
        return replacement

    new, n_url = URL_OLLAMA_RE.subn(repl_url, new)

    # 2) Replace ollama://... scheme
    def repl_scheme(m: re.Match) -> str:
        model = m.group("model")
        tag = m.group("tag") or ""
        replacement = HF_BASE + (tag or "")
        changes.append(f"ollama:// -> {replacement}")
        return replacement

    new, n_scheme = OLLAMA_SCHEME_RE.subn(repl_scheme, new)

    # 3) Replace ollama:timidlly/unified-v1 patterns (inline)
    def repl_colon(m: re.Match) -> str:
        model = m.group("model")
        tag = m.group("tag") or ""
        replacement = HF_BASE + (tag or "")
        changes.append(f"ollama: -> {replacement}")
        return replacement

    new, n_colon = OLLAMA_COLON_RE.subn(repl_colon, new)

    # 4) For 'ollama pull' commands: keep original, insert commented HF alternatives
    # We'll iterate lines and build output to preserve context and exact indentation.
    if "ollama pull" in new.lower():
        lines = new.splitlines(keepends=True)
        out_lines: List[str] = []
        for ln in lines:
            m = OLLAMA_PULL_RE.search(ln)
            if m:
                leading = m.group("leading") or ""
                cmd = m.group("cmd")
                model = m.group("model")
                tag = m.group("tag") or ""
                trail = m.group("trail") or ""
                out_lines.append(ln)  # keep original
                # build alternatives
                hf_cmd = f"hf download {model}{tag}"
                git_cmd = f"git clone https://huggingface.co/{model}"
                # Comment style
                c_pref = comment_prefix_for_path(path)
                c_suf = comment_suffix_for_path(path)
                # Use the same indentation as line start
                indent = ""
                leading_ws = re.match(r"^(\s*)", ln)
                if leading_ws:
                    indent = leading_ws.group(1)
                # Insert commented alternatives
                comment_lines = []
                comment_lines.append(f"{indent}{c_pref} HF alternative: {hf_cmd} {c_suf}\n")
                comment_lines.append(f"{indent}{c_pref} Git alternative: {git_cmd} {c_suf}\n")
                out_lines.extend(comment_lines)
                changes.append(f"Inserted HF alternatives for ollama pull {model}{tag}")
            else:
                out_lines.append(ln)
        new = "".join(out_lines)

    return new, changes

def files_to_check(root: Path) -> List[Path]:
    result: List[Path] = []
    for p in root.rglob("*"):
        if p.is_dir():
            if p.name in SKIP_DIRS:
                # skip walking into this dir
                # rglob still visits inside dirs; we'll simply skip by continue
                continue
        if p.is_file():
            if p.suffix.lower() in SKIP_EXTS:
                continue
            if ".git" in p.parts:
                continue
            result.append(p)
    return result

def main(argv: List[str] | None = None) -> int:
    ap = argparse.ArgumentParser(
        description="Replace Ollama timidlly/unified-v1 references with Hugging Face equivalents."
    )
    ap.add_argument("--apply", action="store_true", help="Write changes to files (dry-run by default).")
    ap.add_argument("--root", default=".", help="Repository root to scan (default: current directory).")
    ap.add_argument("--branch", default="fix/replace-unified-links", help="Suggested branch name for commits.")
    args = ap.parse_args(argv)

    root = Path(args.root).resolve()
    print(f"[replace_unified_links] Scanning {root} (dry-run={not args.apply})")

    candidates = files_to_check(root)
    total_changes = 0
    changed_files: List[Tuple[Path, List[str]]] = []

    for f in candidates:
        try:
            text = f.read_text(encoding="utf-8")
        except Exception:
            continue
        new_text, changes = transform_text(f, text)
        if changes and new_text != text:
            total_changes += len(changes)
            changed_files.append((f, changes))
            print(f"[PROPOSE] {f} -> {len(changes)} change(s): {changes}")
            if args.apply:
                # write atomically
                bak = f.with_suffix(f.suffix + ".bak")
                f.rename(bak)
                f.write_text(new_text, encoding="utf-8")
                print(f"[WRITE] updated {f} (backup at {bak})")
        elif changes and new_text == text:
            # unusual: logged change but text same; still report
            print(f"[NOTE] {f} had detections but no textual change necessary: {changes}")

    print(f"[replace_unified_links] Completed. Files with proposed changes: {len(changed_files)}")
    if not args.apply:
        print("Run with --apply to write changes. Recommended workflow:")
        print("  git checkout -b fix/replace-unified-links")
        print("  ./scripts/replace_unified_links.py --apply")
        print("  git add -A && git commit -m \"Replace Ollama timidlly/unified-v1 links with HuggingFace URLs\"")
        print("  git push --set-upstream origin fix/replace-unified-links")
        print('  Create a PR and review changes; verify `grep -R "timidlly/unified-v1" -n` shows HF URLs or commented alternatives.')
    else:
        print("Changes written. Please review, git-add and commit on branch and push.")
        print("Suggested commit message:")
        print('"Replace Ollama timidlly/unified-v1 links with HuggingFace URLs"')

    return 0

if __name__ == "__main__":
    sys.exit(main())
