// 로컬 스토리지에 저장하는 함수

// 사용 예시
const itemData = {
  item_id: 101,
  item_quantity: 2,
  item_size: 42,
  item_name: 'Cool Sneaker',
  item_price: 120,
  item_color: 'black',
  image_url: 'path/to/image.jpg' // 이미지 URL 추가
};

const itemData1 = {
  item_id: 320,
  item_quantity: 5,
  item_size: 53,
  item_name: 'Bdn',
  item_price: 138,
  item_color: 'white',
  image_url: 'path/to/image.jpg' // 이미지 URL 추가
};

function addItemToCart(item_name, item_price, item_id, item_quantity, item_size, item_color, image_url) {
  // 장바구니에 담긴 아이템을 로컬 스토리지에서 가져오기
  let cart = JSON.parse(localStorage.getItem('cart')) || [];

  // 새로운 아이템 객체 생성
  const newItem = {
    item_id: item_id,
    item_quantity: item_quantity,
    item_size: item_size,
    item_name: item_name,
    item_price: item_price,
    item_color: item_color,
    image_url: image_url // 이미지 URL 추가
  };

  // 장바구니에 아이템 추가 (중복 체크)
  const existingItemIndex = cart.findIndex(item => item.item_id === item_id);
  if (existingItemIndex > -1) {
    // 이미 존재하는 아이템이면 수량만 증가
    cart[existingItemIndex].quantity += item_quantity;
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
    console.log("404 not found");
  }
}

// 장바구니 아이템의 사이즈나 수량을 업데이트하는 함수
function updateItemSizeOrQuantity(item_id, item_size, new_quantity = null, new_size = null) {
  console.log("updateItemSizeOrQuantity function called");
  // 로컬 스토리지에서 장바구니를 가져오기
  let cart = JSON.parse(localStorage.getItem('cart')) || [];

  // 업데이트할 아이템 찾기
  const itemIndex = cart.findIndex(item => item.item_id === item_id && item.item_size === item_size);

  if (itemIndex > -1) {
    // 새로운 수량이 주어지면 업데이트
    if (new_quantity !== null) {
      cart[itemIndex].quantity = new_quantity;
    }

    // 새로운 사이즈가 주어지면 업데이트
    if (new_size !== null) {
      cart[itemIndex].item_size = new_size;
    }

    // 업데이트된 장바구니를 로컬 스토리지에 저장
    localStorage.setItem('cart', JSON.stringify(cart));
  } else {
    // 아이템이 존재하지 않으면 404 not found 로그 출력
    console.log("404 not found ");
  }
}

// 사용 예시
const item_id = 101;
const item_size = 270;
const new_quantity = 3;  // 수량을 3으로 업데이트
const new_size = 260;    // 사이즈를 260으로 업데이트

// 장바구니 아이템 업데이트




// 장바구니에 아이템 추가
addItemToCart(itemData.item_name, itemData.item_price, itemData.item_id, itemData.item_size, itemData.item_color, itemData.image_url);
addItemToCart(itemData1.item_name, itemData1.item_price, itemData1.item_id, itemData1.item_size, itemData1.item_color, itemData1.image_url);
updateItemSizeOrQuantity(320, 53, 3, 265)