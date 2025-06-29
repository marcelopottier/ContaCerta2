function isLoggedIn() {
  return Boolean(localStorage.getItem('authToken'));
}

document.addEventListener('DOMContentLoaded', function() {
    // Verificar se já está logado
    if (isLoggedIn()) {
        window.location.href = '/dashboard';
        return;
    }

    // Configurar formulário de login
    setupLoginForm();
});

function setupLoginForm() {
    const loginForm = document.getElementById('loginForm');
    if (!loginForm) return;

    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        // Limpar mensagens anteriores
        FinanceUtils.hideMessage('errorMessage');
        FinanceUtils.hideMessage('successMessage');

        try {
            FinanceUtils.showLoading();

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

            FinanceUtils.hideLoading();

            if (response.ok) {
                const data = await response.json();

                // Salvar dados no localStorage
                localStorage.setItem('authToken', data.token);
                localStorage.setItem('userEmail', data.email);
                localStorage.setItem('userName', data.name);

                FinanceUtils.showMessage('successMessage', 'Login realizado com sucesso!');

                // Redirecionar para o dashboard
                setTimeout(() => {
                    window.location.href = '/dashboard';
                }, 1000);
            } else {
                const errorText = await response.text();
                FinanceUtils.showMessage('errorMessage', errorText || 'Erro ao fazer login', true);
            }
        } catch (error) {
            FinanceUtils.hideLoading();
            FinanceUtils.showMessage('errorMessage', 'Erro de conexão. Tente novamente.', true);
        }
    });
}
