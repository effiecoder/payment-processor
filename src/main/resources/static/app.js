// Payment Processor - Modern Frontend JavaScript

const API_BASE = '/api';
let token = localStorage.getItem('token');
let currentPage = 0;
let currentFilter = '';

// DOM Elements
const screens = {
    login: document.getElementById('login-screen'),
    main: document.getElementById('main-screen')
};

const modals = {
    transaction: document.getElementById('transaction-modal'),
    newTransaction: document.getElementById('new-transaction-modal'),
    editTransaction: document.getElementById('edit-transaction-modal')
};

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    if (token) {
        showMainScreen();
        loadTransactions();
        loadStatusCounts();
    } else {
        showLoginScreen();
    }

    setupEventListeners();
});

function setupEventListeners() {
    // Login form
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('logout-btn').addEventListener('click', handleLogout);
    document.getElementById('refresh-btn').addEventListener('click', () => {
        loadTransactions();
        loadStatusCounts();
    });
    
    // Filters
    document.getElementById('status-filter').addEventListener('change', (e) => {
        currentFilter = e.target.value;
        currentPage = 0;
        loadTransactions();
    });
    
    // Pagination
    document.getElementById('prev-page').addEventListener('click', () => {
        if (currentPage > 0) {
            currentPage--;
            loadTransactions();
        }
    });
    
    document.getElementById('next-page').addEventListener('click', () => {
        currentPage++;
        loadTransactions();
    });
    
    // New Transaction
    document.getElementById('new-transaction-btn').addEventListener('click', () => {
        // Set default date to tomorrow
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        const form = document.getElementById('new-transaction-form');
        form.valueDate.value = tomorrow.toISOString().split('T')[0];
        showModal('newTransaction');
    });
    
    document.getElementById('close-new-modal').addEventListener('click', () => {
        hideModal('newTransaction');
    });
    
    document.getElementById('new-transaction-form').addEventListener('submit', handleNewTransaction);
    
    // Edit Transaction
    document.getElementById('close-edit-modal').addEventListener('click', () => {
        hideModal('editTransaction');
    });
    
    document.getElementById('edit-transaction-form').addEventListener('submit', handleEditTransaction);
    
    // Transaction Modal
    document.getElementById('close-modal').addEventListener('click', () => {
        hideModal('transaction');
    });
    
    // Close modal on backdrop click
    Object.values(modals).forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.add('hidden');
            }
        });
    });
}

// Auth
async function handleLogin(e) {
    e.preventDefault();
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorEl = document.getElementById('login-error');
    
    errorEl.classList.add('hidden');
    
    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        if (!response.ok) {
            throw new Error('Nieprawidłowa nazwa użytkownika lub hasło');
        }
        
        const data = await response.json();
        token = data.token;
        localStorage.setItem('token', token);
        
        showMainScreen();
        loadTransactions();
        loadStatusCounts();
    } catch (error) {
        errorEl.textContent = error.message;
        errorEl.classList.remove('hidden');
    }
}

function handleLogout() {
    token = null;
    localStorage.removeItem('token');
    showLoginScreen();
}

// API Helper
async function apiCall(endpoint, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers
    });
    
    if (response.status === 401) {
        handleLogout();
        throw new Error('Sesja wygasła');
    }
    
    return response;
}

// Screens
function showLoginScreen() {
    screens.login.classList.remove('hidden');
    screens.main.classList.add('hidden');
}

function showMainScreen() {
    screens.login.classList.add('hidden');
    screens.main.classList.remove('hidden');
}

// Modals
function showModal(name) {
    modals[name].classList.remove('hidden');
    document.body.style.overflow = 'hidden';
}

function hideModal(name) {
    modals[name].classList.add('hidden');
    document.body.style.overflow = '';
}

