// Global variables
let vouchers = [];
let editingVoucherId = null;

// Load vouchers on page load
document.addEventListener('DOMContentLoaded', function() {
    // Only load if manager has shop
    if (window.hasShop !== false) {
        loadVouchers();
        
        // Setup filters
        document.getElementById('searchInput').addEventListener('input', filterVouchers);
        document.getElementById('filterStatus').addEventListener('change', filterVouchers);
        document.getElementById('filterFlags').addEventListener('change', filterVouchers);
    }
});

/**
 * Load all vouchers from API
 */
function loadVouchers() {
    fetch('/manager/vouchers/api')
        .then(response => response.json())
        .then(data => {
            vouchers = data;
            updateStats();
            renderVouchers(vouchers);
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('danger', 'Không thể tải danh sách mã giảm giá');
        });
}

/**
 * Update statistics cards
 */
function updateStats() {
    const total = vouchers.length;
    const active = vouchers.filter(v => v.status === 'ACTIVE').length;
    const exhausted = vouchers.filter(v => v.status === 'EXHAUSTED').length;
    const totalUsage = vouchers.reduce((sum, v) => sum + (v.usedCount || 0), 0);

    document.getElementById('totalVouchers').textContent = total;
    document.getElementById('activeVouchers').textContent = active;
    document.getElementById('exhaustedVouchers').textContent = exhausted;
    document.getElementById('totalUsage').textContent = totalUsage;
}

/**
 * Render vouchers to table
 */
