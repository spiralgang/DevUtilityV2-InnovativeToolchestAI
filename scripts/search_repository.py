#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Repository Search Tool
Demonstrates how to search the indexed repository content.
"""

import sqlite3
import sys
from pathlib import Path

def search_content(query, index_db_path):
    """Search indexed content using FTS5"""
    if not Path(index_db_path).exists():
        print(f"[!] Index database not found at {index_db_path}")
        print("    Run: python3 scripts/index_main_properly.py")
        return
    
    conn = sqlite3.connect(index_db_path)
    
    # Check if FTS is available
    try:
        results = conn.execute("""
            SELECT path, snippet(content_fts, 1, '<b>', '</b>', '...', 10) as snippet
            FROM content_fts 
            WHERE content_fts MATCH ? 
            ORDER BY bm25(content_fts) 
            LIMIT 20
        """, (query,)).fetchall()
        
        if results:
            print(f"🔍 Found {len(results)} matches for '{query}':")
            for i, (path, snippet) in enumerate(results, 1):
                print(f"\n{i}. {path}")
                print(f"   ...{snippet}...")
        else:
            print(f"❌ No matches found for '{query}'")
            
    except sqlite3.OperationalError:
        # Fallback to token search
        print("🔍 Using fallback token search...")
        results = conn.execute("""
            SELECT DISTINCT path 
            FROM content_tokens 
            WHERE term LIKE ? 
            LIMIT 20
        """, (f"%{query.lower()}%",)).fetchall()
        
        if results:
            print(f"Found {len(results)} files containing '{query}':")
            for i, (path,) in enumerate(results, 1):
                print(f"{i}. {path}")
        else:
            print(f"❌ No matches found for '{query}'")
    
    conn.close()

def main():
    if len(sys.argv) != 2:
        print("Usage: python3 scripts/search_repository.py <query>")
        print("Example: python3 scripts/search_repository.py 'living code'")
        return 1
    
    query = sys.argv[1]
    repo_root = Path(__file__).parent.parent
    index_db = repo_root / ".index_state" / "techula_index.db"
    
    search_content(query, index_db)
    return 0

if __name__ == "__main__":
    sys.exit(main())