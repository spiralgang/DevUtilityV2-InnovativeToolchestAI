#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Comprehensive File Audit Script
===============================

This script goes through EVERY FILE ONE AT A TIME and provides a detailed analysis
of the repository contents, capabilities, and potential improvements.

Part of: DevUl Army : Living Sriracha AGI
License: See LICENSE file for terms
"""

import os
import sys
import hashlib
import json
import mimetypes
import subprocess
import re
from pathlib import Path
from typing import Dict, List, Any, Optional
from datetime import datetime
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class FileAuditor:
    """Comprehensive file auditor for the repository."""
    
    def __init__(self, repo_path: str):
        self.repo_path = Path(repo_path)
        self.audit_results = {
            'audit_timestamp': datetime.now().isoformat(),
            'repository_path': str(self.repo_path),
            'summary': {},
            'files': [],
            'capabilities': [],
            'recommendations': [],
            'security_notes': []
        }
        
        # Initialize counters
        self.file_types = {}
        self.total_size = 0
        self.total_files = 0
        
        # Security patterns to look for
        self.security_patterns = {
            'passwords': [r'password\s*=\s*["\'][^"\']+["\']', r'pwd\s*='],
            'api_keys': [r'api[_-]?key\s*=\s*["\'][^"\']+["\']', r'secret[_-]?key'],
            'credentials': [r'username\s*=\s*["\'][^"\']+["\']', r'token\s*='],
            'hardcoded_ips': [r'\b(?:[0-9]{1,3}\.){3}[0-9]{1,3}\b'],
            'urls': [r'https?://[^\s\'"<>]+'],
            'file_paths': [r'/[a-zA-Z0-9._/-]+', r'[A-Z]:\\[^\\s\'"<>|*?:]+']
        }

    def get_file_hash(self, file_path: Path) -> str:
        """Calculate SHA-256 hash of a file."""
        try:
            with open(file_path, 'rb') as f:
                return hashlib.sha256(f.read()).hexdigest()
        except Exception:
            return "error_calculating_hash"

    def analyze_file_content(self, file_path: Path) -> Dict[str, Any]:
        """Analyze the content of a text file for capabilities and security issues."""
        analysis = {
            'capabilities': [],
            'security_flags': [],
            'technologies': [],
            'functions': [],
            'line_count': 0,
            'encoding_issues': False
        }
        
        try:
            # Try to read as text
            with open(file_path, 'r', encoding='utf-8', errors='replace') as f:
                content = f.read()
                lines = content.split('\n')
                analysis['line_count'] = len(lines)
                
            # Check for encoding issues
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    f.read()
            except UnicodeDecodeError:
                analysis['encoding_issues'] = True
                
            # Technology detection
            file_ext = file_path.suffix.lower()
            if file_ext in ['.py']:
                analysis['technologies'].append('Python')
                # Look for imports and function definitions
                imports = re.findall(r'^(?:from\s+\S+\s+)?import\s+(\S+)', content, re.MULTILINE)
                functions = re.findall(r'^def\s+(\w+)', content, re.MULTILINE)
                analysis['functions'].extend(functions)
                
                # Check for specific capabilities
                if 'flask' in content.lower() or 'django' in content.lower():
                    analysis['capabilities'].append('Web Framework')
                if 'tensorflow' in content.lower() or 'pytorch' in content.lower():
                    analysis['capabilities'].append('Machine Learning')
                if 'subprocess' in content or 'os.system' in content:
                    analysis['capabilities'].append('System Execution')
                if 'socket' in content or 'requests' in content:
                    analysis['capabilities'].append('Network Communication')
                    
            elif file_ext in ['.kt', '.java']:
                analysis['technologies'].append('Kotlin/Java')
                if 'android' in content.lower():
                    analysis['capabilities'].append('Android Development')
                if 'retrofit' in content.lower() or 'okhttp' in content.lower():
                    analysis['capabilities'].append('HTTP Client')
                if 'room' in content.lower() or 'sqlite' in content.lower():
                    analysis['capabilities'].append('Database')
                    
            elif file_ext in ['.sh', '.bash']:
                analysis['technologies'].append('Shell Script')
                if 'curl' in content or 'wget' in content:
                    analysis['capabilities'].append('HTTP Downloads')
                if 'git' in content:
                    analysis['capabilities'].append('Git Operations')
                if 'docker' in content:
                    analysis['capabilities'].append('Docker')
                if 'ssh' in content or 'scp' in content:
                    analysis['capabilities'].append('Remote Access')
                    
            elif file_ext in ['.xml']:
                analysis['technologies'].append('XML')
                if 'android' in content.lower():
                    analysis['capabilities'].append('Android Configuration')
                    
            elif file_ext in ['.gradle']:
                analysis['technologies'].append('Gradle Build')
                analysis['capabilities'].append('Build System')
                
            # Security pattern checking
            for category, patterns in self.security_patterns.items():
                for pattern in patterns:
                    if re.search(pattern, content, re.IGNORECASE):
                        analysis['security_flags'].append(f"Contains {category}")
                        
            # Look for TODO/FIXME comments
            todos = re.findall(r'(?:TODO|FIXME|HACK|XXX)[\s:]*(.+)', content, re.IGNORECASE)
            if todos:
                analysis['capabilities'].append(f"Has {len(todos)} TODO/FIXME items")
                
        except Exception as e:
            analysis['error'] = str(e)
            
        return analysis

    def get_file_info(self, file_path: Path) -> Dict[str, Any]:
        """Get comprehensive information about a file."""
        try:
            stat = file_path.stat()
            mime_type, _ = mimetypes.guess_type(str(file_path))
            
            file_info = {
                'path': str(file_path.relative_to(self.repo_path)),
                'name': file_path.name,
                'extension': file_path.suffix.lower(),
                'size': stat.st_size,
                'modified': datetime.fromtimestamp(stat.st_mtime).isoformat(),
                'permissions': oct(stat.st_mode)[-3:],
                'mime_type': mime_type or 'unknown',
                'hash': self.get_file_hash(file_path),
                'is_executable': os.access(file_path, os.X_OK),
                'is_symlink': file_path.is_symlink()
            }
            
            # Add content analysis for text files
            if self.is_text_file(file_path):
                file_info['content_analysis'] = self.analyze_file_content(file_path)
                
            return file_info
            
        except Exception as e:
            return {
                'path': str(file_path.relative_to(self.repo_path)),
                'error': str(e)
            }

    def is_text_file(self, file_path: Path) -> bool:
        """Determine if a file is a text file."""
        text_extensions = {
            '.txt', '.md', '.py', '.kt', '.java', '.sh', '.xml', '.json',
            '.html', '.css', '.js', '.sql', '.yaml', '.yml', '.cfg', '.conf',
            '.ini', '.properties', '.log', '.csv', '.gradle', '.pro', '.gitignore'
        }
        
        if file_path.suffix.lower() in text_extensions:
            return True
            
        # Check if file has no extension but might be text
        if not file_path.suffix:
            try:
                with open(file_path, 'rb') as f:
                    sample = f.read(1024)
                    return b'\x00' not in sample
            except Exception:
                return False
                
        return False

    def audit_repository(self) -> None:
        """Perform comprehensive audit of the entire repository."""
        logger.info(f"Starting comprehensive audit of: {self.repo_path}")
        
        # Walk through all files
        for root, dirs, files in os.walk(self.repo_path):
            # Skip hidden directories and common build directories
            dirs[:] = [d for d in dirs if not d.startswith('.') or d in ['.github']]
            
            root_path = Path(root)
            
            for file in files:
                file_path = root_path / file
                
                # Skip hidden files except important ones
                if file.startswith('.') and file not in ['.gitignore', '.gitattributes']:
                    continue
                    
                self.total_files += 1
                file_info = self.get_file_info(file_path)
                self.audit_results['files'].append(file_info)
                
                # Update statistics
                ext = file_path.suffix.lower() or 'no_extension'
                self.file_types[ext] = self.file_types.get(ext, 0) + 1
                self.total_size += file_info.get('size', 0)
                
                # Extract capabilities
                if 'content_analysis' in file_info:
                    capabilities = file_info['content_analysis'].get('capabilities', [])
                    for cap in capabilities:
                        if cap not in self.audit_results['capabilities']:
                            self.audit_results['capabilities'].append(cap)
                            
        # Generate summary
        self.generate_summary()
        self.generate_recommendations()

    def generate_summary(self) -> None:
        """Generate summary statistics."""
        self.audit_results['summary'] = {
            'total_files': self.total_files,
            'total_size_bytes': self.total_size,
            'total_size_mb': round(self.total_size / (1024 * 1024), 2),
            'file_types': self.file_types,
            'largest_files': self.get_largest_files(),
            'most_common_extensions': sorted(self.file_types.items(), key=lambda x: x[1], reverse=True)[:10]
        }

    def get_largest_files(self) -> List[Dict[str, Any]]:
        """Get the 10 largest files."""
        files_with_size = [(f['path'], f.get('size', 0)) for f in self.audit_results['files'] if 'size' in f]
        largest = sorted(files_with_size, key=lambda x: x[1], reverse=True)[:10]
        return [{'path': path, 'size_mb': round(size / (1024 * 1024), 2)} for path, size in largest]

    def generate_recommendations(self) -> None:
        """Generate recommendations based on audit findings."""
        recommendations = []
        
        # Check for encoding issues
        encoding_issues = [f for f in self.audit_results['files'] 
                          if f.get('content_analysis', {}).get('encoding_issues', False)]
        if encoding_issues:
            recommendations.append(f"Found {len(encoding_issues)} files with encoding issues - run UTF-8 conversion")
            
        # Check for security flags
        security_files = [f for f in self.audit_results['files'] 
                         if f.get('content_analysis', {}).get('security_flags')]
        if security_files:
            recommendations.append(f"Found {len(security_files)} files with potential security issues - review manually")
            
        # Check for executable files
        executables = [f for f in self.audit_results['files'] if f.get('is_executable', False)]
        if executables:
            recommendations.append(f"Found {len(executables)} executable files - verify permissions are correct")
            
        # Check for large files
        large_files = [f for f in self.audit_results['files'] if f.get('size', 0) > 10 * 1024 * 1024]
        if large_files:
            recommendations.append(f"Found {len(large_files)} files larger than 10MB - consider Git LFS")
            
        # Check for TODO items
        todo_files = [f for f in self.audit_results['files'] 
                     if any('TODO' in cap for cap in f.get('content_analysis', {}).get('capabilities', []))]
        if todo_files:
            recommendations.append(f"Found TODO/FIXME items in {len(todo_files)} files - review and implement")
            
        self.audit_results['recommendations'] = recommendations

    def save_audit_report(self) -> None:
        """Save the comprehensive audit report."""
        report_path = self.repo_path / 'COMPREHENSIVE_AUDIT_REPORT.json'
        
        with open(report_path, 'w', encoding='utf-8') as f:
            json.dump(self.audit_results, f, indent=2, ensure_ascii=False)
            
        # Also create a human-readable summary
        summary_path = self.repo_path / 'AUDIT_SUMMARY.md'
        with open(summary_path, 'w', encoding='utf-8') as f:
            self.write_markdown_summary(f)
            
        logger.info(f"Audit report saved to: {report_path}")
        logger.info(f"Human-readable summary saved to: {summary_path}")

    def write_markdown_summary(self, f) -> None:
        """Write a markdown summary of the audit."""
        f.write("# DevUl Army Repository Audit Report\n\n")
        f.write(f"**Audit Date:** {self.audit_results['audit_timestamp']}\n\n")
        
        # Summary Statistics
        f.write("## Summary Statistics\n\n")
        summary = self.audit_results['summary']
        f.write(f"- **Total Files:** {summary['total_files']}\n")
        f.write(f"- **Total Size:** {summary['total_size_mb']} MB\n\n")
        
        # File Types
        f.write("### File Type Distribution\n\n")
        for ext, count in summary['most_common_extensions']:
            f.write(f"- `{ext}`: {count} files\n")
        f.write("\n")
        
        # Capabilities Found
        f.write("## Discovered Capabilities\n\n")
        if self.audit_results['capabilities']:
            for cap in sorted(self.audit_results['capabilities']):
                f.write(f"- {cap}\n")
        else:
            f.write("No specific capabilities detected.\n")
        f.write("\n")
        
        # Recommendations
        f.write("## Recommendations\n\n")
        if self.audit_results['recommendations']:
            for i, rec in enumerate(self.audit_results['recommendations'], 1):
                f.write(f"{i}. {rec}\n")
        else:
            f.write("No specific recommendations at this time.\n")
        f.write("\n")
        
        # Largest Files
        f.write("## Largest Files\n\n")
        for file_info in summary['largest_files']:
            f.write(f"- `{file_info['path']}`: {file_info['size_mb']} MB\n")

def main():
    import argparse
    
    parser = argparse.ArgumentParser(description='Comprehensive Repository File Auditor')
    parser.add_argument('--repo-path', default='.', help='Repository path to audit')
    parser.add_argument('--output-format', choices=['json', 'markdown', 'both'], default='both',
                       help='Output format for the report')
    
    args = parser.parse_args()
    
    repo_path = Path(args.repo_path).resolve()
    
    if not repo_path.exists():
        logger.error(f"Repository path does not exist: {repo_path}")
        sys.exit(1)
        
    auditor = FileAuditor(repo_path)
    
    try:
        auditor.audit_repository()
        auditor.save_audit_report()
        
        # Print summary to console
        print("\n" + "="*60)
        print("COMPREHENSIVE REPOSITORY AUDIT COMPLETE")
        print("="*60)
        print(f"Total files analyzed: {auditor.total_files}")
        print(f"Repository size: {auditor.audit_results['summary']['total_size_mb']} MB")
        print(f"Capabilities discovered: {len(auditor.audit_results['capabilities'])}")
        print(f"Recommendations generated: {len(auditor.audit_results['recommendations'])}")
        print("="*60)
        
    except KeyboardInterrupt:
        logger.info("Audit interrupted by user")
        sys.exit(1)
    except Exception as e:
        logger.error(f"Unexpected error during audit: {e}")
        sys.exit(1)

if __name__ == '__main__':
    main()