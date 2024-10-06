// 로컬 스토리지에 저장하는 함수

// 사용 예시
const itemData = {
  item_id: 101,
  item_quantity: 2,
  item_size: 280,
  item_name: 'Cool Sneaker',
  item_price: 120,
  item_color: 'black',
  image_url: 'path/to/image.jpg' // 이미지 URL 추가
};

const itemData1 = {
  item_id: 102,
  item_quantity: 5,
  item_size: 270,
  item_name: 'Bdn',
  item_price: 140,
  item_color: 'white',
  image_url: 'path/to/image.jpg' // 이미지 URL 추가
};

function addItemToCart(item_name, item_price, item_id, item_quantity, item_color, item_size, image_url) {
  // 장바구니에 담긴 아이템을 로컬 스토리지에서 가져오기
  let cart = JSON.parse(localStorage.getItem('cart')) || [];

  // 새로운 아이템 객체 생성
  const newItem = {
    item_id: item_id,
    item_quantity: item_quantity, // 수량 필드 유지
    item_size: item_size, // item_size가 올바른지 확인
    item_name: item_name,
    item_price: item_price,
    item_color: item_color, // item_color가 올바른지 확인
    image_url: image_url // 이미지 URL 추가
  };


  // 장바구니에 아이템 추가 (중복 체크)
  const existingItemIndex = cart.findIndex(item => item.item_id === item_id && item.item_size === item_size);
  if (existingItemIndex > -1) {
    // 이미 존재하는 아이템이면 수량만 증가
    cart[existingItemIndex].item_quantity += item_quantity; // item_quantity로 수정
  } else {
    // 새로운 아이템이면 추가
    cart.push(newItem);
  }

  // 업데이트된 장바구니를 로컬 스토리지에 저장
  localStorage.setItem('cart', JSON.stringify(cart));
}

function deleteItemFromCart(item_id, item_size) {
  let cart = JSON.parse(localStorage.getItem('cart')) || [];

  // 삭제할 아이템 찾기 (item_id와 item_size로 식별)
  const itemIndex = cart.findIndex(item => item.item_id === item_id && item.item_size === item_size);

  if (itemIndex > -1) {
    cart.splice(itemIndex, 1);
    localStorage.setItem('cart', JSON.stringify(cart));
  } else {
    // 아이템이 존재하지 않으면 404 not found 로그 출력
    console.log("404 not found in  deleteItemFromCart");
  }
}

// 장바구니 아이템의 사이즈나 수량을 업데이트하는 함수
function updateItemSizeOrQuantity(item_id, item_size, new_quantity = null, new_size = null) {
  console.log("updateItemSizeOrQuantity function called");
  // 로컬 스토리지에서 장바구니를 가져오기
  let cart = JSON.parse(localStorage.getItem('cart')) || [];

  // 업데이트할 아이템 찾기
  console.log("Searching for item_id:", item_id, "and item_size:", item_size);
  console.log("Current cart:", cart);
  const itemIndex = cart.findIndex(item => item.item_id === item_id && item.item_size === item_size);

  if (itemIndex > -1) {
    // 새로운 수량이 주어지면 업데이트
    if (new_quantity !== null) {
      cart[itemIndex].item_quantity = new_quantity;
    }

    // 새로운 사이즈가 주어지면 업데이트
    if (new_size !== null) {
      cart[itemIndex].item_size = new_size;
    }

    // 업데이트된 장바구니를 로컬 스토리지에 저장
    localStorage.setItem('cart', JSON.stringify(cart));
  } else {
    // 아이템이 존재하지 않으면 404 not found 로그 출력
    console.log("404 not found in updateItemSizeOrQuantity");
  }
}

// 선택된 아이템들의 총 가격을 계산하는 함수
export function calculateTotal() {
  const selectedItems = getSelectedItems(); // 선택된 아이템 목록 가져오기

  // 총 가격 초기화
  let totalPrice = 0;

  // 선택된 아이템의 가격 계산
  selectedItems.forEach(item => {
    totalPrice += item.item_price * item.item_quantity; // 각 아이템의 가격과 수량을 곱해서 총 가격에 추가
  });

  return totalPrice; // 계산된 총 가격 반환
}

