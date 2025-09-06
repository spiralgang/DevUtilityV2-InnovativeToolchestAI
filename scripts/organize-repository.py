#!/usr/bin/env python3

import os
import shutil
import glob

# Change to the repository directory
repo_dir = "/home/runner/work/DevUtilityV2-InnovativeToolchestAI/DevUtilityV2-InnovativeToolchestAI"
os.chdir(repo_dir)

# Define file categorization patterns
file_moves = {
    # Persona files
    "docs/personas/": [
        "PerplexXx.txt",
        "*CodeReaver*",
        "*Persona*",
        "*SUDO-PERSONA*",
        "*Cognitive*",
    ],
    
    # Training datasets
    "docs/training/": [
        "*TrainingSet*",
        "*Training*Dataset*",
        "*Quantum*TrainingSet*",
        "DevUtility Trainingset*",
        "IceMaster AI*",
        "*Near-Quantum*",
        "*Specialized*",
    ],
    
    # Specifications and version docs
    "docs/specifications/": [
        "#V252*",
        "**New Version*",
        "*V2.5*",
        "*DevUtilityAndroid*",
        "cCc*",
    ],
    
    # Architecture and libraries
    "docs/architecture/": [
        "*Libraries*Requirements*",
        "*Illuminati-Structured*",
        "*Directory*",
        "*Index*Librarian*",
    ],
    
    # Legacy and miscellaneous
    "docs/legacy/": [
        "EATEN*",
        "HackLiberty.txt",
        "Heidegger.txt",
        "neuralagentic.txt",
        "nnmm-aa*",
        "autonomous-agency*",
        "UTF-8-demo.txt",
        "USE USERLAND*",
        "*BASH*SSH*",
        "*nightmare*",
    ],
    
    # Assets (images, PDFs, etc.)
    "docs/assets/": [
        "*.jpg",
        "*.png", 
        "*.pdf",
        "*.webp",
        "ic_launcher.png",
        "*NotebookLM*",
        "*Notes_*",
        "*Screenshot*",
        "*cambridge*",
    ],
}

def safe_move(src_pattern, dest_dir):
    """Safely move files matching pattern to destination directory"""
    try:
        # Ensure destination directory exists
        os.makedirs(dest_dir, exist_ok=True)
        
        # Find files matching the pattern
        files = glob.glob(src_pattern)
        moved_count = 0
        
        for file_path in files:
            if os.path.isfile(file_path):
                filename = os.path.basename(file_path)
                dest_path = os.path.join(dest_dir, filename)
                
                # Handle filename conflicts by adding a number
                counter = 1
                original_dest = dest_path
                while os.path.exists(dest_path):
                    name, ext = os.path.splitext(original_dest)
                    dest_path = f"{name}_{counter}{ext}"
                    counter += 1
                
                shutil.move(file_path, dest_path)
                print(f"Moved: {file_path} -> {dest_path}")
                moved_count += 1
                
        return moved_count
        
    except Exception as e:
        print(f"Error moving {src_pattern} to {dest_dir}: {e}")
        return 0

# Execute the file organization
total_moved = 0
for dest_dir, patterns in file_moves.items():
    print(f"\n=== Moving files to {dest_dir} ===")
    for pattern in patterns:
        count = safe_move(pattern, dest_dir)
        total_moved += count

print(f"\n✅ Total files organized: {total_moved}")

# List the resulting structure
print("\n📁 Organized structure:")
for root, dirs, files in os.walk("docs"):
    level = root.replace("docs", "").count(os.sep)
    indent = " " * 2 * level
    print(f"{indent}{os.path.basename(root)}/")
    subindent = " " * 2 * (level + 1)
    for file in files[:5]:  # Show first 5 files per directory
        print(f"{subindent}{file}")
    if len(files) > 5:
        print(f"{subindent}... and {len(files) - 5} more files")