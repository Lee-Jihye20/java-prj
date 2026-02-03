import { ModalAnimations, ButtonAnimations, TableAnimations } from './animations.js';
class UserListManager {
    constructor() {
        this.roleAssignModal = document.getElementById('roleAssignModal');
        this.workSettingsModal = document.getElementById('workSettingsModal');
        this.exportDailyModal = document.getElementById('exportDailyModal');
        this.exportMonthlyModal = document.getElementById('exportMonthlyModal');
        this.slackIdSettingsModal = document.getElementById('slackIdSettingsModal');
        this.init();
    }
    init() {
        this.setupEventListeners();
        this.setupButtonAnimations();
        this.setupTableAnimations();
    }
    setupEventListeners() {
        document.addEventListener('click', (e) => {
            const target = e.target;
            if (target.closest('.assign-role-btn')) {
                this.handleRoleAssignClick(e);
            }
            else if (target.closest('.work-settings-btn')) {
                this.handleWorkSettingsClick(e);
            }
            else if (target.closest('.export-daily-btn')) {
                this.handleExportDailyClick(e);
            }
            else if (target.closest('.export-monthly-btn')) {
                this.handleExportMonthlyClick(e);
            }
            else if (target.closest('.slack-id-settings-btn')) {
                this.handleSlackIdSettingsClick(e);
            }
        });
        document.addEventListener('submit', (e) => {
            const form = e.target;
            if (form.id === 'exportDailyForm') {
                this.handleExportDailySubmit(e);
            }
            else if (form.id === 'exportMonthlyForm') {
                this.handleExportMonthlySubmit(e);
            }
        });
        this.setupModalCloseListeners();
        this.setupWorkTypeToggle();
    }
    handleRoleAssignClick(e) {
        e.preventDefault();
        const btn = e.target.closest('.assign-role-btn');
        if (!btn)
            return;
        const userData = {
            userId: btn.getAttribute('data-user-id') || '',
            username: btn.getAttribute('data-username') || '',
            currentRole: btn.getAttribute('data-current-role') || ''
        };
        const roleIds = [];
        btn.querySelectorAll('[data-role-id]').forEach((el) => {
            const roleId = el.getAttribute('data-role-id');
            if (roleId)
                roleIds.push(roleId);
        });
        userData.roleIds = roleIds;
        this.showRoleAssignModal(userData);
    }
    showRoleAssignModal(userData) {
        if (!this.roleAssignModal)
            return;
        const userIdInput = document.getElementById('roleAssignUserId');
        const usernameElement = document.getElementById('roleAssignUsername');
        const basicRoleSelect = document.getElementById('roleAssignBasicRole');
        const form = document.getElementById('roleAssignForm');
        if (userIdInput && usernameElement && form) {
            userIdInput.value = userData.userId;
            usernameElement.textContent = userData.username;
            if (basicRoleSelect && userData.currentRole) {
                basicRoleSelect.value = userData.currentRole;
            }
            form.action = `/admin/users/${userData.userId}/roles`;
            const checkboxes = document.querySelectorAll('#roleAssignModal input[type="checkbox"].custom-role-checkbox');
            checkboxes.forEach((cb) => {
                const checkbox = cb;
                const roleId = checkbox.value;
                checkbox.checked = userData.roleIds?.includes(roleId) || false;
            });
            ModalAnimations.showModal(this.roleAssignModal);
        }
    }
    handleWorkSettingsClick(e) {
        e.preventDefault();
        const btn = e.target.closest('.work-settings-btn');
        if (!btn)
            return;
        const userData = {
            userId: btn.getAttribute('data-user-id') || '',
            username: btn.getAttribute('data-username') || '',
            workType: btn.getAttribute('data-work-type') || 'FULLTIME',
            startTime: btn.getAttribute('data-start-time') || '09:00',
            coreTimeStart: btn.getAttribute('data-core-time-start') || '10:00',
            coreTimeEnd: btn.getAttribute('data-core-time-end') || '15:00'
        };
        this.showWorkSettingsModal(userData);
    }
    showWorkSettingsModal(userData) {
        if (!this.workSettingsModal)
            return;
        const userIdInput = document.getElementById('workSettingsUserId');
        const usernameEl = document.getElementById('workSettingsUsername');
        const workTypeSelect = document.getElementById('workSettingsWorkType');
        const startTimeInput = document.getElementById('workSettingsStartTime');
        const startTimeGroup = document.getElementById('workSettingsStartTimeGroup');
        const coreTimeStartInput = document.getElementById('workSettingsCoreTimeStart');
        const coreTimeEndInput = document.getElementById('workSettingsCoreTimeEnd');
        const coreTimeGroup = document.getElementById('workSettingsCoreTimeGroup');
        const form = document.getElementById('workSettingsForm');
        if (!form)
            return;
        if (userIdInput)
            userIdInput.value = userData.userId;
        if (usernameEl)
            usernameEl.textContent = userData.username;
        if (workTypeSelect)
            workTypeSelect.value = userData.workType || 'FULLTIME';
        if (startTimeInput)
            startTimeInput.value = userData.startTime || '09:00';
        if (coreTimeStartInput)
            coreTimeStartInput.value = userData.coreTimeStart || '10:00';
        if (coreTimeEndInput)
            coreTimeEndInput.value = userData.coreTimeEnd || '15:00';
        form.action = `/admin/users/${userData.userId}/work-settings`;
        if (startTimeGroup) {
            startTimeGroup.style.display = userData.workType === 'FULLTIME' ? 'block' : 'none';
        }
        if (coreTimeGroup) {
            coreTimeGroup.style.display = userData.workType === 'FLEX' ? 'block' : 'none';
        }
        ModalAnimations.showModal(this.workSettingsModal);
    }
    handleExportDailyClick(e) {
        e.preventDefault();
        const btn = e.target.closest('.export-daily-btn');
        if (!btn || !this.exportDailyModal)
            return;
        const uidEl = document.getElementById('exportDailyUserId');
        if (uidEl) {
            uidEl.value = btn.getAttribute('data-user-id') || '';
            ModalAnimations.showModal(this.exportDailyModal);
        }
    }
    handleExportMonthlyClick(e) {
        e.preventDefault();
        const btn = e.target.closest('.export-monthly-btn');
        if (!btn || !this.exportMonthlyModal)
            return;
        const uidEl = document.getElementById('exportMonthlyUserId');
        if (uidEl) {
            uidEl.value = btn.getAttribute('data-user-id') || '';
            ModalAnimations.showModal(this.exportMonthlyModal);
        }
    }
    handleSlackIdSettingsClick(e) {
        e.preventDefault();
        e.stopPropagation();
        const btn = e.target.closest('.slack-id-settings-btn');
        if (!btn)
            return;
        const userData = {
            userId: btn.getAttribute('data-user-id') || '',
            username: btn.getAttribute('data-username') || '',
            slackUserId: btn.getAttribute('data-slack-user-id') || ''
        };
        this.showSlackIdSettingsModal(userData);
    }
    showSlackIdSettingsModal(userData) {
        if (!this.slackIdSettingsModal)
            return;
        const userIdInput = document.getElementById('slackIdSettingsUserId');
        const usernameElement = document.getElementById('slackIdSettingsUsername');
        const slackUserIdInput = document.getElementById('slackIdSettingsSlackUserId');
        const form = document.getElementById('slackIdSettingsForm');
        if (!userIdInput || !usernameElement || !slackUserIdInput || !form) {
            console.error('SlackID設定モーダルの要素が見つかりません');
            return;
        }
        userIdInput.value = userData.userId;
        usernameElement.textContent = userData.username;
        slackUserIdInput.value = userData.slackUserId || '';
        form.action = `/admin/users/${userData.userId}/slack-id`;
        ModalAnimations.showModal(this.slackIdSettingsModal);
    }
    handleExportDailySubmit(e) {
        e.preventDefault();
        const uid = document.getElementById('exportDailyUserId');
        const date = document.getElementById('exportDailyDate');
        if (uid && date && date.value) {
            window.location.href = `/export/daily?userId=${encodeURIComponent(uid.value)}&date=${encodeURIComponent(date.value)}`;
            this.closeExportDailyModal();
        }
    }
    handleExportMonthlySubmit(e) {
        e.preventDefault();
        const uid = document.getElementById('exportMonthlyUserId');
        const year = document.getElementById('exportMonthlyYear');
        const month = document.getElementById('exportMonthlyMonth');
        if (uid && year && month && year.value && month.value) {
            window.location.href = `/export/monthly?userId=${encodeURIComponent(uid.value)}&year=${encodeURIComponent(year.value)}&month=${encodeURIComponent(month.value)}`;
            this.closeExportMonthlyModal();
        }
    }
    setupModalCloseListeners() {
        if (this.roleAssignModal) {
            this.roleAssignModal.addEventListener('click', (e) => {
                if (e.target === this.roleAssignModal) {
                    this.closeRoleAssignModal();
                }
            });
        }
        if (this.workSettingsModal) {
            this.workSettingsModal.addEventListener('click', (e) => {
                if (e.target === this.workSettingsModal) {
                    this.closeWorkSettingsModal();
                }
            });
        }
        if (this.exportDailyModal) {
            this.exportDailyModal.addEventListener('click', (e) => {
                if (e.target === this.exportDailyModal) {
                    this.closeExportDailyModal();
                }
            });
        }
        if (this.exportMonthlyModal) {
            this.exportMonthlyModal.addEventListener('click', (e) => {
                if (e.target === this.exportMonthlyModal) {
                    this.closeExportMonthlyModal();
                }
            });
        }
        if (this.slackIdSettingsModal) {
            this.slackIdSettingsModal.addEventListener('click', (e) => {
                if (e.target === this.slackIdSettingsModal) {
                    this.closeSlackIdSettingsModal();
                }
            });
        }
    }
    setupWorkTypeToggle() {
        const workTypeSelect = document.getElementById('workSettingsWorkType');
        if (workTypeSelect) {
            workTypeSelect.addEventListener('change', () => {
                const v = workTypeSelect.value;
                const startTimeGroup = document.getElementById('workSettingsStartTimeGroup');
                const coreTimeGroup = document.getElementById('workSettingsCoreTimeGroup');
                if (startTimeGroup) {
                    startTimeGroup.style.display = v === 'FULLTIME' ? 'block' : 'none';
                }
                if (coreTimeGroup) {
                    coreTimeGroup.style.display = v === 'FLEX' ? 'block' : 'none';
                }
            });
        }
    }
    setupButtonAnimations() {
        document.querySelectorAll('.btn-primary, .btn-secondary').forEach((btn) => {
            const button = btn;
            ButtonAnimations.addHoverEffect(button);
            ButtonAnimations.addClickEffect(button);
        });
    }
    setupTableAnimations() {
        const rows = document.querySelectorAll('.user-row, .attendance-table tbody tr');
        if (rows.length > 0) {
            TableAnimations.animateRowAppearance(rows);
        }
    }
    closeRoleAssignModal() {
        if (this.roleAssignModal) {
            ModalAnimations.hideModal(this.roleAssignModal);
        }
    }
    closeWorkSettingsModal() {
        if (this.workSettingsModal) {
            ModalAnimations.hideModal(this.workSettingsModal);
        }
    }
    closeExportDailyModal() {
        if (this.exportDailyModal) {
            ModalAnimations.hideModal(this.exportDailyModal);
        }
    }
    closeExportMonthlyModal() {
        if (this.exportMonthlyModal) {
            ModalAnimations.hideModal(this.exportMonthlyModal);
        }
    }
    closeSlackIdSettingsModal() {
        if (this.slackIdSettingsModal) {
            ModalAnimations.hideModal(this.slackIdSettingsModal);
        }
    }
}
let userListManagerInstance = null;
function closeRoleAssignModal() {
    if (userListManagerInstance) {
        userListManagerInstance.closeRoleAssignModal();
    }
}
function closeWorkSettingsModal() {
    if (userListManagerInstance) {
        userListManagerInstance.closeWorkSettingsModal();
    }
}
function closeExportDailyModal() {
    if (userListManagerInstance) {
        userListManagerInstance.closeExportDailyModal();
    }
}
function closeExportMonthlyModal() {
    if (userListManagerInstance) {
        userListManagerInstance.closeExportMonthlyModal();
    }
}
function closeSlackIdSettingsModal() {
    if (userListManagerInstance) {
        userListManagerInstance.closeSlackIdSettingsModal();
    }
}
document.addEventListener('DOMContentLoaded', () => {
    userListManagerInstance = new UserListManager();
});
//# sourceMappingURL=user-list.js.map