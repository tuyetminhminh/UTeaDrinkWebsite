// Global variables
let promotions = [];
let editingPromoId = null;

// Load promotions on page load
document.addEventListener('DOMContentLoaded', function() {
    // Only load if manager has shop
    if (window.hasShop !== false) {
        loadPromotions();
        
        // Setup filters
        document.getElementById('searchInput').addEventListener('input', filterPromotions);
        document.getElementById('filterType').addEventListener('change', filterPromotions);
        document.getElementById('filterStatus').addEventListener('change', filterPromotions);
    }
});

/**
 * Load all promotions from API
 */
function loadPromotions() {
    fetch('/manager/promotions/api')
        .then(response => response.json())
        .then(data => {
            promotions = data;
            updateStats();
            renderPromotions(promotions);
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('danger', 'Không thể tải danh sách khuyến mãi');
        });
}

/**
 * Update statistics cards
 */
function updateStats() {
    const total = promotions.length;
    const active = promotions.filter(p => p.status === 'ACTIVE').length;
    const inactive = promotions.filter(p => p.status === 'INACTIVE').length;
    
    // Calculate scheduled (activeFrom > now)
    const now = new Date();
    const scheduled = promotions.filter(p => {
        if (!p.activeFrom) return false;
        return new Date(p.activeFrom) > now && p.status === 'ACTIVE';
    }).length;

    document.getElementById('totalPromotions').textContent = total;
    document.getElementById('activePromotions').textContent = active;
    document.getElementById('scheduledPromotions').textContent = scheduled;
    document.getElementById('inactivePromotions').textContent = inactive;
}

/**
 * Render promotions table
 */
