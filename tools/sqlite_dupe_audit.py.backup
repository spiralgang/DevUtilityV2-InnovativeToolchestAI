#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SQLite duplication auditor:
- Lists duplicated DB filenames across profiles
- Suggests canonical location and optional symlink/bind plan
No destructive changes; emits JSONL suggestions.

References: /reference vault (SQLite, XDG, consolidation)
"""
import os, sys, sqlite3, json
from pathlib import Path
from collections import defaultdict

def main():
    import argparse
    ap = argparse.ArgumentParser()
    ap.add_argument("--state", required=True)
    args = ap.parse_args()
    db = Path(args.state)/"techula_index.db"
    if not db.exists():
        print("[!] Run index first.", file=sys.stderr)
        sys.exit(2)

    conn = sqlite3.connect(db)
    rows = list(conn.execute("SELECT path, profile FROM sqlite_dbs"))
    groups = defaultdict(list)
    for path, prof in rows:
        name = os.path.basename(path)
        groups[name].append((path, prof))

    out = Path(args.state)/"reports"/"sqlite_dupes.jsonl"
    out.parent.mkdir(parents=True, exist_ok=True)
    with out.open("w", encoding="utf-8") as fh:
        for name, items in groups.items():
            if len(items) < 2: continue
            canonical = max(items, key=lambda x: len(x[0]))[0]
            fh.write(json.dumps({
                "db_name": name,
                "canonical": canonical,
                "duplicates": [{"path": p, "profile": prof} for p, prof in items if p != canonical]
            })+"\n")
    print(f"[i] SQLite dupes => {out}")

if __name__ == "__main__":
    main()