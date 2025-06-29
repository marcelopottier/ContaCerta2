document.addEventListener('DOMContentLoaded', function() {
    initAuthPage();
});

function initAuthPage() {
    // Verificar se já está logado
    if (localStorage.getItem('authToken')) {
        window.location.href = '/dashboard';
        return;
    }

    setupAuthForms();
}

function setupAuthForms() {
    // Setup formulário de login
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Setup formulário de cadastro
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
}

// Manipular login
async function handleLogin(e) {
    e.preventDefault();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    FinanceUtils.clearMessages();

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        if (response.ok) {
            const data = await response.json();

            // Salvar dados no localStorage
            localStorage.setItem('authToken', data.token);
            localStorage.setItem('userEmail', data.email);
            localStorage.setItem('userName', data.name);

            FinanceUtils.showSuccess('successMessage', 'Login realizado com sucesso!');

            // Redirecionar para o dashboard
            setTimeout(() => {
                window.location.href = '/dashboard';
            }, 1000);
        } else {
            const errorText = await response.text();
            FinanceUtils.showError('errorMessage', errorText || 'Erro ao fazer login');
        }
    } catch (error) {
        FinanceUtils.showError('errorMessage', 'Erro de conexão. Tente novamente.');
    }
}

// Manipular cadastro
async function handleRegister(e) {
    e.preventDefault();

    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    FinanceUtils.clearMessages();

    // Validações
    if (password !== confirmPassword) {
        FinanceUtils.showError('errorMessage', 'As senhas não coincidem!');
        return;
    }

    if (password.length < 6) {
        FinanceUtils.showError('errorMessage', 'A senha deve ter pelo menos 6 caracteres!');
        return;
    }

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                name: name,
                email: email,
                password: password
            })
        });

        if (response.ok) {
            const data = await response.json();

            // Salvar dados no localStorage
            localStorage.setItem('authToken', data.token);
            localStorage.setItem('userEmail', data.email);
            localStorage.setItem('userName', data.name);

            FinanceUtils.showSuccess('successMessage', 'Cadastro realizado com sucesso!');

            // Redirecionar para o dashboard
            setTimeout(() => {
                window.location.href = '/dashboard';
            }, 1000);
        } else {
            const errorText = await response.text();
            FinanceUtils.showError('errorMessage', errorText || 'Erro ao fazer cadastro');
        }
    } catch (error) {
        FinanceUtils.showError('errorMessage', 'Erro de conexão. Tente novamente.');
    }
}