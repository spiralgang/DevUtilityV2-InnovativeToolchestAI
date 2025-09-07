# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This file is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
CDC (content-defined chunking) using simple gear hashing.
- target ~16KiB, min 4KiB, max 64KiB.
- Stores chunk sha256 per file for redundancy analysis.
"""
import os, sys, sqlite3, argparse, hashlib
from pathlib import Path
TARGET=16*1024; MIN_SZ=4*1024; MAX_SZ=64*1024
MASK=(1<<13)-1  # 8K boundary mask approx; combined with limits

# 256 random 64-bit constants (trimmed for brevity but deterministic)
GEAR=[0x0000000000000000,0x7f4a7c15ab1234df,0x5d1b3f8a91c27bb1,0x2e3d6a9f4b8c1d07,
0x9bcdef0123456789,0xfedcba9876543210,0x1a2b3c4d5e6f7081,0xbadf00d0c0ffee00] + [0]*248
def gear_hash_step(h, b): return ((h<<1) + GEAR[b & 0xff]) & 0xffffffffffffffff

def ensure_schema(conn):
  conn.execute("""CREATE TABLE IF NOT EXISTS cdc_chunks(
    path TEXT, chunk_ix INTEGER, chunk_sha256 TEXT, size INTEGER,
    PRIMARY KEY(path, chunk_ix)
  )""")
  conn.execute("CREATE INDEX IF NOT EXISTS idx_cdc_sha ON cdc_chunks(chunk_sha256)")

def chunk_and_hash(p: Path):
  res=[]; ix=0
  with open(p, "rb") as f:
    h=0; chunk=bytearray(); total=0
    while True:
      b=f.read(1)
      if not b:
        if chunk: res.append((ix, hashlib.sha256(chunk).hexdigest(), len(chunk))); ix+=1
        break
      chunk += b; total+=1
      h = gear_hash_step(h, b[0])
      full=len(chunk)>=MAX_SZ; enough=len(chunk)>=MIN_SZ
      boundary = (h & MASK)==0 and len(chunk)>=TARGET
      if full or (enough and boundary):
        res.append((ix, hashlib.sha256(chunk).hexdigest(), len(chunk))); ix+=1
        h=0; chunk=bytearray()
  return res

def index_cmd(conn, state):
  # iterate files table (cap size to 128MB)
  cap=128*1024*1024
  q=conn.execute("SELECT path,size FROM files WHERE size BETWEEN ? AND ?", (MIN_SZ, cap))
  n=0; ins=0
  for path,size in q:
    p=Path(path)
    if not p.exists(): continue
    # skip if already indexed and chunk count plausible
    have=conn.execute("SELECT COUNT(*) FROM cdc_chunks WHERE path=?", (path,)).fetchone()[0]
    if have>0: continue
    try:
      chunks=chunk_and_hash(p)
    except Exception:
      continue
    for ix, sha, csz in chunks:
      conn.execute("INSERT OR REPLACE INTO cdc_chunks(path,chunk_ix,chunk_sha256,size) VALUES(?,?,?,?)",
                   (path, ix, sha, csz))
      ins+=1
    n+=1
    if n%50==0: conn.commit()
  conn.commit()
  print(f"[i] CDC indexed files={n} chunks={ins}")

def similar_cmd(conn, path):
  # list files sharing chunk hashes with target (descending by shared count)
  rows=conn.execute("SELECT chunk_sha256 FROM cdc_chunks WHERE path=?", (path,)).fetchall()
  if not rows:
    print("[!] No CDC data for path; run cdc-index."); return
  shas=[r[0] for r in rows]
  tmp=",".join("?"*len(shas))
  q=f"SELECT path, COUNT(*) as hits FROM cdc_chunks WHERE chunk_sha256 IN ({tmp}) AND path<>? GROUP BY path ORDER BY hits DESC LIMIT 50"
  res=conn.execute(q, (*shas, path)).fetchall()
  for p,h in res:
    print(f"{h}\t{p}")

def main():
  ap=argparse.ArgumentParser()
  sub=ap.add_subparsers(dest="cmd", required=True)
  ap_i=sub.add_parser("index"); ap_i.add_argument("--state", required=True)
  ap_s=sub.add_parser("similar"); ap_s.add_argument("--state", required=True); ap_s.add_argument("--path", required=True)
  args=ap.parse_args()
  db=Path(args.state)/"techula_index.db"
  conn=sqlite3.connect(db); ensure_schema(conn)
  if args.cmd=="index": index_cmd(conn, args.state)
  else: similar_cmd(conn, args.path)
if __name__=="__main__":
  main()