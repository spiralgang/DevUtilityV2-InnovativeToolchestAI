#!/usr/bin/env python3
"""
GitHub-Native Assimilation Agent - Frontend-Backend Integration Auditor

This script is part of the comprehensive GitHub-native assimilation agent system.
It validates that frontend files are properly integrated into backend features,
ensuring complete assimilation rather than shallow file copying.

Referenced by: assimilationClient.js for frontend audit integration

Features:
- Manifest-driven validation
- Backend binding verification  
- Forensic JSONL logging
- Strict mode for merge gates
- Deep code analysis
- Integration completeness scoring

Integration bindings:
- configs/frontend_manifest.json
- .github/workflows/pr-assimilation-audit.yml
"""
import argparse
import json
import os
import re
import subprocess
import sys
import time
import hashlib
from pathlib import Path
from typing import Dict, List, Set, Any, Optional

def sh(*cmd) -> str:
    """Execute shell command and return output"""
    try:
        return subprocess.check_output(list(cmd), text=True, stderr=subprocess.DEVNULL).strip()
    except subprocess.CalledProcessError:
        return ""

def changed_files(base: str) -> Set[str]:
    """Get list of files changed since base branch"""
    out = sh("git", "diff", "--name-only", f"{base}...")
    return set(filter(None, out.splitlines()))

def file_exists(path: str) -> bool:
    """Check if file exists"""
    return os.path.exists(path)

def file_contains(path: str, needles: List[str]) -> bool:
    """Check if file contains any of the needle strings"""
    if not file_exists(path): 
        return False
    try:
        with open(path, "r", errors="ignore") as f:
            data = f.read()
    except Exception:
        return False
    
    for needle in needles:
        # Support both exact matches and regex patterns
        if needle.startswith("regex:"):
            pattern = needle[6:]  # Remove "regex:" prefix
            if re.search(pattern, data, re.IGNORECASE):
                return True
        else:
            if re.search(re.escape(needle), data, re.IGNORECASE):
                return True
    return False

def analyze_file_depth(path: str) -> Dict[str, Any]:
    """Perform deep analysis of a file"""
    if not file_exists(path):
        return {"exists": False}
    
    try:
        with open(path, "r", errors="ignore") as f:
            content = f.read()
        
        analysis = {
            "exists": True,
            "size": len(content),
            "lines": len(content.splitlines()),
            "hash": hashlib.sha256(content.encode()).hexdigest()[:16]
        }
        
        # Language-specific analysis
        if path.endswith(('.js', '.ts', '.vue')):
            # JavaScript/TypeScript/Vue analysis
            analysis.update({
                "imports": re.findall(r'import.*from.*["\']([^"\']+)["\']', content),
                "exports": len(re.findall(r'export\s+', content)),
                "api_calls": len(re.findall(r'\.(get|post|put|delete|patch)\(|fetch\(|axios\.|api\.', content)),
                "vue_components": len(re.findall(r'<template>|<script>|<style>', content)) if path.endswith('.vue') else 0
            })
        elif path.endswith(('.py',)):
            # Python analysis
            analysis.update({
                "imports": re.findall(r'from\s+([^\s]+)\s+import|import\s+([^\s]+)', content),
                "classes": len(re.findall(r'class\s+\w+', content)),
                "functions": len(re.findall(r'def\s+\w+', content)),
                "api_endpoints": len(re.findall(r'@app\.route|@router\.|endpoint|FastAPI', content))
            })
        elif path.endswith(('.kt', '.java')):
            # Kotlin/Java analysis
            analysis.update({
                "package": re.findall(r'package\s+([^\s;]+)', content),
                "imports": re.findall(r'import\s+([^\s;]+)', content),
                "classes": len(re.findall(r'class\s+\w+|interface\s+\w+|object\s+\w+', content)),
                "activities": len(re.findall(r'Activity|Fragment|Service', content))
            })
        
        return analysis
        
    except Exception as e:
        return {"exists": True, "error": str(e)}

def calculate_integration_score(src: str, mapping: Dict, analysis: Dict) -> float:
    """Calculate integration completeness score (0.0 to 1.0)"""
    score = 0.0
    max_score = 5.0  # Maximum possible score
    
    # File exists in expected location (20%)
    if analysis.get("target_present"):
        score += 1.0
    
    # Backend bindings are complete (30%)
    if not analysis.get("binds_missing"):
        score += 1.5
    
    # File has been modified in this PR (indicates active work) (10%)
    if analysis.get("changed_in_pr"):
        score += 0.5
    
    # Size/complexity indicates real implementation (20%)
    target_analysis = analysis.get("target_analysis", {})
    if target_analysis.get("lines", 0) > 10:  # More than just boilerplate
        score += 1.0
    
    # Language-specific integration indicators (20%)
    if target_analysis.get("api_calls", 0) > 0 or target_analysis.get("api_endpoints", 0) > 0:
        score += 0.5
    if target_analysis.get("imports") and len(target_analysis["imports"]) > 0:
        score += 0.5
    
    return min(score / max_score, 1.0)

