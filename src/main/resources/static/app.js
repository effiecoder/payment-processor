// Payment Processor - New UI

const API_BASE = '/api';
let token = localStorage.getItem('token');
let currentPage = 0;
let currentFilter = '';
let currentTxId = null;

// DOM Elements
const screens = {
    login: document.getElementById('login-screen'),
    main: document.getElementById('main-app')
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
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('new-tx-btn').addEventListener('click', showNewTransaction);
    document.getElementById('close-detail').addEventListener('click', closeDetail);
    document.getElementById('search-input').addEventListener('input', handleSearch);
}

async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        if (!response.ok) throw new Error('Invalid credentials');
        
        const data = await response.json();
        token = data.token;
        localStorage.setItem('token', token);
        
        showMainScreen();
        loadTransactions();
        loadStatusCounts();
    } catch (error) {
        alert('Błąd logowania: ' + error.message);
    }
}

function showLoginScreen() {
    screens.login.classList.remove('hidden');
    screens.main.classList.add('hidden');
}

function showMainScreen() {
    screens.login.classList.add('hidden');
    screens.main.classList.remove('hidden');
}

// API
async function apiCall(endpoint, options = {}) {
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    
    const response = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
    if (response.status === 401) {
        token = null;
        localStorage.removeItem('token');
        showLoginScreen();
    }
    return response;
}

// Status Flow
async function loadStatusCounts() {
    try {
        const response = await apiCall('/transactions/status-counts');
        const counts = await response.json();
        renderStatusSteps(counts);
    } catch (e) { console.error(e); }
}

function renderStatusSteps(counts) {
    const steps = [
        { key: 'RECEIVED', label: 'Odebrane', color: '#60A5FA' },
        { key: 'VALIDATED', label: 'Zwalidowane', color: '#F59E0B' },
        { key: 'AUTHORIZED', label: 'Autoryzowane', color: '#F59E0B' },
        { key: 'PENDING_APPROVAL', label: 'Oczekuje', color: '#F59E0B' },
        { key: 'APPROVED', label: 'Zatwierdzone', color: '#4CAF50' },
        { key: 'SENT_TO_CLEARING', label: 'Wysłane', color: '#7A52F4' }
    ];
    
    const total = Object.values(counts).reduce((a, b) => a + b, 0);
    document.getElementById('total-count').textContent = total;
    
    let html = '';
    steps.forEach((step, i) => {
        const c = counts[step.key] || 0;
        const isActive = currentFilter === step.key;
        html += `
            <div class="status-step ${isActive ? 'active' : ''}" data-status="${step.key}">
                <div class="status-step-content" onclick="filterByStatus('${step.key}')">
                    <span class="status-step-dot"></span>
                    <span class="status-step-label">${step.label}</span>
                    <span class="status-step-count">${c}</span>
                </div>
            </div>
            ${i < steps.length - 1 ? '<span class="status-step-arrow">›</span>' : ''}
        `;
    });
    
    document.getElementById('status-steps').innerHTML = html;
}

function filterByStatus(status) {
    currentFilter = currentFilter === status ? '' : status;
    currentPage = 0;
    loadTransactions();
    loadStatusCounts();
}

// Transactions List
async function loadTransactions() {
    const listEl = document.getElementById('tx-list');
    listEl.innerHTML = '<div style="padding:40px;text-align:center;color:#9E9BAE;">Ładowanie...</div>';
    
    try {
        let url = `/transactions?page=${currentPage}&size=50`;
        if (currentFilter) url = `/transactions/status/${currentFilter}`;
        
        const response = await apiCall(url);
        const txs = await response.json();
        
        if (!txs?.length) {
            listEl.innerHTML = '<div style="padding:40px;text-align:center;color:#9E9BAE;">Brak transakcji</div>';
            return;
        }
        
        listEl.innerHTML = txs.map(tx => `
            <div class="tx-item ${currentTxId === tx.id ? 'active' : ''}" data-id="${tx.id}">
                <div class="tx-item-left">
                    <span class="tx-item-id">#${tx.id}</span>
                    <span class="tx-item-sender">${escapeHtml(tx.debtorName || tx.senderName || '-')}</span>
                    <span class="tx-item-iban">${formatIban(tx.debtorAccountIban || tx.senderAccount)}</span>
                </div>
                <div class="tx-item-right">
                    <span class="tx-item-amount">${formatAmount(tx.amount)} ${tx.currency}</span>
                    <span class="tx-item-status">${formatStatus(tx.status)}</span>
                </div>
            </div>
        `).join('');
        
        listEl.querySelectorAll('.tx-item').forEach(item => {
            item.addEventListener('click', () => showTransaction(parseInt(item.dataset.id)));
        });
    } catch (error) {
        listEl.innerHTML = `<div style="padding:40px;text-align:center;color:#EF4444;">${error.message}</div>`;
    }
}

