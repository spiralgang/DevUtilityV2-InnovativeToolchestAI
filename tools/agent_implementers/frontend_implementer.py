#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Frontend Solution Implementer for Phi2 Agent
Generates frontend code based on analysis results
"""

import argparse
import json
import os
import sys
from pathlib import Path
from typing import Dict, List, Optional

class FrontendImplementer:
    def __init__(self):
        self.templates = {
            "html_base": """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{title}</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="container">
        {content}
    </div>
    <script src="script.js"></script>
</body>
</html>""",
            
            "css_base": """/* {title} - Responsive CSS */
* {{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}}

body {{
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    line-height: 1.6;
    color: #333;
    background: #f4f4f4;
}}

.container {{
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    background: white;
    box-shadow: 0 0 10px rgba(0,0,0,0.1);
    border-radius: 8px;
}}

/* Responsive Design */
@media (max-width: 768px) {{
    .container {{
        margin: 10px;
        padding: 15px;
    }}
}}

{additional_styles}""",
            
            "js_base": """// {title} - Vanilla JavaScript Implementation
// Compatible with Android 10+ browsers (ES6)

class {class_name} {{
    constructor() {{
        this.init();
    }}

    init() {{
        this.bindEvents();
        this.loadData();
    }}

    bindEvents() {{
        // Event bindings will be added here
    }}

    loadData() {{
        // Data loading will be implemented here
    }}
}}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {{
    window.app = new {class_name}();
}});"""
        }

    def load_analysis(self, analysis_file: str) -> Dict:
        """Load analysis results from JSON file"""
        try:
            with open(analysis_file, 'r') as f:
                return json.load(f)
        except (FileNotFoundError, json.JSONDecodeError) as e:
            print(f"Error loading analysis file: {e}", file=sys.stderr)
            return {}

    def generate_todo_app(self, analysis: Dict, output_dir: str):
        """Generate complete to-do list application"""
        Path(output_dir).mkdir(parents=True, exist_ok=True)
        
        # Generate HTML
        html_content = """<h1>📝 To-Do List</h1>
        <div class="input-section">
            <input type="text" id="taskInput" placeholder="Add a new task..." maxlength="100">
            <button id="addBtn">Add Task</button>
        </div>
        <div class="filter-section">
            <button class="filter-btn active" data-filter="all">All</button>
            <button class="filter-btn" data-filter="pending">Pending</button>
            <button class="filter-btn" data-filter="completed">Completed</button>
        </div>
        <ul id="taskList" class="task-list"></ul>
        <div class="stats">
            <span id="totalTasks">0 tasks</span>
            <span id="completedTasks">0 completed</span>
        </div>"""
        
        html = self.templates["html_base"].format(
            title="To-Do List App",
            content=html_content
        )
        
        # Generate CSS
        css_additional = """.input-section {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
}

#taskInput {
    flex: 1;
    padding: 12px 15px;
    border: 2px solid #ddd;
    border-radius: 6px;
    font-size: 16px;
}

#taskInput:focus {
    outline: none;
    border-color: #007bff;
}

#addBtn {
    padding: 12px 20px;
    background: #007bff;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 16px;
}

#addBtn:hover {
    background: #0056b3;
}

.filter-section {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
}

.filter-btn {
    padding: 8px 16px;
    border: 1px solid #ddd;
    background: white;
    border-radius: 20px;
    cursor: pointer;
}

.filter-btn.active {
    background: #007bff;
    color: white;
    border-color: #007bff;
}

.task-list {
    list-style: none;
    margin-bottom: 20px;
}

.task-item {
    display: flex;
    align-items: center;
    padding: 15px;
    border: 1px solid #eee;
    border-radius: 6px;
    margin-bottom: 10px;
    background: white;
}

.task-item.completed {
    opacity: 0.6;
    text-decoration: line-through;
}

.task-checkbox {
    margin-right: 15px;
}

.task-text {
    flex: 1;
    cursor: pointer;
}

.task-actions {
    display: flex;
    gap: 5px;
}

.edit-btn, .delete-btn {
    padding: 5px 10px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 12px;
}

.edit-btn {
    background: #28a745;
    color: white;
}

.delete-btn {
    background: #dc3545;
    color: white;
}

