/**
 * JavaScript para páginas de autenticação (login/register)
 * Funciona com o sistema JWT unificado
 */

// ========== PÁGINA DE LOGIN ==========
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

// ========== PÁGINA DE REGISTER ==========
if (window.location.pathname === '/register') {
    document.addEventListener('DOMContentLoaded', function() {
        // Se já está logado, redirecionar
        if (FinanceApp.auth.isAuthenticated()) {
            window.location.href = '/dashboard';
            return;
        }

        setupRegisterForm();
    });

    function setupRegisterForm() {
        const registerForm = document.querySelector('form[action="/register"]');
        if (!registerForm) return;

        // Interceptar submit para usar API
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const name = document.getElementById('name').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            // Validações
            if (!name || !email || !password || !confirmPassword) {
                showMessage('Por favor, preencha todos os campos.', 'error');
                return;
            }

            if (password !== confirmPassword) {
                showMessage('As senhas não coincidem.', 'error');
                return;
            }

            if (password.length < 6) {
                showMessage('A senha deve ter pelo menos 6 caracteres.', 'error');
                return;
            }

            if (!document.getElementById('terms').checked) {
                showMessage('Você deve concordar com os termos de uso.', 'error');
                return;
            }

            const submitBtn = this.querySelector('button[type="submit"]');
            FinanceApp.ui.disableButton(submitBtn);

            try {
                const response = await FinanceApp.http.post('/api/auth/register', {
                    name,
                    email,
                    password
                });

                if (response && response.ok) {
                    const data = await response.json();
                    
                    // Salvar token e dados
                    FinanceApp.auth.setToken(data.token);
                    FinanceApp.auth.setUserData({
                        name: data.name,
                        email: data.email
                    });

                    showMessage('Cadastro realizado com sucesso!', 'success');
                    
                    // Redirecionar após 1 segundo
                    setTimeout(() => {
                        window.location.href = '/dashboard';
                    }, 1000);
                } else {
                    const errorData = await response.json();
                    showMessage(errorData.error || 'Erro ao fazer cadastro', 'error');
                }
            } catch (error) {
                showMessage('Erro de conexão. Tente novamente.', 'error');
            } finally {
                FinanceApp.ui.enableButton(submitBtn);
            }
        });

        // Validação de força da senha
        setupPasswordStrength();
    }

    function setupPasswordStrength() {
        const passwordInput = document.getElementById('password');
        const strengthIndicator = document.getElementById('passwordStrength');

        if (passwordInput && strengthIndicator) {
            passwordInput.addEventListener('input', function() {
                const password = this.value;
                const strength = calculatePasswordStrength(password);

                strengthIndicator.className = 'password-strength';
                strengthIndicator.textContent = '';

                if (password.length > 0) {
                    if (strength.score < 2) {
                        strengthIndicator.classList.add('strength-weak');
                        strengthIndicator.textContent = 'Senha fraca';
                    } else if (strength.score < 4) {
                        strengthIndicator.classList.add('strength-medium');
                        strengthIndicator.textContent = 'Senha média';
                    } else {
                        strengthIndicator.classList.add('strength-strong');
                        strengthIndicator.textContent = 'Senha forte';
                    }
                }
            });
        }

        // Validação de confirmação de senha
        const confirmPasswordInput = document.getElementById('confirmPassword');
        if (confirmPasswordInput) {
            confirmPasswordInput.addEventListener('input', function() {
                const password = passwordInput.value;
                const confirmPassword = this.value;

                if (confirmPassword && password !== confirmPassword) {
                    this.setCustomValidity('As senhas não coincidem');
                } else {
                    this.setCustomValidity('');
                }
            });
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