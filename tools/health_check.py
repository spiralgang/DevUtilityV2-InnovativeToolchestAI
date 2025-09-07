# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This file is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from __future__ import annotations
import os, requests

def main():
    base = os.environ.get("API_BASE", "http://127.0.0.1:8000")
    r = requests.get(f"{base}/health", timeout=5)
    print(r.json())

if __name__ == "__main__":
    main()

# References:
# - /reference vault