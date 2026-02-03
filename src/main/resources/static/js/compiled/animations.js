export class Animations {
    static fadeIn(element, duration = 300) {
        element.style.opacity = '0';
        element.style.display = 'block';
        element.style.transition = `opacity ${duration}ms ease-in-out`;
        requestAnimationFrame(() => {
            element.style.opacity = '1';
        });
    }
    static fadeOut(element, duration = 300, callback) {
        element.style.transition = `opacity ${duration}ms ease-in-out`;
        element.style.opacity = '0';
        setTimeout(() => {
            element.style.display = 'none';
            if (callback)
                callback();
        }, duration);
    }
    static slideDown(element, duration = 300) {
        const height = element.scrollHeight + 'px';
        element.style.height = '0';
        element.style.overflow = 'hidden';
        element.style.display = 'block';
        element.style.transition = `height ${duration}ms ease-in-out`;
        requestAnimationFrame(() => {
            element.style.height = height;
        });
        setTimeout(() => {
            element.style.height = 'auto';
        }, duration);
    }
    static slideUp(element, duration = 300, callback) {
        element.style.height = element.scrollHeight + 'px';
        element.style.overflow = 'hidden';
        element.style.transition = `height ${duration}ms ease-in-out`;
        requestAnimationFrame(() => {
            element.style.height = '0';
        });
        setTimeout(() => {
            element.style.display = 'none';
            element.style.height = '';
            if (callback)
                callback();
        }, duration);
    }
    static shake(element) {
        element.style.animation = 'shake 0.5s';
        setTimeout(() => {
            element.style.animation = '';
        }, 500);
    }
    static pulse(element) {
        element.style.animation = 'pulse 0.6s';
        setTimeout(() => {
            element.style.animation = '';
        }, 600);
    }
    static highlight(element, color = '#ffeb3b') {
        const originalBg = element.style.backgroundColor;
        element.style.transition = 'background-color 0.3s ease-in-out';
        element.style.backgroundColor = color;
        setTimeout(() => {
            element.style.backgroundColor = originalBg;
            setTimeout(() => {
                element.style.transition = '';
            }, 300);
        }, 500);
    }
}
export class ModalAnimations {
    static showModal(modal) {
        modal.style.display = 'block';
        const content = modal.querySelector('div[style*="position: absolute"]');
        if (content) {
            content.style.opacity = '0';
            content.style.transform = 'translate(-50%, -60%) scale(0.9)';
            content.style.transition = 'all 0.3s ease-out';
            requestAnimationFrame(() => {
                if (content) {
                    content.style.opacity = '1';
                    content.style.transform = 'translate(-50%, -50%) scale(1)';
                }
            });
        }
        Animations.fadeIn(modal, 200);
    }
    static hideModal(modal, callback) {
        const content = modal.querySelector('div[style*="position: absolute"]');
        if (content) {
            content.style.opacity = '0';
            content.style.transform = 'translate(-50%, -60%) scale(0.9)';
        }
        Animations.fadeOut(modal, 200, callback);
    }
}
export class ButtonAnimations {
    static addHoverEffect(button) {
        button.style.transition = 'all 0.2s ease-in-out';
        button.addEventListener('mouseenter', () => {
            button.style.transform = 'translateY(-2px)';
            button.style.boxShadow = '0 4px 8px rgba(0,0,0,0.2)';
        });
        button.addEventListener('mouseleave', () => {
            button.style.transform = 'translateY(0)';
            button.style.boxShadow = '';
        });
    }
    static addClickEffect(button) {
        button.addEventListener('click', () => {
            Animations.pulse(button);
        });
    }
}
export class TableAnimations {
    static animateRowAppearance(rows) {
        rows.forEach((row, index) => {
            row.style.opacity = '0';
            row.style.transform = 'translateY(20px)';
            row.style.transition = 'all 0.3s ease-out';
            setTimeout(() => {
                row.style.opacity = '1';
                row.style.transform = 'translateY(0)';
            }, index * 50);
        });
    }
}
//# sourceMappingURL=animations.js.map