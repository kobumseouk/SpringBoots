// 로컬 스토리지에 저장하는 함수
function addItemToCart(itemId, itemQuantity, itemColor, itemSize) {
  // 장바구니에 담긴 아이템을 로컬 스토리지에서 가져오기
  const cart = JSON.parse(localStorage.getItem('cart')) || [];

  // 새로운 아이템 객체 생성
  const newItem = {
    itemId: itemId,
    itemQuantity: itemQuantity, // 수량 필드 유지
    itemSize: itemSize, // itemSize가 올바른지 확인
    itemColor: itemColor, // itemColor가 올바른지 확인
  };


  // 장바구니에 아이템 추가 (중복 체크)
  const existingItem = cart.find(item => item.itemId === itemId && item.itemSize === itemSize && item.itemColor === itemColor);
  if (existingItem) {
    // 이미 존재하는 아이템이면 수량만 증가
    existingItem.itemQuantity += itemQuantity;
  } else {
    // 새로운 아이템이면 추가
    cart.push(newItem);
  }

  // 업데이트된 장바구니를 로컬 스토리지에 저장
  localStorage.setItem('cart', JSON.stringify(cart));
}

function deleteItemFromCart(itemId, itemSize, itemColor) {
  const cart = JSON.parse(localStorage.getItem('cart')) || [];

  // 해당 itemId와 itemSize가 아닌 아이템들만 남기기
  const newCart = cart.filter(item =>
      !(item.itemId === itemId && item.itemSize === itemSize && item.itemColor === itemColor));

  if (newCart.length !== cart.length) {
    // 업데이트된 장바구니 저장
    localStorage.setItem('cart', JSON.stringify(newCart));
  } else {
    // 아이템이 존재하지 않으면 404 not found 로그 출력
    console.log("404 not found in deleteItemFromCart");
  }
}


// 장바구니 아이템의 사이즈나 수량을 업데이트하는 함수
function updateItemSizeOrQuantity(itemId, itemSize, new_quantity = null, new_size = null) {
  console.log("updateItemSizeOrQuantity function called");
  // 로컬 스토리지에서 장바구니를 가져오기
  const cart = JSON.parse(localStorage.getItem('cart')) || [];

  // 업데이트할 아이템 찾기
  console.log("Searching for itemId:", itemId, "and itemSize:", itemSize);
  console.log("Current cart:", cart);
  const existingItem = cart.find(item => item.itemId === itemId && item.itemSize === itemSize);

  if (existingItem) {
    // 새로운 수량이 주어지면 업데이트
    if (new_quantity !== null) {
      existingItem.itemQuantity =parseInt(new_quantity);
    }

    // 새로운 사이즈가 주어지면 업데이트
    if (new_size !== null) {
      existingItem.itemSize = parseInt(new_size);
    }

    // 업데이트된 장바구니를 로컬 스토리지에 저장
    localStorage.setItem('cart', JSON.stringify(cart));
    location.reload();
  } else {
    // 아이템이 존재하지 않으면 404 not found 로그 출력
    console.log("404 not found in updateItemSizeOrQuantity");
  }
}

// 선택된 아이템들의 총 가격을 계산하는 함수
function calculateTotal() {
  const selectedItems = getSelectedItems(); // 선택된 아이템 목록 가져오기

  // 선택된 아이템에 대한 가격을 가져오는 Promise 배열 생성
  const promises = selectedItems.map(item => {
    return getData(item.itemId).then(productData => {
      if (productData) {
        // 제품 데이터가 있는 경우 가격을 계산
        return productData.item_price * item.itemQuantity;
      }
      return 0; // 데이터가 없는 경우 0 반환
    }).catch(() => 0); // 오류 발생 시 0 반환
  });

  console.log(promises.length);

  // promises가 비어있으면 0 반환
  if (promises.length === 0) {
    return Promise.resolve(0);
  }

  // 모든 Promise가 완료되면 총 가격 계산
  return Promise.all(promises).then(prices => {
    // 가격 합산, 기본값 0
    return prices.reduce((total, price) => total + price, 0);
  }).catch(() => 0); // 모든 과정에서 문제가 발생해도 기본값 0 반환
}



