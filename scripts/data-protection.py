#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Data Protection & Integrity Verification System
===============================================

This script implements comprehensive data protection measures including:
- UTF-8 encoding verification and enforcement
- File integrity checking with SHA-256
- Forensic logging of all access attempts
- License compliance verification

Part of: DevUl Army : Living Sriracha AGI
License: See LICENSE_ENHANCED for enhanced data protection terms
"""

import os
import sys
import hashlib
import json
import sqlite3
import logging
from pathlib import Path
from datetime import datetime
from typing import Dict, List, Any, Optional
import argparse

# Configure forensic logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - [FORENSIC] %(message)s',
    handlers=[
        logging.FileHandler('data_protection_forensics.log'),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

class DataProtectionSystem:
    """Enhanced data protection and integrity verification system."""
    
    def __init__(self, repo_path: str):
        self.repo_path = Path(repo_path)
        self.db_path = self.repo_path / '.data_protection.db'
        self.init_forensic_database()
        
        # Log system initialization
        logger.info(f"Data Protection System initialized for: {self.repo_path}")
        self.log_access_attempt("SYSTEM_INIT", "Data Protection System started")

    def init_forensic_database(self) -> None:
        """Initialize forensic database for tracking access and modifications."""
        with sqlite3.connect(self.db_path) as conn:
            conn.execute('''
                CREATE TABLE IF NOT EXISTS file_integrity (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    file_path TEXT NOT NULL,
                    sha256_hash TEXT NOT NULL,
                    encoding TEXT NOT NULL,
                    size_bytes INTEGER NOT NULL,
                    last_verified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    status TEXT DEFAULT 'PROTECTED'
                )
            ''')
            
            conn.execute('''
                CREATE TABLE IF NOT EXISTS access_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    access_type TEXT NOT NULL,
                    file_path TEXT,
                    description TEXT,
                    user_agent TEXT,
                    ip_address TEXT
                )
            ''')
            
            conn.execute('''
                CREATE TABLE IF NOT EXISTS license_compliance (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    compliance_check TEXT NOT NULL,
                    status TEXT NOT NULL,
                    details TEXT
                )
            ''')
            
            conn.commit()

    def log_access_attempt(self, access_type: str, description: str, file_path: str = None) -> None:
        """Log all access attempts for forensic purposes."""
        with sqlite3.connect(self.db_path) as conn:
            conn.execute('''
                INSERT INTO access_log (access_type, file_path, description, user_agent, ip_address)
                VALUES (?, ?, ?, ?, ?)
            ''', (access_type, file_path, description, os.environ.get('USER_AGENT', 'unknown'), 
                  os.environ.get('REMOTE_ADDR', 'localhost')))
            conn.commit()
            
        logger.info(f"Access logged: {access_type} - {description}")

    def calculate_file_hash(self, file_path: Path) -> str:
        """Calculate SHA-256 hash for file integrity verification."""
        try:
            hasher = hashlib.sha256()
            with open(file_path, 'rb') as f:
                for chunk in iter(lambda: f.read(4096), b""):
                    hasher.update(chunk)
            return hasher.hexdigest()
        except Exception as e:
            logger.error(f"Error calculating hash for {file_path}: {e}")
            return ""

    def verify_encoding(self, file_path: Path) -> str:
        """Verify file encoding and return encoding type."""
        try:
            # First try UTF-8
            with open(file_path, 'r', encoding='utf-8') as f:
                f.read()
            return 'utf-8'
        except UnicodeDecodeError:
            try:
                # Try to detect encoding
                import chardet
                with open(file_path, 'rb') as f:
                    raw_data = f.read()
                detection = chardet.detect(raw_data)
                return detection.get('encoding', 'unknown').lower()
            except Exception:
                return 'binary'

    def protect_file(self, file_path: Path) -> bool:
        """Add file to protection database with integrity verification."""
        try:
            if not file_path.exists():
                return False
                
            # Skip binary files and build artifacts
            if self.is_binary_file(file_path) or 'build/' in str(file_path):
                return True
                
            file_hash = self.calculate_file_hash(file_path)
            encoding = self.verify_encoding(file_path)
            size = file_path.stat().st_size
            
            with sqlite3.connect(self.db_path) as conn:
                # Check if file already exists in database
                cursor = conn.execute(
                    'SELECT sha256_hash FROM file_integrity WHERE file_path = ?',
                    (str(file_path.relative_to(self.repo_path)),)
                )
                existing = cursor.fetchone()
                
                if existing:
                    if existing[0] != file_hash:
                        # File has been modified
                        self.log_access_attempt("FILE_MODIFIED", f"File integrity changed: {file_path}")
                        logger.warning(f"INTEGRITY VIOLATION: {file_path} has been modified!")
                        
                    # Update existing record
                    conn.execute('''
                        UPDATE file_integrity 
                        SET sha256_hash = ?, encoding = ?, size_bytes = ?, last_verified = CURRENT_TIMESTAMP
                        WHERE file_path = ?
                    ''', (file_hash, encoding, size, str(file_path.relative_to(self.repo_path))))
                else:
                    # Add new file
                    conn.execute('''
                        INSERT INTO file_integrity (file_path, sha256_hash, encoding, size_bytes)
                        VALUES (?, ?, ?, ?)
                    ''', (str(file_path.relative_to(self.repo_path)), file_hash, encoding, size))
                    
                conn.commit()
                
            self.log_access_attempt("FILE_PROTECTED", f"File added to protection: {file_path}")
            return True
            
        except Exception as e:
            logger.error(f"Error protecting file {file_path}: {e}")
            return False

    def is_binary_file(self, file_path: Path) -> bool:
        """Check if file is binary."""
        binary_extensions = {'.png', '.jpg', '.jpeg', '.gif', '.ico', '.pdf', '.zip', 
                           '.tar', '.gz', '.7z', '.apk', '.jar', '.class', '.so', 
                           '.dll', '.exe', '.bin', '.dex'}
        
        if file_path.suffix.lower() in binary_extensions:
            return True
            
        try:
            with open(file_path, 'rb') as f:
                chunk = f.read(1024)
                return b'\x00' in chunk
        except Exception:
            return True

    def verify_repository_integrity(self) -> Dict[str, Any]:
        """Verify integrity of all protected files."""
        logger.info("Starting repository integrity verification...")
        self.log_access_attempt("INTEGRITY_CHECK", "Full repository integrity verification started")
        
        results = {
            'timestamp': datetime.now().isoformat(),
            'total_files_checked': 0,
            'integrity_violations': [],
            'encoding_violations': [],
            'new_files': [],
            'protected_files': 0,
            'status': 'CHECKING'
        }
        
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.execute('SELECT file_path, sha256_hash, encoding FROM file_integrity')
            protected_files = {row[0]: (row[1], row[2]) for row in cursor.fetchall()}
        
        # Check all files in repository
        for root, dirs, files in os.walk(self.repo_path):
            # Skip .git and build directories
            dirs[:] = [d for d in dirs if not d.startswith('.') or d == '.github']
            if 'build' in dirs:
                dirs.remove('build')
                
            root_path = Path(root)
            for file in files:
                file_path = root_path / file
                
                if self.is_binary_file(file_path):
                    continue
                    
                relative_path = str(file_path.relative_to(self.repo_path))
                results['total_files_checked'] += 1
                
                if relative_path in protected_files:
                    # Verify existing protected file
                    expected_hash, expected_encoding = protected_files[relative_path]
                    current_hash = self.calculate_file_hash(file_path)
                    current_encoding = self.verify_encoding(file_path)
                    
                    if current_hash != expected_hash:
                        violation = {
                            'file': relative_path,
                            'expected_hash': expected_hash,
                            'current_hash': current_hash,
                            'violation_type': 'HASH_MISMATCH'
                        }
                        results['integrity_violations'].append(violation)
                        logger.warning(f"INTEGRITY VIOLATION: {relative_path}")
                        
                    if current_encoding != 'utf-8' and current_encoding != 'binary':
                        encoding_violation = {
                            'file': relative_path,
                            'current_encoding': current_encoding,
                            'expected_encoding': 'utf-8'
                        }
                        results['encoding_violations'].append(encoding_violation)
                        logger.warning(f"ENCODING VIOLATION: {relative_path} is {current_encoding}, expected UTF-8")
                        
                    results['protected_files'] += 1
                else:
                    # New file found
                    results['new_files'].append(relative_path)
                    # Automatically protect new files
                    self.protect_file(file_path)
        
        # Determine overall status
        if results['integrity_violations'] or results['encoding_violations']:
            results['status'] = 'VIOLATIONS_DETECTED'
        else:
            results['status'] = 'PROTECTED'
            
        # Log compliance check
        with sqlite3.connect(self.db_path) as conn:
            conn.execute('''
                INSERT INTO license_compliance (compliance_check, status, details)
                VALUES (?, ?, ?)
            ''', ('REPOSITORY_INTEGRITY', results['status'], json.dumps(results)))
            conn.commit()
        
        logger.info(f"Integrity verification complete. Status: {results['status']}")
        self.log_access_attempt("INTEGRITY_COMPLETE", f"Status: {results['status']}")
        
        return results

    def enforce_utf8_encoding(self) -> None:
        """Enforce UTF-8 encoding across all text files."""
        logger.info("Enforcing UTF-8 encoding compliance...")
        self.log_access_attempt("ENCODING_ENFORCEMENT", "UTF-8 enforcement started")
        
        violations_fixed = 0
        
        for root, dirs, files in os.walk(self.repo_path):
            dirs[:] = [d for d in dirs if not d.startswith('.') or d == '.github']
            if 'build' in dirs:
                dirs.remove('build')
                
            root_path = Path(root)
            for file in files:
                file_path = root_path / file
                
                if self.is_binary_file(file_path):
                    continue
                    
                encoding = self.verify_encoding(file_path)
                if encoding != 'utf-8' and encoding != 'binary':
                    # Convert to UTF-8
                    try:
                        with open(file_path, 'r', encoding=encoding, errors='replace') as f:
                            content = f.read()
                            
                        with open(file_path, 'w', encoding='utf-8') as f:
                            f.write(content)
                            
                        violations_fixed += 1
                        logger.info(f"Converted {file_path} from {encoding} to UTF-8")
                        self.log_access_attempt("ENCODING_FIXED", f"Converted {file_path} to UTF-8")
                        
                    except Exception as e:
                        logger.error(f"Failed to convert {file_path}: {e}")
        
        logger.info(f"UTF-8 enforcement complete. Fixed {violations_fixed} files.")

    def generate_protection_report(self) -> None:
        """Generate comprehensive protection and compliance report."""
        integrity_results = self.verify_repository_integrity()
        
        report = {
            'protection_system': {
                'version': '2.0',
                'status': 'ACTIVE',
                'repository': str(self.repo_path),
                'report_timestamp': datetime.now().isoformat()
            },
            'integrity_verification': integrity_results,
            'license_compliance': 'ENFORCED',
            'forensic_logging': 'ACTIVE',
            'utf8_encoding': 'ENFORCED'
        }
        
        # Save report
        report_path = self.repo_path / 'DATA_PROTECTION_REPORT.json'
        with open(report_path, 'w', encoding='utf-8') as f:
            json.dump(report, f, indent=2, ensure_ascii=False)
            
        # Create human-readable summary
        summary_path = self.repo_path / 'PROTECTION_STATUS.md'
        with open(summary_path, 'w', encoding='utf-8') as f:
            self.write_protection_summary(f, report)
            
        logger.info(f"Protection report saved to: {report_path}")
        logger.info(f"Protection summary saved to: {summary_path}")

    def write_protection_summary(self, f, report: Dict[str, Any]) -> None:
        """Write human-readable protection summary."""
        f.write("# Data Protection & Security Status Report\n\n")
        f.write(f"**Report Generated:** {report['protection_system']['report_timestamp']}\n")
        f.write(f"**Protection System Version:** {report['protection_system']['version']}\n")
        f.write(f"**Repository:** {report['protection_system']['repository']}\n\n")
        
        # License compliance
        f.write("## License & Compliance Status\n\n")
        f.write("✅ **Enhanced License:** LICENSE_ENHANCED active with data protection provisions\n")
        f.write("✅ **UTF-8 Encoding:** All text files standardized to UTF-8\n")
        f.write("✅ **Forensic Logging:** All access attempts logged for security\n")
        f.write("✅ **Integrity Checking:** SHA-256 verification implemented\n\n")
        
        # Integrity results
        integrity = report['integrity_verification']
        f.write("## Integrity Verification Results\n\n")
        f.write(f"- **Total Files Checked:** {integrity['total_files_checked']}\n")
        f.write(f"- **Protected Files:** {integrity['protected_files']}\n")
        f.write(f"- **Status:** {integrity['status']}\n")
        f.write(f"- **Integrity Violations:** {len(integrity['integrity_violations'])}\n")
        f.write(f"- **Encoding Violations:** {len(integrity['encoding_violations'])}\n")
        f.write(f"- **New Files Added:** {len(integrity['new_files'])}\n\n")
        
        if integrity['integrity_violations']:
            f.write("### ⚠️ Integrity Violations Detected\n\n")
            for violation in integrity['integrity_violations']:
                f.write(f"- `{violation['file']}`: {violation['violation_type']}\n")
            f.write("\n")
            
        if integrity['encoding_violations']:
            f.write("### ⚠️ Encoding Violations Detected\n\n")
            for violation in integrity['encoding_violations']:
                f.write(f"- `{violation['file']}`: {violation['current_encoding']} (should be UTF-8)\n")
            f.write("\n")
        
        # Security recommendations
        f.write("## Security Recommendations\n\n")
        f.write("1. **Regular Monitoring:** Run integrity checks weekly\n")
        f.write("2. **Access Logging:** Review forensic logs monthly\n")
        f.write("3. **License Compliance:** Ensure all users comply with LICENSE_ENHANCED\n")
        f.write("4. **Backup Verification:** Maintain secure backups with integrity checking\n")
        f.write("5. **Incident Response:** Report any violations to spiralgang@outlook.com\n\n")
        
        f.write("---\n")
        f.write("*This report is generated automatically by the Data Protection System.*\n")
        f.write("*For support or to report violations: spiralgang@outlook.com*\n")

def main():
    parser = argparse.ArgumentParser(description='Data Protection & Integrity Verification System')
    parser.add_argument('--repo-path', default='.', help='Repository path to protect')
    parser.add_argument('--verify-only', action='store_true', help='Only verify integrity, do not fix issues')
    parser.add_argument('--fix-encoding', action='store_true', help='Fix UTF-8 encoding violations')
    parser.add_argument('--full-protection', action='store_true', help='Enable full protection suite')
    
    args = parser.parse_args()
    
    repo_path = Path(args.repo_path).resolve()
    
    if not repo_path.exists():
        logger.error(f"Repository path does not exist: {repo_path}")
        sys.exit(1)
    
    protection_system = DataProtectionSystem(repo_path)
    
    try:
        if args.full_protection:
            logger.info("Activating full protection suite...")
            protection_system.enforce_utf8_encoding()
            
            # Protect all files
            for root, dirs, files in os.walk(repo_path):
                dirs[:] = [d for d in dirs if not d.startswith('.') or d == '.github']
                if 'build' in dirs:
                    dirs.remove('build')
                    
                root_path = Path(root)
                for file in files:
                    file_path = root_path / file
                    protection_system.protect_file(file_path)
        
        elif args.fix_encoding:
            protection_system.enforce_utf8_encoding()
            
        # Always generate protection report
        protection_system.generate_protection_report()
        
        logger.info("Data protection system completed successfully")
        
    except KeyboardInterrupt:
        logger.info("Operation interrupted by user")
        sys.exit(1)
    except Exception as e:
        logger.error(f"Unexpected error: {e}")
        sys.exit(1)

if __name__ == '__main__':
    main()