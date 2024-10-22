// 로컬 스토리지에 저장하는 함수
function addItemToCart(itemId, itemQuantity, itemColor, itemSize) {
  const cart = JSON.parse(localStorage.getItem('cart')) || [];
  const newItem = {
    itemId: itemId,
    itemQuantity: itemQuantity,
    itemSize: itemSize,
    itemColor: itemColor,
  };
  const existingItem = cart.find(item => item.itemId === itemId && item.itemSize === itemSize && item.itemColor === itemColor);
  if (existingItem) {
    existingItem.itemQuantity += itemQuantity;
  } else {
    cart.push(newItem);
  }
  localStorage.setItem('cart', JSON.stringify(cart));
}

function deleteItemFromCart(itemId, itemSize, itemColor) {
  const cart = JSON.parse(localStorage.getItem('cart')) || [];
  const newCart = cart.filter(item =>
      !(item.itemId === itemId && item.itemSize === itemSize && item.itemColor === itemColor));
  if (newCart.length !== cart.length) {
    localStorage.setItem('cart', JSON.stringify(newCart));
  } else {
    console.log("404 not found in deleteItemFromCart");
  }
}

function updateItemSizeOrQuantity(itemId, itemSize, new_quantity = null, new_size = null) {
  console.log("updateItemSizeOrQuantity function called");
  const cart = JSON.parse(localStorage.getItem('cart')) || [];
  console.log("Searching for itemId:", itemId, "and itemSize:", itemSize);
  console.log("Current cart:", cart);
  const existingItem = cart.find(item => item.itemId === itemId && item.itemSize === itemSize);
  if (existingItem) {
    if (new_quantity !== null) {
      existingItem.itemQuantity = parseInt(new_quantity);
    }
    if (new_size !== null) {
      existingItem.itemSize = parseInt(new_size);
    }
    localStorage.setItem('cart', JSON.stringify(cart));
    location.reload();
  } else {
    console.log("404 not found in updateItemSizeOrQuantity");
  }
}

function calculateTotal() {
  const selectedItems = getSelectedItems();
  const promises = selectedItems.map(item => {
    return getData(item.itemId).then(productData => {
      if (productData) {
        return productData.item_price * item.itemQuantity;
      }
      return 0;
    }).catch(() => 0);
  });
  console.log(promises.length);
  if (promises.length === 0) {
    return Promise.resolve(0);
  }
  return Promise.all(promises).then(prices => {
    return prices.reduce((total, price) => total + price, 0);
  }).catch(() => 0);
}

function getSelectedItems() {
  const selectedItems = [];
  const checkboxes = document.querySelectorAll('.item-checkbox:checked');
  checkboxes.forEach(checkbox => {
    const itemId = parseInt(checkbox.getAttribute('data-item-id'));
    const itemSize = parseInt(checkbox.getAttribute('data-item-size'));
    const cart = JSON.parse(localStorage.getItem('cart')) || [];
    console.log(`itemId: ${itemId}, itemSize: ${itemSize}`);
    const existingItem = cart.find(item => item.itemId === itemId && item.itemSize === itemSize);
    if (existingItem) {
      selectedItems.push(existingItem);
      console.log(`${existingItem.itemId} has been selected`);
    }
  });
  return selectedItems;
}

function deleteSelectedItems() {
  const selectedItems = getSelectedItems();
  if (selectedItems.length > 0) {
    const cart = JSON.parse(localStorage.getItem('cart')) || [];
    const updatedCart = cart.filter(item => {
      return !selectedItems.some(selectedItem =>
          selectedItem.itemId === item.itemId && selectedItem.itemSize === item.itemSize);
    });
    localStorage.setItem('cart', JSON.stringify(updatedCart));
    console.log('Selected items have been deleted.');
    updateOrderTotal();
    renderCartItems();
  } else {
    console.log('No items selected to delete.');
    renderCartItems();
  }
}

function renderCartItems() {
  const cartContainer = document.getElementById('cartProductsContainer');
  cartContainer.innerHTML = '';
  const cart = JSON.parse(localStorage.getItem('cart')) || [];
  if (cart.length === 0) {
    cartContainer.innerHTML = '<p class="help">Your cart is empty.</p>';
    return;
  }
  cart.forEach(item => {
    getData(item.itemId).then(productData => {
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
        const deleteBtn = itemCard.querySelector('.delete-btn');
        deleteBtn.addEventListener('click', () => {
          const confirmed = confirm('장바구니에서 상품을 삭제하시겠습니까?');
          if (confirmed) {
            deleteItemFromCart(item.itemId, item.itemSize);
            location.reload();
          }
        });
        const optionChangeBtn = itemCard.querySelector('.option-change-btn');
        optionChangeBtn.addEventListener('click', () => {
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
          const sizeSelect = quantityControl.querySelector('.size-select');
          sizeSelect.addEventListener('change', () => {
            const selectedSize = sizeSelect.value;
            console.log(`Selected size: ${selectedSize}`);
          });
          const applyBtn = quantityControl.querySelector('.apply-btn');
          applyBtn.addEventListener('click', () => {
            const selectedSize = sizeSelect.value;
            updateItemSizeOrQuantity(item.itemId, item.itemSize, currentQuantity, selectedSize);
            location.reload();
          });
          const mediaContent = itemCard.querySelector('.media-content');
          mediaContent.appendChild(quantityControl);
        });
        cartContainer.appendChild(itemCard);
      }
    });
  });
  cartContainer.addEventListener('change', (event) => {
    if (event.target.matches('.item-checkbox')) {
      const selectedItems = getSelectedItems();
      console.log('Selected items:', selectedItems);
      updateOrderTotal();
      updateAllSelectCheckbox();
    }
  });
  setTimeout(setupItemCheckboxListeners, 100);
}