// 선택된 아이템들 목록 가져오기
function getSelectedItems() {
  const selectedItems = [];
  const checkboxes = document.querySelectorAll('.item-checkbox:checked'); // 체크된 체크박스만 선택

  checkboxes.forEach(checkbox => {
    const itemId = parseInt(checkbox.getAttribute('data-item-id'));
    const itemSize = parseInt(checkbox.getAttribute('data-item-size'));

    // 로컬 스토리지에서 장바구니 정보 가져오기
    const cart = JSON.parse(localStorage.getItem('cart')) || [];
    console.log(`itemId: ${itemId}, itemSize: ${itemSize}`);

    const existingItem = cart.find(item => item.itemId === itemId && item.itemSize === itemSize);

    if (existingItem) {
      selectedItems.push(existingItem); // 선택된 아이템을 배열에 추가
      console.log(`${existingItem.itemId} has been selected`);
    }
  });

  return selectedItems; // 선택된 아이템 목록 반환
}

// 체크박스 변경 시 총 가격을 자동으로 업데이트
document.querySelectorAll('.item-checkbox').forEach(checkbox => {
  checkbox.addEventListener('change', () => {
    calculateTotal().then(totalPrice => {
      document.getElementById('total-price').innerText = `￦${totalPrice}`;
    }).catch(error => {
      console.error('Error calculating total:', error);
      document.getElementById('total-price').innerText = `Total Price: ￦0`; // 오류 발생 시 기본값 설정
    });
  });
});



