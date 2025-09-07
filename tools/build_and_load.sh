#!/usr/bin/env bash
# Build and load the SELinux module (needs checkpolicy, semodule)
set -euo pipefail
mod=ban_enforcer

make -f /usr/share/selinux/devel/Makefile ${mod}.pp
semodule -i ${mod}.pp

# Apply file contexts
semanage fcontext -a -t ban_space_t '/opt/ban-space(/.*)?'
restorecon -R /opt/ban-space