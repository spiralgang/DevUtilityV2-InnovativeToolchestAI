# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This file is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Content indexer with FTS5 + fallback tokens table.
- Scans files table; indexes text files only (size <= 32MB by default).
- Skips unchanged via (size, mtime) and sha256 if present.
Android 10 compatible; stdlib only.
"""
import os, sys, sqlite3, argparse, mimetypes, time
from pathlib import Path

MAX_SIZE = 32 * 1024 * 1024
TEXT_EXT = {".txt",".md",".log",".cfg",".ini",".json",".yaml",".yml",".xml",".csv",
            ".py",".js",".ts",".java",".c",".h",".cpp",".go",".rs",".sh",".bash",".zsh",
            ".conf",".service",".sql"}
def is_text(path: Path, size: int) -> bool:
  if size > MAX_SIZE: return False
  ext = path.suffix.lower()
  if ext in TEXT_EXT: return True
  typ, _ = mimetypes.guess_type(str(path))
  return typ is not None and typ.startswith("text/")

def ensure_schema(conn: sqlite3.Connection):
  conn.execute("CREATE TABLE IF NOT EXISTS content_documents(path TEXT PRIMARY KEY, size INTEGER, mtime REAL, sha256 TEXT)")
  # try FTS5
  try:
    conn.execute("CREATE VIRTUAL TABLE IF NOT EXISTS content_fts USING fts5(path, body)")
    conn.execute("PRAGMA user_version=1")
    return "fts"
  except sqlite3.OperationalError:
    conn.execute("CREATE TABLE IF NOT EXISTS content_tokens(term TEXT, path TEXT)")
    conn.execute("CREATE INDEX IF NOT EXISTS idx_tokens_term ON content_tokens(term)")
    conn.execute("PRAGMA user_version=0")
    return "tokens"

def tokenize(s: str):
  w = []
  cur = []
  for ch in s:
    if ch.isalnum() or ch in ("_","-",".","/"):
      cur.append(ch)
    else:
      if cur: w.append("".join(cur)); cur=[]
  if cur: w.append("".join(cur))
  return [t.lower() for t in w if t]

def upsert_doc(conn, path, size, mtime, sha):
  conn.execute("INSERT INTO content_documents(path,size,mtime,sha256) VALUES(?,?,?,?) ON CONFLICT(path) DO UPDATE SET size=excluded.size, mtime=excluded.mtime, sha256=excluded.sha256", (path,size,mtime,sha))

def main():
  ap = argparse.ArgumentParser()
  ap.add_argument("--state", required=True)
  ap.add_argument("--max-size", type=int, default=MAX_SIZE)
  args = ap.parse_args()
  state = Path(args.state)
  db = state/"techula_index.db"
  if not db.exists():
    print("[!] Run metadata index first.", file=sys.stderr); sys.exit(2)
  conn = sqlite3.connect(db)
  mode = ensure_schema(conn)
  conn.commit()
  cur = conn.cursor()
  count=0; skipped=0; indexed=0
  for (path,size,mtime,sha) in cur.execute("SELECT path,size,mtime,sha256 FROM files"):
    p = Path(path)
    count+=1
    if not p.exists(): continue
    if size>args.max_size: skipped+=1; continue
    if not is_text(p, size): skipped+=1; continue
    # check doc recency
    row = conn.execute("SELECT size, mtime, sha256 FROM content_documents WHERE path=?", (path,)).fetchone()
    if row and row[0]==size and abs(row[1]-mtime)<1e-6 and (not sha or sha==row[2]):
      continue
    try:
      with open(p, "r", encoding="utf-8", errors="ignore") as fh:
        body = fh.read()
    except Exception:
      skipped+=1; continue
    if mode=="fts":
      # FTS5 doesn't support UPSERT, so delete then insert
      conn.execute("DELETE FROM content_fts WHERE path=?", (path,))
      conn.execute("INSERT INTO content_fts(path, body) VALUES(?, ?)", (path, body))
    else:
      conn.execute("DELETE FROM content_tokens WHERE path=?", (path,))
      toks = tokenize(body)
      conn.executemany("INSERT INTO content_tokens(term, path) VALUES(?,?)", ((t, path) for t in toks[:20000]))
    upsert_doc(conn, path, size, mtime, sha)
    indexed+=1
    if indexed%200==0: conn.commit()
  conn.commit()
  print(f"[i] scanned={count} indexed={indexed} skipped={skipped} mode={mode}")

if __name__ == "__main__":
  main()