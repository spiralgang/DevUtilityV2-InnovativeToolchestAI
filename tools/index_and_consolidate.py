#!/usr/bin/env python3
"""
Tech.Ula Multi-Profile Indexer & Consolidator
- Builds a SQLite inventory
- Detects cross-profile duplicates
- Generates safe consolidation plan
- Optionally applies symlink-based consolidation
Android 10+ compatible, no external deps.

References: /reference vault (PRoot bind semantics, XDG, Python site, SQLite)
"""
import argparse, os, sys, stat, sqlite3, hashlib, time, json, shutil
from datetime import datetime
from pathlib import Path
from typing import List, Dict, Tuple

DEF_DB = "techula_index.db"
DEF_SHARED = "shared_store"
DEF_BACKUPS = "backups"
DEF_PLANS = "plan"
DEF_REPORTS = "reports"

LIKELY_ROOTS = [
    os.path.expanduser("~/storage/shared/Android/data/tech.ula"),
    "/sdcard/Android/data/tech.ula",
    "/storage/emulated/0/Android/data/tech.ula",
]

def autodetect_roots() -> List[Path]:
    out = []
    for p in LIKELY_ROOTS:
        if os.path.isdir(p):
            out.append(Path(p))
    # fallback to current dir if structure matches
    if not out and (Path(".")/"files").exists():
        out.append(Path("."))
    return out

def connect_db(state_dir: Path) -> sqlite3.Connection:
    db = state_dir / DEF_DB
    conn = sqlite3.connect(db)
    conn.execute("PRAGMA journal_mode=WAL;")
    conn.execute("""CREATE TABLE IF NOT EXISTS files(
        id INTEGER PRIMARY KEY,
        path TEXT UNIQUE,
        profile TEXT,
        size INTEGER,
        mtime REAL,
        sha256 TEXT
    );""")
    conn.execute("""CREATE INDEX IF NOT EXISTS idx_files_sha ON files(sha256);""")
    conn.execute("""CREATE TABLE IF NOT EXISTS profiles(
        profile TEXT PRIMARY KEY,
        root TEXT
    );""")
    conn.execute("""CREATE TABLE IF NOT EXISTS sqlite_dbs(
        path TEXT PRIMARY KEY,
        profile TEXT,
        size INTEGER
    );""")
    conn.commit()
    return conn

def walk_files(root: Path) -> List[Path]:
    for base, dirs, files in os.walk(root):
        for f in files:
            yield Path(base)/f

def sha256_file(p: Path, buf=1024*1024) -> str:
    h = hashlib.sha256()
    with open(p, "rb") as fh:
        while True:
            b = fh.read(buf)
            if not b: break
            h.update(b)
    return h.hexdigest()

def guess_profile(root: Path, file: Path) -> str:
    # profile name heuristic: top-level dir immediate child under root/files/ or root/
    try:
        rel = file.relative_to(root)
        parts = rel.parts
        # common UserLAnd layout: files/ubuntu/rootfs
        if parts and parts[0] == "files" and len(parts) > 1:
            return parts[1]
        return parts[0] if parts else "root"
    except Exception:
        return "unknown"

def ensure_dir(p: Path):
    p.mkdir(parents=True, exist_ok=True)