// Transactions
async function loadTransactions() {
    const listEl = document.getElementById('transactions-list');
    listEl.innerHTML = '<div class="loading"><div class="spinner"></div>Ładowanie transakcji...</div>';
    
    try {
        let url = `/transactions?page=${currentPage}&size=20`;
        if (currentFilter) {
            url = `/transactions/status/${currentFilter}`;
        }
        
        const response = await apiCall(url);
        const transactions = await response.json();
        
        if (!transactions || transactions.length === 0) {
            listEl.innerHTML = `
                <div class="empty-state">
                    <div class="icon">📭</div>
                    <p>Brak transakcji do wyświetlenia</p>
                </div>
            `;
            return;
        }
        
        listEl.innerHTML = transactions.map(tx => `
            <div class="transaction-item" onclick="viewTransaction(${tx.id})">
                <div class="tx-id">#${tx.id}</div>
                <div class="tx-parties">
                    <span class="sender">${escapeHtml(tx.senderName || tx.senderAccount)}</span>
                    <span class="receiver">→ ${escapeHtml(tx.receiverName || tx.receiverAccount)}</span>
                </div>
                <div class="tx-amount">${formatAmount(tx.amount)} ${tx.currency}</div>
                <div class="tx-status status-${tx.status}">${formatStatus(tx.status)}</div>
                <div class="tx-date">${formatDate(tx.valueDate)}</div>
            </div>
        `).join('');
        
        // Update pagination
        document.getElementById('page-info').textContent = `Strona ${currentPage + 1}`;
        document.getElementById('prev-page').disabled = currentPage === 0;
    } catch (error) {
        listEl.innerHTML = `
            <div class="loading">
                <p style="color: var(--accent-danger)">Błąd: ${escapeHtml(error.message)}</p>
            </div>
        `;
    }
}

