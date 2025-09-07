# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This file is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# -*- coding: utf-8 -*-
from __future__ import annotations
import os, sqlite3, fnmatch
from typing import Iterable, List, Tuple

class LocalIndex:
    """
    SQLite-backed content index (FTS5 with tokens fallback).
    Respects allow_roots, include_ext, exclude_glob filters.
    """
    def __init__(self, state_dir: str, allow_roots: Iterable[str], include_ext: Iterable[str], exclude_glob: Iterable[str]) -> None:
        self.db = os.path.join(state_dir, "techula_index.db")
        self.conn = sqlite3.connect(self.db, check_same_thread=False)
        self.conn.row_factory = sqlite3.Row
        self.roots = tuple(os.path.abspath(p) for p in allow_roots)
        self.ext = {e.lower() for e in include_ext} if include_ext else set()
        self.ex_glob = list(exclude_glob) if exclude_glob else []
        self._fts = self.conn.execute("PRAGMA user_version").fetchone()[0] == 1

    def _allowed(self, path: str) -> bool:
        ap = os.path.abspath(path)
        if self.roots and not any(ap == r or ap.startswith(r.rstrip(os.sep)+os.sep) for r in self.roots):
            return False
        if self.ext and not any(ap.lower().endswith(e) for e in self.ext):
            return False
        for pat in self.ex_glob:
            if fnmatch.fnmatch(ap, pat):
                return False
        return True

    def search(self, query: str, limit: int = 8) -> List[Tuple[str, str]]:
        if self._fts:
            rows = self.conn.execute(
                "SELECT path, snippet(content_fts, 1, '[', ']', 'â€¦', 10) AS snip FROM content_fts WHERE content_fts MATCH ? LIMIT ?",
                (query, limit)
            ).fetchall()
            return [(r["path"], r["snip"] or "") for r in rows if self._allowed(r["path"])]
        # tokens fallback: naive AND
        terms = [t for t in query.split() if t]
        if not terms:
            return []
        sets = []
        for t in terms:
            rs = self.conn.execute("SELECT path FROM content_tokens WHERE term=?", (t.lower(),)).fetchall()
            sets.append({row["path"] for row in rs})
        paths = list(set.intersection(*sets)) if sets else []
        out = []
        for p in paths[:limit]:
            if self._allowed(p):
                out.append((p, ""))  # no snippet in tokens mode
        return out

    def close(self) -> None:
        try: self.conn.close()
        except Exception: pass

# References:
# - /reference vault (SQLite FTS5 patterns, Android constraints)