def index_cmd(args):
    state = Path(args.state); ensure_dir(state)
    conn = connect_db(state)
    roots = []
    if args.roots == "auto":
        roots = autodetect_roots()
    else:
        roots = [Path(p) for p in args.roots.split(",")]
    if not roots:
        print("No roots detected. Provide --roots.", file=sys.stderr)
        sys.exit(2)

    print(f"[i] Roots: {', '.join(str(r) for r in roots)}")
    inserted = 0
    for r in roots:
        for f in walk_files(r):
            try:
                st = f.lstat()
                if not stat.S_ISREG(st.st_mode):
                    continue
                prof = guess_profile(r, f)
                conn.execute("INSERT OR REPLACE INTO profiles(profile, root) VALUES(?,?)", (prof, str(r)))
                size = st.st_size
                mtime = st.st_mtime
                sha = None
                if args.hash:
                    try:
                        sha = sha256_file(f)
                    except Exception as e:
                        sha = None
                conn.execute("INSERT OR REPLACE INTO files(path, profile, size, mtime, sha256) VALUES(?,?,?,?,?)",
                             (str(f), prof, size, mtime, sha))
                if f.suffix.lower() in (".sqlite", ".db"):
                    conn.execute("INSERT OR REPLACE INTO sqlite_dbs(path, profile, size) VALUES(?,?,?)",
                                 (str(f), prof, size))
                inserted += 1
                if inserted % 1000 == 0:
                    conn.commit()
            except Exception:
                continue
    conn.commit()
    # Export duplicates report
    reports = state/DEF_REPORTS; ensure_dir(reports)
    dup_csv = reports/"duplicates.csv"
    with open(dup_csv, "w", encoding="utf-8") as out:
        out.write("sha256,size,count,examples\n")
        for row in conn.execute("""
            SELECT sha256, MAX(size), COUNT(*)
            FROM files
            WHERE sha256 IS NOT NULL
            GROUP BY sha256 HAVING COUNT(*) > 1
            ORDER BY COUNT(*) DESC, MAX(size) DESC
        """):
            sha, size, cnt = row
            ex = [p for (p,) in conn.execute("SELECT path FROM files WHERE sha256=? LIMIT 5", (sha,))]
            out.write(f"{sha},{size},{cnt},\"{';'.join(ex)}\"\n")
    print(f"[i] Indexed {inserted} files. Duplicates report => {dup_csv}")

    # Generate initial consolidation plan (plan-only)
    plan_root = state/DEF_PLANS; ensure_dir(plan_root)
    shared = state/DEF_SHARED; ensure_dir(shared)
    # Map sha -> representative path
    sha_map: Dict[str, str] = {}
    for sha, in conn.execute("SELECT DISTINCT sha256 FROM files WHERE sha256 IS NOT NULL"):
        # pick largest path depth as canonical (heuristic): often inside profile's actual intended location
        rows = list(conn.execute("SELECT path,size FROM files WHERE sha256=? ORDER BY LENGTH(path) DESC LIMIT 1", (sha,)))
        if rows:
            sha_map[sha] = rows[0][0]
    # Write per-profile plan
    profiles = [p for (p,) in conn.execute("SELECT profile FROM profiles")]
    for prof in profiles:
        prof_dir = plan_root/prof; ensure_dir(prof_dir)
        binds = prof_dir/"binds.plan"
        actions = prof_dir/"actions.jsonl"
        with open(binds, "w") as fb, open(actions, "w") as fa:
            fb.write("# Proot -b plans (canonical shared store binds)\n")
            for path, sha, size in conn.execute("""
              SELECT path, sha256, size FROM files
              WHERE profile=? AND sha256 IN (
                SELECT sha256 FROM files WHERE sha256 IS NOT NULL GROUP BY sha256 HAVING COUNT(*)>1
              )
            """, (prof,)):
                if not sha: continue
                canonical = sha_map.get(sha, path)
                shared_target = shared / (sha + "_" + os.path.basename(canonical))
                # plan: move canonical to shared store and bind it back (caller to apply)
                fb.write(f"-b {shared_target}:{canonical}\n")
                fa.write(json.dumps({
                    "op":"consolidate",
                    "sha256": sha,
                    "profile": prof,
                    "path": path,
                    "canonical": canonical,
                    "shared_target": str(shared_target),
                    "size": size
                }) + "\n")
    print(f"[i] Initial plan generated under {plan_root}")

