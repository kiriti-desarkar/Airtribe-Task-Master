const BASE_URL = '/api';
let token = localStorage.getItem('token') || null;

document.addEventListener("DOMContentLoaded", () => {
    if (token) { showDashboard(); loadInitialData(); }
});

async function apiCall(endpoint, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const config = { method, headers };
    if (body instanceof FormData) {
        delete headers['Content-Type']; // Let browser set boundary
        config.body = body;
    } else if (body) {
        config.body = JSON.stringify(body);
    }

    const res = await fetch(`${BASE_URL}${endpoint}`, config);
    const data = await res.json().catch(() => null);
    if (!res.ok) throw new Error(data?.message || 'API Error');
    return data?.data || data;
}

// ---- AUTH ----
async function login() {
    try {
        const u = document.getElementById('login-username').value;
        const p = document.getElementById('login-password').value;
        const data = await apiCall('/auth/login', 'POST', { usernameOrEmail: u, password: p });
        token = data.accessToken;
        localStorage.setItem('token', token);
        showDashboard();
        loadInitialData();
    } catch (e) { alert(e.message); }
}

async function register() {
    try {
        const u = document.getElementById('reg-username').value;
        const e = document.getElementById('reg-email').value;
        const p = document.getElementById('reg-password').value;
        const f = document.getElementById('reg-fullname').value;
        await apiCall('/auth/register', 'POST', { username: u, email: e, password: p, fullName: f });
        alert('Registered. Please login.');
    } catch (e) { alert(e.message); }
}

function logout() {
    token = null; 
    localStorage.removeItem('token');
    document.getElementById('auth-section').style.display = 'block';
    document.getElementById('dashboard-section').style.display = 'none';
}

function showDashboard() {
    document.getElementById('auth-section').style.display = 'none';
    document.getElementById('dashboard-section').style.display = 'block';
}

// ---- PROFILE ----
async function loadProfile() {
    const user = await apiCall('/users/me');
    document.getElementById('user-greeting').innerText = `Hi, ${user.username} (ID: ${user.id})`;
    document.getElementById('prof-fullname').value = user.fullName || '';
    document.getElementById('prof-bio').value = user.bio || '';
}

async function updateProfile() {
    try {
        const f = document.getElementById('prof-fullname').value;
        const b = document.getElementById('prof-bio').value;
        await apiCall('/users/me', 'PUT', { fullName: f, bio: b });
        alert('Profile updated');
        loadProfile();
    } catch(e) { alert(e.message); }
}

// ---- TEAMS ----
async function createTeam() {
    try {
        const n = document.getElementById('new-team-name').value;
        await apiCall('/teams', 'POST', { name: n, description: "Generic description" });
        document.getElementById('new-team-name').value = '';
        loadTeams();
    } catch(e){ alert(e.message); }
}

async function loadTeams() {
    const teams = await apiCall('/teams');
    const ul = document.getElementById('team-list');
    ul.innerHTML = '';
    teams.forEach(t => {
        const li = document.createElement('li');
        li.innerText = `${t.name} (ID: ${t.id})`;
        const btn = document.createElement('button');
        btn.innerText = 'Select Team';
        btn.onclick = () => selectTeam(t.id, t.name);
        li.appendChild(btn);
        ul.appendChild(li);
    });
}

function selectTeam(id, name) {
    document.getElementById('invite-member-div').style.display = 'block';
    document.getElementById('invite-team-id').value = id;
    document.getElementById('selected-team-name').innerText = name;
}

async function inviteMember() {
    try {
        const t = document.getElementById('invite-team-id').value;
        const u = document.getElementById('invite-user-id').value;
        await apiCall(`/teams/${t}/members`, 'POST', { userId: parseInt(u) });
        alert('Member added via ID');
    } catch(e){ alert(e.message); }
}


// ---- TASKS ----
async function createTask() {
    try {
        const t = document.getElementById('task-title').value;
        const d = document.getElementById('task-desc').value;
        const dt = document.getElementById('task-due-date').value;
        const payload = { title: t, description: d };
        if (dt) payload.dueDate = dt;
        await apiCall('/tasks', 'POST', payload);
        fetchTasks();
    } catch(e) { alert(e.message); }
}

