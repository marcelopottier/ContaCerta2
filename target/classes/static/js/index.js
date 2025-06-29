document.addEventListener('DOMContentLoaded', function() {
    initIndexPage();
});

function initIndexPage() {
    checkAuthStatus();
    animateOnLoad();
    setupSmoothScroll();
    addHoverEffects();
    setupParallaxEffect();
}

// Verificar se o usuário já está logado
function checkAuthStatus() {
    const token = localStorage.getItem('authToken');
    const userName = localStorage.getItem('userName');

    if (token && userName) {
        // Usuário já está logado, atualizar interface
        updateNavForLoggedUser(userName);
        updateCTAForLoggedUser();
    }
}

function updateNavForLoggedUser(userName) {
    const navButtons = document.getElementById('navButtons');
    if (navButtons) {
        navButtons.innerHTML = `
            <span style="margin-right: 10px;">Olá, ${userName}!</span>
            <a href="/dashboard" class="nav-btn">Dashboard</a>
            <button class="nav-btn" onclick="confirmLogout()">Sair</button>
        `;
    }
}

function updateCTAForLoggedUser() {
    const ctaButtons = document.querySelector('.cta-buttons');
    if (ctaButtons) {
        ctaButtons.innerHTML = `
            <a href="/dashboard" class="cta-btn cta-primary">Ir para Dashboard</a>
            <a href="#" class="cta-btn cta-secondary" onclick="confirmLogout()">Sair da Conta</a>
        `;
    }
}

// Função de logout com confirmação
function confirmLogout() {
    if (confirm('Deseja realmente sair?')) {
        FinanceUtils.logout();
    }
}

// Animações de entrada
function animateOnLoad() {
    const heroSection = document.querySelector('.hero-section');
    const featureCards = document.querySelectorAll('.feature-card');

    if (heroSection) {
        heroSection.style.opacity = '0';
        heroSection.style.transform = 'translateY(30px)';

        setTimeout(() => {
            heroSection.style.transition = 'all 0.8s ease';
            heroSection.style.opacity = '1';
            heroSection.style.transform = 'translateY(0)';
        }, 100);
    }

    featureCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';

        setTimeout(() => {
            card.style.transition = 'all 0.6s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, 300 + (index * 100));
    });
}

// Smooth scroll para links internos
function setupSmoothScroll() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

// Adicionar efeitos de hover nos cards
function addHoverEffects() {
    const featureCards = document.querySelectorAll('.feature-card');

    featureCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.transition = 'transform 0.3s ease';
        });

        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
}

// Efeito de parallax sutil no scroll
function setupParallaxEffect() {
    window.addEventListener('scroll', function() {
        const scrolled = window.pageYOffset;
        const heroSection = document.querySelector('.hero-section');

        if (heroSection && scrolled < window.innerHeight) {
            heroSection.style.transform = `translateY(${scrolled * 0.3}px)`;
        }
    });
}