// src/main/resources/static/js/common.js
// JavaScript compartilhado e utilitários financeiros

class FinanceUtils {
    // Verificar se o usuário está logado
    static isLoggedIn() {
        return localStorage.getItem('authToken') !== null;
    }

    // Obter dados do usuário logado
    static getCurrentUser() {
        return {
            token: localStorage.getItem('authToken'),
            email: localStorage.getItem('userEmail'),
            name: localStorage.getItem('userName')
        };
    }

    // Fazer logout
    static logout() {
        if (confirm('Deseja realmente sair?')) {
            localStorage.removeItem('authToken');
            localStorage.removeItem('userEmail');
            localStorage.removeItem('userName');
            window.location.href = '/login';
        }
    }

    // Fazer requisições autenticadas
    static async fetchWithAuth(url, options = {}) {
        const token = localStorage.getItem('authToken');
        if (!token) {
            console.warn('Token não encontrado, redirecionando para login');
            window.location.href = '/login';
            return null;
        }

        const headers = {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
            ...options.headers
        };

        try {
            const response = await fetch(url, {
                ...options,
                headers
            });

            if (response.status === 401) {
                console.warn('Token inválido, redirecionando para login');
                FinanceUtils.logout();
                return null;
            }

            return response;
        } catch (error) {
            console.error('Erro na requisição:', error);
            throw error;
        }
    }

    // Formatação de moeda
    static formatCurrency(value) {
        return new Intl.NumberFormat('pt-BR', {
            style: 'currency',
            currency: 'BRL'
        }).format(value);
    }

    // Formatação de data
    static formatDate(dateString) {
        return new Date(dateString + 'T00:00:00').toLocaleDateString('pt-BR');
    }

    // Formatação de data e hora
    static formatDateTime(dateString) {
        return new Date(dateString).toLocaleString('pt-BR');
    }

    // Validar email
    static validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    // Validar senha
    static validatePassword(password) {
        return password && password.length >= 6;
    }

    // Mostrar/esconder loading
    static showLoading(show = true) {
        const overlay = document.getElementById('overlay');
        const loading = document.getElementById('loading');
        
        if (overlay) overlay.style.display = show ? 'block' : 'none';
        if (loading) loading.style.display = show ? 'flex' : 'none';
    }

    // Mostrar mensagens
    static showMessage(elementId, message, isError = false) {
        const messageDiv = document.getElementById(elementId);
        if (messageDiv) {
            messageDiv.textContent = message;
            messageDiv.className = isError ? 'error-message' : 'success-message';
            messageDiv.style.display = 'block';
        }
    }

    // Esconder mensagem
    static hideMessage(elementId) {
        const messageDiv = document.getElementById(elementId);
        if (messageDiv) {
            messageDiv.style.display = 'none';
        }
    }

    // Debounce para otimizar requisições
    static debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // Copiar texto para clipboard
    static async copyToClipboard(text) {
        try {
            await navigator.clipboard.writeText(text);
            return true;
        } catch (err) {
            console.error('Erro ao copiar texto:', err);
            return false;
        }
    }

    // Gerar cores aleatórias para categorias
    static generateRandomColor() {
        const colors = [
            '#ff6b35', '#28a745', '#007bff', '#ffc107', 
            '#6f42c1', '#e83e8c', '#fd7e14', '#20c997',
            '#6c757d', '#343a40', '#17a2b8', '#dc3545'
        ];
        return colors[Math.floor(Math.random() * colors.length)];
    }

    // Validar valor monetário
    static validateCurrency(value) {
        const num = parseFloat(value);
        return !isNaN(num) && num > 0;
    }

    // Converter string para número monetário
    static parseCurrency(value) {
        if (typeof value === 'string') {
            // Remove símbolos de moeda e espaços
            return parseFloat(value.replace(/[R$\s.,]/g, '').replace(',', '.'));
        }
        return parseFloat(value);
    }

    // Calcular porcentagem
    static calculatePercentage(part, total) {
        if (total === 0) return 0;
        return ((part / total) * 100).toFixed(1);
    }

