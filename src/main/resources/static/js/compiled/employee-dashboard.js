"use strict";
class EmployeeDashboardTabs {
    constructor() {
        this.activeTab = 'work-status';
        this.tabs = new Map();
        this.tabContents = new Map();
        this.init();
    }
    init() {
        this.setupTabs();
        this.setupMenuDropdown();
        this.setupInitialTab();
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
            if (tabId === 'monthly-report') {
                setTimeout(() => {
                    this.initializeWorkHoursChart();
                }, 100);
            }
        }
    }
    initializeWorkHoursChart() {
        const canvas = document.getElementById('workHoursTrendChart');
        if (!canvas)
            return;
        const ctx = canvas.getContext('2d');
        if (!ctx)
            return;
        const existingChart = window.Chart?.getChart?.(canvas);
        if (existingChart) {
            existingChart.destroy();
        }
        const dailyReports = window.monthlyReportData || [];
        if (!dailyReports || dailyReports.length === 0) {
            ctx.font = '16px Arial';
            ctx.fillStyle = '#666';
            ctx.textAlign = 'center';
            ctx.fillText('データがありません', canvas.width / 2, canvas.height / 2);
            return;
        }
        const sortedReports = [...dailyReports].sort((a, b) => {
            const dateA = new Date(a.date);
            const dateB = new Date(b.date);
            return dateA.getTime() - dateB.getTime();
        });
        const labels = sortedReports.map((report) => {
            const date = new Date(report.date);
            return `${date.getMonth() + 1}/${date.getDate()}`;
        });
        const workHoursData = sortedReports.map((report) => {
            return parseFloat((report.workMinutes / 60.0).toFixed(1));
        });
        const overtimeData = sortedReports.map((report) => {
            return parseFloat((report.overtimeMinutes / 60.0).toFixed(1));
        });
        const Chart = window.Chart;
        if (!Chart) {
            console.error('Chart.js is not loaded');
            return;
        }
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                        label: '勤務時間',
                        data: workHoursData,
                        borderColor: 'rgb(75, 192, 192)',
                        backgroundColor: 'rgba(75, 192, 192, 0.2)',
                        tension: 0.1,
                        fill: true
                    }, {
                        label: '残業時間',
                        data: overtimeData,
                        borderColor: 'rgb(255, 99, 132)',
                        backgroundColor: 'rgba(255, 99, 132, 0.2)',
                        tension: 0.1,
                        fill: true
                    }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: '時間 (h)'
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: '日付'
                        }
                    }
                }
            }
        });
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
            this.switchTab('work-status');
        }
    }
}
document.addEventListener('DOMContentLoaded', () => {
    new EmployeeDashboardTabs();
});
//# sourceMappingURL=employee-dashboard.js.map