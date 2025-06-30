if (window.location.pathname === '/login') {
    document.addEventListener('DOMContentLoaded', function() {
        // Se já está logado, redirecionar
        if (FinanceApp.auth.isAuthenticated()) {
            window.location.href = '/dashboard';
            return;
        }

        setupLoginForm();
    });

    function setupLoginForm() {
        const loginForm = document.querySelector('form[action="/login"]');
        if (!loginForm) return;

        // Interceptar submit para usar API
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            // Validação básica
            if (!email || !password) {
                showMessage('Por favor, preencha todos os campos.', 'error');
                return;
            }

            const submitBtn = this.querySelector('button[type="submit"]');
            FinanceApp.ui.disableButton(submitBtn);

            try {
                const result = await FinanceApp.auth.login(email, password);

                if (result.success) {
                    showMessage('Login realizado com sucesso!', 'success');

                    // Redirecionar após 1 segundo
                    setTimeout(() => {
                        window.location.href = '/dashboard';
                    }, 1000);
                } else {
                    showMessage(result.error || 'Erro ao fazer login', 'error');
                }
            } catch (error) {
                showMessage('Erro de conexão. Tente novamente.', 'error');
            } finally {
                FinanceApp.ui.enableButton(submitBtn);
            }
        });

        // Auto-fill para desenvolvimento
        if (isDevelopmentMode()) {
            document.getElementById('email').value = 'teste@email.com';
            document.getElementById('password').value = '123456';
        }
    }
}
function showMessage(message, type) {
    // Remover mensagens anteriores
    const existingAlerts = document.querySelectorAll('.alert');
    existingAlerts.forEach(alert => alert.remove());

    // Criar nova mensagem
    const alertClass = type === 'error' ? 'alert-danger' : 'alert-success';
    const iconClass = type === 'error' ? 'bi-exclamation-triangle-fill' : 'bi-check-circle-fill';

    const alertHtml = `
        <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
            <i class="bi ${iconClass} me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;

    // Encontrar container
    const container = document.querySelector('.auth-card .card-body') || document.querySelector('.container');
    const form = container.querySelector('form');

    // Inserir antes do formulário
    form.insertAdjacentHTML('beforebegin', alertHtml);
}

function isDevelopmentMode() {
    return document.querySelector('.bg-dark') !== null ||
           window.location.hostname === 'localhost' ||
           window.location.hostname === '127.0.0.1';
}