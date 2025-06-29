// src/main/resources/static/js/categories.js
// JavaScript moderno para gerenciamento de categorias

class CategoryManager {
    constructor() {
        this.categories = [];
        this.editingId = null;
        this.isLoading = false;
        
        this.init();
    }
    
    async init() {
        // Verificar autenticação
        if (!FinanceUtils.isLoggedIn()) {
            window.location.href = '/login';
            return;
        }
        
        // Configurar interface
        this.setupUI();
        this.setupEventListeners();
        
        // Carregar dados
        await this.loadCategories();
    }
    
    setupUI() {
        // Mostrar nome do usuário
        const user = FinanceUtils.getCurrentUser();
        const userName = document.getElementById('userName');
        if (userName && user.name) {
            userName.textContent = user.name;
        }
        
        // Configurar data atual no modal
        this.setupModal();
    }
    
    setupEventListeners() {
        // Formulário de categoria
        const form = document.getElementById('categoryForm');
        if (form) {
            form.addEventListener('submit', (e) => this.handleSubmit(e));
        }
        
        // Tecla ESC para fechar modal
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeModal();
            }
        });
        
        // Clique fora do modal
        const modal = document.getElementById('categoryModal');
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal || e.target.classList.contains('modal-backdrop')) {
                    this.closeModal();
                }
            });
        }
    }
    
    setupModal() {
        // Configurar color picker
        const colorInput = document.getElementById('categoryColor');
        if (colorInput) {
            colorInput.addEventListener('change', (e) => {
                this.updateColorPreview(e.target.value);
            });
        }
    }
    
    async loadCategories() {
        try {
            this.showLoading(true);
            
            const response = await FinanceUtils.fetchWithAuth('/api/categories');
            if (response && response.ok) {
                this.categories = await response.json();
                this.renderCategories();
                
                if (this.categories.length === 0) {
                    this.showEmptyState();
                }
            } else {
                this.showToast('Erro ao carregar categorias', 'error');
            }
        } catch (error) {
            console.error('Erro ao carregar categorias:', error);
            this.showToast('Erro de conexão', 'error');
        } finally {
            this.showLoading(false);
        }
    }
    
    renderCategories() {
        const grid = document.getElementById('categoriesGrid');
        const emptyState = document.getElementById('emptyState');
        
        if (!grid) return;
        
        if (this.categories.length === 0) {
            grid.style.display = 'none';
            if (emptyState) emptyState.style.display = 'block';
            return;
        }
        
        if (emptyState) emptyState.style.display = 'none';
        grid.style.display = 'grid';
        
        grid.innerHTML = '';
        
        this.categories.forEach((category, index) => {
            const card = this.createCategoryCard(category, index);
            grid.appendChild(card);
        });
    }
    
    createCategoryCard(category, index) {
        const card = document.createElement('div');
        card.className = 'category-card';
        card.style.setProperty('--category-color', category.color || '#ff6b35');
        card.style.animationDelay = `${index * 0.1}s`;
        
        // Calcular estatísticas (simulado por enquanto)
        const transactionCount = Math.floor(Math.random() * 20) + 1;
        const totalAmount = (Math.random() * 5000).toFixed(2);
        
        card.innerHTML = `
            <div class="category-header">
                <div class="category-info">
                    <h3 class="category-name">${category.name}</h3>
                    <div class="category-stats">
                        ${transactionCount} movimentações • R$ ${totalAmount}
                    </div>
                </div>
                <div class="category-icon">
                    <i class="fas fa-tag"></i>
                </div>
            </div>
            <div class="category-actions">
                <button class="action-btn edit" onclick="categoryManager.editCategory(${category.id})">
                    <i class="fas fa-edit"></i>
                    Editar
                </button>
                <button class="action-btn delete" onclick="categoryManager.deleteCategory(${category.id})">
                    <i class="fas fa-trash"></i>
                    Excluir
                </button>
            </div>
        `;
        
        return card;
    }
    
    openAddModal() {
        this.editingId = null;
        this.resetForm();
        
        const modal = document.getElementById('categoryModal');
        const title = document.getElementById('modalTitle');
        
        if (title) {
            title.innerHTML = '<i class="fas fa-plus"></i> Nova Categoria';
        }
        
        if (modal) {
            modal.style.display = 'block';
            setTimeout(() => modal.classList.add('show'), 10);
        }
        
        // Focar no campo nome
        const nameInput = document.getElementById('categoryName');
        if (nameInput) {
            setTimeout(() => nameInput.focus(), 300);
        }
    }
    
    editCategory(id) {
        const category = this.categories.find(c => c.id === id);
        if (!category) return;
        
        this.editingId = id;
        
        // Preencher formulário
        const nameInput = document.getElementById('categoryName');
        const colorInput = document.getElementById('categoryColor');
        
        if (nameInput) nameInput.value = category.name;
        if (colorInput) {
            colorInput.value = category.color || '#ff6b35';
            this.updateColorPreview(category.color || '#ff6b35');
        }
        
        // Atualizar título do modal
        const title = document.getElementById('modalTitle');
        if (title) {
            title.innerHTML = '<i class="fas fa-edit"></i> Editar Categoria';
        }
        
        // Abrir modal
        const modal = document.getElementById('categoryModal');
        if (modal) {
            modal.style.display = 'block';
            setTimeout(() => modal.classList.add('show'), 10);
        }
    }
    
    async deleteCategory(id) {
        const category = this.categories.find(c => c.id === id);
        if (!category) return;
        
        // Confirmação estilizada
        if (!await this.showConfirmDialog(
            `Deseja excluir a categoria "${category.name}"?`,
            'Esta ação não pode ser desfeita.'
        )) {
            return;
        }
        
        try {
            this.showLoading(true);
            
            const response = await FinanceUtils.fetchWithAuth(`/api/categories/${id}`, {
                method: 'DELETE'
            });
            
            if (response && response.ok) {
                this.showToast('Categoria excluída com sucesso!', 'success');
                await this.loadCategories();
            } else {
                const errorText = await response.text();
                this.showToast(errorText || 'Erro ao excluir categoria', 'error');
            }
        } catch (error) {
            console.error('Erro ao excluir categoria:', error);
            this.showToast('Erro de conexão', 'error');
        } finally {
            this.showLoading(false);
        }
    }
    
    async handleSubmit(e) {
        e.preventDefault();
        
        const nameInput = document.getElementById('categoryName');
        const colorInput = document.getElementById('categoryColor');
        
        if (!nameInput || !nameInput.value.trim()) {
            this.showToast('Nome da categoria é obrigatório', 'error');
            return;
        }
        
        const categoryData = {
            name: nameInput.value.trim(),
            color: colorInput ? colorInput.value : '#ff6b35'
        };
        
        try {
            this.showLoading(true);
            
            const url = this.editingId ? `/api/categories/${this.editingId}` : '/api/categories';
            const method = this.editingId ? 'PUT' : 'POST';
            
            const response = await FinanceUtils.fetchWithAuth(url, {
                method,
                body: JSON.stringify(categoryData)
            });
            
            if (response && response.ok) {
                this.showToast(
                    this.editingId ? 'Categoria atualizada com sucesso!' : 'Categoria criada com sucesso!',
                    'success'
                );
                this.closeModal();
                await this.loadCategories();
            } else {
                const errorText = await response.text();
                this.showToast(errorText || 'Erro ao salvar categoria', 'error');
            }
        } catch (error) {
            console.error('Erro ao salvar categoria:', error);
            this.showToast('Erro de conexão', 'error');
        } finally {
            this.showLoading(false);
        }
    }
    
    closeModal() {
        const modal = document.getElementById('categoryModal');
        if (modal) {
            modal.classList.remove('show');
            setTimeout(() => {
                modal.style.display = 'none';
                this.resetForm();
            }, 300);
        }
    }
    
    resetForm() {
        const form = document.getElementById('categoryForm');
        if (form) {
            form.reset();
        }
        
        this.editingId = null;
        
        // Reset color picker
        const colorInput = document.getElementById('categoryColor');
        if (colorInput) {
            colorInput.value = '#ff6b35';
            this.updateColorPreview('#ff6b35');
        }
    }
    
    updateColorPreview(color) {
        // Atualizar cor selecionada nos color options
        const colorOptions = document.querySelectorAll('.color-option');
        colorOptions.forEach(option => {
            option.classList.remove('selected');
            if (option.style.background === color) {
                option.classList.add('selected');
            }
        });
    }
    
    selectColor(color) {
        const colorInput = document.getElementById('categoryColor');
        if (colorInput) {
            colorInput.value = color;
            this.updateColorPreview(color);
        }
    }
    
    showLoading(show = true) {
        const loading = document.getElementById('loading');
        if (loading) {
            loading.style.display = show ? 'flex' : 'none';
        }
        
        // Desabilitar botões durante loading
        const buttons = document.querySelectorAll('button');
        buttons.forEach(btn => {
            btn.disabled = show;
        });
    }
    
    showEmptyState() {
        const grid = document.getElementById('categoriesGrid');
        const emptyState = document.getElementById('emptyState');
        
        if (grid) grid.style.display = 'none';
        if (emptyState) emptyState.style.display = 'block';
    }
    
    showToast(message, type = 'success') {
        const toast = document.getElementById('toast');
        if (!toast) return;
        
        const icon = toast.querySelector('.toast-icon');
        const messageSpan = toast.querySelector('.toast-message');
        
        // Configurar ícone e tipo
        toast.className = `toast ${type}`;
        if (icon) {
            icon.className = `toast-icon fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}`;
        }
        if (messageSpan) {
            messageSpan.textContent = message;
        }
        
        // Mostrar toast
        toast.classList.add('show');
        
        // Esconder após 3 segundos
        setTimeout(() => {
            toast.classList.remove('show');
        }, 3000);
    }
    
    async showConfirmDialog(title, message) {
        return new Promise((resolve) => {
            // Criar modal de confirmação dinâmico
            const confirmModal = document.createElement('div');
            confirmModal.className = 'modal';
            confirmModal.innerHTML = `
                <div class="modal-backdrop"></div>
                <div class="modal-content" style="max-width: 400px;">
                    <div class="modal-header">
                        <h3><i class="fas fa-exclamation-triangle" style="color: #ffc107;"></i> Confirmar</h3>
                    </div>
                    <div class="modal-body">
                        <p style="margin: 0 0 10px 0; font-weight: 600;">${title}</p>
                        <p style="margin: 0; color: #666;">${message}</p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" id="confirmCancel">
                            <i class="fas fa-times"></i> Cancelar
                        </button>
                        <button type="button" class="btn" id="confirmDelete" style="background: #dc3545; color: white;">
                            <i class="fas fa-trash"></i> Excluir
                        </button>
                    </div>
                </div>
            `;
            
            document.body.appendChild(confirmModal);
            confirmModal.style.display = 'block';
            
            // Event listeners
            const cancelBtn = confirmModal.querySelector('#confirmCancel');
            const deleteBtn = confirmModal.querySelector('#confirmDelete');
            
            const cleanup = () => {
                document.body.removeChild(confirmModal);
            };
            
            cancelBtn.addEventListener('click', () => {
                cleanup();
                resolve(false);
            });
            
            deleteBtn.addEventListener('click', () => {
                cleanup();
                resolve(true);
            });
            
            // Fechar com ESC ou clique fora
            confirmModal.addEventListener('click', (e) => {
                if (e.target === confirmModal || e.target.classList.contains('modal-backdrop')) {
                    cleanup();
                    resolve(false);
                }
            });
        });
    }
}

// Funções globais para compatibilidade
let categoryManager;

document.addEventListener('DOMContentLoaded', function() {
    categoryManager = new CategoryManager();
});

// Funções que podem ser chamadas do HTML
function openAddModal() {
    if (categoryManager) {
        categoryManager.openAddModal();
    }
}

function selectColor(color) {
    if (categoryManager) {
        categoryManager.selectColor(color);
    }
}

function closeModal() {
    if (categoryManager) {
        categoryManager.closeModal();
    }
}


window.categoryManager = {
    editCategory: (id) => categoryManager?.editCategory(id),
    deleteCategory: (id) => categoryManager?.deleteCategory(id)
};