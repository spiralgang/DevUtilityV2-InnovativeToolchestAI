#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Main Repository Indexing Script
Properly indexes the main repository and ensures all systems work correctly.
"""

import os
import sys
import subprocess
from pathlib import Path

def run_command(cmd, description):
    """Run a command and handle errors"""
    print(f"[i] {description}...")
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"[!] Error: {result.stderr}")
        return False
    print(f"[✓] {description} completed successfully")
    if result.stdout.strip():
        print(f"    Output: {result.stdout.strip()}")
    return True

def main():
    repo_root = Path(__file__).parent.parent
    os.chdir(repo_root)
    
    print("🔥 Starting Main Repository Indexing Process...")
    print(f"📁 Repository: {repo_root}")
    
    # Create indexing state directory
    index_state = repo_root / ".index_state"
    index_state.mkdir(exist_ok=True)
    
    # Step 1: Run main file indexing
    cmd1 = f"python3 tools/index_and_consolidate.py index --state {index_state} --roots ."
    if not run_command(cmd1, "Building file metadata index"):
        return 1
    
    # Step 2: Run content indexing  
    cmd2 = f"python3 tools/content_indexer.py --state {index_state}"
    if not run_command(cmd2, "Building full-text search index"):
        return 1
    
    # Step 3: Generate search audit
    cmd3 = f"python3 tools/sqlite_dupe_audit.py --state {index_state}"
    if not run_command(cmd3, "Auditing for duplicates"):
        return 1
    
    print("🎉 Repository indexing completed successfully!")
    print(f"📊 Index database: {index_state / 'techula_index.db'}")
    print("🔍 Full-text search and duplicate detection are now available")
    
    return 0

if __name__ == "__main__":
    sys.exit(main())