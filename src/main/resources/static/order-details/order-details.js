let currentOrder = null; // 현재 주문 정보를 저장할 전역 변수

function getOrderIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('orderId');
}

document.addEventListener("DOMContentLoaded", async function() {
    try {
        // Load header.html into #header-placeholder
        const headerResponse = await fetch("/common/header.html");
        if (!headerResponse.ok) {
            throw new Error("헤더를 불러오는 중 오류가 발생했습니다.");
        }
        const headerData = await headerResponse.text();
        document.getElementById("header-placeholder").innerHTML = headerData;

        // Extract order ID from the URL and load order summary
        const orderId = getOrderIdFromUrl();
        if (orderId) {
            await loadOrderSummary(orderId);
        }
    } catch (error) {
        console.error("헤더를 로드할 수 없습니다:", error);
    }
});

async function loadOrderSummary(orderId) {
    try {
        const response = await fetch(`/api/orders/${orderId}`);
        // 404 상태일 때
        if (response.status === 404) {
            alert("유효하지 않은 주문번호입니다.");
            // 메인 페이지나 주문 목록 페이지로 이동
            window.location.href = "/order-list";  // 예: 주문 목록 페이지로 이동
            return;
        }
        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.errorMessage || '주문 정보를 가져오는 중 오류가 발생했습니다.');
        }
        const order = await response.json();
        currentOrder = order; // 현재 주문 정보를 전역 변수에 저장
        renderOrderSummary(order);
    } catch (error) {
        document.getElementById('order-summary-root').innerHTML = `<div class="notification is-danger">${error.message}</div>`;
    }
}

function renderOrderSummary(order) {
    const orderSummaryRoot = document.getElementById('order-summary-root');

    if (!order) {
        orderSummaryRoot.innerHTML = "주문 정보를 불러오는 중입니다...";
        return;
    }

    let itemsHtml = '';
    order.items.forEach((item) => {
        itemsHtml += `
            <div class="box mb-4">
                <div class="media">
                    <figure class="media-left">
                        <p class="image is-128x128">
                            <img src="${item.itemImage}" alt="${item.itemName}">
                        </p>
                    </figure>
                    <div class="media-content">
                        <div class="content">
                            <p><strong>제품명: </strong>${item.itemName}</p>
                            <p><strong>사이즈(mm): </strong>${item.itemsSize}</p>
                            <p><strong>수량: </strong>${item.orderItemsQuantity}개</p>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });

    orderSummaryRoot.innerHTML = `
        <div class="box">
            <h2 class="title is-4 has-text-centered">주문 배송 상세</h2>
            <div class="content">
                <h5 class="title is-5">주문상품 정보</h5>
                ${itemsHtml}

                <hr />

                <h5 class="title is-5">주문자</h5>
                <p class="mb-1">${order.recipientName}</p>
                <p class="mb-1">${order.recipientContact}</p>

                <hr />

                <h5 class="title is-5">주문 상태</h5>
                <p><strong>주문 상태: </strong>${order.orderStatus}</p>

                <hr />

                <h5 class="title is-5">배송지</h5>
                <p class="mb-1">${order.recipientName}</p>
                <p class="mb-1">${order.shippingAddress}</p>
                <p>${order.recipientContact}</p>

                ${
        order.orderStatus !== "Shipped"
            ? `<div class="mt-3">
                            <button class="button is-link is-light" onclick="editShippingAddress()">배송지 수정</button>
                          </div>`
            : ""
    }

                <hr />

                <h5 class="title is-5">결제 금액</h5>
                <p class="mb-1"><strong>총 상품 금액: </strong>${addCommas(order.ordersTotalPrice)}원</p>
                <p class="mb-1"><strong>배송비: </strong>${addCommas(order.deliveryFee)}원</p>
                <p><strong>총 결제 금액: </strong>${addCommas(order.ordersTotalPrice + order.deliveryFee)}원</p>
            </div>
        </div>
    `;
}

function editShippingAddress() {
    // 모달 열기
    document.getElementById("edit-shipping-modal").classList.add("is-active");

    // 현재 배송 정보를 모달 폼에 채우기
    document.getElementById("edit-recipient-name").value = currentOrder.recipientName;
    document.getElementById("edit-shipping-address").value = currentOrder.shippingAddress;
    document.getElementById("edit-recipient-contact").value = currentOrder.recipientContact;
}

function closeShippingModal() {
    document.getElementById("edit-shipping-modal").classList.remove("is-active");
}

async function submitShippingUpdate() {
    let updateOrderRequest = {
        recipient_name: document.getElementById("edit-recipient-name").value,
        shipping_address: document.getElementById("edit-shipping-address").value + " " + document.getElementById("edit-shipping-address2").value,
        recipient_contact: document.getElementById("edit-recipient-contact").value,
    };

    // 유효성 검사 추가
    if (!updateOrderRequest.recipient_name || !updateOrderRequest.shipping_address || !updateOrderRequest.recipient_contact) {
        alert('모든 필수 정보를 입력해주세요: 받는 분, 주소, 전화번호');
        return;
    }

    try {
        const response = await fetch(`/api/orders/${currentOrder.ordersId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(updateOrderRequest)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.errorMessage || '배송 정보를 업데이트하는 중 오류가 발생했습니다.');
        }

        closeShippingModal();
        await loadOrderSummary(currentOrder.ordersId); // 업데이트된 주문 정보 다시 로드
    } catch (error) {
        alert(`오류 발생: ${error.message}`);
    }
}

function openDaumPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            // 검색 결과에서 선택된 주소를 가져와서 입력
            document.getElementById("edit-shipping-address").value = data.address;
        }
    }).open();
}

const addCommas = (n) => {
    return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};