function handleSearch(e) {
    // Simple search - filter current list
    const query = e.target.value.toLowerCase();
    document.querySelectorAll('.tx-item').forEach(item => {
        const sender = item.querySelector('.tx-item-sender').textContent.toLowerCase();
        const iban = item.querySelector('.tx-item-iban').textContent.toLowerCase();
        item.style.display = (sender.includes(query) || iban.includes(query)) ? 'flex' : 'none';
    });
}

// Transaction Detail
async function showTransaction(id) {
    try {
        const response = await apiCall(`/transactions/${id}`);
        const tx = await response.json();
        
        currentTxId = id;
        loadTransactions(); // refresh to update active state
        
        document.getElementById('empty-state').classList.add('hidden');
        document.getElementById('detail-view').classList.remove('hidden');
        document.getElementById('detail-id').textContent = tx.id;
        
        // Render detail content
        document.getElementById('detail-content').innerHTML = `
            <div class="detail-card">
                <div class="detail-card-title">Identyfikator i metoda</div>
                <div class="detail-card-grid">
                    <div class="detail-field">
                        <span class="detail-field-label">Message ID</span>
                        <span class="detail-field-value mono">${escapeHtml(tx.messageId || '-')}</span>
                    </div>
                    <div class="detail-field">
                        <span class="detail-field-label">Transaction ID</span>
                        <span class="detail-field-value mono">${escapeHtml(tx.transactionId || '-')}</span>
                    </div>
                    <div class="detail-field">
                        <span class="detail-field-label">Payment Method</span>
                        <span class="detail-field-value">${tx.paymentMethod || 'TRN'}</span>
                    </div>
                    <div class="detail-field">
                        <span class="detail-field-label">Charge Bearer</span>
                        <span class="detail-field-value">${tx.chargeBearer || 'SLEV'}</span>
                    </div>
                </div>
            </div>
            
            <div class="detail-card">
                <div class="detail-card-title">Dane finansowe</div>
                <div class="amount-block">
                    <span class="amount-value">${formatAmount(tx.amount)} ${tx.currency}</span>
                    <div class="amount-grid">
                        <div class="detail-field">
                            <span class="detail-field-label">Waluta</span>
                            <span class="detail-field-value">${tx.currency}</span>
                        </div>
                        <div class="detail-field">
                            <span class="detail-field-label">Data waluty</span>
                            <span class="detail-field-value">${tx.valueDate || '-'}</span>
                        </div>
                        <div class="detail-field">
                            <span class="detail-field-label">Data realizacji</span>
                            <span class="detail-field-value">${tx.requestedExecutionDate || '-'}</span>
                        </div>
                        <div class="detail-field">
                            <span class="detail-field-label">Status</span>
                            <span class="detail-field-value">${formatStatus(tx.status)}</span>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="detail-card">
                <div class="detail-card-title">Nadawca</div>
                <div class="detail-card-grid">
                    <div class="detail-field">
                        <span class="detail-field-label">Nazwa</span>
                        <span class="detail-field-value">${escapeHtml(tx.debtorName || '-')}</span>
                    </div>
                    <div class="detail-field">
                        <span class="detail-field-label">LEI</span>
                        <span class="detail-field-value mono">${escapeHtml(tx.debtorLegalName || '-')}</span>
                    </div>
                    <div class="detail-field">
                        <span class="detail-field-label">IBAN</span>
                        <span class="detail-field-value mono">${formatIban(tx.debtorAccountIban)}</span>
                    </div>
                    <div class="detail-field">
                        <span class="detail-field-label">BIC</span>
                        <span class="detail-field-value mono">${escapeHtml(tx.debtorAgentBic || '-')}</span>
                    </div>
                </div>
            </div>
            
            <div class="detail-card">
                <div class="detail-card-title">Odbiorca</div>
                <div class="detail-card-grid">
                    <div class="detail-field">
                        <span class="detail-field-label">Nazwa</span>
                        <span class="detail-field-value">${escapeHtml(tx.creditorName || '-')}</span>
                    </div>
                    <div class="detail-field">
                        <span class="detail-field-label">LEI</span>
                        <span class="detail-field-value mono">${escapeHtml(tx.creditorLegalName || '-')}</span>
                    </div>
                    <div class="detail-field">
                        <span class="detail-field-label">IBAN</span>
                        <span class="detail-field-value mono">${formatIban(tx.creditorAccountIban)}</span>
                    </div>
                    <div class="detail-field">
                        <span class="detail-field-label">BIC</span>
                        <span class="detail-field-value mono">${escapeHtml(tx.creditorAgentBic || '-')}</span>
                    </div>
                </div>
            </div>
            
            <div class="detail-card">
                <div class="detail-card-title">Tytuł płatności</div>
                <div class="detail-field">
                    <span class="detail-field-value">${escapeHtml(tx.remittanceUnstructured || tx.paymentTitle || '-')}</span>
                </div>
            </div>
        `;
        
        // Render action buttons
        let actions = '';
        if (tx.status === 'PENDING_APPROVAL') {
            actions = `
                <button class="action-btn action-btn-outline" onclick="approveTx(${id})">✓ Zweryfikuj</button>
                <button class="action-btn action-btn-danger" onclick="rejectTx(${id})">✕ Odrzuć</button>
                <button class="action-btn action-btn-outline" onclick="suspendTx(${id})">⏸ Wstrzymaj</button>
                <button class="action-btn action-btn-primary" onclick="approveTx(${id})">✓ Zatwierdź</button>
            `;
        } else if (tx.status === 'SUSPENDED') {
            actions = `<button class="action-btn action-btn-primary" onclick="resumeTx(${id})">▶️ Wznów</button>`;
        } else if (tx.status === 'AUTHORIZED' || tx.status === 'APPROVED') {
            actions = `<button class="action-btn action-btn-outline" onclick="suspendTx(${id})">⏸ Wstrzymaj</button>`;
        }
        
        document.getElementById('action-bar').innerHTML = actions;
        
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

function closeDetail() {
    currentTxId = null;
    document.getElementById('empty-state').classList.remove('hidden');
    document.getElementById('detail-view').classList.add('hidden');
    loadTransactions();
}

function showNewTransaction() {
    alert('Tworzenie nowej transakcji - TODO');
}

// Actions
async function approveTx(id) {
    try {
        await apiCall(`/transactions/${id}/approve`, { method: 'POST' });
        loadTransactions();
        loadStatusCounts();
        showTransaction(id);
    } catch (e) { alert('Błąd: ' + e.message); }
}

async function rejectTx(id) {
    const reason = prompt('Powód odrzucenia:');
    if (!reason) return;
    try {
        await apiCall(`/transactions/${id}/reject`, { method: 'POST', body: JSON.stringify({ reason }) });
        loadTransactions();
        loadStatusCounts();
        showTransaction(id);
    } catch (e) { alert('Błąd: ' + e.message); }
}

async function suspendTx(id) {
    try {
        await apiCall(`/transactions/${id}/suspend`, { method: 'POST' });
        loadTransactions();
        loadStatusCounts();
        showTransaction(id);
    } catch (e) { alert('Błąd: ' + e.message); }
}

async function resumeTx(id) {
    try {
        await apiCall(`/transactions/${id}/resume`, { method: 'POST' });
        loadTransactions();
        loadStatusCounts();
        showTransaction(id);
    } catch (e) { alert('Błąd: ' + e.message); }
}

// Helpers
function formatStatus(s) {
    const m = {
        RECEIVED: 'Odebrane', VALIDATED: 'Zwalidowane',
        AUTHORIZING: 'Autoryzacja', AUTHORIZED: 'Autoryzowane',
        PENDING_APPROVAL: 'Oczekuje', APPROVED: 'Zatwierdzone',
        REJECTED: 'Odrzucone', SUSPENDED: 'Wstrzymane',
        SENT_TO_CLEARING: 'Wysłane', COMPLETED: 'Zakończone',
        FAILED: 'Failed', VALIDATION_FAILED: 'Błąd',
        AUTHORIZATION_FAILED: 'Błąd'
    };
    return m[s] || s;
}

function formatAmount(a) {
    return new Intl.NumberFormat('pl-PL', { minimumFractionDigits: 2 }).format(a);
}

function formatIban(iban) {
    if (!iban) return '-';
    return iban.replace(/\s/g, '').match(/.{1,4}/g)?.join(' ') || iban;
}

function escapeHtml(t) {
    if (!t) return '';
    const d = document.createElement('div');
    d.textContent = t;
    return d.innerHTML;
}

// Global functions
window.filterByStatus = filterByStatus;
window.approveTx = approveTx;
window.rejectTx = rejectTx;
window.suspendTx = suspendTx;
window.resumeTx = resumeTx;
