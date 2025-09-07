#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Query content index:
- FTS5: SELECT path FROM content_fts WHERE content_fts MATCH ?
- tokens fallback: AND all terms present
"""
import sqlite3, argparse, sys
def main():
  ap = argparse.ArgumentParser()
  ap.add_argument("--state", required=True)
  ap.add_argument("--query", required=True)
  args = ap.parse_args()
  db = f"{args.state}/techula_index.db"
  conn = sqlite3.connect(db)
  ver = conn.execute("PRAGMA user_version").fetchone()[0]
  if ver==1:
    sql = "SELECT path FROM content_fts WHERE content_fts MATCH ? LIMIT 200"
    rows = conn.execute(sql, (args.query,)).fetchall()
    for (p,) in rows: print(p)
  else:
    terms = [t for t in args.query.split() if t]
    if not terms: sys.exit(0)
    # naive intersection
    sets = []
    for t in terms:
      rows = conn.execute("SELECT path FROM content_tokens WHERE term=?", (t.lower(),)).fetchall()
      sets.append(set(p for (p,) in rows))
    res = set.intersection(*sets) if sets else set()
    for p in list(sorted(res))[:200]: print(p)
if __name__ == "__main__":
  main()