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

    try {
        // 로그인된 사용자 정보 불러오기
        const response = await fetch("/api/users-info");
        if (!response.ok) {
            throw new Error("사용자 정보를 불러오는 도중 오류가 발생했습니다.");
        }
        const userData = await response.json();

        // 사용자 정보로 주문자 정보 자동 채우기
        document.getElementById('buyerName').value = userData.username;
        document.getElementById('buyerContact').value = userData.userInfoList[0].phone;

        // 주문자 정보 동일 체크박스 클릭 시 수신자 정보로 복사
        document.getElementById('sameAsBuyer').addEventListener('change', function() {
            if (this.checked) {
                document.getElementById('recipientName').value = userData.username;
                document.getElementById('recipientContact').value = userData.userInfoList[0].phone;
                document.getElementById('shippingAddress').value = userData.userInfoList[0].streetAddress;
                document.getElementById('shippingAddress2').value = userData.userInfoList[0].detailedAddress;
            } else {
                document.getElementById('recipientName').value = '';
                document.getElementById('recipientContact').value = '';
                document.getElementById('shippingAddress').value = '';
                document.getElementById('shippingAddress2').value = '';
            }
        });

    } catch (error) {
        console.error("사용자 정보를 로드할 수 없습니다:", error);
    }
});

// 로컬 스토리지에서 장바구니 정보 로드 후 결제 정보 출력
async function loadCartSummary() {
    const cart = JSON.parse(localStorage.getItem('cart')) || [];
    let itemsHtml = '';
    let totalPrice = 0;

    for (const item of cart) {
        // API를 사용하여 item_id로 아이템 정보 가져오기
        const productData = await getData(item.item_id);

        // 가져온 데이터가 없을 경우 continue
        if (!productData) continue;

        itemsHtml += `
            <div class="box mb-4">
                <article class="media">
                    <figure class="media-left">
                        <p class="image is-128x128">
                            <img src="${productData.image_url}" alt="${productData.item_name}">
                        </p>
                    </figure>
                    <div class="media-content">
                        <div class="content">
                            <p><strong>제품명: </strong>${productData.item_name}</p>
                            <p><strong>사이즈(mm): </strong>${item.item_size}</p>
                            <p><strong>수량: </strong>${item.item_quantity}개</p>
                        </div>
                    </div>
                </article>
            </div>
        `;
        totalPrice += productData.item_price * item.item_quantity;
    }

    const summaryHtml = `
        <p><strong>총 상품 금액: </strong>₩${totalPrice}</p>
        <p><strong>배송비: </strong>₩0</p>
        <p><strong>총 결제 금액: </strong>₩${totalPrice}</p>
    `;

    document.getElementById('order-items').innerHTML = itemsHtml;
    document.getElementById('order-summary').innerHTML = summaryHtml;
}

// API로부터 item_id를 이용해 상품 데이터를 가져오는 함수
async function getData(item_id) {
    const loc = `/api/items/${item_id}`;

    try {
        const res = await fetch(loc); // API 호출
        if (!res.ok) {
            throw new Error('Network response was not ok');
        }

        const data = await res.json(); // JSON 데이터 파싱
        const { itemName, itemPrice, imageUrl } = data;

        // 로그 추가: 받아온 상품 정보 확인
        console.log("상품 정보:", { itemName, itemPrice, imageUrl });

        // 결과 반환
        return { item_name: itemName, item_price: itemPrice, image_url: imageUrl };
    } catch (error) {
        console.error('Failed to fetch data:', error); // 오류 처리
        return null; // 오류가 발생한 경우 null 반환
    }
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
        shippingAddress: document.getElementById("shippingAddress").value + " " + document.getElementById("shippingAddress2").value,
        deliveryMessage: document.getElementById('deliveryMessage').value,
        items: await Promise.all(cart.map(async (item) => {
            const productData = await getData(item.item_id); // 상품 데이터 가져오기
            return {
                itemId: item.item_id,
                itemQuantity: item.item_quantity,
                itemSize: item.item_size,
                itemPrice: productData.item_price // 상품 가격 설정
            };
        }))
    };

    // 로그 추가: 주문 데이터 확인
    console.log("주문 데이터: ", orderData);

    try {
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
        const orderId = data.ordersId;

        window.location.href = `/order-summary?orderId=${orderId}`; // 주문 내역 페이지로 이동

    } catch (error) {
        console.error("주문 실패:", error);
        alert("주문에 실패했습니다. 다시 시도해 주세요.");
    }
}

function openDaumPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            // 검색 결과에서 선택된 주소를 가져와서 입력
            document.getElementById("shippingAddress").value = data.address;
        }
    }).open();
}
