document.addEventListener("DOMContentLoaded", function () {
        const sidebarHTML = `
    <div id="sidebar" class="box p-4">
        <h4 class="title is-4">MYPAGE</h4>
        <ul class="sidebar-menu">
            <li class="sidebar-section mb-4">
                <p class="title is-5 mb-3">나의 쇼핑</p>
                <ul class="section-menu">
                    <li><a href="/order-list">주문/배송</a></li>
                    <li><a href="/cancel-return">취소/환불</a></li>
                    <li><a href="/recent-viewed">최근 본 상품</a></li>
                </ul>
            </li>
            <li class="sidebar-section">
                <p class="title is-5 mb-3">나의 정보</p>
                <ul class="section-menu">
                    <li><a href="/">회원정보 수정</a></li>
                    <li><a href="/account/signout">회원탈퇴</a></li>
                </ul>
            </li>
        </ul>
    </div>
    `;

        document.getElementById("sidebar-placeholder").innerHTML = sidebarHTML;
});