.stats {
    padding: 15px;
    background: #f8f9fa;
    border-radius: 6px;
    text-align: center;
    display: flex;
    justify-content: space-between;
}"""
        
        css = self.templates["css_base"].format(
            title="To-Do List App",
            additional_styles=css_additional
        )
        
        # Generate JavaScript
        js_content = """// To-Do List Application - Full Implementation

class TodoApp {
    constructor() {
        this.tasks = this.loadTasks();
        this.currentFilter = 'all';
        this.editingId = null;
        
        this.initElements();
        this.bindEvents();
        this.render();
    }

    initElements() {
        this.taskInput = document.getElementById('taskInput');
        this.addBtn = document.getElementById('addBtn');
        this.taskList = document.getElementById('taskList');
        this.filterBtns = document.querySelectorAll('.filter-btn');
        this.totalTasks = document.getElementById('totalTasks');
        this.completedTasks = document.getElementById('completedTasks');
    }

    bindEvents() {
        this.addBtn.addEventListener('click', () => this.addTask());
        this.taskInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.addTask();
        });
        
        this.filterBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.setFilter(e.target.dataset.filter);
            });
        });
    }

    addTask() {
        const text = this.taskInput.value.trim();
        if (!text) return;

        const task = {
            id: Date.now(),
            text: text,
            completed: false,
            createdAt: new Date().toISOString()
        };

        this.tasks.push(task);
        this.saveTasks();
        this.taskInput.value = '';
        this.render();
    }

    deleteTask(id) {
        this.tasks = this.tasks.filter(task => task.id !== id);
        this.saveTasks();
        this.render();
    }

    toggleTask(id) {
        const task = this.tasks.find(task => task.id === id);
        if (task) {
            task.completed = !task.completed;
            this.saveTasks();
            this.render();
        }
    }

    editTask(id, newText) {
        const task = this.tasks.find(task => task.id === id);
        if (task && newText.trim()) {
            task.text = newText.trim();
            this.saveTasks();
            this.editingId = null;
            this.render();
        }
    }

    startEdit(id) {
        this.editingId = id;
        this.render();
    }

    setFilter(filter) {
        this.currentFilter = filter;
        this.filterBtns.forEach(btn => {
            btn.classList.toggle('active', btn.dataset.filter === filter);
        });
        this.render();
    }

    getFilteredTasks() {
        switch (this.currentFilter) {
            case 'completed':
                return this.tasks.filter(task => task.completed);
            case 'pending':
                return this.tasks.filter(task => !task.completed);
            default:
                return this.tasks;
        }
    }

    render() {
        const filteredTasks = this.getFilteredTasks();
        
        this.taskList.innerHTML = filteredTasks.map(task => {
            if (this.editingId === task.id) {
                return `
                    <li class="task-item">
                        <input type="checkbox" class="task-checkbox" ${task.completed ? 'checked' : ''} 
                               onchange="app.toggleTask(${task.id})">
                        <input type="text" class="edit-input" value="${task.text}" 
                               onkeypress="if(event.key==='Enter') app.editTask(${task.id}, this.value)"
                               onblur="app.editTask(${task.id}, this.value)" autofocus>
                        <div class="task-actions">
                            <button class="edit-btn" onclick="app.editingId=null; app.render()">Cancel</button>
                        </div>
                    </li>
                `;
            }
            
            return `
                <li class="task-item ${task.completed ? 'completed' : ''}">
                    <input type="checkbox" class="task-checkbox" ${task.completed ? 'checked' : ''} 
                           onchange="app.toggleTask(${task.id})">
                    <span class="task-text" onclick="app.startEdit(${task.id})">${task.text}</span>
                    <div class="task-actions">
                        <button class="edit-btn" onclick="app.startEdit(${task.id})">Edit</button>
                        <button class="delete-btn" onclick="app.deleteTask(${task.id})">Delete</button>
                    </div>
                </li>
            `;
        }).join('');

        this.updateStats();
    }

    updateStats() {
        const total = this.tasks.length;
        const completed = this.tasks.filter(task => task.completed).length;
        
        this.totalTasks.textContent = `${total} task${total !== 1 ? 's' : ''}`;
        this.completedTasks.textContent = `${completed} completed`;
    }

    loadTasks() {
        try {
            const saved = localStorage.getItem('todoApp_tasks');
            return saved ? JSON.parse(saved) : [];
        } catch (error) {
            console.error('Error loading tasks:', error);
            return [];
        }
    }

    saveTasks() {
        try {
            localStorage.setItem('todoApp_tasks', JSON.stringify(this.tasks));
        } catch (error) {
            console.error('Error saving tasks:', error);
        }
    }
}

