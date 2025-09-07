#!/usr/bin/env python3
"""
Generate per-profile Python .pth injectors pointing to a shared site-packages
to avoid path fragmentation under changing binds.

- Shared site located under state/python_shared/site-packages
- Emits per-profile _site_unify.pth (to be placed in user site dir)
References: /reference vault (Python site, sysconfig)
"""
import os, sys, sqlite3, json
from pathlib import Path

def main():
    import argparse
    ap = argparse.ArgumentParser()
    ap.add_argument("--state", required=True)
    args = ap.parse_args()

    state = Path(args.state)
    shared = state/"python_shared"/"site-packages"
    shared.mkdir(parents=True, exist_ok=True)

    db = state/"techula_index.db"
    if not db.exists():
        print("[!] Run index first.", file=sys.stderr)
        sys.exit(2)

    # Find per-profile $HOME candidates from files table heuristics
    conn = sqlite3.connect(db)
    profiles = [p for (p,) in conn.execute("SELECT profile FROM profiles")]
    out_dir = state/"python"; out_dir.mkdir(exist_ok=True)
    for prof in profiles:
        pth = out_dir/f"{prof}_site_unify.pth"
        pth.write_text(str(shared)+"\n", encoding="utf-8")
        print(f"[i] Wrote {pth} (append to user site dir for profile {prof})")

    print("[hint] Place each _site_unify.pth into the profile's user site-packages (python -m site --user-site).")

if __name__ == "__main__":
    main()