function renderPromotions(data) {
    const tbody = document.getElementById('promoTableBody');
    const emptyState = document.getElementById('emptyState');
    const tableContainer = document.querySelector('.promo-table-container');

    if (!data || data.length === 0) {
        tbody.innerHTML = '';
        tableContainer.style.display = 'none';
        emptyState.style.display = 'block';
        return;
    }

    tableContainer.style.display = 'block';
    emptyState.style.display = 'none';

    tbody.innerHTML = data.map(promo => {
        const rule = promo.ruleJson ? JSON.parse(promo.ruleJson) : {};
        const typeLabel = getTypeLabel(promo.type);
        const typeBadge = getTypeBadgeClass(promo.type);
        
        return `
            <tr>
                <td>
                    <div class="promo-title">
                        ${promo.title}
                        ${promo.isEditable === false ? '<span class="badge bg-info text-white ms-2" style="font-size: 0.75rem;"><i class="fas fa-globe"></i> Toàn hệ thống</span>' : ''}
                    </div>
                    <div class="promo-desc">${promo.description || 'Không có mô tả'}</div>
                    <div class="promo-desc mt-1">
                        ${getRuleDescription(promo.type, rule)}
                    </div>
                </td>
                <td>
                    <span class="badge-type ${typeBadge}">${typeLabel}</span>
                </td>
                <td>
                    <div class="promo-dates">
                        <div><i class="fas fa-play"></i> ${formatDateTime(promo.activeFrom)}</div>
                        <div><i class="fas fa-stop"></i> ${formatDateTime(promo.activeTo)}</div>
                    </div>
                </td>
                <td>
                    <span class="badge-status ${promo.status.toLowerCase()}">
                        ${promo.status === 'ACTIVE' ? '✓ Hoạt động' : '⏸ Tạm dừng'}
                    </span>
                </td>
                <td>
                    ${promo.isEditable === false ? `
                        <span class="badge bg-secondary text-white" title="Khuyến mãi toàn hệ thống - Không thể chỉnh sửa">
                            <i class="fas fa-lock"></i> Toàn hệ thống
                        </span>
                    ` : `
                        <button class="btn-action btn-toggle" onclick="toggleStatus(${promo.id})" 
                                title="${promo.status === 'ACTIVE' ? 'Tạm dừng' : 'Kích hoạt'}">
                            <i class="fas fa-${promo.status === 'ACTIVE' ? 'pause' : 'play'}"></i>
                        </button>
                        <button class="btn-action btn-edit" onclick="editPromotion(${promo.id})" title="Sửa">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn-action btn-delete" onclick="deletePromotion(${promo.id})" title="Xóa">
                            <i class="fas fa-trash"></i>
                        </button>
                    `}
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * Filter promotions based on search and filters
 */
function filterPromotions() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const typeFilter = document.getElementById('filterType').value;
    const statusFilter = document.getElementById('filterStatus').value;

    let filtered = promotions;

    // Search filter
    if (searchTerm) {
        filtered = filtered.filter(p => 
            p.title.toLowerCase().includes(searchTerm) ||
            (p.description && p.description.toLowerCase().includes(searchTerm))
        );
    }

    // Type filter
    if (typeFilter) {
        filtered = filtered.filter(p => p.type === typeFilter);
    }

    // Status filter
    if (statusFilter) {
        filtered = filtered.filter(p => p.status === statusFilter);
    }

    renderPromotions(filtered);
}

/**
 * Reset all filters
 */
function resetFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('filterType').value = '';
    document.getElementById('filterStatus').value = '';
    renderPromotions(promotions);
}

/**
 * Open create modal
 */
function openCreateModal() {
    editingPromoId = null;
    document.getElementById('modalTitle').innerHTML = '<i class="fas fa-plus-circle"></i> Tạo Khuyến mãi mới';
    document.getElementById('promoForm').reset();
    document.getElementById('promoId').value = '';
    
    // Set default datetime
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    document.getElementById('activeFrom').value = now.toISOString().slice(0, 16);
    
    const tomorrow = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
    document.getElementById('activeTo').value = tomorrow.toISOString().slice(0, 16);
    
    updateRuleFields();
}

/**
 * Edit promotion
 */
function editPromotion(id) {
    const promo = promotions.find(p => p.id === id);
    if (!promo) return;

    editingPromoId = id;
    document.getElementById('modalTitle').innerHTML = '<i class="fas fa-edit"></i> Chỉnh sửa Khuyến mãi';
    
    document.getElementById('promoId').value = promo.id;
    document.getElementById('promoTitle').value = promo.title;
    document.getElementById('promoType').value = promo.type;
    document.getElementById('promoDesc').value = promo.description || '';
    
    // Parse rule JSON
    const rule = promo.ruleJson ? JSON.parse(promo.ruleJson) : {};
    document.getElementById('minTotal').value = rule.minTotal || '';
    document.getElementById('percentOff').value = rule.percentOff || '';
    document.getElementById('amountOff').value = rule.amountOff || '';
    document.getElementById('amountCap').value = rule.amountCap || '';
    
    // Format datetime
    if (promo.activeFrom) {
        const from = new Date(promo.activeFrom);
        from.setMinutes(from.getMinutes() - from.getTimezoneOffset());
        document.getElementById('activeFrom').value = from.toISOString().slice(0, 16);
    }
    
    if (promo.activeTo) {
        const to = new Date(promo.activeTo);
        to.setMinutes(to.getMinutes() - to.getTimezoneOffset());
        document.getElementById('activeTo').value = to.toISOString().slice(0, 16);
    }
    
    updateRuleFields();
    
    // Open modal
    const modal = new bootstrap.Modal(document.getElementById('promoModal'));
    modal.show();
}

/**
 * Update rule fields based on promotion type
 */
function updateRuleFields() {
    const type = document.getElementById('promoType').value;
    
    // Hide all
    document.getElementById('percentOffGroup').style.display = 'none';
    document.getElementById('amountOffGroup').style.display = 'none';
    document.getElementById('amountCapGroup').style.display = 'none';
    
    // Show relevant fields
    if (type === 'PERCENT') {
        document.getElementById('percentOffGroup').style.display = 'block';
        document.getElementById('amountCapGroup').style.display = 'block';
    } else if (type === 'AMOUNT') {
        document.getElementById('amountOffGroup').style.display = 'block';
    }
}

/**
 * Save promotion (create or update)
 */
function savePromotion() {
    const form = document.getElementById('promoForm');
    if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return;
    }

    const type = document.getElementById('promoType').value;
    
    // Build rule JSON
    const rule = {
        minTotal: parseFloat(document.getElementById('minTotal').value) || null,
        percentOff: type === 'PERCENT' ? parseInt(document.getElementById('percentOff').value) : null,
        amountOff: type === 'AMOUNT' ? parseFloat(document.getElementById('amountOff').value) : null,
        amountCap: type === 'PERCENT' ? parseFloat(document.getElementById('amountCap').value) : null,
        freeShip: type === 'FREESHIP' ? true : null
    };

    // Remove null values
    Object.keys(rule).forEach(key => rule[key] === null && delete rule[key]);

    const data = {
        title: document.getElementById('promoTitle').value.trim(),
        type: type,
        description: document.getElementById('promoDesc').value.trim() || null,
        ruleJson: JSON.stringify(rule),
        activeFrom: document.getElementById('activeFrom').value,
        activeTo: document.getElementById('activeTo').value,
        status: 'ACTIVE'
    };

    const url = editingPromoId 
        ? `/manager/promotions/api/${editingPromoId}`
        : '/manager/promotions/api';
    const method = editingPromoId ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text) });
        }
        return response.json();
    })
    .then(result => {
        showAlert('success', `Khuyến mãi đã được ${editingPromoId ? 'cập nhật' : 'tạo'} thành công!`);
        bootstrap.Modal.getInstance(document.getElementById('promoModal')).hide();
        loadPromotions();
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('danger', `Lỗi: ${error.message}`);
    });
}

/**
 * Toggle promotion status
 */
function toggleStatus(id) {
    if (!confirm('Bạn có chắc muốn thay đổi trạng thái khuyến mãi này?')) {
        return;
    }

    fetch(`/manager/promotions/api/${id}/toggle`, {
        method: 'PUT'
    })
    .then(response => response.json())
    .then(result => {
        showAlert('success', 'Đã cập nhật trạng thái khuyến mãi!');
        loadPromotions();
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('danger', 'Không thể cập nhật trạng thái');
    });
}

/**
 * Delete promotion
 */
function deletePromotion(id) {
    if (!confirm('⚠️ Bạn có chắc muốn xóa khuyến mãi này? Hành động này không thể hoàn tác!')) {
        return;
    }

    fetch(`/manager/promotions/api/${id}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text) });
        }
        return response.text();
    })
    .then(message => {
        showAlert('success', 'Khuyến mãi đã được xóa!');
        loadPromotions();
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('danger', 'Không thể xóa khuyến mãi');
    });
}