// 선택된 모든 아이템 삭제
function deleteSelectedItems() {
  const selectedItems = getSelectedItems(); // 선택된 아이템 목록 가져오기
  if (selectedItems.length > 0) {
    const cart = JSON.parse(localStorage.getItem('cart')) || []; // 기존 장바구니 데이터

    // 선택된 아이템들을 장바구니에서 제거
    const updatedCart = cart.filter(item => {
      return !selectedItems.some(selectedItem =>
          selectedItem.itemId === item.itemId && selectedItem.itemSize === item.itemSize);
    });

    // 로컬 스토리지에 업데이트된 장바구니 저장
    localStorage.setItem('cart', JSON.stringify(updatedCart));
    console.log('Selected items have been deleted.');

    // 삭제 후 총 가격 다시 계산
    updateOrderTotal();
    const totalPrice = calculateTotal(updatedCart); // 총 가격 재계산
    document.getElementById('orderTotal').innerText = `￦${totalPrice}`;
    // 장바구니 아이템 다시 렌더링
    renderCartItems();

  } else {
    console.log('No items selected to delete.'); // 선택된 항목이 없을 때
    // 장바구니 아이템 다시 렌더링
    renderCartItems();

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
    getData(item.itemId).then(productData => { // async를 사용하지 않고 Promise로 처리
      if (productData) {
        const itemCard = document.createElement('div');
        itemCard.classList.add('card', 'cart-item');

        itemCard.innerHTML = `
        <div class="cart-item">
          <div class="card-content">
            <div class="notification is-light"  >
              <input type="checkbox" class="item-checkbox" data-item-id="${item.itemId}" data-item-size="${item.itemSize}">
              <div class="buttons" style="margin-left: auto;">
                <button class="button is-small option-change-btn">옵션/수량변경</button>
                <button class="button is-small is-danger delete-btn">삭제</button>
              </div>
            </div>
            <div class="media" style="margin-top: 7px">
              <div class="media-left">
                <figure class="image">
                  <img src="${productData.image_url}" alt="${productData.item_name}" width="80" height="100">
                </figure>
              </div>
              <div class="media-content">
                <p class="title is-5">${productData.item_name}</p>
                <p class="subtitle is-6 item-size" >사이즈(UK): <span class="item-size">${item.itemSize}</span> | Color: ${item.itemColor}</p>
                <p class="subsubtitle is-6 item-quantity">수량: <span class="item-quantity">${item.itemQuantity}</span> 개</p>
                <p class="has-text-right">￦${productData.item_price}</p>
              </div>
            </div>
          </div>
        </div>
        `;

        // 삭제 버튼 이벤트 리스너 추가
        const deleteBtn = itemCard.querySelector('.delete-btn');
        deleteBtn.addEventListener('click', () => {
          const confirmed = confirm('장바구니에서 상품을 삭제하시겠습니까?');
          if (confirmed) {
            deleteItemFromCart(item.itemId, item.itemSize); // 버튼이 눌리면 해당 아이템 삭제
            location.reload(); // 새로고침
          }
        });

        // 옵션/수량변경 버튼 이벤트 리스너 추가
        const optionChangeBtn = itemCard.querySelector('.option-change-btn');
        optionChangeBtn.addEventListener('click', () => {
          // 수량 조절 UI 추가
          const quantityControl = document.createElement('div');
          quantityControl.classList.add('quantity-control');
          quantityControl.innerHTML = `
    <div style="margin-bottom: 10px;">
      <label>수량: </label>
      <button class="button is-small quantity-decrease">-</button>
      <span class="item-quantity">${item.itemQuantity}</span>
      <button class="button is-small quantity-increase">+</button>
    </div>
    <div style="margin-bottom: 10px;">
      <label>사이즈: </label>
      <select class="size-select">
        <option value="${item.itemSize}" selected>${item.itemSize}</option>
        <option value="230">230</option>
        <option value="240">240</option>
        <option value="250">250</option>
        <option value="260">260</option>
        <option value="270">270</option>
        <option value="280">280</option>
        <option value="290">290</option>
      </select>
    </div>
    <button class="button is-success apply-btn">적용</button>
  `;

          // 수량 변경 이벤트
          const decreaseBtn = quantityControl.querySelector('.quantity-decrease');
          const increaseBtn = quantityControl.querySelector('.quantity-increase');
          const quantityDisplay = quantityControl.querySelector('.item-quantity');
          let currentQuantity = item.itemQuantity;

          decreaseBtn.addEventListener('click', () => {
            if (currentQuantity > 1) {
              currentQuantity--;
              quantityDisplay.innerText = currentQuantity;
            }
          });

          increaseBtn.addEventListener('click', () => {
            currentQuantity++;
            quantityDisplay.innerText = currentQuantity;
          });

          // 사이즈 선택 이벤트
          const sizeSelect = quantityControl.querySelector('.size-select');
          sizeSelect.addEventListener('change', () => {
            const selectedSize = sizeSelect.value;
            console.log(`Selected size: ${selectedSize}`);
          });

          // 적용 버튼 클릭 시 이벤트
          const applyBtn = quantityControl.querySelector('.apply-btn');
          applyBtn.addEventListener('click', () => {
            const selectedSize = sizeSelect.value; // 선택된 사이즈 가져오기
            updateItemSizeOrQuantity(item.itemId, item.itemSize, currentQuantity, selectedSize); // 로컬 스토리지 업데이트
            location.reload(); // 새로고침
          });

          // 수량 및 사이즈 조절 UI를 카드에 추가
          const mediaContent = itemCard.querySelector('.media-content');
          mediaContent.appendChild(quantityControl);
        });

        cartContainer.appendChild(itemCard); // itemCard를 컨테이너에 추가
      }
    });
  });

  // 체크박스 이벤트 리스너 추가 (이벤트 위임)
  cartContainer.addEventListener('change', (event) => {
    if (event.target.matches('.item-checkbox')) {
      const selectedItems = getSelectedItems();
      console.log('Selected items:', selectedItems);

      // 총 가격 계산
      updateOrderTotal();
    }
  });
}


