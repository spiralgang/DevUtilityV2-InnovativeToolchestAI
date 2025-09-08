#!/usr/bin/env python3
import argparse, json, os, re, subprocess, sys, time, hashlib

def sh(*cmd):
    return subprocess.check_output(list(cmd), text=True).strip()

def changed_files(base):
    out = sh("git","diff","--name-only",f"{base}...")
    return set(filter(None, out.splitlines()))

def file_exists(path): 
    return os.path.exists(path)

def file_contains(path, needles):
    if not file_exists(path): 
        return False
    try:
        data = open(path, "r", errors="ignore").read()
    except Exception:
        return False
    for n in needles:
        if re.search(re.escape(n), data):
            return True
    return False

def log_line(fp, obj):
    fp.write(json.dumps(obj, ensure_ascii=False) + "\n")
    fp.flush()

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--manifest", required=True)
    ap.add_argument("--mapping", required=True)
    ap.add_argument("--base", required=True)
    ap.add_argument("--log", required=True)
    ap.add_argument("--strict", action="store_true")
    args = ap.parse_args()

    ts = time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    ch = changed_files(args.base)

    if not os.path.exists(args.manifest):
        print(f"Manifest not found: {args.manifest}. Creating minimal manifest.")
        manifest = {"files": []}
    else:
        manifest = json.load(open(args.manifest))
    
    if not os.path.exists(args.mapping):
        print(f"Mapping not found: {args.mapping}. Creating minimal mapping.")
        mapping = {}
    else:
        mapping = json.load(open(args.mapping))
    
    files = manifest.get("files", [])

    os.makedirs(os.path.dirname(args.log), exist_ok=True)
    fp = open(args.log, "a", encoding="utf-8")

    total = len(files)
    ok = 0
    missing = []
    unbound = []

    log_line(fp, {"ts": ts, "phase": "start", "total": total, "changed": len(ch)})

    for src in files:
        m = mapping.get(src)
        entry = {"ts": ts, "src": src, "phase": "audit"}
        if not m:
            entry.update({"status":"mapping_missing"})
            log_line(fp, entry)
            unbound.append(src)
            continue

        tgt = m.get("target")
        binds = m.get("binds", [])
        entry.update({"target": tgt, "binds": binds})

        present = file_exists(tgt) if tgt else False
        entry["target_present"] = bool(present)

        binds_ok = []
        binds_missing = []
        for b in binds:
            if file_contains(b, [os.path.basename(tgt or ""), os.path.splitext(os.path.basename(tgt or ""))[0]]):
                binds_ok.append(b)
            else:
                binds_missing.append(b)

        entry["binds_ok"] = binds_ok
        entry["binds_missing"] = binds_missing
        entry["changed_in_pr"] = (src in ch) or (tgt in ch if tgt else False) or any(b in ch for b in binds)

        if present and not binds_missing:
            entry["status"] = "assimilated"
            ok += 1
        elif not present:
            entry["status"] = "target_missing"
            missing.append(src)
        else:
            entry["status"] = "binds_incomplete"
            unbound.append(src)

        log_line(fp, entry)

    summary = {
        "ts": ts,
        "phase": "summary",
        "total": total,
        "ok": ok,
        "missing": len(missing),
        "unbound": len(unbound),
        "strict": args.strict
    }
    log_line(fp, summary)

    print(f"Assimilation audit complete: {ok}/{total} assimilated")
    if missing:
        print(f"Missing targets: {missing[:5]}{'...' if len(missing) > 5 else ''}")
    if unbound:
        print(f"Unbound files: {unbound[:5]}{'...' if len(unbound) > 5 else ''}")

    if args.strict and (ok != total):
        print(f"Assimilation incomplete: ok={ok}/{total} missing={len(missing)} unbound={len(unbound)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()