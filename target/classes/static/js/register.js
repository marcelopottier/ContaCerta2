function isLoggedIn() {
  return Boolean(localStorage.getItem('authToken'));
}

document.addEventListener('DOMContentLoaded', function() {
    if (isLoggedIn()) {
        window.location.href = '/dashboard';
        return;
    }

    // Configurar formulário de cadastro
    setupRegisterForm();
});

function setupRegisterForm() {
    const registerForm = document.getElementById('registerForm');
    if (!registerForm) return;

    registerForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const name = document.getElementById('name').value;
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        // Limpar mensagens anteriores
        FinanceUtils.hideMessage('errorMessage');
        FinanceUtils.hideMessage('successMessage');

        // Validações
        if (password !== confirmPassword) {
            FinanceUtils.showMessage('errorMessage', 'As senhas não coincidem!', true);
            return;
        }

        if (password.length < 6) {
            FinanceUtils.showMessage('errorMessage', 'A senha deve ter pelo menos 6 caracteres!', true);
            return;
        }

        try {
            FinanceUtils.showLoading();

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

            FinanceUtils.hideLoading();

            if (response.ok) {
                const data = await response.json();

                // Salvar dados no localStorage
                localStorage.setItem('authToken', data.token);
                localStorage.setItem('userEmail', data.email);
                localStorage.setItem('userName', data.name);

                FinanceUtils.showMessage('successMessage', 'Cadastro realizado com sucesso!');

                // Redirecionar para o dashboard
                setTimeout(() => {
                    window.location.href = '/dashboard';
                }, 1000);
            } else {
                const errorText = await response.text();
                FinanceUtils.showMessage('errorMessage', errorText || 'Erro ao fazer cadastro', true);
            }
        } catch (error) {
            FinanceUtils.hideLoading();
            FinanceUtils.showMessage('errorMessage', 'Erro de conexão. Tente novamente.', true);
        }
    });
}
