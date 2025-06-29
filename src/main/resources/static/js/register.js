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
        hideMessage('errorMessage');
        hideMessage('successMessage');

        // Validações
        if (password !== confirmPassword) {
            showMessage('errorMessage', 'As senhas não coincidem!', true);
            return;
        }

        if (password.length < 6) {
            showMessage('errorMessage', 'A senha deve ter pelo menos 6 caracteres!', true);
            return;
        }

        try {
            showLoading();

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

            hideLoading();

            if (response.ok) {
                const data = await response.json();

                // Salvar dados no localStorage
                localStorage.setItem('authToken', data.token);
                localStorage.setItem('userEmail', data.email);
                localStorage.setItem('userName', data.name);

                showMessage('successMessage', 'Cadastro realizado com sucesso!');

                // Redirecionar para o dashboard
                setTimeout(() => {
                    window.location.href = '/dashboard';
                }, 1000);
            } else {
                const errorText = await response.text();
                showMessage('errorMessage', errorText || 'Erro ao fazer cadastro', true);
            }
        } catch (error) {
            hideLoading();
            showMessage('errorMessage', 'Erro de conexão. Tente novamente.', true);
        }
    });
}
