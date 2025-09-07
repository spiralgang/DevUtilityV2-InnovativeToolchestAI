#!/bin/bash
# Manual File Indexing - One File at a Time
# Use this to manually organize and index files using simple mv commands

echo "🔥 MANUAL FILE INDEXING - ONE FILE AT A TIME 🔥"
echo "================================================"

# Create organization directories
echo "📁 Creating organization directories..."
mkdir -p manual_index/{scripts,configs,docs,tools,apps,archives,logs}

echo "📋 MANUAL INDEXING COMMANDS:"
echo "Use these commands to move files one by one:"
echo ""

# Example commands for different file types
echo "# Move script files:"
echo "mv filename.sh manual_index/scripts/"
echo "mv filename.py manual_index/scripts/"
echo ""

echo "# Move config files:"
echo "mv filename.conf manual_index/configs/"
echo "mv filename.yml manual_index/configs/"
echo ""

echo "# Move documentation:"
echo "mv filename.md manual_index/docs/"
echo "mv filename.txt manual_index/docs/"
echo ""

echo "# Move tools:"
echo "mv toolname.py manual_index/tools/"
echo ""

echo "# Move apps:"
echo "mv appname/ manual_index/apps/"
echo ""

echo "# Move logs:"
echo "mv *.log manual_index/logs/"
echo ""

echo "✋ MANUAL PROCESS:"
echo "1. Look at each file individually"
echo "2. Decide where it belongs"
echo "3. Use 'mv' command to move it"
echo "4. Repeat for next file"
echo ""

echo "🎯 EXAMPLES OF MANUAL COMMANDS:"
echo "mv cloud.sh manual_index/scripts/"
echo "mv README.md manual_index/docs/"
echo "mv .gitignore manual_index/configs/"

echo ""
echo "📝 To see what files need indexing:"
echo "ls -la | grep -v '^d'"
echo ""
echo "🔍 To check a specific file:"
echo "file filename"
echo "head -5 filename"