// 선택된 아이템들 목록 가져오기
export function getSelectedItems() {
  const selectedItems = [];
  const checkboxes = document.querySelectorAll('.item-checkbox:checked'); // 체크된 체크박스만 선택

  checkboxes.forEach(checkbox => {
    const itemId = checkbox.getAttribute('data-item-id');
    const itemSize = checkbox.getAttribute('data-item-size');

    // 로컬 스토리지에서 장바구니 정보 가져오기
    const cart = JSON.parse(localStorage.getItem('cart')) || [];
    const item = cart.find(cartItem => cartItem.item_id == itemId && cartItem.item_size == itemSize);

    if (item) {
      selectedItems.push(item); // 선택된 아이템을 배열에 추가
      console.log(`${item.item_name} has been selected`);
    }
  });

  return selectedItems; // 선택된 아이템 목록 반환
}

// 체크박스 변경 시 총 가격을 자동으로 업데이트
document.querySelectorAll('.item-checkbox').forEach(checkbox => {
  checkbox.addEventListener('change', () => {
    const totalPrice = calculateTotal();
    document.getElementById('total-price').innerText = `Total Price: ￦${totalPrice}`;
  });
});


// 선택된 모든 아이템 삭제
function deleteSelectedItems() {
  const selectedItems = getSelectedItems(); // 체크박스 선택된 아이템 목록 가져오기

  // 선택된 아이템 목록이 있는지 확인
  if (selectedItems.length > 0) {
    selectedItems.forEach(item => {
      deleteItemFromCart(item.item_id, item.item_size); // 각 선택된 아이템 삭제
      console.log(`${item.item_id} with size of ${item.item_size} have been deleted`);
    });

    // 삭제 후 총 가격 다시 계산
    const totalPrice = calculateTotal(getSelectedItems()); // 총 가격 재계산
    document.getElementById('total-price').innerText = `Total Price: $${totalPrice}`; // 화면에 표시
  } else {
    console.log("No items selected to delete."); // 선택된 항목이 없을 때
  }
}

//화면 renderㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
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
                <button class="button is-small is-danger delete-btn" onclick="deleteItemFromCart(${item.item_id}, '${item.item_size}')">삭제</button>
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
    // 삭제 버튼 이벤트 리스너 추가
    const deleteBtn = itemCard.querySelector('.delete-btn');
    deleteBtn.addEventListener('click', () => {
      alert('장바구니에서 상품을 삭제하시겠습니까?');
      deleteItemFromCart(item.item_id, item.item_size); // 버튼이 눌리면 해당 아이템 삭제
      renderCartItems();    //TODO: 새로고침으로 바꾸기
    });
    cartContainer.appendChild(itemCard);
  });

// 체크박스 이벤트 리스너 추가
  document.querySelectorAll('.item-checkbox').forEach(checkbox => {
    checkbox.addEventListener('change', () => {
      // 1. 선택된 아이템 정보 가져오기
      const selectedItems = getSelectedItems();
      console.log('Selected items:', selectedItems);

      // 2. 총 가격 계산
      updateOrderTotal();
    });
  });

}

// 총 결제금액 업데이트 함수
function updateOrderTotal() {
  const totalPrice = calculateTotal(); // calculateTotal 함수로 총 가격 계산
  document.getElementById('orderTotal').innerText = `￦${totalPrice}`; // HTML에 값 넣기
}

// 페이지 로드 시 장바구니 아이템 카드 표시
document.addEventListener('DOMContentLoaded', () => {
  updateOrderTotal();
  renderCartItems(); // 장바구니 아이템 표시 함수 호출
});





// 장바구니에 아이템 추가
addItemToCart(itemData.item_name, itemData.item_price, itemData.item_id, itemData.item_quantity, itemData.item_color, itemData.item_size, itemData.image_url);
addItemToCart(itemData1.item_name, itemData1.item_price, itemData1.item_id, itemData1.item_quantity, itemData1.item_color, itemData1.item_size,  itemData1.image_url);
