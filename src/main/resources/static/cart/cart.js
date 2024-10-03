// 로컬 스토리지에 저장하는 함수
function addItemToCart(item_name, item_price, item_id, item_size, item_color, image_url) {
  // 장바구니에 담긴 아이템을 로컬 스토리지에서 가져오기
  let cart = JSON.parse(localStorage.getItem('cart')) || [];

  // 새로운 아이템 객체 생성
  const newItem = {
    item_id: item_id,
    quantity: 1, // 기본 수량 1로 설정
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
    cart[existingItemIndex].quantity += 1;
  } else {
    // 새로운 아이템이면 추가
    cart.push(newItem);
  }

  // 업데이트된 장바구니를 로컬 스토리지에 저장
  localStorage.setItem('cart', JSON.stringify(cart));
}

// 사용 예시
const itemData = {
  item_id: 101,
  quantity: 2,
  item_size: 42,
  item_name: 'Cool Sneaker',
  item_price: 120,
  item_color: 'black',
  image_url: 'path/to/image.jpg' // 이미지 URL 추가
};

// 장바구니에 아이템 추가
addItemToCart(itemData.item_name, itemData.item_price, itemData.item_id, itemData.item_size, itemData.item_color, itemData.image_url);
