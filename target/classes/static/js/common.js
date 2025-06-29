const FinanceUtils = {
    // Formatar moeda brasileira
    formatCurrency: function(value) {
        return new Intl.NumberFormat('pt-BR', {
            style: 'currency',
            currency: 'BRL'
        }).format(value);
    },

    // Formatar data brasileira
    formatDate: function(dateString) {
        return new Date(dateString + 'T00:00:00').toLocaleDateString('pt-BR');
    },

    // Obter informações do usuário armazenadas
    getCurrentUser: function() {
        return {
            email: localStorage.getItem('userEmail') || '',
            name: localStorage.getItem('userName') || ''
        };
    },

    // Verificar se existe token no armazenamento
    isLoggedIn: function() {
        return Boolean(localStorage.getItem('authToken'));
    },

    // Verificar autenticação
    checkAuth: function() {
        const token = localStorage.getItem('authToken');
        if (!token) {
            window.location.href = '/login';
            return false;
        }
        return token;
    },

    // Fazer requisições autenticadas
    fetchWithAuth: async function(url, options = {}) {
        const token = this.checkAuth();
        if (!token) return null;

        const headers = {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
            ...options.headers
        };

        const response = await fetch(url, {
            ...options,
            headers
        });

        if (response.status === 401) {
            this.logout();
            return null;
        }

        return response;
    },

    // Logout
    logout: function() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userEmail');
        localStorage.removeItem('userName');
        window.location.href = '/login';
    },

    // Mostrar mensagem de erro
    showError: function(elementId, message) {
        const errorDiv = document.getElementById(elementId);
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
        }
    },

    // Mostrar mensagem de sucesso
    showSuccess: function(elementId, message) {
        const successDiv = document.getElementById(elementId);
        if (successDiv) {
            successDiv.textContent = message;
            successDiv.style.display = 'block';
        }
    },

    // Mostrar ou esconder mensagens genéricas
    showMessage: function(elementId, message, isError = false) {
        const el = document.getElementById(elementId);
        if (el) {
            el.textContent = message;
            el.style.display = 'block';
            if (isError) {
                el.classList.add('error-message');
                el.classList.remove('success-message');
            } else {
                el.classList.add('success-message');
                el.classList.remove('error-message');
            }
        }
    },

    hideMessage: function(elementId) {
        const el = document.getElementById(elementId);
        if (el) {
            el.style.display = 'none';
        }
    },

    // Exibir e ocultar indicador de carregamento
    showLoading: function() {
        const overlay = document.getElementById('overlay');
        const loading = document.getElementById('loading');
        if (overlay) overlay.style.display = 'block';
        if (loading) loading.style.display = 'block';
    },

    hideLoading: function() {
        const overlay = document.getElementById('overlay');
        const loading = document.getElementById('loading');
        if (overlay) overlay.style.display = 'none';
        if (loading) loading.style.display = 'none';
    },

    // Limpar mensagens
    clearMessages: function() {
        const errorDiv = document.getElementById('errorMessage');
        const successDiv = document.getElementById('successMessage');

        if (errorDiv) errorDiv.style.display = 'none';
        if (successDiv) successDiv.style.display = 'none';
    }
};