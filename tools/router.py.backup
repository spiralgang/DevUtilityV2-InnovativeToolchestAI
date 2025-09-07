from __future__ import annotations
from dataclasses import dataclass
from typing import Iterable, List, Optional
from .sqlite_fts import SqliteFTS, FTSQuery, FTSResult

@dataclass
class RetrievalRequest:
    query: str
    top_k: int = 8
    mode: str = "auto"  # "auto"|"fts"|"path"|"code"

@dataclass
class RetrievalResult:
    hits: List[FTSResult]
    index_used: str

class IndexRouter:
    """
    Very lightweight router: 
    - path-like queries -> path mode
    - otherwise -> FTS
    Extend as needed to call CDC/SimHash for dupe-awareness in re-ranking.
    """
    def __init__(self, fts: SqliteFTS) -> None:
        self.fts = fts

    def retrieve(self, req: RetrievalRequest) -> RetrievalResult:
        mode = req.mode
        q = req.query.strip()
        if mode == "auto":
            if "/" in q or q.endswith((".py", ".sh", ".json", ".md", ".log", ".conf")):
                mode = "path"
            else:
                mode = "fts"

        if mode == "path":
            # exact-ish path filter via FTS prefix fallback
            hits = self.fts.search(FTSQuery(text=q, limit=req.top_k))
            return RetrievalResult(hits=hits, index_used="fts-path")
        if mode == "fts":
            hits = self.fts.search(FTSQuery(text=q, limit=req.top_k))
            return RetrievalResult(hits=hits, index_used="fts")
        # default
        hits = self.fts.search(FTSQuery(text=q, limit=req.top_k))
        return RetrievalResult(hits=hits, index_used="fts")

# References:
# - /reference vault (multi-index routing patterns)