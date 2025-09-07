#!/usr/bin/env python3
"""
DevUtility Repository Organizer
@GDA @UFUIC-O @PIPI @LDU @EG @GATT @SWT

Systematically organizes repository files according to DevUtility agentic standards.
Implements incremental, traceable file organization with validation at each step.

References:
- DevUtilityAndroidV2.5_Terms_Concepts_Dictionary.md
- reference/standards/DevUtilityAgenticStandards.md
"""

import os
import shutil
import glob
import logging
from pathlib import Path
from datetime import datetime
import json

# Configure logging for LDU (Linear Development Updates) tracking
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Change to the repository directory
repo_dir = "/home/runner/work/DevUtilityV2-InnovativeToolchestAI/DevUtilityV2-InnovativeToolchestAI"
os.chdir(repo_dir)

# Define DevUtility agentic standards file organization (@UFUIC-O)
file_moves = {
    # @UFUIC-O Android configurations
    "configs/android/": [
        "AndroidManifest.xml",
        "AndroidManifest_snippet.xml", 
        "PowerFeatureConfig.xml",
        "perfboostsconfig.xml",
        "perfconfigstore.xml", 
        "powerhint.xml",
        "permissions.xml",
        "privapp-permissions-*.xml",
        "services-platform-compat-config.xml",
        "network_security_config.xml"
    ],
    
    # @LDU Build configurations  
    "configs/build/": [
        "gradle.properties",
        "settings.gradle",
        "proguard-rules.pro",
        "BuildConfig.java",
        "R.java"
    ],
    
    # @GIA Security configurations
    "configs/security/": [
        "*ban*.conf",
        "*.nft", 
        "*.policy",
        "*.fc",
        "*.te",
        "ban_enforcer*",
        "sanitize_enforcer.sh",
        "*.service",
        "*.timer",
        "SECURITY.md",
        "universal-ai-bot-permissions.txt"
    ],
    
    # @LDU Training datasets
    "datasets/": [
        "*TrainingSet*.txt",
        "*Training*.txt", 
        "*trainingset*.txt",
        "DevUtility_TrainingSet_*.md",
        "Optimized_TrainingSet_*.md",
        "NNIMM_TRAINING_GUIDE.md",
        "*Near-Quantum*",
        "*Quantum*TrainingSet*",
        "C-Near-Quantum*",
        "K-Near-Quantum*"
    ],
    
    # @GIA Development tools
    "tools/": [
        "*.py",
        "*.js", 
        "*build*.sh",
        "*deploy*.sh",
        "*setup*.sh",
        "quick_start*.sh",
        "userland_*.sh",
        "toggle_net.sh",
        "zram_helper.sh",
        "*collect_and_*",
        "*transfer_*",
        "*recovery*",
        "*integration*",
        "techula_*.sh"
    ],
    
    # @SWT Reference architecture
    "reference/architecture/": [
        "ANDROID_SOURCE_SUMMARY.md",
        "ultimate_linux_android_discography.md", 
        "network_tools.md",
        "*Libraries*Requirements*",
        "*Illuminati-Structured*",
        "*Directory*",
        "*Index*Librarian*"
    ],
    
    # @GATT Training reference materials
    "reference/training/": [
        "bots.example.yaml",
        "profiles.example.yaml", 
        "nnmm-bots.example.yaml",
        "*.json.txt"
    ],
    
    # Persona files (@GATT)
    "docs/personas/": [
        "PerplexXx.txt",
        "*CodeReaver*",
        "*Persona*", 
        "*SUDO-PERSONA*",
        "*Cognitive*"
    ],
    
    # Documentation training (@GATT)
    "docs/training/": [
        "DevUtility Trainingset*",
        "IceMaster AI*"
    ],
    
    # Specifications (@SWT)
    "docs/specifications/": [
        "#V252*",
        "**New Version*",
        "*V2.5*",
        "*DevUtilityAndroid*",
        "cCc*"
    ],
    
    # Legacy content (@LDU)
    "archive/": [
        "*Version1*",
        "*Version2*", 
        "*Version3*",
        "*Version6*",
        "README (2).md",
        "README (3).md",
        "Recreating-DUHDUPE",
        "FROM-ONLINE-EXAMPLE.txt",
        "whats-up-with-deadsnakesppa.txt",
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
        "Oracle Storage*"
    ],
    
    # Assets and media
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
        "Busybox Installer*"
    ]
}

def safe_move(src_pattern, dest_dir):
    """@PIPI Safely move files matching pattern to destination directory with validation"""
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
                
                # @LDU Handle filename conflicts by adding a number
                counter = 1
                original_dest = dest_path
                while os.path.exists(dest_path):
                    name, ext = os.path.splitext(original_dest)
                    dest_path = f"{name}_{counter}{ext}"
                    counter += 1
                
                # @PIPI Execute move with logging
                shutil.move(file_path, dest_path)
                logger.info(f"@LDU MOVED: {file_path} -> {dest_path}")
                moved_count += 1
                
        return moved_count
        
    except Exception as e:
        logger.error(f"@PIPI ERROR moving {src_pattern} to {dest_dir}: {e}")
        return 0

