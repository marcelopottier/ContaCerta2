let categories = [];
let editingId = null;

document.addEventListener('DOMContentLoaded', function() {
    if (!FinanceUtils.isLoggedIn()) {
        window.location.href = '/login';
        return;
    }
    const user = FinanceUtils.getCurrentUser();
    const userName = document.getElementById('userName');
    if (userName) userName.textContent = user.name;

    loadCategories();
    setupForm();
});

function setupForm() {
    const form = document.getElementById('categoryForm');
    const cancelBtn = document.getElementById('cancelEdit');

    if (form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            const name = document.getElementById('categoryName').value;
            const payload = { name };
            let url = '/api/categories';
            let method = 'POST';
            if (editingId) {
                url += '/' + editingId;
                method = 'PUT';
            }
            const resp = await FinanceUtils.fetchWithAuth(url, {
                method,
                body: JSON.stringify(payload)
            });
            if (resp && resp.ok) {
                form.reset();
                editingId = null;
                if (cancelBtn) cancelBtn.style.display = 'none';
                loadCategories();
            } else if (resp) {
                alert(await resp.text());
            }
        });
    }

    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            form.reset();
            editingId = null;
            cancelBtn.style.display = 'none';
        });
    }
}

async function loadCategories() {
    const resp = await FinanceUtils.fetchWithAuth('/api/categories');
    if (resp && resp.ok) {
        categories = await resp.json();
        renderTable();
    }
}

function renderTable() {
    const tbody = document.getElementById('categoriesTbody');
    if (!tbody) return;
    tbody.innerHTML = '';
    categories.forEach(cat => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${cat.name}</td>
                        <td class="actions">
                            <button class="action-btn" onclick="editCategory(${cat.id})">Editar</button>
                            <button class="action-btn" onclick="deleteCategory(${cat.id})">Excluir</button>
                        </td>`;
        tbody.appendChild(tr);
    });
}

function editCategory(id) {
    const cat = categories.find(c => c.id === id);
    if (!cat) return;
    document.getElementById('categoryName').value = cat.name;
    editingId = id;
    const cancelBtn = document.getElementById('cancelEdit');
    if (cancelBtn) cancelBtn.style.display = 'inline-block';
}

async function deleteCategory(id) {
    if (!confirm('Deseja remover esta categoria?')) return;
    const resp = await FinanceUtils.fetchWithAuth('/api/categories/' + id, {
        method: 'DELETE'
    });
    if (resp && resp.ok) {
        loadCategories();
    } else if (resp) {
        alert(await resp.text());
    }
}