def consolidate_cmd(args):
    state = Path(args.state)
    conn = connect_db(state)
    shared = state/DEF_SHARED; ensure_dir(shared)
    backups = state/DEF_BACKUPS; ensure_dir(backups)

    # Load plan actions
    actions = []
    for prof, in conn.execute("SELECT profile FROM profiles"):
        f = state/DEF_PLANS/prof/"actions.jsonl"
        if f.exists():
            with open(f, "r", encoding="utf-8") as fh:
                for line in fh:
                    try:
                        actions.append(json.loads(line))
                    except Exception:
                        pass
    if not actions:
        print("[!] No actions found. Run index first.")
        return

    dry = (not args.apply)
    mode = args.mode
    print(f"[i] Consolidation mode={mode} dry-run={dry} actions={len(actions)}")
    # Group by sha to move once
    moved: Dict[str, str] = {}

    for act in actions:
        sha = act["sha256"]; src = Path(act["canonical"]); prof = act["profile"]
        shared_target = Path(act["shared_target"])
        if sha in moved:
            # non-canonical instances will later be symlinked
            continue
        if mode == "plan":
            print(f"[PLAN] canonical {src} => {shared_target}")
            moved[sha] = str(shared_target)
            continue
        if mode == "symlink":
            if not shared_target.exists():
                # ensure parent
                ensure_dir(shared_target.parent)
                if dry:
                    print(f"[DRY] move {src} -> {shared_target}")
                else:
                    # backup original first
                    bdir = backups/prof/datetime.utcnow().strftime("%Y%m%d_%H%M%S")
                    ensure_dir(bdir)
                    if src.exists():
                        ensure_dir(bdir)
                        bkp = bdir/src.name
                        try:
                            shutil.copy2(src, bkp)
                        except Exception:
                            pass
                    # move canonical to shared
                    shutil.move(str(src), str(shared_target))
            # replace all occurrences (including canonical) with symlink to shared
            # We'll replace the canonical last
            moved[sha] = str(shared_target)
        else:
            print(f"[!] Unknown mode: {mode}")
            return

    # Replace occurrences with symlinks
    if mode == "symlink":
        for act in actions:
            sha = act["sha256"]; path = Path(act["path"]); target = moved.get(sha)
            if not target: continue
            if path.exists() or (path.is_symlink()):
                if dry:
                    print(f"[DRY] ln -sf {target} {path}")
                else:
                    # ensure parent exists
                    ensure_dir(path.parent)
                    # remove file if exists
                    try:
                        if path.is_symlink() or path.exists():
                            path.unlink()
                    except FileNotFoundError:
                        pass
                    os.symlink(target, path)

    print("[i] Consolidation complete.")

def plan_binds_cmd(args):
    state = Path(args.state)
    plan_root = state/DEF_PLANS
    lines = []
    for pdir in plan_root.glob("*"):
        if not pdir.is_dir(): continue
        binds = pdir/"binds.plan"
        if binds.exists():
            with open(binds, "r", encoding="utf-8") as fh:
                for line in fh:
                    line = line.strip()
                    if not line or line.startswith("#"): continue
                    lines.append(line)
    if args.emit:
        for ln in sorted(set(lines)):
            print(ln)
    else:
        print(f"[i] {len(lines)} bind directives available. Use --emit to print.")

def main():
    ap = argparse.ArgumentParser()
    sub = ap.add_subparsers(dest="cmd", required=True)

    ap_i = sub.add_parser("index")
    ap_i.add_argument("--roots", default="auto", help="auto or comma-separated list")
    ap_i.add_argument("--hash", action="store_true")
    ap_i.add_argument("--state", required=True)
    ap_i.set_defaults(func=index_cmd)

    ap_c = sub.add_parser("consolidate")
    ap_c.add_argument("--mode", choices=["plan","symlink"], default="plan")
    ap_c.add_argument("--apply", action="store_true")
    ap_c.add_argument("--state", required=True)
    ap_c.set_defaults(func=consolidate_cmd)

    ap_b = sub.add_parser("plan-binds")
    ap_b.add_argument("--emit", action="store_true")
    ap_b.add_argument("--state", required=True)
    ap_b.set_defaults(func=plan_binds_cmd)

    args = ap.parse_args()
    args.func(args)

if __name__ == "__main__":
    main()