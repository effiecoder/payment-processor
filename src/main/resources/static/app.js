// Payment Processor - Frontend JavaScript

const API_BASE = '/api';
let token = localStorage.getItem('token');
let currentPage = 0;
let currentFilter = '';
let currentViewMode = 'view';
let currentTransactionId = null;

// DOM Elements
const screens = {
    login: document.getElementById('login-screen'),
    main: document.getElementById('main-screen')
};

const detailPanel = {
    panel: document.getElementById('detail-panel'),
    empty: document.getElementById('detail-empty'),
    content: document.getElementById('detail-content'),
    title: document.getElementById('detail-title'),
    closeBtn: document.getElementById('close-detail'),
    form: document.getElementById('transaction-form'),
    formActions: document.getElementById('form-actions'),
    statusSection: document.getElementById('status-section')
};

const transactionsPanel = document.getElementById('transactions-panel');

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM loaded, initializing...');
    if (token) {
        console.log('Token found, showing main screen');
        showMainScreen();
        loadTransactions();
        loadStatusCounts();
    } else {
        console.log('No token, showing login');
        showLoginScreen();
    }
    setupEventListeners();
    console.log('Event listeners set up');
});

function setupEventListeners() {
    console.log('Setting up event listeners...');
    // Login
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
    const newBtn = document.getElementById('new-transaction-btn');
    console.log('New transaction button:', newBtn);
    newBtn.addEventListener('click', () => {
        console.log('New transaction button clicked');
        showCreateForm();
    });
    
    // Close detail
    detailPanel.closeBtn.addEventListener('click', closeDetail);
    
    // Form submit
    detailPanel.form.addEventListener('submit', handleFormSubmit);
    
    console.log('Event listeners set up complete');
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
        
        if (!response.ok) throw new Error('Nieprawidłowa nazwa użytkownika lub hasło');
        
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
    const headers = { 'Content-Type': 'application/json', ...options.headers };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    
    const response = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
    
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
function closeDetail() {
    detailPanel.panel.classList.remove('open');
    transactionsPanel.classList.remove('has-detail');
    currentTransactionId = null;
}

function openDetail() {
    console.log('openDetail called', detailPanel.panel, transactionsPanel);
    console.log('panel classList:', detailPanel.panel.classList);
    console.log('panel style:', detailPanel.panel.style.cssText);
    console.log('content classList:', detailPanel.content.classList);
    detailPanel.panel.classList.add('open');
    transactionsPanel.classList.add('has-detail');
    console.log('after add - panel classList:', detailPanel.panel.classList);
    console.log('after add - content classList:', detailPanel.content.classList);
    console.log('empty hidden:', detailPanel.empty.classList.contains('hidden'));
    console.log('content hidden:', detailPanel.content.classList.contains('hidden'));
}

// Form Modes
function showCreateForm() {
    console.log('showCreateForm called');
    currentViewMode = 'create';
    currentTransactionId = null;
    
    detailPanel.form.reset();
    detailPanel.form.querySelectorAll('input, select, textarea').forEach(el => {
        el.removeAttribute('readonly');
        el.removeAttribute('disabled');
    });
    
    // Default date
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    detailPanel.form.valueDate.value = tomorrow.toISOString().split('T')[0];
    
    detailPanel.title.textContent = '➕ Nowa transakcja ISO20022';
    detailPanel.statusSection.style.display = 'none';
    detailPanel.formActions.innerHTML = `<button type="submit" class="btn btn-primary">Utwórz</button>`;
    
    detailPanel.empty.classList.add('hidden');
    detailPanel.content.classList.remove('hidden');
    openDetail();
}

async function showViewForm(id) {
    try {
        const response = await apiCall(`/transactions/${id}`);
        const tx = await response.json();
        
        currentViewMode = 'view';
        currentTransactionId = id;
        
        fillFormWithTransaction(tx);
        
        detailPanel.form.querySelectorAll('input, select, textarea').forEach(el => {
            el.setAttribute('readonly', true);
            el.setAttribute('disabled', true);
        });
        
        detailPanel.title.textContent = `📋 Transakcja #${id}`;
        
        let actionsHtml = '';
        if (tx.status === 'PENDING_APPROVAL') {
            actionsHtml = `
                <button type="button" class="btn btn-success" onclick="approveTransaction(${id})">✓ Zatwierdź</button>
                <button type="button" class="btn btn-danger" onclick="rejectTransaction(${id})">✕ Odrzuć</button>
                <button type="button" class="btn" onclick="showEditForm(${id})">✏️ Edytuj</button>
                <button type="button" class="btn" onclick="suspendTransaction(${id})">⏸ Wstrzymaj</button>
            `;
        } else if (tx.status === 'SUSPENDED') {
            actionsHtml = `<button type="button" class="btn btn-primary" onclick="resumeTransaction(${id})">▶️ Wznów</button>`;
        } else if (tx.status === 'AUTHORIZED' || tx.status === 'APPROVED') {
            actionsHtml = `<button type="button" class="btn" onclick="suspendTransaction(${id})">⏸ Wstrzymaj</button>`;
        }
        
        detailPanel.formActions.innerHTML = actionsHtml;
        detailPanel.statusSection.style.display = 'block';
        
        detailPanel.empty.classList.add('hidden');
        detailPanel.content.classList.remove('hidden');
        openDetail();
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
        
        fillFormWithTransaction(tx);
        
        detailPanel.form.querySelectorAll('input, select, textarea').forEach(el => {
            const name = el.name;
            if (['messageId', 'paymentInstructionId', 'creationDateTime', 'status', 'createdAt', 'authorizedBy', 'approvedBy', 'rejectionReason'].includes(name)) {
                el.setAttribute('readonly', true);
                el.setAttribute('disabled', true);
            } else {
                el.removeAttribute('readonly');
                el.removeAttribute('disabled');
            }
        });
        
        detailPanel.title.textContent = `✏️ Edycja #${id}`;
        detailPanel.formActions.innerHTML = `<button type="submit" class="btn btn-primary">Zapisz</button>`;
        detailPanel.statusSection.style.display = 'block';
        
        detailPanel.empty.classList.add('hidden');
        detailPanel.content.classList.remove('hidden');
        openDetail();
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

function fillFormWithTransaction(tx) {
    const form = detailPanel.form;
    const setValue = (name, value) => {
        const el = form.querySelector(`[name="${name}"]`);
        if (el) {
            if (el.type === 'datetime-local' && value) el.value = new Date(value).toISOString().slice(0, 16);
            else el.value = value || '';
        }
    };
    
    setValue('messageId', tx.messageId);
    setValue('paymentInstructionId', tx.paymentInstructionId);
    setValue('creationDateTime', tx.creationDateTime);
    setValue('paymentMethod', tx.paymentMethod);
    setValue('amount', tx.amount);
    setValue('currency', tx.currency);
    setValue('valueDate', tx.valueDate);
    setValue('requestedExecutionDate', tx.requestedExecutionDate);
    setValue('chargeBearer', tx.chargeBearer);
    setValue('debtorName', tx.debtorName);
    setValue('debtorLegalName', tx.debtorLegalName);
    setValue('debtorAccountIban', tx.debtorAccountIban);
    setValue('debtorAgentBic', tx.debtorAgentBic);
    setValue('creditorName', tx.creditorName);
    setValue('creditorLegalName', tx.creditorLegalName);
    setValue('creditorAccountIban', tx.creditorAccountIban);
    setValue('creditorAgentBic', tx.creditorAgentBic);
    setValue('remittanceUnstructured', tx.remittanceUnstructured);
    setValue('remittanceReference', tx.remittanceReference);
    setValue('remittanceStructuredType', tx.remittanceStructuredType);
    setValue('purposeCode', tx.purposeCode);
    setValue('transactionId', tx.transactionId);
    setValue('status', formatStatus(tx.status));
    setValue('createdAt', tx.createdAt);
    setValue('authorizedBy', tx.authorizedBy);
    setValue('approvedBy', tx.approvedBy);
    setValue('rejectionReason', tx.rejectionReason);
    
    document.getElementById('rejection-row').style.display = tx.rejectionReason ? 'flex' : 'none';
}

async function handleFormSubmit(e) {
    e.preventDefault();
    if (currentViewMode === 'create') await createTransaction();
    else if (currentViewMode === 'edit') await updateTransaction();
}

async function createTransaction() {
    const formData = new FormData(detailPanel.form);
    const data = {};
    formData.forEach((v, k) => { if (v) data[k] = v; });
    data.messageType = 'pain.001';
    data.paymentMethod = data.paymentMethod || 'TRN';
    data.chargeBearer = data.chargeBearer || 'SLEV';
    
    try {
        const response = await apiCall('/transactions', { method: 'POST', body: JSON.stringify(data) });
        if (response.ok) {
            const tx = await response.json();
            loadTransactions();
            loadStatusCounts();
            showViewForm(tx.id);
        } else {
            const err = await response.json();
            alert('Błąd: ' + (err.error || 'Nie udało się utworzyć'));
        }
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

async function updateTransaction() {
    const formData = new FormData(detailPanel.form);
    const data = {};
    formData.forEach((v, k) => { if (v) data[k] = v; });
    
    try {
        const response = await apiCall(`/transactions/${currentTransactionId}`, {
            method: 'PATCH',
            body: JSON.stringify({ amount: data.amount, valueDate: data.valueDate, paymentTitle: data.remittanceUnstructured })
        });
        if (response.ok) {
            loadTransactions();
            loadStatusCounts();
            showViewForm(currentTransactionId);
        } else {
            const err = await response.json();
            alert('Błąd: ' + (err.error || 'Nie udało się zapisać'));
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
            const err = await response.json();
            alert('Błąd: ' + (err.error || 'Nie udało się zatwierdzić'));
        }
    } catch (error) {
        alert('Błąd: ' + error.message);
    }
}

async function rejectTransaction(id) {
    const reason = prompt('Powód odrzucenia:');
    if (!reason) return;
    try {
        const response = await apiCall(`/transactions/${id}/reject`, { method: 'POST', body: JSON.stringify({ reason }) });
        if (response.ok) {
            loadTransactions();
            loadStatusCounts();
            showViewForm(id);
        } else {
            const err = await response.json();
            alert('Błąd: ' + (err.error || 'Nie udało się odrzucić'));
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
            const err = await response.json();
            alert('Błąd: ' + (err.error || 'Nie udało się wstrzymać'));
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
            const err = await response.json();
            alert('Błąd: ' + (err.error || 'Nie udało się wznowić'));
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
        if (currentFilter) url = `/transactions/status/${currentFilter}`;
        
        const response = await apiCall(url);
        const transactions = await response.json();
        
        if (!transactions?.length) {
            listEl.innerHTML = '<div class="empty-state"><p>Brak transakcji</p></div>';
            return;
        }
        
        listEl.innerHTML = transactions.map(tx => `
            <div class="transaction-item ${currentTransactionId === tx.id ? 'active' : ''}" data-id="${tx.id}">
                <div class="tx-id">#${tx.id}</div>
                <div class="tx-parties">
                    <div class="party-row">
                        <span class="party-name">${escapeHtml(tx.debtorName || tx.senderName || '-')}</span>
                        <span class="party-iban">${formatIban(tx.debtorAccountIban || tx.senderAccount)}</span>
                    </div>
                    <div class="party-row">
                        <span class="party-name">→ ${escapeHtml(tx.creditorName || tx.receiverName || '-')}</span>
                        <span class="party-iban">${formatIban(tx.creditorAccountIban || tx.receiverAccount)}</span>
                    </div>
                </div>
                <div class="tx-amount">${formatAmount(tx.amount)} ${tx.currency}</div>
                <div class="tx-status status-${tx.status}">${formatStatusShort(tx.status)}</div>
            </div>
        `).join('');
        
        // Add click handlers
        listEl.querySelectorAll('.transaction-item').forEach(item => {
            item.addEventListener('click', () => {
                const id = parseInt(item.dataset.id);
                showViewForm(id);
            });
        });
        
        document.getElementById('page-info').textContent = `Strona ${currentPage + 1}`;
    } catch (error) {
        listEl.innerHTML = `<div class="empty-state"><p style="color:var(--accent-danger)">${error.message}</p></div>`;
    }
}

// Status Flow
async function loadStatusCounts() {
    try {
        const response = await apiCall('/transactions/status-counts');
        const counts = await response.json();
        renderStatusFlow(counts);
    } catch (e) { console.error(e); }
}

function renderStatusFlow(counts) {
    const container = document.getElementById('status-flow-diagram');
    const main = [
        {key:'RECEIVED', label:'Odebrane'},
        {key:'VALIDATED', label:'Zwalidowane'},
        {key:'AUTHORIZING', label:'Autoryzacja'},
        {key:'AUTHORIZED', label:'Autoryzowane'},
        {key:'PENDING_APPROVAL', label:'Oczekuje'},
        {key:'APPROVED', label:'Zatwierdzone'},
        {key:'SENT_TO_CLEARING', label:'Wysłane'},
        {key:'COMPLETED', label:'Zakończone'}
    ];
    const sec = [
        {key:'VALIDATION_FAILED', label:'Błąd wal.'},
        {key:'AUTHORIZATION_FAILED', label:'Błąd aut.'},
        {key:'REJECTED', label:'Odrzucone'},
        {key:'SUSPENDED', label:'Wstrzymane'},
        {key:'FAILED', label:'Failed'}
    ];
    
    let html = '';
    main.forEach(s => {
        html += `<div class="status-box ${currentFilter===s.key?'active':''}" data-status="${s.key}" onclick="filterByStatus('${s.key}')"><span class="status-name">${s.label}</span><span class="status-count">${counts[s.key]||0}</span></div>`;
    });
    html += '<div style="width:100%;height:8px;"></div>';
    sec.forEach(s => {
        html += `<div class="status-box ${currentFilter===s.key?'active':''}" data-status="${s.key}" onclick="filterByStatus('${s.key}')"><span class="status-name">${s.label}</span><span class="status-count">${counts[s.key]||0}</span></div>`;
    });
    container.innerHTML = html;
}

function filterByStatus(status) {
    currentFilter = currentFilter === status ? '' : status;
    document.getElementById('status-filter').value = currentFilter;
    currentPage = 0;
    loadTransactions();
    loadStatusCounts();
}

// Helpers
function formatStatus(s) {
    const m = {RECEIVED:'Odebrane',VALIDATED:'Zwalidowane',AUTHORIZING:'Autoryzacja',AUTHORIZED:'Autoryzowane',PENDING_APPROVAL:'Oczekuje',APPROVED:'Zatwierdzone',REJECTED:'Odrzucone',SUSPENDED:'Wstrzymane',SENT_TO_CLEARING:'Wysłane',COMPLETED:'Zakończone',FAILED:'Failed',VALIDATION_FAILED:'Błąd wal.',AUTHORIZATION_FAILED:'Błąd aut.'};
    return m[s] || s;
}

function formatStatusShort(s) {
    const m = {RECEIVED:'Odebrane',VALIDATED:'OK',AUTHORIZING:'Autor.',AUTHORIZED:'OK',PENDING_APPROVAL:'Oczekuje',APPROVED:'OK',REJECTED:'Odrzucone',SUSPENDED:'Wstrzymane',SENT_TO_CLEARING:'Wysłane',COMPLETED:'OK',FAILED:'Fail',VALIDATION_FAILED:'Err',AUTHORIZATION_FAILED:'Err'};
    return m[s] || s;
}

function formatAmount(a) { return new Intl.NumberFormat('pl-PL',{minimumFractionDigits:2,maximumFractionDigits:2}).format(a); }
function formatIban(iban) { if(!iban)return'-'; return iban.replace(/\s/g,'').match(/.{1,4}/g)?.join(' ')||iban; }
function escapeHtml(t) { if(!t)return''; const d=document.createElement('div'); d.textContent=t; return d.innerHTML; }

// Global
window.showViewForm = showViewForm;
window.showEditForm = showEditForm;
window.showCreateForm = showCreateForm;
window.approveTransaction = approveTransaction;
window.rejectTransaction = rejectTransaction;
window.suspendTransaction = suspendTransaction;
window.resumeTransaction = resumeTransaction;
window.filterByStatus = filterByStatus;