// 총 결제금액 업데이트 함수
function updateOrderTotal() {
  calculateTotal().then(totalPrice => {
    const productsTotal = totalPrice; // 총 가격을 productsTotal에 할당
    console.log(`￦${productsTotal}`);

    let deliveryTotal = 0; // 배달 총액 초기화
    let discountTotal = 0; // 할인 총액 초기화
    let orderTotal = productsTotal + deliveryTotal - discountTotal; // 주문 총액 계산

    // HTML에 값 넣기
    document.getElementById('productsTotal').innerText = `￦${productsTotal}`;
    document.getElementById('deliveryTotal').innerText = `￦${deliveryTotal}`;
    document.getElementById('discountTotal').innerText = `￦${discountTotal}`;
    document.getElementById('orderTotal').innerText = `￦${orderTotal}`;
  }).catch(error => {
    console.error('Error calculating total:', error);
    // 오류 발생 시, 기본값 설정
    document.getElementById('productsTotal').innerText = `￦0`;
    document.getElementById('deliveryTotal').innerText = `￦0`;
    document.getElementById('discountTotal').innerText = `￦0`;
    document.getElementById('orderTotal').innerText = `￦0`;
  });
}


// 페이지 로드 시 장바구니 아이템 카드 표시
document.addEventListener('DOMContentLoaded', () => {
  updateOrderTotal();
  renderCartItems(); // 장바구니 아이템 표시 함수 호출
});

// 전체선택 체크박스 이벤트 추가
const allSelectCheckbox = document.getElementById('allSelectCheckbox');
allSelectCheckbox.addEventListener('change', function () {
  const itemCheckboxes = document.querySelectorAll('.item-checkbox'); // 모든 아이템 체크박스
  itemCheckboxes.forEach(checkbox => {
    checkbox.checked = allSelectCheckbox.checked; // 전체선택의 상태에 따라 개별 체크박스 설정
  });
    const selectedItems = getSelectedItems();
    console.log('All Selected items:', selectedItems);

    // 총 가격 계산
    updateOrderTotal();
});

// 개별 체크박스와 전체 선택 동기화 (모든 체크박스가 선택되었을 때 전체 선택도 체크되도록)
function syncAllSelectCheckbox() {
  const itemCheckboxes = document.querySelectorAll('.item-checkbox');
  const allChecked = Array.from(itemCheckboxes).every(checkbox => checkbox.checked);
  allSelectCheckbox.checked = allChecked;
}

document.getElementById('deleteSelectedButton').addEventListener('click', () => {
  const confirmed = confirm('선택된 아이템을 삭제하시겠습니까?');

  if (confirmed) {
    deleteSelectedItems(); // 선택된 아이템 삭제
  }
});

//header.html 불러오기
document.addEventListener("DOMContentLoaded", async function() {
  try {
    // Load header.html into #header-placeholder
    const response = await fetch("/common/header.html");
    if (!response.ok) {
      throw new Error("헤더를 불러오는 중 오류가 발생했습니다.");
    }
    const data = await response.text();
    document.getElementById("header-placeholder").innerHTML = data;

  } catch (error) {
    console.error("헤더를 로드할 수 없습니다:", error);
  }
});

async function getData(itemId) {
  console.log('Received itemId:', itemId); // itemId 값 확인
  const loc = `/api/items/${itemId}`; // URL 생성
  console.log('loc:', loc);

  try {
    const res = await fetch(loc); // API 호출
    if (!res.ok) {
      throw new Error('Network response was not ok');
    }

    const data = await res.json(); // JSON 데이터 파싱
    console.log(data);

    // 필요한 데이터 추출
    const { itemName, itemPrice, imageUrl } = data;
    const item_name = itemName;
    const item_price = itemPrice;
    const image_url = imageUrl;

    // 결과 반환
    return { item_name, item_price, image_url };
  } catch (error) {
    console.error('Failed to fetch data:', error); // 오류 처리
    return null; // 오류가 발생한 경우 null 반환
  }
}
