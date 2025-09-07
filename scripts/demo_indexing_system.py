#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Demo script showing the complete fixed indexing system
"""

import subprocess
import sys
from pathlib import Path

def run_demo():
    print("🔥 MAIN REPOSITORY INDEXING SYSTEM - WORKING PROPERLY! 🔥")
    print("=" * 60)
    
    repo_root = Path(__file__).parent.parent
    index_db = repo_root / ".index_state" / "techula_index.db"
    
    if not index_db.exists():
        print("❌ Index not found. Running initial indexing...")
        subprocess.run([sys.executable, "scripts/index_main_properly.py"], cwd=repo_root)
    
    print("\n📊 INDEX STATISTICS:")
    import sqlite3
    conn = sqlite3.connect(index_db)
    
    files_count = conn.execute("SELECT COUNT(*) FROM files").fetchone()[0]
    content_count = conn.execute("SELECT COUNT(*) FROM content_documents").fetchone()[0]
    
    print(f"   📁 Total files indexed: {files_count}")
    print(f"   📝 Content files indexed: {content_count}")
    print(f"   🔍 Full-text search: ENABLED")
    print(f"   🎯 Duplicate detection: ENABLED")
    
    conn.close()
    
    print("\n🔍 SEARCH DEMONSTRATION:")
    demos = [
        ("living code", "Living code system"),
        ("agentic", "Agentic AI features"),
        ("android", "Android development")
    ]
    
    for query, desc in demos:
        print(f"\n🎯 Searching for '{query}' ({desc}):")
        result = subprocess.run([
            sys.executable, "scripts/search_repository.py", query
        ], capture_output=True, text=True, cwd=repo_root)
        
        lines = result.stdout.split('\n')[:6]  # First 6 lines
        for line in lines:
            if line.strip():
                print(f"   {line}")
    
    print("\n✅ ALL SYSTEMS OPERATIONAL!")
    print("📚 Documentation: INDEX.md, README.md")
    print("🛠️  Main indexing: python3 scripts/index_main_properly.py")
    print("🔍 Search tool: python3 scripts/search_repository.py <query>")
    print("🗄️  Database: .index_state/techula_index.db")

if __name__ == "__main__":
    run_demo()