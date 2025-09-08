// To-Do List Application - Full Implementation

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
});