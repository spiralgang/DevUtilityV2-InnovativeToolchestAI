# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This file is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
examples/nimm_mind_zarr_example.py

Lightweight "NIMM-MIND" demo: use Zarr + Blosc to hold large numpy arrays compressed on disk or in-memory.
- Good for image stacks, tiled model inputs, and datasets that fit chunked access patterns.
- Choose compressor 'blosc:lz4' for speed or 'blosc:zstd' for higher compression.
"""

import numpy as np
import zarr
from numcodecs import Blosc
import tempfile
import os

# Choose compressor: lz4 for speed on mobile, zstd for better ratio
compressor = Blosc(cname='lz4', clevel=1, shuffle=Blosc.BITSHUFFLE)

# Create a temporary zarr store on disk (you can also use MemoryStore for pure-RAM)
tmpdir = tempfile.mkdtemp(prefix="nimm_zarr_")
store_path = os.path.join(tmpdir, "dataset.zarr")

# Create a chunked array: e.g., 10000 x 1024 float32 (large)
shape = (10000, 1024)
chunks = (64, 1024)  # tune by access pattern
z = zarr.open(store_path, mode='w', shape=shape, chunks=chunks, dtype='float32', compressor=compressor)

# Populate few chunks lazily (simulate on-demand)
for i in range(0, shape[0], chunks[0]):
    block = np.random.rand(chunks[0], chunks[1]).astype('float32')
    z[i:i+chunks[0], :] = block  # compressed on write

print("Zarr store created at:", store_path)
print("Total uncompressed bytes (approx):", np.prod(shape) * 4)
print("Zarr directory size (on disk):", sum(os.path.getsize(os.path.join(dp, f)) for dp, dn, fn in os.walk(store_path) for f in fn))
print("Read single chunk (decompresses into RAM):")
a = z[0:1, :]   # only that chunk loads into memory
print("shape:", a.shape, "sum:", a.sum())