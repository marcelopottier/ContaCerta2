let categories = [];
let currentChart = null;

document.addEventListener('DOMContentLoaded', function() {
    if (!checkAuth()) return;

    // Mostrar nome do usuário
    displayUserInfo();

    // Carregar dados
    loadCategories();
    loadDashboard();

    // Configurar modais
    setupModals();

    // Configurar formulário de transação
    setupTransactionForm();
});

// Verificar autenticação
function checkAuth() {
    if (!isLoggedIn()) {
        window.location.href = '/login';
        return false;
    }
    return true;
}

// Mostrar informações do usuário
function displayUserInfo() {
    const user = getCurrentUser();
    const userName = document.getElementById('userName');
    if (userName && user.name) {
        userName.textContent = user.name;
    }
}

// Carregar dados do dashboard
async function loadDashboard() {
    try {
        showLoading();

        const response = await fetchWithAuth('/api/dashboard');
        if (!response) return;

        if (response.ok) {
            const data = await response.json();
            updateDashboard(data);
        } else {
            showError('Erro ao carregar dados do dashboard');
        }
    } catch (error) {
        showError('Erro de conexão');
    } finally {
        hideLoading();
    }
}

// Atualizar interface do dashboard
function updateDashboard(data) {
    // Atualizar totais
    const totalEntradas = document.getElementById('totalEntradas');
    const totalSaidas = document.getElementById('totalSaidas');
    const saldoAtual = document.getElementById('saldoAtual');

    if (totalEntradas) totalEntradas.textContent = formatCurrency(data.totalEntradas || 0);
    if (totalSaidas) totalSaidas.textContent = formatCurrency(data.totalSaidas || 0);
    if (saldoAtual) saldoAtual.textContent = formatCurrency(data.saldo || 0);

    // Atualizar gráfico
    updateChart(data);

    // Atualizar atividades recentes
    updateRecentTransactions(data.recentTransactions || []);

    // Mostrar conteúdo
    const loadingMessage = document.getElementById('loadingMessage');
    const dashboardContent = document.getElementById('dashboardContent');

    if (loadingMessage) loadingMessage.style.display = 'none';
    if (dashboardContent) dashboardContent.style.display = 'block';
}

// Atualizar gráfico
function updateChart(data) {
    const ctx = document.getElementById('categoryChart');
    if (!ctx) return;

    // Destruir gráfico anterior se existir
    if (currentChart) {
        currentChart.destroy();
    }

    const entradas = data.entradasPorCategoria || {};
    const saidas = data.saidasPorCategoria || {};

    // Combinar categorias
    const allCategories = new Set([...Object.keys(entradas), ...Object.keys(saidas)]);
    const categories = Array.from(allCategories);

    const entradasData = categories.map(cat => entradas[cat] || 0);
    const saidasData = categories.map(cat => saidas[cat] || 0);

    currentChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: categories,
            datasets: [{
                label: 'Entradas',
                data: entradasData,
                backgroundColor: '#4caf50',
                borderColor: '#388e3c',
                borderWidth: 1
            }, {
                label: 'Saídas',
                data: saidasData,
                backgroundColor: '#f44336',
                borderColor: '#d32f2f',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return 'R$ ' + value.toLocaleString('pt-BR', {minimumFractionDigits: 2});
                        }
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': R$ ' +
                                   context.parsed.y.toLocaleString('pt-BR', {minimumFractionDigits: 2});
                        }
                    }
                }
            }
        }
    });
}

// Atualizar transações recentes
function updateRecentTransactions(transactions) {
    const container = document.getElementById('recentTransactions');
    if (!container) return;

    if (transactions.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: #666;">Nenhuma transação encontrada</p>';
        return;
    }

    container.innerHTML = transactions.map(transaction => `
        <div class="activity-item">
            <div class="activity-info">
                <div class="activity-description">${transaction.description || 'Sem descrição'}</div>
                <div class="activity-category">${transaction.categoryName}</div>
                <div class="activity-date">${formatDate(transaction.date)}</div>
            </div>
            <div class="activity-value ${transaction.type.toLowerCase()}">
                ${transaction.type === 'ENTRADA' ? '+' : '-'} ${formatCurrency(transaction.value)}
            </div>
        </div>
    `).join('');
}

// Carregar categorias
async function loadCategories() {
    try {
        const response = await fetchWithAuth('/api/categories');
        if (!response) return;

        if (response.ok) {
            categories = await response.json();
            updateCategorySelect();
        }
    } catch (error) {
        console.error('Erro ao carregar categorias:', error);
    }
}

// Atualizar select de categorias
function updateCategorySelect() {
    const select = document.getElementById('transactionCategory');
    if (!select) return;

    select.innerHTML = '<option value="">Selecione uma categoria</option>';

    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id;
        option.textContent = category.name;
        select.appendChild(option);
    });
}

// Configurar modais
function setupModals() {
    // Fechar modal ao clicar fora
    window.onclick = function(event) {
        const modal = document.getElementById('transactionModal');
        if (event.target === modal) {
            closeTransactionModal();
        }
    }
}

// Abrir modal de transação
function openTransactionModal(type = 'ENTRADA') {
    const modal = document.getElementById('transactionModal');
    const typeSelect = document.getElementById('transactionType');
    const dateInput = document.getElementById('transactionDate');

    if (typeSelect) typeSelect.value = type;
    if (dateInput) dateInput.value = new Date().toISOString().split('T')[0];
    if (modal) modal.style.display = 'block';
}

// Fechar modal de transação
function closeTransactionModal() {
    const modal = document.getElementById('transactionModal');
    const form = document.getElementById('transactionForm');

    if (modal) modal.style.display = 'none';
    if (form) form.reset();
}

// Configurar formulário de transação
function setupTransactionForm() {
    const form = document.getElementById('transactionForm');
    if (!form) return;

    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        const formData = {
            type: document.getElementById('transactionType').value,
            value: parseFloat(document.getElementById('transactionValue').value),
            date: document.getElementById('transactionDate').value,
            categoryId: parseInt(document.getElementById('transactionCategory').value),
            description: document.getElementById('transactionDescription').value
        };

        try {
            const response = await fetchWithAuth('/api/transactions', {
                method: 'POST',
                body: JSON.stringify(formData)
            });

            if (response && response.ok) {
                closeTransactionModal();
                loadDashboard(); // Recarregar dashboard
                alert('Transação salva com sucesso!');
            } else {
                const error = await response.text();
                alert('Erro ao salvar transação: ' + error);
            }
        } catch (error) {
            alert('Erro de conexão');
        }
    });
}

// Mostrar erro
function showError(message) {
    const loadingMessage = document.getElementById('loadingMessage');
    if (loadingMessage) {
        loadingMessage.innerHTML = `<div class="error">${message}</div>`;
    }
}

// Navegação (placeholder)
function showDashboard() {
    // Implementar navegação para dashboard
}

function showTransactions() {
    alert('Funcionalidade em desenvolvimento');
}

function showCategories() {
    alert('Funcionalidade em desenvolvimento');
}

function showReports() {
    alert('Funcionalidade em desenvolvimento');
}