async function fetchTasks() {
    try {
        const status = document.getElementById('status-filter').value;
        const search = document.getElementById('search-input').value;
        let url = '/tasks?size=50';
        if (status) url += `&status=${status}`;
        if (search) url += `&search=${search}`;
        const page = await apiCall(url);
        renderTasks(page.content);
    } catch(e) { alert(e.message); }
}

async function fetchMyTasks() {
    try {
        const page = await apiCall('/tasks/my-tasks?size=50');
        renderTasks(page.content);
    } catch(e) { alert(e.message); }
}

function renderTasks(tasks) {
    const list = document.getElementById('tasks-list');
    list.innerHTML = '';
    tasks.forEach(t => {
        const div = document.createElement('div');
        div.className = 'task-item';
        div.innerHTML = `<span><strong>[${t.status}]</strong> ${t.title}</span> <button onclick="openTaskDetails(${t.id})">Details</button>`;
        list.appendChild(div);
    });
}

// ---- TASK DETAILS ----
async function openTaskDetails(id) {
    try {
        const task = await apiCall(`/tasks/${id}`);
        document.getElementById('task-details-container').style.display = 'block';
        document.getElementById('det-id').value = task.id;
        document.getElementById('det-title').innerText = task.title;
        document.getElementById('det-status').innerText = task.status;
        document.getElementById('det-assignee').innerText = task.assignee ? task.assignee.username : 'Unassigned';
        loadComments(id);
        loadAttachments(id);
    } catch(e) { alert(e.message); }
}

function closeTaskDetails() {
    document.getElementById('task-details-container').style.display = 'none';
}

async function markTaskCompleted() {
    const id = document.getElementById('det-id').value;
    try {
        await apiCall(`/tasks/${id}/status`, 'PATCH', { status: 'COMPLETED' });
        openTaskDetails(id);
        fetchTasks();
    } catch(e) { alert(e.message); }
}

async function assignTask() {
    const taskId = document.getElementById('det-id').value;
    const userId = document.getElementById('assign-user-id').value;
    try {
        await apiCall(`/tasks/${taskId}/assign`, 'PATCH', { assigneeId: parseInt(userId) });
        openTaskDetails(taskId);
        fetchTasks();
    } catch(e) { alert(e.message); }
}

// ---- COMMENTS & ATTACHMENTS ----
async function loadComments(taskId) {
    const list = document.getElementById('comment-list');
    list.innerHTML = '';
    try {
        const res = await apiCall(`/tasks/${taskId}/comments`);
        res.content.forEach(c => {
            const li = document.createElement('li');
            li.innerText = `${c.createdBy.username}: ${c.text}`;
            list.appendChild(li);
        });
    } catch(e) {}
}

async function addComment() {
    const taskId = document.getElementById('det-id').value;
    const txt = document.getElementById('new-comment').value;
    try {
        await apiCall(`/tasks/${taskId}/comments`, 'POST', { content: txt });
        document.getElementById('new-comment').value = '';
        loadComments(taskId);
    } catch(e) {}
}

async function loadAttachments(taskId) {
    const list = document.getElementById('attachment-list');
    list.innerHTML = '';
    try {
        const atts = await apiCall(`/tasks/${taskId}/attachments`);
        atts.forEach(a => {
            const li = document.createElement('li');
            li.innerHTML = `<a href="${BASE_URL}/tasks/${taskId}/attachments/${a.id}/download" target="_blank">${a.fileName}</a>`;
            list.appendChild(li);
        });
    } catch(e) {}
}

async function uploadAttachment() {
    const taskId = document.getElementById('det-id').value;
    const file = document.getElementById('new-attachment').files[0];
    if (!file) return;
    
    const formData = new FormData();
    formData.append("file", file);

    try {
        await apiCall(`/tasks/${taskId}/attachments`, 'POST', formData);
        loadAttachments(taskId);
    } catch(e) { alert(e.message); }
}

// ---- INIT ----
function loadInitialData() {
    loadProfile();
    loadTeams();
    fetchTasks();
}
