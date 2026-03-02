// Payment Processor - Modern Frontend JavaScript

const API_BASE = '/api';
let token = localStorage.getItem('token');
let currentPage = 0;
let currentFilter = '';
let currentViewMode = 'view'; // 'view', 'edit', 'create'
let currentTransactionId = null;

// DOM Elements
const screens = {
    login: document.getElementById('login-screen'),
    main: document.getElementById('main-screen')
};

const detailPanel = {
    empty: document.getElementById('detail-empty'),
    content: document.getElementById('detail-content'),
    title: document.getElementById('detail-title'),
    actions: document.getElementById('detail-actions'),
    form: document.getElementById('transaction-form'),
    formActions: document.getElementById('form-actions')
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
    document.getElementById('new-transaction-btn').addEventListener('click', showCreateForm);
    
    // Form submit
    detailPanel.form.addEventListener('submit', handleFormSubmit);
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

// Detail Panel
function showDetailEmpty() {
    detailPanel.empty.classList.remove('hidden');
    detailPanel.content.classList.add('hidden');
    document.getElementById('detail-panel').classList.remove('open');
    document.getElementById('master-panel').classList.remove('has-detail');
}

function showDetailContent() {
    document.getElementById('detail-panel').classList.add('open');
    document.getElementById('master-panel').classList.add('has-detail');
    detailPanel.empty.classList.add('hidden');
    detailPanel.content.classList.remove('hidden');
}

// Form Modes
function showCreateForm() {
    currentViewMode = 'create';
    currentTransactionId = null;
    
    // Clear form
    detailPanel.form.reset();
    detailPanel.form.querySelectorAll('input, select, textarea').forEach(el => {
        el.removeAttribute('readonly');
        el.removeAttribute('disabled');
    });
    
    // Set default date
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    detailPanel.form.valueDate.value = tomorrow.toISOString().split('T')[0];
    
    // Set title and actions
    detailPanel.title.textContent = '➕ Nowa transakcja ISO20022';
    detailPanel.actions.innerHTML = `
        <button type="button" class="btn" onclick="showDetailEmpty()">Anuluj</button>
    `;
    detailPanel.formActions.innerHTML = `
        <button type="submit" class="btn btn-primary">Utwórz transakcję</button>
    `;
    
    // Hide status section
    const statusSection = detailPanel.form.querySelector('input[name="status"]')?.closest('fieldset');
    if (statusSection) statusSection.style.display = 'none';
    
    showDetailContent();
}

async function showViewForm(id) {
    try {
        const response = await apiCall(`/transactions/${id}`);
        const tx = await response.json();
        
        currentViewMode = 'view';
        currentTransactionId = id;
        
        // Fill form with data
        fillFormWithTransaction(tx);
        
        // Make fields readonly
        detailPanel.form.querySelectorAll('input, select, textarea').forEach(el => {
            el.setAttribute('readonly', true);
            el.setAttribute('disabled', true);
        });
        
        // Set title and actions
        detailPanel.title.textContent = `📋 Transakcja #${id}`;
        
        let actionsHtml = `<button type="button" class="btn" onclick="showDetailEmpty()">Zamknij</button>`;
        
        // Add action buttons based on status
        if (tx.status === 'PENDING_APPROVAL') {
            actionsHtml += `
                <button type="button" class="btn btn-success" onclick="approveTransaction(${id})">✓ Zatwierdź</button>
                <button type="button" class="btn btn-danger" onclick="rejectTransaction(${id})">✕ Odrzuć</button>
                <button type="button" class="btn" onclick="showEditForm(${id})">✏️ Edytuj</button>
                <button type="button" class="btn" onclick="suspendTransaction(${id})">⏸ Wstrzymaj</button>
            `;
        } else if (tx.status === 'SUSPENDED') {
            actionsHtml += `<button type="button" class="btn btn-primary" onclick="resumeTransaction(${id})">▶️ Wznów</button>`;
        } else if (tx.status === 'AUTHORIZED' || tx.status === 'APPROVED') {
            actionsHtml += `<button type="button" class="btn" onclick="suspendTransaction(${id})">⏸ Wstrzymaj</button>`;
        }
        
        detailPanel.actions.innerHTML = actionsHtml;
        detailPanel.formActions.innerHTML = '';
        
        // Show status section
        const statusSection = detailPanel.form.querySelector('input[name="status"]')?.closest('fieldset');
        if (statusSection) statusSection.style.display = 'block';
        
        showDetailContent();
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

async function showEditForm(id) {
    try {
        const response = await apiCall(`/transactions/${id}`);
        const tx = await response.json();
        
        currentViewMode = 'edit';
        currentTransactionId = id;
        
        // Fill form with data
        fillFormWithTransaction(tx);
        
        // Make editable fields writable
        detailPanel.form.querySelectorAll('input, select, textarea').forEach(el => {
            const name = el.name;
            // Keep some fields readonly
            if (['messageId', 'paymentInstructionId', 'creationDateTime', 'status', 'createdAt', 'authorizedBy', 'approvedBy', 'rejectionReason'].includes(name)) {
                el.setAttribute('readonly', true);
                el.setAttribute('disabled', true);
            } else {
                el.removeAttribute('readonly');
                el.removeAttribute('disabled');
            }
        });
        
        // Set title and actions
        detailPanel.title.textContent = `✏️ Edycja transakcji #${id}`;
        detailPanel.actions.innerHTML = `
            <button type="button" class="btn" onclick="showViewForm(${id})">Anuluj</button>
        `;
        detailPanel.formActions.innerHTML = `
            <button type="submit" class="btn btn-primary">Zapisz zmiany</button>
        `;
        
        // Show status section
        const statusSection = detailPanel.form.querySelector('input[name="status"]')?.closest('fieldset');
        if (statusSection) statusSection.style.display = 'block';
        
        showDetailContent();
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

function fillFormWithTransaction(tx) {
    const form = detailPanel.form;
    
    // Helper to set value
    const setValue = (name, value) => {
        const el = form.querySelector(`[name="${name}"]`);
        if (el) {
            if (el.type === 'datetime-local' && value) {
                // Convert timestamp to datetime-local format
                el.value = new Date(value).toISOString().slice(0, 16);
            } else if (el.type === 'date' && value) {
                el.value = value;
            } else {
                el.value = value || '';
            }
        }
    };
    
    // Header
    setValue('messageId', tx.messageId);
    setValue('paymentInstructionId', tx.paymentInstructionId);
    setValue('creationDateTime', tx.creationDateTime);
    setValue('paymentMethod', tx.paymentMethod);
    
    // Amount
    setValue('amount', tx.amount);
    setValue('currency', tx.currency);
    setValue('valueDate', tx.valueDate);
    setValue('requestedExecutionDate', tx.requestedExecutionDate);
    setValue('chargeBearer', tx.chargeBearer);
    
    // Debtor
    setValue('debtorName', tx.debtorName);
    setValue('debtorLegalName', tx.debtorLegalName);
    setValue('debtorAccountIban', tx.debtorAccountIban);
    setValue('debtorAgentBic', tx.debtorAgentBic);
    setValue('debtorAddressLine', tx.debtorAddressLine);
    setValue('debtorCountry', tx.debtorCountry);
    
    // Creditor
    setValue('creditorName', tx.creditorName);
    setValue('creditorLegalName', tx.creditorLegalName);
    setValue('creditorAccountIban', tx.creditorAccountIban);
    setValue('creditorAgentBic', tx.creditorAgentBic);
    setValue('creditorAddressLine', tx.creditorAddressLine);
    setValue('creditorCountry', tx.creditorCountry);
    
    // Remittance
    setValue('remittanceUnstructured', tx.remittanceUnstructured);
    setValue('remittanceReference', tx.remittanceReference);
    setValue('remittanceStructuredType', tx.remittanceStructuredType);
    setValue('purposeCode', tx.purposeCode);
    setValue('transactionId', tx.transactionId);
    
    // Status
    setValue('status', formatStatus(tx.status));
    setValue('createdAt', tx.createdAt);
    setValue('authorizedBy', tx.authorizedBy);
    setValue('approvedBy', tx.approvedBy);
    setValue('rejectionReason', tx.rejectionReason);
    
    // Show rejection reason if exists
    const rejectionRow = document.getElementById('rejection-row');
    if (tx.rejectionReason) {
        rejectionRow.style.display = 'flex';
    } else {
        rejectionRow.style.display = 'none';
    }
}

async function handleFormSubmit(e) {
    e.preventDefault();
    
    if (currentViewMode === 'create') {
        await createTransaction();
    } else if (currentViewMode === 'edit') {
        await updateTransaction();
    }
}

async function createTransaction() {
    const form = detailPanel.form;
    const formData = new FormData(form);
    const data = {};
    
    formData.forEach((value, key) => {
        if (value) data[key] = value;
    });
    
    // Set defaults
    data.messageType = 'pain.001';
    data.paymentMethod = data.paymentMethod || 'TRN';
    data.chargeBearer = data.chargeBearer || 'SLEV';
    
    try {
        const response = await apiCall('/transactions', {
            method: 'POST',
            body: JSON.stringify(data)
        });
        
        if (response.ok) {
            const tx = await response.json();
            loadTransactions();
            loadStatusCounts();
            showViewForm(tx.id);
        } else {
            const error = await response.json();
            alert('Błąd: ' + (error.error || 'Nie udało się utworzyć transakcji'));
        }
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

async function updateTransaction() {
    const form = detailPanel.form;
    const formData = new FormData(form);
    const data = {};
    
    formData.forEach((value, key) => {
        if (value) data[key] = value;
    });
    
    try {
        const response = await apiCall(`/transactions/${currentTransactionId}`, {
            method: 'PATCH',
            body: JSON.stringify({
                amount: data.amount,
                valueDate: data.valueDate,
                paymentTitle: data.remittanceUnstructured
            })
        });
        
        if (response.ok) {
            loadTransactions();
            loadStatusCounts();
            showViewForm(currentTransactionId);
        } else {
            const error = await response.json();
            alert('Błąd: ' + (error.error || 'Nie udało się zapisać'));
        }
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

// Transaction Actions
async function approveTransaction(id) {
    try {
        const response = await apiCall(`/transactions/${id}/approve`, { method: 'POST' });
        if (response.ok) {
            loadTransactions();
            loadStatusCounts();
            showViewForm(id);
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
            loadTransactions();
            loadStatusCounts();
            showViewForm(id);
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
            loadTransactions();
            loadStatusCounts();
            showViewForm(id);
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
            loadTransactions();
            loadStatusCounts();
            showViewForm(id);
        } else {
            const data = await response.json();
            alert('Błąd: ' + (data.error || 'Nie udało się wznowić'));
        }
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

// Transactions List
async function loadTransactions() {
    const listEl = document.getElementById('transactions-list');
    listEl.innerHTML = '<div class="loading"><div class="spinner"></div></div>';
    
    try {
        let url = `/transactions?page=${currentPage}&size=20`;
        if (currentFilter) {
            url = `/transactions/status/${currentFilter}`;
        }
        
        const response = await apiCall(url);
        const transactions = await response.json();
        
        if (!transactions || transactions.length === 0) {
            listEl.innerHTML = '<div class="empty-state"><p>Brak transakcji</p></div>';
            return;
        }
        
        listEl.innerHTML = transactions.map(tx => `
            <div class="transaction-item ${currentTransactionId === tx.id ? 'active' : ''}" onclick="showViewForm(${tx.id})">
                <div class="tx-id">#${tx.id}</div>
                <div class="tx-parties">
                    <div class="party-row">
                        <span>${escapeHtml(tx.debtorName || tx.senderName || '-')}</span>
                    </div>
                    <div class="party-row">
                        <span>→ ${escapeHtml(tx.creditorName || tx.receiverName || '-')}</span>
                    </div>
                </div>
                <div class="tx-amount">${formatAmount(tx.amount)} ${tx.currency}</div>
            </div>
        `).join('');
        
        // Update pagination
        document.getElementById('page-info').textContent = currentPage + 1;
    } catch (error) {
        listEl.innerHTML = `<div class="loading"><p style="color: var(--accent-danger)">${error.message}</p></div>`;
    }
}

// Status Flow
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
    
    const mainFlow = [
        { key: 'RECEIVED', label: 'Odebrane' },
        { key: 'VALIDATED', label: 'Zwalidowane' },
        { key: 'AUTHORIZING', label: 'Autoryzacja' },
        { key: 'AUTHORIZED', label: 'Autoryzowane' },
        { key: 'PENDING_APPROVAL', label: 'Oczekuje' },
        { key: 'APPROVED', label: 'Zatwierdzone' },
        { key: 'SENT_TO_CLEARING', label: 'Wysłane' },
        { key: 'COMPLETED', label: 'Zakończone' }
    ];
    
    const secondaryFlow = [
        { key: 'VALIDATION_FAILED', label: 'Błąd' },
        { key: 'AUTHORIZATION_FAILED', label: 'Błąd' },
        { key: 'REJECTED', label: 'Odrzucone' },
        { key: 'SUSPENDED', label: 'Wstrzymane' },
        { key: 'FAILED', label: 'Failed' }
    ];
    
    let html = '';
    
    mainFlow.forEach((status) => {
        const count = counts[status.key] || 0;
        const isActive = currentFilter === status.key;
        html += `
            <div class="status-box ${isActive ? 'active' : ''}" data-status="${status.key}" onclick="filterByStatus('${status.key}')">
                <span class="status-count">${count}</span>
            </div>
        `;
    });
    
    html += '<span style="color: var(--text-muted); font-size: 10px;">|</span>';
    
    secondaryFlow.forEach((status) => {
        const count = counts[status.key] || 0;
        const isActive = currentFilter === status.key;
        html += `
            <div class="status-box ${isActive ? 'active' : ''}" data-status="${status.key}" onclick="filterByStatus('${status.key}')">
                <span class="status-count">${count}</span>
            </div>
        `;
    });
    
    container.innerHTML = html;
}

function filterByStatus(status) {
    if (currentFilter === status) {
        currentFilter = '';
    } else {
        currentFilter = status;
    }
    document.getElementById('status-filter').value = currentFilter;
    currentPage = 0;
    loadTransactions();
    loadStatusCounts();
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

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Make functions global
window.showViewForm = showViewForm;
window.showEditForm = showEditForm;
window.showCreateForm = showCreateForm;
window.approveTransaction = approveTransaction;
window.rejectTransaction = rejectTransaction;
window.suspendTransaction = suspendTransaction;
window.resumeTransaction = resumeTransaction;
window.filterByStatus = filterByStatus;