function renderVouchers(voucherList) {
    const tbody = document.getElementById('voucherTableBody');
    const emptyState = document.getElementById('emptyState');
    
    if (voucherList.length === 0) {
        tbody.innerHTML = '';
        emptyState.style.display = 'block';
        document.querySelector('.voucher-table').style.display = 'none';
        return;
    }
    
    emptyState.style.display = 'none';
    document.querySelector('.voucher-table').style.display = 'table';
    
    tbody.innerHTML = voucherList.map(voucher => {
        const rule = parseRuleJson(voucher.ruleJson);
        const usagePercent = voucher.usageLimit ? (voucher.usedCount / voucher.usageLimit * 100) : 0;
        const usageClass = usagePercent >= 90 ? 'danger' : usagePercent >= 70 ? 'warning' : '';
        
        return `
            <tr>
                <td>
                    <div class="voucher-code">
                        ${voucher.code}
                        ${voucher.isEditable === false ? '<span class="badge bg-info text-white ms-2" style="font-size: 0.75rem;"><i class="fas fa-globe"></i> Toàn hệ thống</span>' : ''}
                    </div>
                </td>
                <td>
                    <div class="voucher-meta">
                        ${voucher.forFirstOrder ? '<span class="badge-flag first-order"><i class="fas fa-user-plus"></i> Đơn đầu</span>' : ''}
                        ${voucher.forBirthday ? '<span class="badge-flag birthday"><i class="fas fa-birthday-cake"></i> Sinh nhật</span>' : ''}
                    </div>
                    <div class="voucher-meta mt-2">
                        ${getVoucherTypeDisplay(rule)}
                        ${rule.minTotal ? `• Đơn tối thiểu ${formatCurrency(rule.minTotal)}` : ''}
                    </div>
                    <div class="voucher-meta">
                        <i class="fas fa-calendar"></i> ${formatDateTime(voucher.activeFrom)} → ${formatDateTime(voucher.activeTo)}
                    </div>
                </td>
                <td>
                    <div class="usage-text">
                        <strong>${voucher.usedCount || 0}</strong> / ${voucher.usageLimit || '∞'} lượt
                    </div>
                    ${voucher.usageLimit ? `
                        <div class="usage-bar">
                            <div class="usage-bar-fill ${usageClass}" style="width: ${usagePercent}%"></div>
                        </div>
                    ` : ''}
                </td>
                <td>
                    <span class="badge-status ${voucher.status.toLowerCase()}">
                        ${getStatusText(voucher.status)}
                    </span>
                </td>
                <td>
                    ${voucher.isEditable === false ? `
                        <span class="badge bg-secondary text-white" title="Voucher toàn hệ thống - Không thể chỉnh sửa">
                            <i class="fas fa-lock"></i> Toàn hệ thống
                        </span>
                    ` : `
                        ${voucher.status === 'ACTIVE' ? `
                            <button class="btn btn-action btn-broadcast" onclick="broadcastVoucher(${voucher.id})" title="Gửi Email">
                                <i class="fas fa-envelope"></i>
                            </button>
                        ` : ''}
                        <button class="btn btn-action btn-edit" onclick="editVoucher(${voucher.id})" title="Sửa">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-action btn-toggle" onclick="toggleStatus(${voucher.id})" title="Bật/Tắt">
                            <i class="fas fa-power-off"></i>
                        </button>
                        <button class="btn btn-action btn-delete" onclick="deleteVoucher(${voucher.id})" title="Xóa">
                            <i class="fas fa-trash"></i>
                        </button>
                    `}
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * Filter vouchers based on search and filters
 */
function filterVouchers() {
    const searchText = document.getElementById('searchInput').value.toLowerCase();
    const statusFilter = document.getElementById('filterStatus').value;
    const flagsFilter = document.getElementById('filterFlags').value;
    
    let filtered = vouchers.filter(voucher => {
        const matchSearch = voucher.code.toLowerCase().includes(searchText);
        const matchStatus = !statusFilter || voucher.status === statusFilter;
        
        let matchFlags = true;
        if (flagsFilter === 'firstOrder') {
            matchFlags = voucher.forFirstOrder === true;
        } else if (flagsFilter === 'birthday') {
            matchFlags = voucher.forBirthday === true;
        }
        
        return matchSearch && matchStatus && matchFlags;
    });
    
    renderVouchers(filtered);
}

/**
 * Reset all filters
 */
function resetFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('filterStatus').value = '';
    document.getElementById('filterFlags').value = '';
    renderVouchers(vouchers);
}

/**
 * Update voucher fields based on type
 */
function updateVoucherFields() {
    const type = document.getElementById('voucherType').value;
    
    // Hide all fields first
    document.getElementById('fieldPercentOff').style.display = 'none';
    document.getElementById('fieldAmountCap').style.display = 'none';
    document.getElementById('fieldAmountOff').style.display = 'none';
    document.getElementById('fieldFreeShip').style.display = 'none';
    
    // Show fields based on type
    if (type === 'PERCENT') {
        document.getElementById('fieldPercentOff').style.display = 'block';
        document.getElementById('fieldAmountCap').style.display = 'block';
    } else if (type === 'AMOUNT') {
        document.getElementById('fieldAmountOff').style.display = 'block';
    } else if (type === 'FREESHIP') {
        document.getElementById('fieldFreeShip').style.display = 'block';
    }
}

/**
 * Open modal for creating new voucher
 */
function openCreateModal() {
    editingVoucherId = null;
    document.getElementById('voucherModalTitle').innerHTML = '<i class="fas fa-ticket-alt"></i> Tạo Mã giảm giá mới';
    document.getElementById('voucherForm').reset();
    
    // Set default values
    document.getElementById('voucherType').value = 'PERCENT';
    document.getElementById('minTotal').value = 0;
    document.getElementById('percentOff').value = 10;
    document.getElementById('amountCap').value = 50000;
    document.getElementById('amountOff').value = 50000;
    document.getElementById('usageLimit').value = 100;
    
    // Set default time (now to 1 month later)
    const now = new Date();
    const later = new Date();
    later.setMonth(later.getMonth() + 1);
    
    document.getElementById('activeFrom').value = formatDateTimeForInput(now);
    document.getElementById('activeTo').value = formatDateTimeForInput(later);
    
    // Update fields visibility
    updateVoucherFields();
}

/**
 * Edit existing voucher
 */
function editVoucher(voucherId) {
    fetch(`/manager/vouchers/api/${voucherId}`)
        .then(response => response.json())
        .then(voucher => {
            editingVoucherId = voucherId;
            document.getElementById('voucherModalTitle').innerHTML = '<i class="fas fa-edit"></i> Chỉnh sửa Mã giảm giá';
            
            // Fill form with voucher data
            document.getElementById('voucherCode').value = voucher.code;
            
            // Parse rule JSON
            const rule = parseRuleJson(voucher.ruleJson);
            document.getElementById('minTotal').value = rule.minTotal || 0;
            document.getElementById('percentOff').value = rule.percentOff || 0;
            document.getElementById('amountCap').value = rule.amountCap || 0;
            document.getElementById('amountOff').value = rule.amountOff || 0;
            
            // Detect voucher type from rule
            let voucherType = 'PERCENT';
            if (rule.freeShip === true) {
                voucherType = 'FREESHIP';
            } else if (rule.amountOff && rule.amountOff > 0) {
                voucherType = 'AMOUNT';
            } else if (rule.percentOff && rule.percentOff > 0) {
                voucherType = 'PERCENT';
            }
            document.getElementById('voucherType').value = voucherType;
            
            document.getElementById('forFirstOrder').checked = voucher.forFirstOrder || false;
            document.getElementById('forBirthday').checked = voucher.forBirthday || false;
            document.getElementById('usageLimit').value = voucher.usageLimit || 100;
            document.getElementById('activeFrom').value = formatDateTimeForInput(new Date(voucher.activeFrom));
            document.getElementById('activeTo').value = formatDateTimeForInput(new Date(voucher.activeTo));
            
            // Update fields visibility
            updateVoucherFields();
            
            // Open modal
            new bootstrap.Modal(document.getElementById('voucherModal')).show();
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('danger', 'Không thể tải thông tin mã giảm giá');
        });
}

/**
 * Save voucher (create or update)
 */
function saveVoucher() {
    const form = document.getElementById('voucherForm');
    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }
    
    const voucherType = document.getElementById('voucherType').value;
    
    // Build rule JSON based on voucher type
    let rule = {
        minTotal: parseInt(document.getElementById('minTotal').value) || 0
    };
    
    if (voucherType === 'PERCENT') {
        rule.percentOff = parseInt(document.getElementById('percentOff').value) || 0;
        rule.amountCap = parseInt(document.getElementById('amountCap').value) || 0;
        rule.freeShip = false;
    } else if (voucherType === 'AMOUNT') {
        rule.amountOff = parseInt(document.getElementById('amountOff').value) || 0;
        rule.freeShip = false;
    } else if (voucherType === 'FREESHIP') {
        rule.freeShip = true;
    }
    
    const voucherData = {
        code: document.getElementById('voucherCode').value.toUpperCase(),
        ruleJson: JSON.stringify(rule),
        forFirstOrder: document.getElementById('forFirstOrder').checked,
        forBirthday: document.getElementById('forBirthday').checked,
        usageLimit: parseInt(document.getElementById('usageLimit').value),
        activeFrom: document.getElementById('activeFrom').value,
        activeTo: document.getElementById('activeTo').value,
        status: 'ACTIVE'
    };
    
    const url = editingVoucherId 
        ? `/manager/vouchers/api/${editingVoucherId}` 
        : '/manager/vouchers/api';
    
    const method = editingVoucherId ? 'PUT' : 'POST';
    
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(voucherData)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text); });
        }
        return response.json();
    })
    .then(data => {
        showAlert('success', editingVoucherId ? 'Cập nhật mã giảm giá thành công!' : 'Tạo mã giảm giá thành công!');
        bootstrap.Modal.getInstance(document.getElementById('voucherModal')).hide();
        loadVouchers();
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('danger', 'Lỗi: ' + error.message);
    });
}

/**
 * Delete voucher
 */
function deleteVoucher(voucherId) {
    if (!confirm('Bạn có chắc chắn muốn xóa mã giảm giá này?')) {
        return;
    }
    
    fetch(`/manager/vouchers/api/${voucherId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text); });
        }
        return response.text();
    })
    .then(() => {
        showAlert('success', 'Xóa mã giảm giá thành công!');
        loadVouchers();
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('danger', 'Lỗi: ' + error.message);
    });
}

