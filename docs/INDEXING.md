# Indexing Architecture (Tech.Ula Multi-Profile)

Scope
- Multi-profile Linux-on-Android under Android/data/tech.ula.
- Non-root, PRoot environment, Android 10+ scoped storage.

Index Types
- Metadata: path, size, mtime, sha256 (exact dupes).
- Full-text: content tokens via SQLite FTS5; fallback inverted-index if FTS5 missing.
- Chunk-level (CDC): content-defined chunking to detect block-level redundancy across files.
- Near-duplicate text: simhash with Hamming thresholds (plagiarism-ish similarity).

DB Layout (techula_index.db)
- files(id, path, profile, size, mtime, sha256)
- profiles(profile, root)
- sqlite_dbs(path, profile, size)
- content(documents: path UNIQUE, size, mtime, sha256; fts: fts5(path, body) OR fallback tokens(term, path))
- cdc_chunks(path, chunk_ix, chunk_sha256, size)
- simhash(path, simhash64, token_count)

Incremental Strategy
- Reuse files table; indexers skip unchanged rows by mtime/size/sha.
- Fallback to hashing if mtime untrustworthy due to FUSE remounts.
- CDC: configurable target size; store chunk hashes and sizes.
- Simhash: tokenize, 64-bit simhash, Hamming distance search.

Android/PRoot Constraints
- inotify unreliable: use scheduled re-scans (polling).
- Symlinks across storage trees can fail: prefer bind plans or keep shared_store under same root.
- FTS5 sometimes disabled in distro builds: auto-fallback to tokens table.

Ops
- Index: metadata → content → CDC → simhash
- Search: FTS (full-text) or fallback tokens; near-duplicate via Hamming distance.
- Consolidate: exact dupes via SHA-256 plan, shadow with bind plans or reversible symlinks.

References
- /reference vault (SQLite FTS5, CDC/FastCDC, SimHash, Android scoped storage, PRoot bind semantics)