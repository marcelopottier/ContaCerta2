/**
 * ARQUIVO: src/main/resources/static/js/auth-pages.js
 *
 * Este arquivo deve ser incluído DEPOIS do main.js nas páginas de login e register
 * Adicione esta linha nos templates:
 * <script th:src="@{/js/auth-pages.js}"></script>
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('Auth pages JavaScript carregado');

    // Detectar página atual
    const currentPath = window.location.pathname;

    if (currentPath === '/login') {
        setupLoginPage();
    } else if (currentPath === '/register') {
        setupRegisterPage();
    }
});

function setupLoginPage() {
    console.log('Configurando página de login');

    // Verificar se já está logado
    if (FinanceApp.auth.isAuthenticated()) {
        console.log('Usuário já autenticado, redirecionando...');
        window.location.href = '/dashboard';
        return;
    }

    // Interceptar submit do formulário
    const loginForm = document.querySelector('form[action="/login"]');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLoginSubmit);
    }

    // Auto-fill para desenvolvimento
    if (isDevelopmentMode()) {
        setTimeout(() => {
            const emailInput = document.getElementById('email');
            const passwordInput = document.getElementById('password');

            if (emailInput && passwordInput) {
                emailInput.value = 'teste@email.com';
                passwordInput.value = '123456';
                console.log('Campos preenchidos automaticamente para desenvolvimento');
            }
        }, 500);
    }
}

function setupRegisterPage() {
    console.log('Configurando página de register');

    // Verificar se já está logado
    if (FinanceApp.auth.isAuthenticated()) {
        console.log('Usuário já autenticado, redirecionando...');
        window.location.href = '/dashboard';
        return;
    }

    // Interceptar submit do formulário
    const registerForm = document.querySelector('form[action="/register"]');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegisterSubmit);
    }

    // Configurar validações
    setupPasswordValidation();
}

async function handleLoginSubmit(e) {
    e.preventDefault();

    const form = e.target;
    const email = form.querySelector('#email').value;
    const password = form.querySelector('#password').value;

    // Validação básica
    if (!email || !password) {
        showAuthMessage('Por favor, preencha todos os campos.', 'error');
        return;
    }

    const submitBtn = form.querySelector('button[type="submit"]');
    FinanceApp.ui.disableButton(submitBtn);

    console.log('Tentando fazer login...');

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
            console.log('Login bem-sucedido:', data);

            // Salvar token e dados do usuário
            FinanceApp.auth.setToken(data.token);
            FinanceApp.auth.setUserData({
                name: data.name,
                email: data.email
            });

            showAuthMessage('Login realizado com sucesso!', 'success');

            // Redirecionar após 1 segundo
            setTimeout(() => {
                window.location.href = '/dashboard';
            }, 1000);
        } else {
            const errorData = await response.json();
            console.error('Erro no login:', errorData);
            showAuthMessage(errorData.error || 'Erro ao fazer login', 'error');
        }
    } catch (error) {
        console.error('Erro de conexão:', error);
        showAuthMessage('Erro de conexão. Tente novamente.', 'error');
    } finally {
        FinanceApp.ui.enableButton(submitBtn);
    }
}

async function handleRegisterSubmit(e) {
    e.preventDefault();

    const form = e.target;
    const name = form.querySelector('#name').value;
    const email = form.querySelector('#email').value;
    const password = form.querySelector('#password').value;
    const confirmPassword = form.querySelector('#confirmPassword').value;
    const termsAccepted = form.querySelector('#terms')?.checked;

    // Validações
    if (!name || !email || !password || !confirmPassword) {
        showAuthMessage('Por favor, preencha todos os campos.', 'error');
        return;
    }

    if (password !== confirmPassword) {
        showAuthMessage('As senhas não coincidem.', 'error');
        return;
    }

    if (password.length < 6) {
        showAuthMessage('A senha deve ter pelo menos 6 caracteres.', 'error');
        return;
    }

    if (termsAccepted === false) {
        showAuthMessage('Você deve concordar com os termos de uso.', 'error');
        return;
    }

    const submitBtn = form.querySelector('button[type="submit"]');
    FinanceApp.ui.disableButton(submitBtn);

    console.log('Tentando fazer cadastro...');

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ name, email, password })
        });

        if (response.ok) {
            const data = await response.json();
            console.log('Cadastro bem-sucedido:', data);

            // Salvar token e dados do usuário
            FinanceApp.auth.setToken(data.token);
            FinanceApp.auth.setUserData({
                name: data.name,
                email: data.email
            });

            showAuthMessage('Cadastro realizado com sucesso!', 'success');

            // Redirecionar após 1 segundo
            setTimeout(() => {
                window.location.href = '/dashboard';
            }, 1000);
        } else {
            const errorData = await response.json();
            console.error('Erro no cadastro:', errorData);
            showAuthMessage(errorData.error || 'Erro ao fazer cadastro', 'error');
        }
    } catch (error) {
        console.error('Erro de conexão:', error);
        showAuthMessage('Erro de conexão. Tente novamente.', 'error');
    } finally {
        FinanceApp.ui.enableButton(submitBtn);
    }
}

function setupPasswordValidation() {
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const strengthIndicator = document.getElementById('passwordStrength');

    if (passwordInput && strengthIndicator) {
        passwordInput.addEventListener('input', function() {
            const password = this.value;
            updatePasswordStrength(password, strengthIndicator);
        });
    }

    if (confirmPasswordInput && passwordInput) {
        confirmPasswordInput.addEventListener('input', function() {
            validatePasswordMatch(passwordInput.value, this.value, this);
        });

        passwordInput.addEventListener('input', function() {
            if (confirmPasswordInput.value) {
                validatePasswordMatch(this.value, confirmPasswordInput.value, confirmPasswordInput);
            }
        });
    }
}

function updatePasswordStrength(password, indicator) {
    if (!indicator) return;

    indicator.className = 'password-strength';
    indicator.textContent = '';

    if (password.length === 0) return;

    const strength = calculatePasswordStrength(password);

    if (strength.score < 2) {
        indicator.classList.add('strength-weak');
        indicator.textContent = 'Senha fraca';
    } else if (strength.score < 4) {
        indicator.classList.add('strength-medium');
        indicator.textContent = 'Senha razoável';
    } else {
        indicator.classList.add('strength-strong');
        indicator.textContent = 'Senha forte';
    }
}

function calculatePasswordStrength(password) {
    let score = 0;

    if (password.length >= 8) score++;
    if (password.length >= 12) score++;
    if (/[a-z]/.test(password)) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/\d/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;

    return { score };
}

function validatePasswordMatch(password, confirmPassword, confirmInput) {
    if (confirmPassword && password !== confirmPassword) {
        confirmInput.setCustomValidity('As senhas não coincidem');
        confirmInput.classList.add('is-invalid');
    } else {
        confirmInput.setCustomValidity('');
        confirmInput.classList.remove('is-invalid');
    }
}

function showAuthMessage(message, type) {
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
    const authCard = document.querySelector('.auth-card .card-body');
    const form = authCard.querySelector('form');

    // Inserir antes do formulário
    if (form) {
        form.insertAdjacentHTML('beforebegin', alertHtml);
    }
}

function isDevelopmentMode() {
    return document.querySelector('.bg-dark') !== null ||
           window.location.hostname === 'localhost' ||
           window.location.hostname === '127.0.0.1' ||
           window.location.port === '8080';
}