def log_line(fp, obj: Dict[str, Any]) -> None:
    """Write a JSON line to the log file"""
    fp.write(json.dumps(obj, ensure_ascii=False) + "\n")
    fp.flush()

def create_minimal_manifest(manifest_path: str, base: str) -> None:
    """Create a minimal manifest from changed files"""
    changed = changed_files(base)
    frontend_files = [f for f in changed if re.search(r'(frontend/|\.vue$|\.js$|\.ts$|\.html$|\.css$|tools/.*\.js$)', f)]
    
    manifest = {
        "files": frontend_files[:50],  # Limit to first 50 files
        "auto_generated": True,
        "created_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "note": "Auto-generated minimal manifest from changed files"
    }
    
    os.makedirs(os.path.dirname(manifest_path), exist_ok=True)
    with open(manifest_path, "w") as f:
        json.dump(manifest, f, indent=2)
    
    print(f"✅ Created minimal manifest with {len(frontend_files)} frontend files")

def create_minimal_mapping(mapping_path: str, manifest: Dict) -> None:
    """Create a minimal mapping file"""
    files = manifest.get("files", [])
    mapping = {}
    
    for f in files:
        # Generate reasonable backend mappings
        if f.startswith("frontend/"):
            base_name = os.path.basename(f)
            name_no_ext = os.path.splitext(base_name)[0]
            
            if f.endswith('.vue'):
                target = f"app/src/main/java/com/spiralgang/ui/{name_no_ext}Activity.kt"
                binds = ["app/src/main/AndroidManifest.xml", f"app/src/main/res/layout/activity_{name_no_ext.lower()}.xml"]
            elif f.endswith(('.js', '.ts')):
                if 'api' in f.lower():
                    target = f"ai/{name_no_ext.lower()}.py"
                    binds = ["configs/agent_registry.json"]
                else:
                    target = f"app/src/main/java/com/spiralgang/utils/{name_no_ext}.kt"
                    binds = ["app/src/main/AndroidManifest.xml"]
            else:
                target = f"app/src/main/java/com/spiralgang/{name_no_ext}.kt"
                binds = ["app/src/main/AndroidManifest.xml"]
        else:
            # Non-frontend files
            target = f"backend/{os.path.basename(f)}"
            binds = ["configs/activation_rules.json"]
        
        mapping[f] = {
            "target": target,
            "binds": binds
        }
    
    os.makedirs(os.path.dirname(mapping_path), exist_ok=True)
    with open(mapping_path, "w") as f:
        json.dump(mapping, f, indent=2)
    
    print(f"✅ Created minimal mapping with {len(mapping)} entries")

