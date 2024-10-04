//카드 형태로 데이터 만들기
// 카드 형태로 장바구니 아이템을 생성하여 표시하는 함수
function renderCartItems() {
    const cartContainer = document.getElementById('cartProductsContainer'); // 카드가 추가될 컨테이너
    cartContainer.innerHTML = ''; // 기존 내용을 초기화

    const cart = JSON.parse(localStorage.getItem('cart')) || []; // 로컬 스토리지에서 장바구니 데이터 가져오기

    if (cart.length === 0) {
        cartContainer.innerHTML = '<p class="help">Your cart is empty.</p>';
        return;
    }

    // 각 장바구니 아이템에 대해 카드 형태의 HTML 생성
    cart.forEach(item => {
        const itemCard = document.createElement('div');
        itemCard.classList.add('card', 'cart-item');

        itemCard.innerHTML = `
    <div class="cart-item">
    <div class="card-content">
        <!-- 위쪽 DIV: 체크박스 및 버튼 -->
        <div class="notification is-light" style="display: flex; justify-content: space-between; align-items: center;">
            <!-- 왼쪽: 체크박스 -->
            <input type="checkbox" class="item-checkbox" data-item-id="${item.item_id}" data-item-size="${item.item_size}">
            
            <!-- 오른쪽: 버튼 그룹 -->
            <div class="buttons" style="margin-left: auto;">
                <button class="button is-small">옵션/수량변경</button>
                <button class="button is-small is-danger" onclick="deleteItemFromCart(${item.item_id}, '${item.item_size}')">삭제</button>
            </div>
        </div>

        <!-- 아래쪽 DIV: 상품 정보 -->
        <div class="media">
            <div class="media-left">
                <figure class="image is-64x64">
                    <img src="${item.image_url}" alt="${item.item_name}">
                </figure>
            </div>
            <div class="media-content">
                <p class="title is-5">${item.item_name}</p>
                <p class="subtitle is-6">Size: ${item.item_size} | Color: ${item.item_color}</p>
                <p class="subsubtitle is-6">수량: ${item.item_quantity}</p>
                 <p class="has-text-right">$${item.item_price}</p>
            </div>
        </div>
    </div>
</div>
</div>
        `;

        cartContainer.appendChild(itemCard); // 생성한 카드 HTML을 컨테이너에 추가
    });

    // 체크박스 이벤트 리스너 추가
    document.querySelectorAll('.item-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', () => {
            const totalPrice = calculateTotal();
            document.getElementById('total-price').innerText = `Total Price: $${totalPrice}`;
        });
    });
}

// 페이지 로드 시 장바구니 아이템 카드 표시
document.addEventListener('DOMContentLoaded', () => {
    renderCartItems(); // 장바구니 아이템 표시 함수 호출
});

