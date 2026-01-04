const API_URL = '/jobs';

const el = {
    list: document.getElementById('job-list'),
    modal: document.getElementById('modal'),
    stats: {
        active: document.getElementById('stat-active'),
        completed: document.getElementById('stat-completed'),
        failed: document.getElementById('stat-failed')
    }
};

let jobs = [];

// Init
fetchJobs();
setInterval(fetchJobs, 2000); // Polling every 2s

async function fetchJobs() {
    // Ideally we need a 'list jobs' API. 
    // Wait, I only implemented GET /jobs/{id}.
    // I NEED TO IMPLEMENT GET /jobs (List) first!
    // For now, I will implement the UI logic, then quickly add the endpoint.
    // Assuming GET /jobs returns array.
    try {
        const res = await fetch(API_URL);
        if(!res.ok) throw new Error("Failed to fetch");
        jobs = await res.json();
        render();
    } catch (e) {
        console.error(e);
    }
}

function render() {
    // Stats
    const active = jobs.filter(j => ['PENDING', 'QUEUED', 'RUNNING'].includes(j.status)).length;
    const completed = jobs.filter(j => j.status === 'COMPLETED').length;
    const failed = jobs.filter(j => j.status === 'FAILED').length;

    el.stats.active.textContent = active;
    el.stats.completed.textContent = completed;
    el.stats.failed.textContent = failed;

    // List (Top 10 recent)
    const sorted = [...jobs].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 20);
    
    el.list.innerHTML = sorted.map(job => `
        <div class="job-item">
            <div>
                <div class="job-type">${job.type}</div>
                <div class="job-id">${job.id}</div>
            </div>
            <div style="font-size: 0.9rem; color: #94a3b8;">
                ${new Date(job.createdAt).toLocaleTimeString()}
            </div>
            <div>
                <span class="badge ${job.status.toLowerCase()}">${job.status}</span>
            </div>
            <div style="text-align: right">
                 <!-- Actions could go here -->
            </div>
        </div>
    `).join('');
}

// Modal
function openModal() { el.modal.classList.add('open'); }
function closeModal() { el.modal.classList.remove('open'); }

// Create Job
async function createJob(e) {
    e.preventDefault();
    const formData = new FormData(e.target);
    const type = formData.get('type');
    const payload = formData.get('payload');
    const delay = parseInt(formData.get('delay') || 0);

    const body = {
        type, 
        payload,
        retryPolicy: { maxRetries: 3, backoffSeconds: 10 }
    };

    if (delay > 0) {
        const date = new Date();
        date.setSeconds(date.getSeconds() + delay);
        body.scheduleAt = date.toISOString();
    }

    try {
        const res = await fetch(API_URL, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body)
        });
        if (res.ok) {
            closeModal();
            fetchJobs();
            e.target.reset();
        } else {
            alert('Failed to create job');
        }
    } catch (e) {
        alert('Error creating job');
    }
}
