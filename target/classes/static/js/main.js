/**
 * FinanceControl - Sistema de Autenticação JWT Unificado
 * Versão refatorada para segurança e consistência
 */

// Global app object
window.FinanceApp = {
    // Configuration
    config: {
        apiBaseUrl: '/api',
        dateFormat: 'dd/MM/yyyy',
        currency: 'BRL',
        locale: 'pt-BR',
        tokenKey: 'authToken'
    },

    // Authentication utilities
    auth: {
        // Obter token do localStorage OU cookie
        getToken: () => {
            // Primeiro tenta localStorage
            let token = localStorage.getItem(FinanceApp.config.tokenKey);

            // Se não tiver no localStorage, tenta cookie
            if (!token) {
                token = FinanceApp.auth.getCookieToken();
            }

            return token;
        },

        // Obter token do cookie
        getCookieToken: () => {
            const cookies = document.cookie.split(';');
            for (let cookie of cookies) {
                const [name, value] = cookie.trim().split('=');
                if (name === FinanceApp.config.tokenKey) {
                    return value;
                }
            }
            return null;
        },

        // Definir token (localStorage + cookie)
        setToken: (token) => {
            // Salvar no localStorage
            localStorage.setItem(FinanceApp.config.tokenKey, token);

            // Salvar no cookie (será sobrescrito pelo backend, mas garante consistência)
            document.cookie = `${FinanceApp.config.tokenKey}=${token}; path=/; max-age=86400; SameSite=Lax`;
        },

        // Obter dados do usuário do localStorage
        getUserData: () => {
            const userData = localStorage.getItem('userData');
            return userData ? JSON.parse(userData) : null;
        },

        // Definir dados do usuário
        setUserData: (data) => {
            localStorage.setItem('userData', JSON.stringify(data));
        },

        // Verificar se está autenticado
        isAuthenticated: () => {
            const token = FinanceApp.auth.getToken();
            if (!token) return false;

            // Verificar se o token não está expirado
            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                const now = Date.now() / 1000;
                return payload.exp > now;
            } catch (e) {
                return false;
            }
        },

        // Obter usuário atual
        getCurrentUser: () => {
            const token = FinanceApp.auth.getToken();
            if (!token) return null;

            try {
                const payload = JSON.parse(atob(token.split('.')[1]));
                return {
                    email: payload.sub,
                    name: FinanceApp.auth.getUserData()?.name || 'Usuário'
                };
            } catch (e) {
                return null;
            }
        },

        // Login programático
        login: async (email, password) => {
            try {
                const response = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ email, password })
                });

                if (response.ok) {
                    const data = await response.json();

                    // Salvar token e dados do usuário
                    FinanceApp.auth.setToken(data.token);
                    FinanceApp.auth.setUserData({
                        name: data.name,
                        email: data.email
                    });

                    return { success: true, data };
                } else {
                    const error = await response.text();
                    return { success: false, error };
                }
            } catch (error) {
                return { success: false, error: 'Erro de conexão' };
            }
        },

        // Logout
        logout: async () => {
            try {
                // Chamar API de logout
                await fetch('/api/auth/logout', {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${FinanceApp.auth.getToken()}`
                    }
                });
            } catch (e) {
                console.warn('Erro ao fazer logout no servidor:', e);
            } finally {
                // Limpar dados locais
                FinanceApp.auth.clearAuth();

                // Redirecionar para login
                window.location.href = '/login';
            }
        },

        // Limpar autenticação
        clearAuth: () => {
            localStorage.removeItem(FinanceApp.config.tokenKey);
            localStorage.removeItem('userData');

            // Limpar cookie
            document.cookie = `${FinanceApp.config.tokenKey}=; path=/; max-age=0`;
        },

        // Verificar autenticação e redirecionar se necessário
        requireAuth: () => {
            if (!FinanceApp.auth.isAuthenticated()) {
                window.location.href = '/login';
                return false;
            }
            return true;
        }
    },

    // HTTP utilities
    http: {
        get: async (url) => FinanceApp.http.request('GET', url),
        post: async (url, data) => FinanceApp.http.request('POST', url, data),
        put: async (url, data) => FinanceApp.http.request('PUT', url, data),
        delete: async (url) => FinanceApp.http.request('DELETE', url),

        request: async (method, url, data = null) => {
            const options = {
                method,
                headers: {
                    'Content-Type': 'application/json'
                }
            };

            // Adicionar token de autenticação
            const token = FinanceApp.auth.getToken();
            if (token) {
                options.headers.Authorization = `Bearer ${token}`;
            }

            // Adicionar body para POST/PUT
            if (data && (method === 'POST' || method === 'PUT')) {
                options.body = JSON.stringify(data);
            }

            try {
                const response = await fetch(url, options);

                // Handle 401 Unauthorized
                if (response.status === 401) {
                    FinanceApp.auth.clearAuth();

                    // Se não estiver na página de login, redirecionar
                    if (!window.location.pathname.includes('/login')) {
                        window.location.href = '/login';
                    }
                    return null;
                }

                return response;
            } catch (error) {
                console.error('HTTP Request failed:', error);
                FinanceApp.ui.showAlert('Erro de conexão. Tente novamente.', 'danger');
                return null;
            }
        }
    },

    // UI utilities
    ui: {
        showLoading: (show = true) => {
            const spinner = document.getElementById('loadingSpinner');
            if (spinner) {
                spinner.classList.toggle('d-none', !show);
            }
        },

        showAlert: (message, type = 'info', duration = 5000) => {
            // Criar alert dinamicamente
            const alertContainer = document.querySelector('.container') || document.body;
            const alertId = 'alert-' + Date.now();

            const alertHtml = `
                <div id="${alertId}" class="alert alert-${type} alert-dismissible fade show" role="alert">
                    <i class="bi bi-${FinanceApp.ui.getAlertIcon(type)} me-2"></i>
                    ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            `;

            // Inserir no topo do container
            alertContainer.insertAdjacentHTML('afterbegin', alertHtml);

            // Auto-dismiss
            if (duration > 0) {
                setTimeout(() => {
                    const alert = document.getElementById(alertId);
                    if (alert) {
                        const bsAlert = new bootstrap.Alert(alert);
                        bsAlert.close();
                    }
                }, duration);
            }
        },

        getAlertIcon: (type) => {
            const icons = {
                success: 'check-circle-fill',
                danger: 'exclamation-triangle-fill',
                warning: 'exclamation-triangle-fill',
                info: 'info-circle-fill'
            };
            return icons[type] || 'info-circle-fill';
        },

        showConfirmModal: (message, onConfirm, title = 'Confirmar ação') => {
            // Criar modal de confirmação dinamicamente
            const modalHtml = `
                <div class="modal fade" id="confirmModalDynamic" tabindex="-1" aria-hidden="true">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title">
                                    <i class="bi bi-exclamation-triangle text-warning me-2"></i>
                                    ${title}
                                </h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                            </div>
                            <div class="modal-body">
                                <p>${message}</p>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                                <button type="button" class="btn btn-danger" id="confirmButton">Confirmar</button>
                            </div>
                        </div>
                    </div>
                </div>
            `;

            // Remover modal anterior se existir
            const existingModal = document.getElementById('confirmModalDynamic');
            if (existingModal) {
                existingModal.remove();
            }

            // Adicionar ao body
            document.body.insertAdjacentHTML('beforeend', modalHtml);

            // Configurar eventos
            const modal = document.getElementById('confirmModalDynamic');
            const confirmBtn = modal.querySelector('#confirmButton');

            confirmBtn.onclick = () => {
                onConfirm();
                bootstrap.Modal.getInstance(modal).hide();
            };

            // Remover modal quando fechado
            modal.addEventListener('hidden.bs.modal', () => {
                modal.remove();
            });

            // Mostrar modal
            new bootstrap.Modal(modal).show();
        },

        disableButton: (button, loading = true) => {
            button.disabled = true;
            if (loading) {
                const originalText = button.innerHTML;
                button.dataset.originalText = originalText;
                button.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Carregando...';
            }
        },

        enableButton: (button) => {
            button.disabled = false;
            if (button.dataset.originalText) {
                button.innerHTML = button.dataset.originalText;
                delete button.dataset.originalText;
            }
        }
    },

    // Formatting utilities
    format: {
        currency: (value) => {
            return new Intl.NumberFormat(FinanceApp.config.locale, {
                style: 'currency',
                currency: FinanceApp.config.currency
            }).format(value);
        },

        date: (date) => {
            if (typeof date === 'string') {
                date = new Date(date + 'T00:00:00');
            }
            return new Intl.DateTimeFormat(FinanceApp.config.locale).format(date);
        },

        dateTime: (date) => {
            if (typeof date === 'string') {
                date = new Date(date);
            }
            return new Intl.DateTimeFormat(FinanceApp.config.locale, {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            }).format(date);
        },

        number: (value, decimals = 2) => {
            return new Intl.NumberFormat(FinanceApp.config.locale, {
                minimumFractionDigits: decimals,
                maximumFractionDigits: decimals
            }).format(value);
        }
    },

    // Form utilities
    form: {
        serialize: (form) => {
            const formData = new FormData(form);
            const data = {};
            for (let [key, value] of formData.entries()) {
                data[key] = value;
            }
            return data;
        },

        validate: (form) => {
            form.classList.add('was-validated');
            return form.checkValidity();
        },

        reset: (form) => {
            form.reset();
            form.classList.remove('was-validated');
        }
    }
};

// Funções globais para compatibilidade
window.logout = FinanceApp.auth.logout;
window.showConfirm = FinanceApp.ui.showConfirmModal;
window.formatCurrency = FinanceApp.format.currency;
window.formatDate = FinanceApp.format.date;

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('FinanceControl app initialized');

    // Verificar autenticação nas páginas protegidas
    const protectedPages = ['/dashboard', '/transactions', '/categories', '/profile'];
    const currentPage = window.location.pathname;

    if (protectedPages.includes(currentPage)) {
        if (!FinanceApp.auth.isAuthenticated()) {
            console.warn('Usuário não autenticado, redirecionando para login');
            window.location.href = '/login';
            return;
        }
    }

    // Verificar se já está logado nas páginas de login/registro
    if (['/login', '/register'].includes(currentPage)) {
        if (FinanceApp.auth.isAuthenticated()) {
            console.log('Usuário já autenticado, redirecionando para dashboard');
            window.location.href = '/dashboard';
            return;
        }
    }

    // Inicializar tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Inicializar popovers
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });

    // Configurar formulários para mostrar loading
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function(e) {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn && !submitBtn.disabled && form.checkValidity()) {
                FinanceApp.ui.disableButton(submitBtn);
            }
        });
    });

    // Animações de entrada
    document.querySelectorAll('.card, .alert').forEach((element, index) => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(20px)';

        setTimeout(() => {
            element.style.transition = 'all 0.3s ease';
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }, index * 100);
    });

    // Formatação de inputs de moeda
    document.querySelectorAll('input[data-currency]').forEach(input => {
        input.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            value = (value / 100).toFixed(2);
            e.target.value = value;
        });
    });

    // Auto-resize para textareas
    document.querySelectorAll('textarea[data-auto-resize]').forEach(textarea => {
        textarea.style.overflow = 'hidden';

        const resize = () => {
            textarea.style.height = 'auto';
            textarea.style.height = textarea.scrollHeight + 'px';
        };

        textarea.addEventListener('input', resize);
        resize();
    });
});

// Handle online/offline status
window.addEventListener('online', () => {
    FinanceApp.ui.showAlert('Conexão restaurada!', 'success');
});

window.addEventListener('offline', () => {
    FinanceApp.ui.showAlert('Você está offline. Algumas funcionalidades podem não estar disponíveis.', 'warning');
});

// Interceptar refreshs para prevenir resubmissões
if (window.history.replaceState && window.history.state !== null) {
    window.history.replaceState(null, null, window.location.href);
}

document.addEventListener('DOMContentLoaded', function() {
    // Animações de entrada para formulários
    const authCard = document.querySelector('.auth-card');
    if (authCard) {
        authCard.style.opacity = '0';
        authCard.style.transform = 'translateY(30px)';

        setTimeout(() => {
            authCard.style.transition = 'all 0.6s ease';
            authCard.style.opacity = '1';
            authCard.style.transform = 'translateY(0)';
        }, 100);
    }

    // Animações de foco nos inputs
    document.querySelectorAll('.form-floating input').forEach(input => {
        input.addEventListener('focus', function() {
            this.parentElement.classList.add('focused');
        });

        input.addEventListener('blur', function() {
            if (!this.value) {
                this.parentElement.classList.remove('focused');
            }
        });
    });

    // Enter key para submeter formulário
    document.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            const form = document.querySelector('form');
            if (form) {
                const submitBtn = form.querySelector('button[type="submit"]');
                if (submitBtn && !submitBtn.disabled) {
                    form.dispatchEvent(new Event('submit'));
                }
            }
        }
    });
});