/**
 * Toggle voucher status (ACTIVE/INACTIVE)
 */
function toggleStatus(voucherId) {
    fetch(`/manager/vouchers/api/${voucherId}/toggle`, {
        method: 'PUT'
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text); });
        }
        return response.json();
    })
    .then(() => {
        showAlert('success', 'Cập nhật trạng thái thành công!');
        loadVouchers();
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('danger', 'Lỗi: ' + error.message);
    });
}

/**
 * Broadcast voucher to eligible customers via email
 */
function broadcastVoucher(voucherId) {
    // Find voucher info for confirmation
    const voucher = vouchers.find(v => v.id === voucherId);
    if (!voucher) return;
    
    let confirmMessage = `Gửi email voucher "${voucher.code}" đến khách hàng?\n\n`;
    
    if (voucher.forFirstOrder) {
        confirmMessage += '📧 Sẽ gửi cho: Khách hàng CHƯA CÓ đơn hàng nào\n';
    } else if (voucher.forBirthday) {
        confirmMessage += '📧 Sẽ gửi cho: Khách hàng có SINH NHẬT trong tháng\n';
    } else {
        confirmMessage += '📧 Sẽ gửi cho: TẤT CẢ khách hàng đã đặt hàng\n';
    }
    
    confirmMessage += '\n⚠️ Email sẽ được gửi ngay lập tức!';
    
    if (!confirm(confirmMessage)) {
        return;
    }
    
    // Show loading
    showAlert('info', 'Đang gửi email... Vui lòng đợi.');
    
    fetch(`/manager/vouchers/api/${voucherId}/broadcast`, {
        method: 'POST'
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text); });
        }
        return response.json();
    })
    .then(result => {
        showAlert('success', `✅ ${result.message}`);
        loadVouchers();
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('danger', 'Lỗi: ' + error.message);
    });
}