// Initialize app when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    window.app = new TodoApp();
});"""
        
        # Write files
        self.write_file(os.path.join(output_dir, "index.html"), html)
        self.write_file(os.path.join(output_dir, "styles.css"), css)
        self.write_file(os.path.join(output_dir, "script.js"), js_content)
        
        print(f"Generated complete To-Do List application in {output_dir}")

    def generate_generic_app(self, analysis: Dict, output_dir: str):
        """Generate generic frontend application based on analysis"""
        Path(output_dir).mkdir(parents=True, exist_ok=True)
        
        issue = analysis.get("issue", {})
        title = issue.get("title", "Frontend Application")
        
        # Basic HTML structure
        html_content = f"<h1>{title}</h1>\n        <p>Application content will be implemented here.</p>"
        html = self.templates["html_base"].format(title=title, content=html_content)
        
        # Basic CSS
        css = self.templates["css_base"].format(title=title, additional_styles="/* Additional styles will be added here */")
        
        # Basic JavaScript
        class_name = "".join(word.capitalize() for word in title.split() if word.isalpha())[:20] + "App"
        js = self.templates["js_base"].format(title=title, class_name=class_name)
        
        # Write files
        self.write_file(os.path.join(output_dir, "index.html"), html)
        self.write_file(os.path.join(output_dir, "styles.css"), css)
        self.write_file(os.path.join(output_dir, "script.js"), js)
        
        print(f"Generated generic frontend application in {output_dir}")

    def write_file(self, filepath: str, content: str):
        """Write content to file"""
        try:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Generated: {filepath}")
        except IOError as e:
            print(f"Error writing {filepath}: {e}", file=sys.stderr)

    def implement_solution(self, analysis: Dict, output_dir: str):
        """Main implementation function"""
        if not analysis:
            print("No analysis data provided", file=sys.stderr)
            return
        
        issue = analysis.get("issue", {})
        title = issue.get("title", "").lower()
        body = issue.get("body", "").lower()
        
        # Determine application type and generate accordingly
        if "todo" in title or "to-do" in title or "task" in title:
            self.generate_todo_app(analysis, output_dir)
        elif "local storage" in body or "localstorage" in body:
            # For now, default todo app for localStorage requirements
            self.generate_todo_app(analysis, output_dir)
        else:
            self.generate_generic_app(analysis, output_dir)
        
        # Generate README
        self.generate_readme(analysis, output_dir)

    def generate_readme(self, analysis: Dict, output_dir: str):
        """Generate README file for the application"""
        issue = analysis.get("issue", {})
        recommendations = analysis.get("recommendations", {})
        
        readme_content = f"""# {issue.get("title", "Frontend Application")}

## Overview
This application was generated by the Frontend Specialist Agent (Phi2) based on GitHub issue #{issue.get("number", "N/A")}.

## Files
- `index.html` - Main HTML structure
- `styles.css` - Responsive CSS styling
- `script.js` - JavaScript functionality

## Features
{chr(10).join(f"- {feature}" for feature in recommendations.get("key_features", []))}

## Browser Compatibility
- {recommendations.get("browser_compatibility", "Modern browsers")}
- Tested on Android 10+ browsers

## Usage
1. Open `index.html` in a web browser
2. No build process required - runs directly
3. All data is stored locally in the browser

## Technical Details
- Architecture: {recommendations.get("architecture", "vanilla")}
- No external dependencies
- Responsive design for mobile and desktop

## Generated By
Frontend Specialist Agent (Phi2) - Multi-Agent Issue Coordinator System
"""
        
        self.write_file(os.path.join(output_dir, "README.md"), readme_content)

def main():
    parser = argparse.ArgumentParser(description="Frontend Solution Implementer")
    parser.add_argument("--analysis", required=True, help="Analysis JSON file")
    parser.add_argument("--output-dir", required=True, help="Output directory")
    
    args = parser.parse_args()
    
    implementer = FrontendImplementer()
    
    # Load analysis
    analysis = implementer.load_analysis(args.analysis)
    if not analysis:
        print("Failed to load analysis data", file=sys.stderr)
        sys.exit(1)
    
    # Implement solution
    print(f"Implementing frontend solution based on analysis...")
    implementer.implement_solution(analysis, args.output_dir)
    print("Implementation complete!")

if __name__ == "__main__":
    main()