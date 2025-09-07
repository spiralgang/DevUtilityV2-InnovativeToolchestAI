#!/usr/bin/env python3
# examples/nimm_mind_zarr_helper.py
# NIMM-MIND style helper: compressed chunked storage using Zarr + Blosc (userland alternative to ZRAM)
#
# Install requirements (if not done):
#   pip install zarr numcodecs blosc numpy
#
# Purpose:
# - Provide a reproducible minimal demo for compressed in-memory-like arrays on low-RAM devices.
# - Useful when kernel ZRAM is unavailable (non-root or kernel lacks module).
#
# Notes:
# - Default compressor uses Blosc(lz4) for low-latency on mobile.
# - Tune 'shape' and 'chunks' to your access pattern (rows vs columns).
# - This script purposely writes only a few chunks to avoid excessive disk IO on first-run.
import os, sys
try:
    import numpy as np
    import zarr
    from numcodecs import Blosc
except Exception as e:
    print("Missing Python deps:", e)
    print("Install with: pip install zarr numcodecs blosc numpy")
    sys.exit(2)

compressor = Blosc(cname='lz4', clevel=1, shuffle=Blosc.BITSHUFFLE)
tmpdir = os.path.expanduser(os.getenv("ICEDMAN_ZARR_DIR", "~/icedman/nimm_zarr"))
os.makedirs(tmpdir, exist_ok=True)
store_path = os.path.join(tmpdir, "dataset.zarr")

shape = (1024*10, 1024)      # ~10M floats -> ~40MB uncompressed; adjust for device
chunks = (64, 1024)         # tune by your access pattern

print("Creating zarr store at", store_path)
z = zarr.open(store_path, mode='w', shape=shape, chunks=chunks, dtype='float32', compressor=compressor)

# Populate a limited set of chunks to seed the store (safe first-run)
for i in range(0, min(256, shape[0]), chunks[0]):
    block = np.random.rand(chunks[0], chunks[1]).astype('float32')
    z[i:i+chunks[0], :] = block

print("Wrote example chunks. Now reading a single chunk (decompress into RAM):")
a = z[0:1, :]
print("chunk shape:", a.shape, "sum:", float(a.sum()))

# report store size (simple)
total_bytes = 0
for root, _, files in os.walk(store_path):
    for f in files:
        try:
            total_bytes += os.path.getsize(os.path.join(root, f))
        except Exception:
            pass

print("Approx on-disk store size (bytes):", total_bytes)
print("Path:", store_path)
print("Done. To reuse: open with zarr.open(store_path) and access slices (loads only touched chunks).")