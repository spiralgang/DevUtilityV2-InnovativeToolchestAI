#!/usr/bin/env python3
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