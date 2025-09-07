#!/usr/bin/env python3
"""
Near-duplicate detection via 64-bit SimHash over tokens.
- index: compute simhash for text docs in content_documents.
- near: list paths with Hamming distance <= threshold.
"""
import sqlite3, argparse, sys, math, os
from pathlib import Path

def ensure_schema(conn):
  conn.execute("CREATE TABLE IF NOT EXISTS simhash(path TEXT PRIMARY KEY, simhash64 INTEGER, token_count INTEGER)")
  conn.execute("CREATE INDEX IF NOT EXISTS idx_simhash ON simhash(simhash64)")

def tokenize(conn, path):
  ver=conn.execute("PRAGMA user_version").fetchone()[0]
  if ver==1:
    row=conn.execute("SELECT body FROM content_fts WHERE path=?", (path,)).fetchone()
    text=row[0] if row else ""
  else:
    # reconstruct minimal term list
    rows=conn.execute("SELECT term FROM content_tokens WHERE path=?", (path,)).fetchall()
    text=" ".join(t for (t,) in rows)
  return [t for t in text.split() if t]

def simhash64(tokens):
  # simple 64-bit simhash
  import hashlib
  v=[0]*64
  for tok in tokens:
    h=int(hashlib.blake2b(tok.encode(), digest_size=8).hexdigest(),16)
    for i in range(64):
      v[i]+=1 if (h>>i)&1 else -1
  out=0
  for i in range(64):
    if v[i]>0: out |= (1<<i)
  return out

def hamming(a,b): return (a^b).bit_count()

def index_cmd(conn):
  paths=[p for (p,) in conn.execute("SELECT path FROM content_documents")]
  n=0; w=0
  for p in paths:
    toks=tokenize(conn, p)
    if not toks: continue
    s=simhash64(toks)
    conn.execute("INSERT OR REPLACE INTO simhash(path, simhash64, token_count) VALUES(?,?,?)", (p, s, len(toks)))
    n+=1
    if n%200==0: conn.commit()
  conn.commit()
  print(f"[i] simhash indexed={n}")

def near_cmd(conn, path, max_hd):
  row=conn.execute("SELECT simhash64 FROM simhash WHERE path=?", (path,)).fetchone()
  if not row:
    print("[!] Target not indexed; run simhash-index and ensure content-index done."); return
  s=row[0]
  # brute-force compare (kept simple; dataset is local)
  rows=conn.execute("SELECT path, simhash64 FROM simhash WHERE path<>?", (path,)).fetchall()
  res=[]
  for p, other in rows:
    hd=hamming(s, other)
    if hd<=max_hd: res.append((hd, p))
  res.sort(key=lambda x:x[0])
  for hd,p in res[:100]: print(f"{hd}\t{p}")

def main():
  ap=argparse.ArgumentParser()
  sub=ap.add_subparsers(dest="cmd", required=True)
  ap_i=sub.add_parser("index"); ap_i.add_argument("--state", required=True)
  ap_n=sub.add_parser("near"); ap_n.add_argument("--state", required=True); ap_n.add_argument("--path", required=True); ap_n.add_argument("--max-hd", type=int, default=8)
  args=ap.parse_args()
  db=Path(args.state)/"techula_index.db"
  if not db.exists(): print("[!] DB missing"); sys.exit(2)
  conn=sqlite3.connect(db); ensure_schema(conn)
  if args.cmd=="index": index_cmd(conn)
  else: near_cmd(conn, args.path, args.max_hd)
if __name__=="__main__":
  main()