async function viewTransaction(id) {
    try {
        const response = await apiCall(`/transactions/${id}`);
        const tx = await response.json();
        
        const detailsEl = document.getElementById('transaction-details');
        detailsEl.innerHTML = `
            <div class="detail-row">
                <div class="detail-label">ID</div>
                <div class="detail-value">#${tx.id}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">ID transakcji</div>
                <div class="detail-value" style="font-family: monospace;">${escapeHtml(tx.transactionId || '-')}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Kwota</div>
                <div class="detail-value" style="font-size: 20px; font-weight: 700; color: var(--accent-success);">
                    ${formatAmount(tx.amount)} ${tx.currency}
                </div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Data waluty</div>
                <div class="detail-value">${formatDate(tx.valueDate)}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Nadawca</div>
                <div class="detail-value">
                    ${escapeHtml(tx.senderName || '-')}<br>
                    <span style="font-family: monospace; color: var(--text-secondary); font-size: 12px;">
                        ${escapeHtml(tx.senderAccount || '-')}
                    </span>
                </div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Odbiorca</div>
                <div class="detail-value">
                    ${escapeHtml(tx.receiverName || '-')}<br>
                    <span style="font-family: monospace; color: var(--text-secondary); font-size: 12px;">
                        ${escapeHtml(tx.receiverAccount || '-')}
                    </span>
                </div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Tytuł</div>
                <div class="detail-value">${escapeHtml(tx.paymentTitle || '-')}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Status</div>
                <div class="detail-value">
                    <span class="tx-status status-${tx.status}">${formatStatus(tx.status)}</span>
                </div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Utworzono</div>
                <div class="detail-value">${formatDateTime(tx.createdAt)}</div>
            </div>
            ${tx.rejectionReason ? `
            <div class="detail-row">
                <div class="detail-label">Powód odrzucenia</div>
                <div class="detail-value" style="color: var(--accent-danger)">${escapeHtml(tx.rejectionReason)}</div>
            </div>
            ` : ''}
        `;
        
        // Action buttons based on status
        const actionsEl = document.getElementById('modal-actions');
        actionsEl.innerHTML = '';
        
        if (tx.status === 'PENDING_APPROVAL') {
            actionsEl.innerHTML = `
                <button class="btn btn-success" onclick="approveTransaction(${tx.id})">✓ Zatwierdź</button>
                <button class="btn btn-danger" onclick="rejectTransaction(${tx.id})">✕ Odrzuć</button>
                <button class="btn" onclick="suspendTransaction(${tx.id})" style="background: var(--bg-glass); color: var(--text-primary);">⏸ Wstrzymaj</button>
                <button class="btn" onclick="editTransaction(${tx.id})" style="background: var(--bg-glass); color: var(--text-primary);">✏️ Edytuj</button>
            `;
        } else if (tx.status === 'SUSPENDED') {
            actionsEl.innerHTML = `
                <button class="btn btn-primary" onclick="resumeTransaction(${tx.id})">▶️ Wznów</button>
            `;
        } else if (tx.status === 'AUTHORIZED' || tx.status === 'APPROVED') {
            actionsEl.innerHTML = `
                <button class="btn" onclick="suspendTransaction(${tx.id})" style="background: var(--bg-glass); color: var(--text-primary);">⏸ Wstrzymaj</button>
            `;
        }
        
        showModal('transaction');
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

// Transaction Actions
async function approveTransaction(id) {
    try {
        const response = await apiCall(`/transactions/${id}/approve`, { method: 'POST' });
        if (response.ok) {
            hideModal('transaction');
            loadTransactions();
            loadStatusCounts();
        } else {
            const data = await response.json();
            alert('Błąd: ' + (data.error || 'Nie udało się zatwierdzić'));
        }
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

async function rejectTransaction(id) {
    const reason = prompt('Podaj powód odrzucenia:');
    if (!reason) return;
    
    try {
        const response = await apiCall(`/transactions/${id}/reject`, {
            method: 'POST',
            body: JSON.stringify({ reason })
        });
        if (response.ok) {
            hideModal('transaction');
            loadTransactions();
            loadStatusCounts();
        } else {
            const data = await response.json();
            alert('Błąd: ' + (data.error || 'Nie udało się odrzucić'));
        }
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

async function suspendTransaction(id) {
    try {
        const response = await apiCall(`/transactions/${id}/suspend`, { method: 'POST' });
        if (response.ok) {
            hideModal('transaction');
            loadTransactions();
            loadStatusCounts();
        } else {
            const data = await response.json();
            alert('Błąd: ' + (data.error || 'Nie udało się wstrzymać'));
        }
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

async function resumeTransaction(id) {
    try {
        const response = await apiCall(`/transactions/${id}/resume`, { method: 'POST' });
        if (response.ok) {
            hideModal('transaction');
            loadTransactions();
            loadStatusCounts();
        } else {
            const data = await response.json();
            alert('Błąd: ' + (data.error || 'Nie udało się wznowić'));
        }
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

async function editTransaction(id) {
    try {
        const response = await apiCall(`/transactions/${id}`);
        const tx = await response.json();
        
        const form = document.getElementById('edit-transaction-form');
        form.id.value = tx.id;
        form.amount.value = tx.amount;
        form.valueDate.value = tx.valueDate;
        form.paymentTitle.value = tx.paymentTitle || '';
        
        hideModal('transaction');
        showModal('editTransaction');
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

async function handleEditTransaction(e) {
    e.preventDefault();
    
    const form = e.target;
    const id = form.id.value;
    const updates = {};
    
    if (form.amount.value) updates.amount = form.amount.value;
    if (form.valueDate.value) updates.valueDate = form.valueDate.value;
    if (form.paymentTitle.value) updates.paymentTitle = form.paymentTitle.value;
    
    try {
        const response = await apiCall(`/transactions/${id}`, {
            method: 'PATCH',
            body: JSON.stringify(updates)
        });
        
        if (response.ok) {
            hideModal('editTransaction');
            loadTransactions();
            loadStatusCounts();
        } else {
            const data = await response.json();
            alert('Błąd: ' + (data.error || 'Nie udało się zapisać'));
        }
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

async function handleNewTransaction(e) {
    e.preventDefault();
    
    const form = e.target;
    const data = {
        transactionId: form.transactionId.value,
        painMessageId: form.painMessageId.value,
        amount: form.amount.value,
        currency: form.currency.value,
        valueDate: form.valueDate.value,
        senderName: form.senderName.value,
        senderAccount: form.senderAccount.value,
        receiverName: form.receiverName.value,
        receiverAccount: form.receiverAccount.value,
        paymentTitle: form.paymentTitle.value
    };
    
    try {
        const response = await apiCall('/transactions', {
            method: 'POST',
            body: JSON.stringify(data)
        });
        
        if (response.ok) {
            hideModal('newTransaction');
            form.reset();
            loadTransactions();
            loadStatusCounts();
        } else {
            const error = await response.json();
            alert('Błąd: ' + (error.error || 'Nie udało się utworzyć transakcji'));
        }
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

// Helpers
function formatStatus(status) {
    const statusMap = {
        'RECEIVED': 'Odebrane',
        'VALIDATED': 'Zwalidowane',
        'VALIDATION_FAILED': 'Błąd walidacji',
        'AUTHORIZING': 'Autoryzacja',
        'AUTHORIZED': 'Autoryzowane',
        'AUTHORIZATION_FAILED': 'Błąd autoryzacji',
        'PENDING_APPROVAL': 'Oczekuje',
        'APPROVED': 'Zatwierdzone',
        'REJECTED': 'Odrzucone',
        'SUSPENDED': 'Wstrzymane',
        'SENT_TO_CLEARING': 'Wysłane',
        'COMPLETED': 'Zakończone',
        'FAILED': 'Nie powiodło się'
    };
    return statusMap[status] || status;
}

function formatAmount(amount) {
    return new Intl.NumberFormat('pl-PL', { 
        minimumFractionDigits: 2,
        maximumFractionDigits: 2 
    }).format(amount);
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleDateString('pl-PL', { 
        day: '2-digit', 
        month: '2-digit', 
        year: 'numeric' 
    });
}

function formatDateTime(dateStr) {
    if (!dateStr) return '-';
    const date = new Date(dateStr);
    return date.toLocaleString('pl-PL', { 
        day: '2-digit', 
        month: '2-digit', 
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Status Flow Diagram
async function loadStatusCounts() {
    try {
        const response = await apiCall('/transactions/status-counts');
        const counts = await response.json();
        renderStatusFlow(counts);
    } catch (error) {
        console.error('Error loading status counts:', error);
    }
}

function renderStatusFlow(counts) {
    const container = document.getElementById('status-flow-diagram');
    
    // Main flow order
    const mainFlow = [
        { key: 'RECEIVED', label: 'Odebrane', icon: '📥' },
        { key: 'VALIDATED', label: 'Zwalidowane', icon: '✅' },
        { key: 'AUTHORIZING', label: 'Autoryzacja', icon: '🔐' },
        { key: 'AUTHORIZED', label: 'Autoryzowane', icon: '🔓' },
        { key: 'PENDING_APPROVAL', label: 'Oczekuje', icon: '⏳' },
        { key: 'APPROVED', label: 'Zatwierdzone', icon: '✓' },
        { key: 'SENT_TO_CLEARING', label: 'Wysłane', icon: '📤' },
        { key: 'COMPLETED', label: 'Zakończone', icon: '🎉' }
    ];
    
    // Secondary statuses
    const secondaryFlow = [
        { key: 'VALIDATION_FAILED', label: 'Błąd walidacji', icon: '❌' },
        { key: 'AUTHORIZATION_FAILED', label: 'Błąd autoryzacji', icon: '⚠️' },
        { key: 'REJECTED', label: 'Odrzucone', icon: '✕' },
        { key: 'SUSPENDED', label: 'Wstrzymane', icon: '⏸' },
        { key: 'FAILED', label: 'Nie powiodło się', icon: '💥' }
    ];
    
    let html = '';
    
    // Main flow with arrows
    mainFlow.forEach((status, index) => {
        const count = counts[status.key] || 0;
        const isActive = currentFilter === status.key;
        html += `
            <div class="status-box status-box-${status.key} ${isActive ? 'active' : ''}" 
                 onclick="filterByStatus('${status.key}')" title="Kliknij aby filtrować">
                <span class="status-name">${status.icon} ${status.label}</span>
                <span class="status-count">${count}</span>
            </div>
        `;
        if (index < mainFlow.length - 1) {
            html += '<span class="status-arrow">→</span>';
        }
    });
    
    html += '<div style="width:100%; height:16px;"></div>';
    
    // Secondary flow
    secondaryFlow.forEach((status, index) => {
        const count = counts[status.key] || 0;
        const isActive = currentFilter === status.key;
        html += `
            <div class="status-box status-box-${status.key} ${isActive ? 'active' : ''}" 
                 onclick="filterByStatus('${status.key}')" title="Kliknij aby filtrować">
                <span class="status-name">${status.icon} ${status.label}</span>
                <span class="status-count">${count}</span>
            </div>
        `;
        if (index < secondaryFlow.length - 1) {
            html += '<span class="status-arrow">→</span>';
        }
    });
    
    container.innerHTML = html;
    
    // Show/hide clear button
    const clearBtn = document.getElementById('clear-filter-btn');
    if (currentFilter) {
        clearBtn.classList.add('visible');
    } else {
        clearBtn.classList.remove('visible');
    }
}

function filterByStatus(status) {
    currentFilter = status;
    document.getElementById('status-filter').value = status;
    currentPage = 0;
    loadTransactions();
    loadStatusCounts();
}

function clearStatusFilter() {
    currentFilter = '';
    document.getElementById('status-filter').value = '';
    currentPage = 0;
    loadTransactions();
    loadStatusCounts();
}

// Make functions global
window.viewTransaction = viewTransaction;
window.approveTransaction = approveTransaction;
window.rejectTransaction = rejectTransaction;
window.suspendTransaction = suspendTransaction;
window.resumeTransaction = resumeTransaction;
window.editTransaction = editTransaction;
window.filterByStatus = filterByStatus;
window.clearStatusFilter = clearStatusFilter;
