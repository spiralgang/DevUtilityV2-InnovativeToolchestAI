#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
UTF-8 Encoding Verification and Conversion Script
=================================================

This script goes through every file in the repository and ensures proper UTF-8 encoding
for data protection and compatibility. It identifies non-UTF-8 files and converts them
while preserving the original content and maintaining file permissions.

Part of: DevUl Army : Living Sriracha AGI
License: See LICENSE file for terms
"""

import os
import sys
import codecs
import chardet
import argparse
import logging
from pathlib import Path
from typing import List, Tuple, Optional
import hashlib
import json
from datetime import datetime

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('utf8_conversion.log'),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

class UTF8Encoder:
    """UTF-8 encoding manager for repository files."""
    
    def __init__(self, repo_path: str, dry_run: bool = False):
        self.repo_path = Path(repo_path)
        self.dry_run = dry_run
        self.stats = {
            'total_files': 0,
            'text_files': 0,
            'already_utf8': 0,
            'converted': 0,
            'errors': 0,
            'skipped': 0
        }
        self.conversion_log = []
        
        # File extensions to process
        self.text_extensions = {
            '.txt', '.md', '.py', '.kt', '.java', '.sh', '.xml', '.json',
            '.html', '.css', '.js', '.sql', '.yaml', '.yml', '.cfg', '.conf',
            '.ini', '.properties', '.log', '.csv', '.tsv', '.gradle', '.pro'
        }
        
        # Directories to skip
        self.skip_dirs = {
            '.git', '.gradle', 'build', 'node_modules', '__pycache__',
            '.idea', '.vscode', 'target', 'dist', 'out'
        }
        
        # Binary file patterns to skip
        self.binary_extensions = {
            '.png', '.jpg', '.jpeg', '.gif', '.ico', '.svg', '.pdf',
            '.zip', '.tar', '.gz', '.7z', '.rar', '.apk', '.jar',
            '.class', '.so', '.dll', '.exe', '.bin', '.dat'
        }

    def is_text_file(self, file_path: Path) -> bool:
        """Determine if a file should be processed as text."""
        if file_path.suffix.lower() in self.binary_extensions:
            return False
            
        if file_path.suffix.lower() in self.text_extensions:
            return True
            
        # For files without extensions, try to detect
        try:
            with open(file_path, 'rb') as f:
                sample = f.read(8192)
                if b'\x00' in sample:  # Null bytes indicate binary
                    return False
                    
            # Try to decode as text
            with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                f.read(1024)
            return True
        except Exception:
            return False

    def detect_encoding(self, file_path: Path) -> Optional[str]:
        """Detect the encoding of a file."""
        try:
            with open(file_path, 'rb') as f:
                raw_data = f.read()
                
            if not raw_data:
                return 'utf-8'  # Empty files are considered UTF-8
                
            detection = chardet.detect(raw_data)
            encoding = detection.get('encoding', '').lower()
            confidence = detection.get('confidence', 0)
            
            # If confidence is too low, try common encodings
            if confidence < 0.7:
                for test_encoding in ['utf-8', 'ascii', 'latin-1', 'cp1252']:
                    try:
                        raw_data.decode(test_encoding)
                        return test_encoding
                    except UnicodeDecodeError:
                        continue
                        
            return encoding if encoding else 'unknown'
            
        except Exception as e:
            logger.error(f"Error detecting encoding for {file_path}: {e}")
            return None

    def convert_to_utf8(self, file_path: Path) -> bool:
        """Convert a file to UTF-8 encoding."""
        try:
            # Detect current encoding
            current_encoding = self.detect_encoding(file_path)
            if not current_encoding or current_encoding == 'unknown':
                logger.warning(f"Could not determine encoding for {file_path}")
                self.stats['errors'] += 1
                return False
                
            if current_encoding == 'utf-8':
                logger.info(f"File already UTF-8: {file_path}")
                self.stats['already_utf8'] += 1
                return True
                
            # Read file with detected encoding
            with open(file_path, 'r', encoding=current_encoding, errors='replace') as f:
                content = f.read()
                
            # Get file stats for preservation
            stat_info = file_path.stat()
            
            if self.dry_run:
                logger.info(f"[DRY RUN] Would convert {file_path} from {current_encoding} to UTF-8")
                return True
                
            # Create backup
            backup_path = file_path.with_suffix(file_path.suffix + '.backup')
            if backup_path.exists():
                backup_path = file_path.with_suffix(f"{file_path.suffix}.backup.{datetime.now().strftime('%Y%m%d_%H%M%S')}")
                
            file_path.rename(backup_path)
            
            # Write file as UTF-8 with BOM for certain file types
            encoding = 'utf-8-sig' if file_path.suffix.lower() in ['.txt', '.csv'] else 'utf-8'
            with open(file_path, 'w', encoding=encoding, newline='') as f:
                f.write(content)
                
            # Restore file permissions and timestamps
            os.chmod(file_path, stat_info.st_mode)
            os.utime(file_path, (stat_info.st_atime, stat_info.st_mtime))
            
            # Log conversion
            conversion_info = {
                'file': str(file_path),
                'from_encoding': current_encoding,
                'to_encoding': encoding,
                'timestamp': datetime.now().isoformat(),
                'backup': str(backup_path)
            }
            self.conversion_log.append(conversion_info)
            
            logger.info(f"Converted {file_path} from {current_encoding} to {encoding}")
            self.stats['converted'] += 1
            return True
            
        except Exception as e:
            logger.error(f"Error converting {file_path}: {e}")
            self.stats['errors'] += 1
            return False

    def scan_directory(self, directory: Path) -> List[Path]:
        """Scan directory for text files that need processing."""
        text_files = []
        
        for root, dirs, files in os.walk(directory):
            # Skip certain directories
            dirs[:] = [d for d in dirs if d not in self.skip_dirs]
            
            root_path = Path(root)
            for file in files:
                file_path = root_path / file
                
                if self.is_text_file(file_path):
                    text_files.append(file_path)
                    
                self.stats['total_files'] += 1
                
        self.stats['text_files'] = len(text_files)
        return text_files

    def process_repository(self) -> None:
        """Process the entire repository for UTF-8 conversion."""
        logger.info(f"Starting UTF-8 encoding process for: {self.repo_path}")
        logger.info(f"Dry run mode: {self.dry_run}")
        
        # Scan for text files
        text_files = self.scan_directory(self.repo_path)
        logger.info(f"Found {len(text_files)} text files out of {self.stats['total_files']} total files")
        
        # Process each text file
        for file_path in text_files:
            try:
                self.convert_to_utf8(file_path)
            except Exception as e:
                logger.error(f"Failed to process {file_path}: {e}")
                self.stats['errors'] += 1
                
        # Generate report
        self.generate_report()

    def generate_report(self) -> None:
        """Generate a detailed report of the conversion process."""
        report = {
            'timestamp': datetime.now().isoformat(),
            'repository': str(self.repo_path),
            'dry_run': self.dry_run,
            'statistics': self.stats,
            'conversions': self.conversion_log
        }
        
        report_path = self.repo_path / 'utf8_conversion_report.json'
        with open(report_path, 'w', encoding='utf-8') as f:
            json.dump(report, f, indent=2, ensure_ascii=False)
            
        logger.info("\n" + "="*50)
        logger.info("UTF-8 CONVERSION REPORT")
        logger.info("="*50)
        logger.info(f"Total files scanned: {self.stats['total_files']}")
        logger.info(f"Text files processed: {self.stats['text_files']}")
        logger.info(f"Already UTF-8: {self.stats['already_utf8']}")
        logger.info(f"Converted to UTF-8: {self.stats['converted']}")
        logger.info(f"Errors: {self.stats['errors']}")
        logger.info(f"Detailed report saved to: {report_path}")
        logger.info("="*50)

    def add_utf8_headers(self) -> None:
        """Add UTF-8 encoding declarations to source files."""
        python_files = list(self.repo_path.glob("**/*.py"))
        
        for py_file in python_files:
            try:
                with open(py_file, 'r', encoding='utf-8') as f:
                    content = f.read()
                    
                # Check if encoding declaration already exists
                lines = content.split('\n')
                has_encoding = False
                has_shebang = False
                
                for i, line in enumerate(lines[:3]):
                    if line.startswith('#!'):
                        has_shebang = True
                    if 'coding:' in line or 'encoding:' in line:
                        has_encoding = True
                        break
                        
                if not has_encoding:
                    if self.dry_run:
                        logger.info(f"[DRY RUN] Would add UTF-8 declaration to {py_file}")
                        continue
                        
                    # Add encoding declaration
                    encoding_line = "# -*- coding: utf-8 -*-"
                    
                    if has_shebang:
                        # Insert after shebang
                        lines.insert(1, encoding_line)
                    else:
                        # Insert at beginning
                        lines.insert(0, encoding_line)
                        
                    new_content = '\n'.join(lines)
                    
                    with open(py_file, 'w', encoding='utf-8') as f:
                        f.write(new_content)
                        
                    logger.info(f"Added UTF-8 declaration to {py_file}")
                    
            except Exception as e:
                logger.error(f"Error processing {py_file}: {e}")

def main():
    parser = argparse.ArgumentParser(description='UTF-8 Encoding Converter for Repository')
    parser.add_argument('--repo-path', default='.', help='Repository path to process')
    parser.add_argument('--dry-run', action='store_true', help='Show what would be done without making changes')
    parser.add_argument('--add-headers', action='store_true', help='Add UTF-8 encoding headers to source files')
    parser.add_argument('--verbose', action='store_true', help='Enable verbose logging')
    
    args = parser.parse_args()
    
    if args.verbose:
        logging.getLogger().setLevel(logging.DEBUG)
        
    repo_path = Path(args.repo_path).resolve()
    
    if not repo_path.exists():
        logger.error(f"Repository path does not exist: {repo_path}")
        sys.exit(1)
        
    encoder = UTF8Encoder(repo_path, dry_run=args.dry_run)
    
    try:
        encoder.process_repository()
        
        if args.add_headers:
            logger.info("Adding UTF-8 headers to source files...")
            encoder.add_utf8_headers()
            
    except KeyboardInterrupt:
        logger.info("Process interrupted by user")
        sys.exit(1)
    except Exception as e:
        logger.error(f"Unexpected error: {e}")
        sys.exit(1)

if __name__ == '__main__':
    main()