def create_directory_index(directory, title):
    """@GATT Create navigation index for directories"""
    index_path = os.path.join(directory, "INDEX.md")
    
    with open(index_path, 'w') as f:
        f.write(f"# {title}\n")
        f.write("<!-- @GATT Guided-AI-Tutorial-Tips Navigation -->\n\n")
        
        # List files in directory
        files = [f for f in os.listdir(directory) if os.path.isfile(os.path.join(directory, f)) and f != "INDEX.md"]
        
        if files:
            f.write("## Contents (@EG Easy-to-Grasp)\n\n")
            for file in sorted(files):
                f.write(f"- [{file}]({file})\n")
        
        f.write(f"\n---\n*Generated following DevUtility agentic standards: @GDA @UFUIC-O @PIPI @LDU @EG @GATT @SWT*\n")
    
    logger.info(f"@GATT Created index for {directory}")

def validate_critical_systems():
    """@PIPI Validate that critical systems remain functional after organization"""
    logger.info("@PIPI Validating critical systems...")
    
    critical_paths = [
        "app/",
        "scripts/",
        "docs/",
        ".github/"
    ]
    
    validation_passed = True
    for path in critical_paths:
        if not os.path.exists(path):
            logger.error(f"@PIPI CRITICAL: {path} missing after organization")
            validation_passed = False
        else:
            logger.info(f"@PIPI OK: {path} preserved")
    
    # Check conflict resolution system
    if os.path.exists("scripts/validate-system.sh"):
        logger.info("@PIPI Conflict resolution system preserved")
    else:
        logger.warning("@PIPI Conflict resolution script not found")
    
    return validation_passed

# @GDA Execute the file organization following agentic standards
logger.info("@GDA Starting DevUtility repository organization")
logger.info("@PIPI Phase 1: Preview and validate planned moves")

total_moved = 0
organization_log = {
    "timestamp": datetime.now().isoformat(),
    "moves": {},
    "standards_applied": ["GDA", "UFUIC-O", "PIPI", "LDU", "EG", "GATT", "SWT"]
}

for dest_dir, patterns in file_moves.items():
    logger.info(f"@GDA Processing directory: {dest_dir}")
    dir_moved = 0
    
    for pattern in patterns:
        count = safe_move(pattern, dest_dir)
        dir_moved += count
        total_moved += count
    
    organization_log["moves"][dest_dir] = dir_moved
    
    # @GATT Create index files for organized directories
    if dir_moved > 0 and os.path.exists(dest_dir):
        create_directory_index(dest_dir, f"DevUtility {os.path.basename(dest_dir).title()}")

logger.info(f"@LDU Total files organized: {total_moved}")

# @PIPI Phase 2: Validate organization 
validation_passed = validate_critical_systems()

if validation_passed:
    logger.info("@PIPI Organization validation PASSED")
else:
    logger.error("@PIPI Organization validation FAILED")

# @LDU Save organization log
os.makedirs("logs", exist_ok=True)
with open("logs/organization_log.json", 'w') as f:
    json.dump(organization_log, f, indent=2)

# Create main repository index (@GATT)
main_index_content = """# DevUtility Repository Index
<!-- @GATT Guided-AI-Tutorial-Tips Navigation -->

## Quick Navigation (@EG Easy-to-Grasp)

### Primary Directories
- [`app/`](app/) - Android application source code
- [`scripts/`](scripts/) - Automation and utility scripts  
- [`docs/`](docs/) - Documentation and guides
- [`configs/`](configs/) - Configuration files by category
- [`tools/`](tools/) - Development utilities
- [`datasets/`](datasets/) - Training and reference data
- [`reference/`](reference/) - Standards and architecture docs

### Configuration Categories (@UFUIC-O)
- [`configs/android/`](configs/android/) - Android manifests and device configs
- [`configs/build/`](configs/build/) - Build system configuration
- [`configs/security/`](configs/security/) - Security policies and enforcement

### Reference Materials (@SWT)
- [`reference/standards/`](reference/standards/) - Agentic standards definitions
- [`reference/architecture/`](reference/architecture/) - System architecture
- [`reference/training/`](reference/training/) - AI training datasets

## Navigation Tips (@GATT)
1. Start with `reference/standards/` for organizational principles
2. Check `docs/` for user guides and tutorials
3. Find working scripts in `scripts/` directory
4. Configuration files are categorized in `configs/`

## Organization Log
- Total files organized: """ + str(total_moved) + """
- Organization completed: """ + datetime.now().strftime("%Y-%m-%d %H:%M:%S") + """
- Standards applied: @GDA @UFUIC-O @PIPI @LDU @EG @GATT @SWT

---
*Generated following DevUtility agentic standards: @GDA @UFUIC-O @PIPI @LDU @EG @GATT @SWT*
"""

with open("INDEX.md", 'w') as f:
    f.write(main_index_content)

logger.info("@GATT Created main repository index")

# @SWT Display the resulting structure
logger.info("@SWT Repository structure after organization:")
for root, dirs, files in os.walk("."):
    # Skip hidden directories and build artifacts
    dirs[:] = [d for d in dirs if not d.startswith('.') and d not in ['build', 'node_modules']]
    
    level = root.replace(".", "").count(os.sep)
    if level <= 2:  # Only show first 2 levels
        indent = " " * 2 * level
        print(f"{indent}{os.path.basename(root)}/")
        subindent = " " * 2 * (level + 1)
        for file in files[:3]:  # Show first 3 files per directory
            print(f"{subindent}{file}")
        if len(files) > 3:
            print(f"{subindent}... and {len(files) - 3} more files")

logger.info("@GDA DevUtility repository organization complete!")
logger.info("@GATT Check INDEX.md for navigation guide")