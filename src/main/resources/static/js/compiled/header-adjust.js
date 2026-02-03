// ヘッダーの高さに応じてコンテナのpadding-topを動的に調整
document.addEventListener('DOMContentLoaded', function() {
    function adjustContainerPadding() {
        const header = document.querySelector('header');
        const container = document.querySelector('.container');
        if (header && container) {
            const headerHeight = header.offsetHeight;
            // ヘッダーの高さ + 余白20px
            container.style.paddingTop = (headerHeight + 20) + 'px';
        }
    }
    
    // 初回実行（少し遅延させてレンダリング後に実行）
    setTimeout(adjustContainerPadding, 100);
    
    // リサイズ時にも調整
    window.addEventListener('resize', function() {
        setTimeout(adjustContainerPadding, 50);
    });
    
    // 画像やフォントの読み込み後にも調整
    window.addEventListener('load', adjustContainerPadding);
});
