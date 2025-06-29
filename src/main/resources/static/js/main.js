/**
 * FinanceControl - Main JavaScript
 * Common functionality for all pages
 */

// Global app object
window.FinanceApp = {
    // Configuration
    config: {
        apiBaseUrl: '/api',
        dateFormat: 'dd/MM/yyyy',
        currency: 'BRL',
        locale: 'pt-BR'
    },

    // Authentication utilities
    auth: {
        getToken: () => localStorage.getItem('authToken'),

        getUserData: () => {
            const userData = localStorage.getItem('userData');
            return userData ? JSON.parse(userData) : null;
        },

        setUserData: (data) => {
            localStorage.setItem('userData', JSON.stringify(data));
        },

        isAuthenticated: () => !!FinanceApp.auth.getToken(),

        logout: () => {
            if (confirm('Tem certeza que deseja sair?')) {
                localStorage.removeItem('authToken');
                localStorage.removeItem('userData');
                window.location.href = '/login';
            }
        }
    },

    // HTTP utilities
    http: {
        get: async (url) => {
            return FinanceApp.http.request('GET', url);
        },

        post: async (url, data) => {
            return FinanceApp.http.request('POST', url, data);
        },

        put: async (url, data) => {
            return FinanceApp.http.request('PUT', url, data);
        },

        delete: async (url) => {
            return FinanceApp.http.request('DELETE', url);
        },

        request: async (method, url, data = null) => {
            const options = {
                method,
                headers: {
                    'Content-Type': 'application/json'
                }
            };

            // Add auth token if available
            const token = FinanceApp.auth.getToken();
            if (token) {
                options.headers.Authorization = `Bearer ${token}`;
            }

            // Add body for POST/PUT requests
            if (data && (method === 'POST' || method === 'PUT')) {
                options.body = JSON.stringify(data);
            }

            try {
                const response = await fetch(url, options);

                // Handle 401 Unauthorized
                if (response.status === 401) {
                    FinanceApp.auth.logout();
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

        showAlert: (message, type = 'info', container = null) => {
            const alertContainer = container || document.querySelector('.container');
            if (!alertContainer) return;

            const alertId = 'alert-' + Date.now();
            const alertHtml = `
                <div id="${alertId}" class="alert alert-${type} alert-dismissible fade show" role="alert">
                    <i class="bi bi-${FinanceApp.ui.getAlertIcon(type)} me-2"></i>
                    ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            `;

            // Insert alert at the beginning of container
            alertContainer.insertAdjacentHTML('afterbegin', alertHtml);

            // Auto-dismiss after 5 seconds
            setTimeout(() => {
                const alert = document.getElementById(alertId);
                if (alert) {
                    const bsAlert = new bootstrap.Alert(alert);
                    bsAlert.close();
                }
            }, 5000);
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
            const modal = document.getElementById('confirmModal');
            if (!modal) return;

            // Set modal content
            modal.querySelector('.modal-title').innerHTML = `
                <i class="bi bi-exclamation-triangle text-warning me-2"></i>${title}
            `;
            modal.querySelector('#confirmModalMessage').textContent = message;

            // Set up confirm button
            const confirmBtn = modal.querySelector('#confirmModalButton');
            confirmBtn.onclick = () => {
                onConfirm();
                bootstrap.Modal.getInstance(modal).hide();
            };

            // Show modal
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
                date = new Date(date + 'T00:00:00'); // Add time to avoid timezone issues
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
        },

        setFieldError: (fieldName, message) => {
            const field = document.querySelector(`[name="${fieldName}"]`);
            if (field) {
                field.classList.add('is-invalid');

                // Remove existing feedback
                const existingFeedback = field.parentNode.querySelector('.invalid-feedback');
                if (existingFeedback) {
                    existingFeedback.remove();
                }

                // Add new feedback
                const feedback = document.createElement('div');
                feedback.className = 'invalid-feedback';
                feedback.textContent = message;
                field.parentNode.appendChild(feedback);
            }
        },

        clearErrors: (form) => {
            form.querySelectorAll('.is-invalid').forEach(field => {
                field.classList.remove('is-invalid');
            });
            form.querySelectorAll('.invalid-feedback').forEach(feedback => {
                feedback.remove();
            });
        }
    }
};

// Global functions for backward compatibility and template usage
window.logout = FinanceApp.auth.logout;
window.showConfirm = FinanceApp.ui.showConfirmModal;
window.formatCurrency = FinanceApp.format.currency;
window.formatDate = FinanceApp.format.date;

// Initialize app when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Initialize tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Initialize popovers
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });

    // Auto-dismiss alerts after 5 seconds
    document.querySelectorAll('.alert:not(.alert-permanent)').forEach(alert => {
        setTimeout(() => {
            if (alert.classList.contains('show')) {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }
        }, 5000);
    });

    // Handle form submissions with loading states
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function(e) {
            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn && !submitBtn.disabled) {
                FinanceApp.ui.disableButton(submitBtn);
            }
        });
    });

    // Fade in animations for cards
    document.querySelectorAll('.card, .alert').forEach((element, index) => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(20px)';

        setTimeout(() => {
            element.style.transition = 'all 0.3s ease';
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }, index * 100);
    });

    // Currency input formatting
    document.querySelectorAll('input[data-currency]').forEach(input => {
        input.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            value = (value / 100).toFixed(2);
            e.target.value = value;
        });
    });

    // Auto-resize textareas
    document.querySelectorAll('textarea[data-auto-resize]').forEach(textarea => {
        textarea.style.overflow = 'hidden';

        const resize = () => {
            textarea.style.height = 'auto';
            textarea.style.height = textarea.scrollHeight + 'px';
        };

        textarea.addEventListener('input', resize);
        resize(); // Initial resize
    });

    console.log('FinanceControl app initialized');
});

// Handle online/offline status
window.addEventListener('online', () => {
    FinanceApp.ui.showAlert('Conexão restaurada!', 'success');
});

window.addEventListener('offline', () => {
    FinanceApp.ui.showAlert('Você está offline. Algumas funcionalidades podem não estar disponíveis.', 'warning');
});

// Prevent form resubmission on page refresh
if (window.history.replaceState && window.history.state !== null) {
    window.history.replaceState(null, null, window.location.href);
}