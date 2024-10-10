document.addEventListener("DOMContentLoaded", async function() {
    try {
        // URL에서 orderId 파라미터 추출
        const urlParams = new URLSearchParams(window.location.search);
        const orderId = urlParams.get('orderId'); // ?orderId= 뒤의 값을 가져옴

        if (!orderId) {
            // 주문 ID가 없으면 오류 메시지 표시
            document.getElementById('order-summary-root').innerHTML = `<div class="notification is-danger">주문 ID가 없습니다.</div>`;
            return;
        }

        // 헤더 불러오기
        const response = await fetch("/common/header.html");
        if (!response.ok) {
            throw new Error("헤더를 불러오는 중 오류가 발생했습니다.");
        }
        const data = await response.text();
        document.getElementById("header-placeholder").innerHTML = data;

        // 주문 요약 정보 불러오기
        await loadOrderSummary(orderId);
    } catch (error) {
        console.error("헤더를 로드할 수 없습니다:", error);
    }
});

// 주문 요약 정보를 로드하는 함수
async function loadOrderSummary(orderId) {
    try {
        const response = await fetch(`/api/orders/${orderId}`);
        if (!response.ok) {
            throw new Error('주문 정보를 가져오는 중 오류가 발생했습니다.');
        }
        const order = await response.json();
        renderOrderSummary(order);
    } catch (error) {
        document.getElementById('order-summary-root').innerHTML = `<div class="notification is-danger">${error.message}</div>`;
    }
}

// 주문 요약 정보를 렌더링하는 함수
function renderOrderSummary(order) {
    const orderSummaryRoot = document.getElementById('order-summary-root');

    if (!order) {
        orderSummaryRoot.innerHTML = `<div class="notification is-info">주문 정보를 불러오는 중입니다...</div>`;
        return;
    }

    let itemsHtml = '';
    order.items.forEach((item) => {
        itemsHtml += `
            <div class="box mb-4">
                <article class="media">
                    <figure class="media-left">
                        <p class="image is-128x128">
                            <img src="${item.itemImage}" alt="${item.itemName}">
                        </p>
                    </figure>
                    <div class="media-content">
                        <div class="content">
                            <p><strong>제품명: </strong>${item.itemName}</p>
                            <p><strong>사이즈(mm): </strong>${item.itemsSize}</p>
                            <p><strong>수량: </strong>${item.orderitemsQuantity}개</p>
                        </div>
                    </div>
                </article>
            </div>
        `;
    });

    orderSummaryRoot.innerHTML = `
        <div class="box">
            <div class="has-background-dark has-text-white has-text-centered p-4 mb-4">
                <h2 class="title is-4">주문 완료</h2>
            </div>
            <div class="content">
                <h5 class="title is-5">주문상품 정보</h5>
                ${itemsHtml}

                <hr />

                <h5 class="title is-5">주문자</h5>
                <p class="mb-1">${order.recipientName}</p>
                <p class="mb-1">${order.recipientContact}</p>

                <hr />

                <h5 class="title is-5">배송지</h5>
                <p class="mb-1">${order.recipientName}</p>
                <p class="mb-1">${order.shippingAddress}</p>
                <p>${order.recipientContact}</p>

                <hr />

                <h5 class="title is-5">결제 금액</h5>
                <p class="mb-1"><strong>총 상품 금액: </strong>₩${order.ordersTotalPrice}</p>
                <p class="mb-1"><strong>배송비: </strong>₩${order.deliveryFee}</p>
                <p><strong>총 결제 금액: </strong>₩${order.ordersTotalPrice + order.deliveryFee}</p>

                <div class="mt-4 has-text-centered">
                    <button class="button is-dark" onclick="window.location.href = '/order-details/order-details.html?id=${order.ordersId}'">주문내역 바로가기</button>
                    <button class="button is-outlined is-dark" onclick="window.location.href = '/shop'">쇼핑 계속하기</button>
                </div>
            </div>
        </div>
    `;
}
