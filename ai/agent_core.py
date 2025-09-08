#!/usr/bin/env python3
# Standard library only. Offline. Local-owned.
# Bridges: core_engine_bridge, living_core
import argparse, os, sys, json, hashlib, time, subprocess

ROOT = os.path.dirname(os.path.abspath(__file__))
LOGS = os.path.join(ROOT, "logs")
SCR  = os.path.join(ROOT, "scratch")
CFG  = os.path.join(ROOT, "configs", "activation_rules.json")
os.makedirs(LOGS, exist_ok=True)
os.makedirs(SCR,  exist_ok=True)

def ts(): return time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())

def log_event(kind, **kwargs):
    rec = {"ts": ts(), "kind": kind, **kwargs}
    with open(os.path.join(LOGS, "agent_run.jsonl"), "a", encoding="utf-8") as fp:
        fp.write(json.dumps(rec, ensure_ascii=False) + "\n")

def sha1_text(s: str) -> str:
    import hashlib
    return hashlib.sha1(s.encode("utf-8")).hexdigest()[:12]

def read_rules():
    if os.path.exists(CFG):
        with open(CFG, "r", encoding="utf-8") as fp:
            return json.load(fp)
    return {
        "frontend_globs": ["frontend/", "web/", "app/src/main/res/"],
        "backend_globs":  ["app/src/", "server/", "ai/"],
        "extensions": [".js", ".ts", ".vue", ".kt", ".java", ".py", ".sh"],
        "bind_rules": [
            {"from_ext": ".vue", "bind_hint": "ModuleUi"},
            {"from_ext": ".js",  "bind_hint": "Registry"},
            {"from_ext": ".ts",  "bind_hint": "Registry"},
            {"from_ext": ".kt",  "bind_hint": "di/"},
            {"from_ext": ".py",  "bind_hint": "scripts/"},
        ],
    }

def walk_files(root="."):
    skip = ["/.git", "/.gradle", "/node_modules", "/build", "/out"]
    for r, d, f in os.walk(root):
        if any(s in r for s in skip): continue
        for name in f: yield os.path.join(r, name)

def classify(paths, rules):
    exts = set(rules["extensions"])
    fronts, backs = [], []
    for p in paths:
        if os.path.splitext(p)[1] not in exts: continue
        rp = p[2:] if p.startswith("./") else p
        if any(rp.startswith(g) for g in rules["frontend_globs"]): fronts.append(rp)
        if any(rp.startswith(g) for g in rules["backend_globs"]):  backs.append(rp)
    return sorted(set(fronts)), sorted(set(backs))

def propose_binds(front_files, back_files, rules):
    plan_lines, notes = [], []
    for f in front_files:
        ext  = os.path.splitext(f)[1]
        base = os.path.splitext(os.path.basename(f))[0]
        hints = [r["bind_hint"] for r in rules["bind_rules"] if r.get("from_ext")==ext]
        for hint in hints:
            cands = [b for b in back_files if (hint in b or base in os.path.basename(b))]
            if not cands:
                notes.append({"src": f, "hint": hint, "status": "no_candidate"})
                continue
            for b in cands[:3]:
                tag = f"# agent-bind:{base}->{hint}"
                cmd = f'grep -q "{tag}" "{b}" || echo "{tag}" >> "{b}"'
                plan_lines.append(cmd)
                notes.append({"src": f, "target": b, "tag": tag, "status": "bind_tag_proposed"})
    return plan_lines, notes

def write_plan(plan_lines):
    plan = "#!/usr/bin/env bash\nset -euo pipefail\n\n" + "\n".join(plan_lines) + "\n"
    with open(os.path.join(SCR, "plan.sh"), "w", encoding="utf-8") as fp:
        fp.write(plan)
    return plan

def run_shell(cmd):
    return subprocess.run(cmd, shell=True, check=False, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--mode", choices=["run", "plan"], default="run")
    args = ap.parse_args()

    rules = read_rules()
    all_files = list(walk_files("."))
    front, back = classify(all_files, rules)

    log_event("scan_summary",
              front_count=len(front),
              back_count=len(back),
              rules_hash=sha1_text(json.dumps(rules, sort_keys=True)))

    plan_lines, notes = propose_binds(front, back, rules)
    write_plan(plan_lines)
    log_event("plan_generated", lines=len(plan_lines), notes=notes[:50])

    print(f"[agent] Proposed {len(plan_lines)} bind operations. Plan: scratch/plan.sh")

    if args.mode == "plan":
        print("[agent] Dry-run complete. No changes applied.")
        return

    if not plan_lines:
        print("[agent] Nothing to apply. Exiting.")
        return

    print("[agent] Applying plan...")
    res = run_shell("bash scratch/plan.sh")
    sys.stdout.write(res.stdout)
    log_event("plan_applied", rc=res.returncode, out_hash=sha1_text(res.stdout[-5000:] if res.stdout else ""))

    if res.returncode != 0:
        print("[agent] Plan completed with non-zero status; inspect logs/agent_run.jsonl")
        sys.exit(res.returncode)

    # Local-only autocommit if diffs exist
    run_shell('git config user.name "agent"')
    run_shell('git config user.email "agent@local"')
    diff = run_shell("git diff --name-only")
    if diff.stdout.strip():
        run_shell('git add -A')
        run_shell('git commit -m "agent: bind tags applied"')
        log_event("commit", files=diff.stdout.strip().splitlines())
        print("[agent] Changes committed locally.")
    else:
        print("[agent] No diffs; nothing to commit.")

if __name__ == "__main__":
    main()