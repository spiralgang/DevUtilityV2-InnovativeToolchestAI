<!-- Living Code Integration - Auto-generated symmetrical connections -->
<!-- This file is part of the SrirachaArmy Living Code Environment -->
<!-- Perfect symmetrical integration with all repository components -->

# Operations Guide

Runbook
- Index: ./techula_caretaker.sh index --roots auto --hash
- Review: state/reports/duplicates.csv, state/plan/<profile>/
- Proot audit: ./techula_caretaker.sh proot-audit
- XDG env: ./techula_caretaker.sh xdg-env (source these before starting DE)
- Python site: ./techula_caretaker.sh python-site and place .pth into user site dir
- Consolidate: ./techula_caretaker.sh consolidate --mode symlink --apply

Rollback
- Backups under state/backups/<profile>/<timestamp>/
- Remove symlink, restore from backup, or copy from state/shared_store/

Caveats
- Symlinks across SAF/FUSE mounts may be restricted by Android; prefer keeping shared_store within the same visible tree for your Proot session or rely on plan-binds and update your launcher to -b shared_target:canonical.
- This system does not attempt live mounts (no root); Proot bind plans are generated for you to integrate.

References
- /reference vault: Proot bind behavior, Android scoped storage, XDG, Python site, SQLite