    // Obter primeiro e último dia do mês
    static getCurrentMonthRange() {
        const now = new Date();
        const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
        const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);
        
        return {
            start: firstDay.toISOString().split('T')[0],
            end: lastDay.toISOString().split('T')[0]
        };
    }

    // Agrupar transações por período
    static groupTransactionsByPeriod(transactions, period = 'month') {
        const groups = {};
        
        transactions.forEach(transaction => {
            const date = new Date(transaction.date);
            let key;
            
            switch (period) {
                case 'day':
                    key = date.toISOString().split('T')[0];
                    break;
                case 'week':
                    const week = FinanceUtils.getWeekNumber(date);
                    key = `${date.getFullYear()}-W${week}`;
                    break;
                case 'month':
                default:
                    key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
                    break;
            }
            
            if (!groups[key]) {
                groups[key] = [];
            }
            groups[key].push(transaction);
        });
        
        return groups;
    }

    // Obter número da semana
    static getWeekNumber(date) {
        const d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
        const dayNum = d.getUTCDay() || 7;
        d.setUTCDate(d.getUTCDate() + 4 - dayNum);
        const yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
        return Math.ceil((((d - yearStart) / 86400000) + 1) / 7);
    }

    // Verificar se é dispositivo móvel
    static isMobile() {
        return window.innerWidth <= 768;
    }

    // Scroll suave para elemento
    static scrollToElement(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    }

    // Adicionar classe com animação
    static animateElement(element, className, duration = 1000) {
        element.classList.add(className);
        setTimeout(() => {
            element.classList.remove(className);
        }, duration);
    }

    // Verificar conexão com internet
    static isOnline() {
        return navigator.onLine;
    }

    // Salvar dados no localStorage de forma segura
    static saveToLocalStorage(key, data) {
        try {
            localStorage.setItem(key, JSON.stringify(data));
            return true;
        } catch (error) {
            console.error('Erro ao salvar no localStorage:', error);
            return false;
        }
    }

    // Recuperar dados do localStorage
    static getFromLocalStorage(key, defaultValue = null) {
        try {
            const item = localStorage.getItem(key);
            return item ? JSON.parse(item) : defaultValue;
        } catch (error) {
            console.error('Erro ao recuperar do localStorage:', error);
            return defaultValue;
        }
    }

    // Limpar dados antigos do localStorage
    static clearExpiredData() {
        const keys = Object.keys(localStorage);
        keys.forEach(key => {
            if (key.startsWith('temp_') || key.startsWith('cache_')) {
                // Verificar se expirou (implementar lógica conforme necessário)
                localStorage.removeItem(key);
            }
        });
    }
}

// Compatibilidade com código anterior
window.FinanceUtils = FinanceUtils;

// Funções legacy para compatibilidade
function isLoggedIn() {
    return FinanceUtils.isLoggedIn();
}

function getCurrentUser() {
    return FinanceUtils.getCurrentUser();
}

function logout() {
    return FinanceUtils.logout();
}

function showMessage(elementId, message, isError = false) {
    return FinanceUtils.showMessage(elementId, message, isError);
}

function hideMessage(elementId) {
    return FinanceUtils.hideMessage(elementId);
}

function formatCurrency(value) {
    return FinanceUtils.formatCurrency(value);
}

function formatDate(dateString) {
    return FinanceUtils.formatDate(dateString);
}

async function fetchWithAuth(url, options = {}) {
    return await FinanceUtils.fetchWithAuth(url, options);
}

// Inicialização quando DOM carrega
document.addEventListener('DOMContentLoaded', function() {
    // Limpar dados expirados
    FinanceUtils.clearExpiredData();
    
    // Verificar se está online
    if (!FinanceUtils.isOnline()) {
        console.warn('Aplicação funcionando offline');
    }
    
    // Configurar eventos globais
    window.addEventListener('online', () => {
        console.log('Conexão restaurada');
    });
    
    window.addEventListener('offline', () => {
        console.warn('Conexão perdida');
    });
});

// Exportar para módulos se necessário
if (typeof module !== 'undefined' && module.exports) {
    module.exports = FinanceUtils;
}