function updateOrderTotal() {
  calculateTotal().then(totalPrice => {
    const productsTotal = totalPrice;
    console.log(`￦${productsTotal}`);
    let deliveryTotal = 0;
    let discountTotal = 0;
    let orderTotal = productsTotal + deliveryTotal - discountTotal;
    document.getElementById('productsTotal').innerText = `￦${productsTotal}`;
    document.getElementById('deliveryTotal').innerText = `￦${deliveryTotal}`;
    document.getElementById('discountTotal').innerText = `￦${discountTotal}`;
    document.getElementById('orderTotal').innerText = `￦${orderTotal}`;
  }).catch(error => {
    console.error('Error calculating total:', error);
    document.getElementById('productsTotal').innerText = `￦0`;
    document.getElementById('deliveryTotal').innerText = `￦0`;
    document.getElementById('discountTotal').innerText = `￦0`;
    document.getElementById('orderTotal').innerText = `￦0`;
  });
}

document.addEventListener('DOMContentLoaded', () => {
  localStorage.removeItem('purchase');
  localStorage.removeItem('selectedItems');
  updateOrderTotal();
  renderCartItems();
});

const allSelectCheckbox = document.getElementById('allSelectCheckbox');
allSelectCheckbox.addEventListener('change', function () {
  const itemCheckboxes = document.querySelectorAll('.item-checkbox');
  itemCheckboxes.forEach(checkbox => {
    checkbox.checked = allSelectCheckbox.checked;
  });
  updateOrderTotal();
});

function updateAllSelectCheckbox() {
  const itemCheckboxes = document.querySelectorAll('.item-checkbox');
  const allChecked = Array.from(itemCheckboxes).every(checkbox => checkbox.checked);
  allSelectCheckbox.checked = allChecked;
}

function setupItemCheckboxListeners() {
  document.querySelectorAll('.item-checkbox').forEach(checkbox => {
    checkbox.addEventListener('change', () => {
      updateAllSelectCheckbox();
      updateOrderTotal();
    });
  });
}

document.getElementById('deleteSelectedButton').addEventListener('click', () => {
  const confirmed = confirm('선택된 아이템을 삭제하시겠습니까?');
  if (confirmed) {
    deleteSelectedItems();
  }
});

document.getElementById('deleteAllButton').addEventListener('click', () => {
  const confirmed = confirm('모든 장바구니 아이템을 삭제하시겠습니까?');
  if (confirmed) {
    localStorage.clear();
    renderCartItems();
    updateOrderTotal();
    console.log('All items have been deleted.');
  }
});

document.getElementById('purchaseButton').addEventListener('click', () => {
  const selectedItems = [];
  const cart = JSON.parse(localStorage.getItem('cart'));
  cart.forEach(item => {
    const checkbox = document.querySelector(`.item-checkbox[data-item-id="${item.itemId}"][data-item-size="${item.itemSize}"]`);
    if (checkbox && checkbox.checked) {
      const { itemId, itemSize, itemColor, itemQuantity } = item;
      selectedItems.push({
        itemId,
        itemSize,
        itemColor,
        itemQuantity
      });
    }
  });
  if (selectedItems.length > 0) {
    localStorage.setItem('selectedItems', JSON.stringify(selectedItems));
    location.href = '../order/order.html';
  } else {
    alert('상품을 선택해주세요.');
  }
});

async function getData(itemId) {
  console.log('Received itemId:', itemId);
  const loc = `/api/items/${itemId}`;
  console.log('loc:', loc);
  try {
    const res = await fetch(loc);
    if (!res.ok) {
      throw new Error('Network response was not ok');
    }
    const data = await res.json();
    console.log(data);
    const { itemName, itemPrice, imageUrl } = data;
    const item_name = itemName;
    const item_price = itemPrice;
    const image_url = imageUrl;
    return { item_name, item_price, image_url };
  } catch (error) {
    console.error('Failed to fetch data:', error);
    return null;
  }
}