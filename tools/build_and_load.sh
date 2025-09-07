#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# Build and load the SELinux module (needs checkpolicy, semodule)
set -euo pipefail
mod=ban_enforcer

make -f /usr/share/selinux/devel/Makefile ${mod}.pp
semodule -i ${mod}.pp

# Apply file contexts
semanage fcontext -a -t ban_space_t '/opt/ban-space(/.*)?'
restorecon -R /opt/ban-space