def main():
    ap = argparse.ArgumentParser(description="GitHub-Native Assimilation Agent - Frontend-Backend Integration Auditor")
    ap.add_argument("--manifest", required=True, help="Path to frontend manifest JSON file")
    ap.add_argument("--mapping", required=True, help="Path to frontend-to-backend mapping JSON file")
    ap.add_argument("--base", required=True, help="Base branch for comparison (e.g., origin/main)")
    ap.add_argument("--log", required=True, help="Path to output JSONL log file")
    ap.add_argument("--strict", action="store_true", help="Strict mode - fail if any files are not assimilated")
    ap.add_argument("--verbose", action="store_true", help="Verbose output with detailed analysis")
    ap.add_argument("--deep", action="store_true", help="Perform deep code analysis")
    args = ap.parse_args()

    ts = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    ch = changed_files(args.base)

    # Load or create manifest
    if not os.path.exists(args.manifest):
        print(f"📋 Manifest not found: {args.manifest}. Creating minimal manifest from changed files.")
        create_minimal_manifest(args.manifest, args.base)
    
    try:
        with open(args.manifest) as f:
            manifest = json.load(f)
    except Exception as e:
        print(f"❌ Error loading manifest: {e}")
        sys.exit(1)

    # Load or create mapping
    if not os.path.exists(args.mapping):
        print(f"🗺️ Mapping not found: {args.mapping}. Creating minimal mapping.")
        create_minimal_mapping(args.mapping, manifest)
    
    try:
        with open(args.mapping) as f:
            mapping = json.load(f)
    except Exception as e:
        print(f"❌ Error loading mapping: {e}")
        sys.exit(1)

    files = manifest.get("files", [])
    if not files:
        print("⚠️ No files in manifest to process")
        sys.exit(0)

    # Initialize logging
    os.makedirs(os.path.dirname(args.log), exist_ok=True)
    with open(args.log, "a", encoding="utf-8") as fp:
        
        # Log audit start
        log_line(fp, {
            "ts": ts,
            "phase": "start",
            "manifest_files": len(files),
            "mapping_entries": len(mapping),
            "changed_files": len(ch),
            "strict_mode": args.strict,
            "verbose": args.verbose,
            "deep_analysis": args.deep
        })

        total = len(files)
        ok = 0
        missing = []
        unbound = []
        integration_scores = []

        for src in files:
            m = mapping.get(src)
            entry = {
                "ts": ts,
                "src": src,
                "phase": "audit"
            }
            
            if not m:
                entry.update({"status": "mapping_missing", "error": "No mapping defined for this file"})
                log_line(fp, entry)
                unbound.append(src)
                if args.verbose:
                    print(f"❌ {src}: No mapping defined")
                continue

            target = m.get("target")
            binds = m.get("binds", [])
            entry.update({"target": target, "binds": binds})

            # Check target exists
            target_present = file_exists(target)
            entry["target_present"] = target_present

            # Check binds
            binds_ok = []
            binds_missing = []
            for b in binds:
                # Create search terms for this target
                target_name = os.path.basename(target) if target else ""
                target_base = os.path.splitext(target_name)[0]
                search_terms = [target_name, target_base]
                
                if file_contains(b, search_terms):
                    binds_ok.append(b)
                else:
                    binds_missing.append(b)

            entry["binds_ok"] = binds_ok
            entry["binds_missing"] = binds_missing
            entry["changed_in_pr"] = (src in ch) or (target in ch) or any(b in ch for b in binds)

            # Deep analysis if requested
            if args.deep:
                entry["src_analysis"] = analyze_file_depth(src)
                entry["target_analysis"] = analyze_file_depth(target)

            # Calculate integration score
            integration_score = calculate_integration_score(src, m, entry)
            entry["integration_score"] = integration_score
            integration_scores.append(integration_score)

            # Determine status
            if target_present and not binds_missing:
                entry["status"] = "assimilated"
                ok += 1
                if args.verbose:
                    print(f"✅ {src}: Fully assimilated (score: {integration_score:.2f})")
            elif not target_present:
                entry["status"] = "target_missing"
                missing.append(src)
                if args.verbose:
                    print(f"❌ {src}: Target missing - {target}")
            else:
                entry["status"] = "binds_incomplete"
                unbound.append(src)
                if args.verbose:
                    print(f"⚠️ {src}: Incomplete binds - {binds_missing}")

            log_line(fp, entry)

        # Calculate overall metrics
        avg_integration_score = sum(integration_scores) / len(integration_scores) if integration_scores else 0.0
        
        summary = {
            "ts": ts,
            "phase": "summary",
            "total": total,
            "ok": ok,
            "missing": len(missing),
            "unbound": len(unbound),
            "strict": args.strict,
            "avg_integration_score": avg_integration_score,
            "assimilation_percentage": (ok / total * 100) if total > 0 else 0
        }
        log_line(fp, summary)

        # Print summary
        print(f"\n📊 Assimilation Audit Summary:")
        print(f"   Total files: {total}")
        print(f"   ✅ Assimilated: {ok}")
        print(f"   ❌ Missing targets: {len(missing)}")
        print(f"   ⚠️ Incomplete binds: {len(unbound)}")
        print(f"   📈 Average integration score: {avg_integration_score:.2f}")
        print(f"   📊 Assimilation percentage: {summary['assimilation_percentage']:.1f}%")

        if args.strict and (ok != total):
            print(f"\n❌ STRICT MODE FAILURE: Assimilation incomplete ({ok}/{total} files)")
            if missing:
                print(f"   Missing targets: {missing[:5]}")  # Show first 5
            if unbound:
                print(f"   Incomplete binds: {unbound[:5]}")  # Show first 5
            sys.exit(1)
        elif ok == total:
            print(f"\n🎉 ASSIMILATION COMPLETE: All {total} files successfully integrated!")
        else:
            print(f"\n⚠️ ASSIMILATION INCOMPLETE: {total - ok} files need attention")

if __name__ == "__main__":
    main()