/**
 * Show alert message
 */
function showAlert(type, message) {
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert" 
             style="position: fixed; top: 90px; right: 20px; z-index: 9999; min-width: 300px; box-shadow: 0 4px 20px rgba(0,0,0,0.2);">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    document.body.insertAdjacentHTML('beforeend', alertHtml);
    
    setTimeout(() => {
        const alert = document.querySelector('.alert');
        if (alert) alert.remove();
    }, 5000);
}

// ==================== HELPER FUNCTIONS ====================

function getTypeLabel(type) {
    const labels = {
        'PERCENT': 'Giảm %',
        'AMOUNT': 'Giảm tiền',
        'FREESHIP': 'Miễn phí ship'
    };
    return labels[type] || type;
}

function getTypeBadgeClass(type) {
    const classes = {
        'PERCENT': 'percent',
        'AMOUNT': 'amount',
        'FREESHIP': 'freeship'
    };
    return classes[type] || 'percent';
}

function getRuleDescription(type, rule) {
    let desc = [];
    
    if (rule.minTotal) {
        desc.push(`Đơn từ ${formatCurrency(rule.minTotal)}`);
    }
    
    if (type === 'PERCENT' && rule.percentOff) {
        desc.push(`Giảm ${rule.percentOff}%`);
        if (rule.amountCap) {
            desc.push(`tối đa ${formatCurrency(rule.amountCap)}`);
        }
    } else if (type === 'AMOUNT' && rule.amountOff) {
        desc.push(`Giảm ${formatCurrency(rule.amountOff)}`);
    } else if (type === 'FREESHIP') {
        desc.push('Miễn phí vận chuyển');
    }
    
    return desc.join(' • ') || 'Không có điều kiện';
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function formatDateTime(dateStr) {
    if (!dateStr) return '--';
    
    const date = new Date(dateStr);
    return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

