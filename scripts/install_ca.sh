#!/usr/bin/env bash
# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This script is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# Source living environment if available
if [[ -f "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh" ]]; then
    source "$(dirname "${BASH_SOURCE[0]}")/../.living_environment_wrapper.sh"
fi

# Install org CA cert into system trust and curl-ca-bundle (Debian/Ubuntu/RHEL style)
set -euo pipefail
CA="${1:?Usage: $0 ORG-ROOT-CA.pem}"

if [ -d /usr/local/share/ca-certificates ]; then
  cp "$CA" "/usr/local/share/ca-certificates/$(basename "$CA" .pem).crt"
  update-ca-certificates
elif [ -d /etc/pki/ca-trust/source/anchors ]; then
  cp "$CA" /etc/pki/ca-trust/source/anchors/
  update-ca-trust extract
else
  echo "Unknown trust store layout; install CA manually." >&2
  exit 1
fi

echo "CA installed. Validate: openssl s_client -connect example.com:443 -CAfile $CA </dev/null"