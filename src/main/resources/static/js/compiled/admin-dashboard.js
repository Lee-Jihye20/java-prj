"use strict";
class AdminDashboardTabs {
    constructor() {
        this.activeTab = 'overview';
        this.tabs = new Map();
        this.tabContents = new Map();
        this.init();
    }
    init() {
        this.setupTabs();
        this.setupMenuDropdown();
        this.setupInitialTab();
        this.setupViewModeToggle();
    }
    setupTabs() {
        const tabButtons = document.querySelectorAll('.tab-button');
        const tabPanels = document.querySelectorAll('.tab-panel');
        tabButtons.forEach((button) => {
            const tabId = button.getAttribute('data-tab');
            if (tabId) {
                this.tabs.set(tabId, button);
                button.addEventListener('click', () => {
                    this.switchTab(tabId);
                });
            }
        });
        tabPanels.forEach((panel) => {
            const tabId = panel.getAttribute('data-tab');
            if (tabId) {
                this.tabContents.set(tabId, panel);
            }
        });
    }
    switchTab(tabId) {
        if (this.activeTab === tabId)
            return;
        const previousTab = this.tabs.get(this.activeTab);
        const currentTab = this.tabs.get(tabId);
        const previousContent = this.tabContents.get(this.activeTab);
        const currentContent = this.tabContents.get(tabId);
        if (previousTab && currentTab && previousContent && currentContent) {
            previousTab.classList.remove('active');
            previousContent.classList.remove('active');
            currentTab.classList.add('active');
            currentContent.classList.add('active');
            this.activeTab = tabId;
            this.animateTabSwitch(currentContent);
        }
    }
    animateTabSwitch(element) {
        element.style.opacity = '0';
        element.style.transform = 'translateY(10px)';
        requestAnimationFrame(() => {
            element.style.transition = 'opacity 0.3s ease-out, transform 0.3s ease-out';
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        });
    }
    setupMenuDropdown() {
        const menuButton = document.getElementById('menu-dropdown-button');
        const menuDropdown = document.getElementById('menu-dropdown');
        const menuItems = document.querySelectorAll('.menu-dropdown-item');
        if (menuButton && menuDropdown) {
            menuButton.addEventListener('click', (e) => {
                e.stopPropagation();
                this.positionMenuDropdown(menuButton, menuDropdown);
                menuDropdown.classList.toggle('show');
            });
            menuDropdown.addEventListener('click', (e) => {
                e.stopPropagation();
            });
            document.addEventListener('click', (e) => {
                const target = e.target;
                if (!menuDropdown.contains(target) && !menuButton.contains(target)) {
                    menuDropdown.classList.remove('show');
                }
            });
            menuItems.forEach((item) => {
                item.addEventListener('click', (e) => {
                    e.stopPropagation();
                    setTimeout(() => {
                        menuDropdown.classList.remove('show');
                    }, 100);
                });
            });
            window.addEventListener('resize', () => {
                if (menuDropdown.classList.contains('show')) {
                    this.positionMenuDropdown(menuButton, menuDropdown);
                }
            });
            window.addEventListener('scroll', () => {
                if (menuDropdown.classList.contains('show')) {
                    this.positionMenuDropdown(menuButton, menuDropdown);
                }
            });
        }
    }
    positionMenuDropdown(button, dropdown) {
        const buttonRect = button.getBoundingClientRect();
        const headerRect = button.closest('.dashboard-tabs-header')?.getBoundingClientRect();
        if (headerRect) {
            dropdown.style.top = `${headerRect.bottom + 4}px`;
            dropdown.style.right = `${window.innerWidth - buttonRect.right}px`;
        }
    }
    setupInitialTab() {
        const urlParams = new URLSearchParams(window.location.search);
        const tabParam = urlParams.get('tab');
        if (tabParam && this.tabs.has(tabParam)) {
            this.switchTab(tabParam);
        }
        else {
            this.switchTab('overview');
        }
    }
    setupViewModeToggle() {
        const simpleView = document.getElementById('simpleView');
        const detailView = document.getElementById('detailView');
        const simpleBtn = document.getElementById('viewModeSimple');
        const detailBtn = document.getElementById('viewModeDetail');
        if (!simpleView || !detailView || !simpleBtn || !detailBtn)
            return;
        const switchMode = (mode) => {
            if (mode === 'simple') {
                simpleView.style.display = 'block';
                detailView.style.display = 'none';
                simpleBtn.classList.remove('btn-secondary');
                simpleBtn.classList.add('btn-primary');
                detailBtn.classList.remove('btn-primary');
                detailBtn.classList.add('btn-secondary');
            }
            else {
                simpleView.style.display = 'none';
                detailView.style.display = 'block';
                detailBtn.classList.remove('btn-secondary');
                detailBtn.classList.add('btn-primary');
                simpleBtn.classList.remove('btn-primary');
                simpleBtn.classList.add('btn-secondary');
            }
        };
        simpleBtn.addEventListener('click', () => switchMode('simple'));
        detailBtn.addEventListener('click', () => switchMode('detail'));
        const setInitialViewMode = () => {
            const isMobile = window.innerWidth <= 480;
            switchMode(isMobile ? 'simple' : 'detail');
        };
        setInitialViewMode();
        window.addEventListener('resize', setInitialViewMode);
    }
}
document.addEventListener('DOMContentLoaded', () => {
    new AdminDashboardTabs();
});
//# sourceMappingURL=admin-dashboard.js.map