// ==================== UTILITY FUNCTIONS ====================

function parseRuleJson(ruleJson) {
    try {
        return JSON.parse(ruleJson || '{}');
    } catch (e) {
        return {};
    }
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { 
        style: 'currency', 
        currency: 'VND' 
    }).format(amount);
}

function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}

function formatDateTimeForInput(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function getStatusText(status) {
    const texts = {
        'ACTIVE': 'Hoạt động',
        'INACTIVE': 'Tạm dừng',
        'EXHAUSTED': 'Hết lượt'
    };
    return texts[status] || status;
}

function getVoucherTypeDisplay(rule) {
    if (rule.freeShip === true) {
        return '<i class="fas fa-shipping-fast text-success"></i> <strong>Miễn phí ship</strong>';
    } else if (rule.amountOff && rule.amountOff > 0) {
        return `<i class="fas fa-hand-holding-usd text-primary"></i> Giảm <strong>${formatCurrency(rule.amountOff)}</strong>`;
    } else if (rule.percentOff && rule.percentOff > 0) {
        const capText = rule.amountCap ? ` • Tối đa ${formatCurrency(rule.amountCap)}` : '';
        return `<i class="fas fa-percentage text-warning"></i> Giảm <strong>${rule.percentOff}%</strong>${capText}`;
    }
    return '<i class="fas fa-tag"></i> Chưa cấu hình';
}

function showAlert(type, message) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x mt-3`;
    alertDiv.style.zIndex = '9999';
    alertDiv.style.minWidth = '300px';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.body.appendChild(alertDiv);
    
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}

