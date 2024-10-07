document.addEventListener("DOMContentLoaded", async function() {
    try {
        // Load header.html into #header-placeholder
        const response = await fetch("/common/header.html");
        if (!response.ok) {
            throw new Error("헤더를 불러오는 중 오류가 발생했습니다.");
        }
        const data = await response.text();
        document.getElementById("header-placeholder").innerHTML = data;

        // Load cart items from local storage
        loadCartSummary();
    } catch (error) {
        console.error("헤더를 로드할 수 없습니다:", error);
    }
});

// 로컬 스토리지에서 장바구니 정보 로드 후 결제 정보 출력
function loadCartSummary() {
    const cart = JSON.parse(localStorage.getItem('cart')) || [];
    let itemsHtml = '';
    let totalPrice = 0;

    cart.forEach(item => {
        itemsHtml += `
            <div class="box mb-4">
                <article class="media">
                    <figure class="media-left">
                        <p class="image is-128x128">
                            <img src="${item.image_url}" alt="${item.item_name}">
                        </p>
                    </figure>
                    <div class="media-content">
                        <div class="content">
                            <p><strong>제품명: </strong>${item.item_name}</p>
                            <p><strong>사이즈(mm): </strong>${item.item_size}</p>
                            <p><strong>수량: </strong>${item.item_quantity}개</p>
                        </div>
                    </div>
                </article>
            </div>
        `;
        totalPrice += item.item_price * item.item_quantity;
    });

    const summaryHtml = `
        <p><strong>총 상품 금액: </strong>₩${totalPrice}</p>
        <p><strong>배송비: </strong>₩3000</p>
        <p><strong>총 결제 금액: </strong>₩${totalPrice + 3000}</p>
    `;

    document.getElementById('order-items').innerHTML = itemsHtml;
    document.getElementById('order-summary').innerHTML = summaryHtml;
}

// 주문자 정보와 동일 체크박스 클릭 시 수신자 정보 복사
function copyBuyerInfo() {
    const buyerName = document.getElementById('buyerName').value;
    const buyerContact = document.getElementById('buyerContact').value;

    if (document.getElementById('sameAsBuyer').checked) {
        document.getElementById('recipientName').value = buyerName;
        document.getElementById('recipientContact').value = buyerContact;
    } else {
        document.getElementById('recipientName').value = '';
        document.getElementById('recipientContact').value = '';
    }
}

// 주문하기 버튼을 눌렀을 때 폼 데이터와 장바구니 데이터로 주문 요청
async function placeOrder() {
    const cart = JSON.parse(localStorage.getItem('cart')) || [];
    if (cart.length === 0) {
        alert("장바구니가 비어있습니다.");
        return;
    }

    const orderData = {
        buyerName: document.getElementById('buyerName').value,
        buyerContact: document.getElementById('buyerContact').value,
        recipientName: document.getElementById('recipientName').value,
        recipientContact: document.getElementById('recipientContact').value,
        shippingAddress: document.getElementById('shippingAddress').value,
        additionalAddress: document.getElementById('additionalAddress').value,
        deliveryMessage: document.getElementById('deliveryMessage').value,
        items: cart.map(item => ({
            itemId: item.item_id,
            itemQuantity: item.item_quantity,
            itemSize: item.item_size,
            itemPrice: item.item_price
        }))
    };

    try {
        // POST 요청으로 주문 데이터 전송
        const response = await fetch('/api/orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(orderData)
        });

        if (!response.ok) {
            throw new Error('주문 처리 중 오류가 발생했습니다.');
        }

        const data = await response.json();
        alert("주문이 성공적으로 처리되었습니다!");
        localStorage.removeItem('cart'); // 주문 완료 후 장바구니 비우기
        window.location.href = "/orders"; // 주문 내역 페이지로 이동
    } catch (error) {
        console.error("주문 실패:", error);
        alert("주문에 실패했습니다. 다시 시도해 주세요.");
    }
}
