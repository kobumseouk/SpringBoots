// 필요한 모듈 및 함수 import
import { loadHeader } from "../../common/header.js";
import * as Api from "../api.js";
import {addCommas} from "../useful-functions.js";
import { addToDb, putToDb } from "../indexed-db.js";

// URL에서 id 파라미터를 추출하는 함수
function getUrlParams() {
  const urlParams = new URLSearchParams(window.location.search);
  return { id: urlParams.get('itemId') };
}

// 요소 선택
const productImageTag = document.querySelector("#productImageTag");
const manufacturerTag = document.querySelector("#manufacturerTag");
const titleTag = document.querySelector("#titleTag");
const priceTag = document.querySelector("#priceTag");
const detailDescriptionTag = document.querySelector("#detailDescriptionTag");
const addToCartButton = document.querySelector("#addToCartButton");
const purchaseButton = document.querySelector("#purchaseButton");
const colorSelect = document.querySelector('#colorSelect');
const sizeButtons = document.querySelectorAll('.size-option');
const quantityInput = document.querySelector('#quantityInput');
const increaseQuantityButton = document.querySelector('#increaseQuantity');
const decreaseQuantityButton = document.querySelector('#decreaseQuantity');


// 상품 정보를 API에서 가져와 화면에 표시하는 함수
let currentProduct = null; // API에서 받아온 상품 정보를 저장할 전역 변수

export async function insertProductData() {
  const { id } = getUrlParams(); // URL에서 itemId 추출
  try {
    const product = await Api.get(`/api/items/${id}`); // API 호출로 상품 정보 받아오기
    console.log("받아온 상품 정보:", product); // 받아온 상품 정보 출력
    currentProduct = product; // 전역 변수에 상품 정보 저장

    // 상품 정보 구조 분해 할당
    const { itemName, itemDescription, itemPrice, itemMaker, imageUrl, itemColor } = product;

    // HTML에 반영
    productImageTag.src = imageUrl;
    titleTag.innerText = itemName;
    detailDescriptionTag.innerText = itemDescription;
    manufacturerTag.innerText = `제조사: ${itemMaker}`;
    priceTag.innerText = `${addCommas(itemPrice)}원`;

    // 단일 색상 옵션 설정 (배열이 아니라 단일 값)
    colorSelect.innerHTML = `<option value="${itemColor}">${itemColor}</option>`;
  } catch (error) {
    console.error("상품 정보를 가져오는 중 오류 발생:", error);
  }
}


// 수량 조절 함수
function updateQuantity(increment) {
  let currentQuantity = parseInt(quantityInput.value);
  if (increment) {
    quantityInput.value = currentQuantity + 1;
  } else if (currentQuantity > 1) {
    quantityInput.value = currentQuantity - 1;
  }
}


// 이벤트 리스너를 추가하는 함수
export function addAllEvents() {
  addToCartButton.addEventListener("click", () => {
    if (currentProduct) {
      addToCart(currentProduct); // currentProduct는 API 호출 후 저장될 전역 변수
    }
  });

  purchaseButton.addEventListener("click", () => {
    if (currentProduct) {
      purchaseNow(currentProduct); // currentProduct를 사용
    }
  });
}

increaseQuantityButton.addEventListener('click', () => updateQuantity(true));
decreaseQuantityButton.addEventListener('click', () => updateQuantity(false));

// 사이즈 버튼 클릭 이벤트
sizeButtons.forEach(button => {
  button.addEventListener("click", (event) => {
    // 모든 버튼에서 'is-black', 'has-text-yellow' 클래스를 제거
    sizeButtons.forEach(btn => btn.classList.remove("is-black", "has-text-yellow"));

    // 클릭한 버튼에 'is-black', 'has-text-yellow' 클래스 추가
    event.target.classList.add("active", "is-black", "has-text-yellow");

    const selectedSize = event.target.value;
    console.log("Selected size:", selectedSize);
  });
});




// 장바구니에 추가하는 함수
function addToCart(product) {
  const selectedSize = getSelectedSize();
  if (!selectedSize) {
    alert("사이즈를 선택해 주세요.");
    return; // 사이즈가 선택되지 않았을 경우 처리
  }

  const cartItem = {
    itemId: product.id,
    itemQuantity: parseInt(quantityInput.value), // 기본 수량 1
    itemSize: selectedSize,
    itemColor: colorSelect.value,
  };

  // 로컬스토리지에 저장
  const cart = JSON.parse(localStorage.getItem('cart')) || [];
  cart.push(cartItem);
  localStorage.setItem('cart', JSON.stringify(cart));

  alert("장바구니에 추가되었습니다.");
}

// 바로 구매하기 함수
function purchaseNow(product) {
  const selectedSize = getSelectedSize();
  if (!selectedSize) {
    alert("사이즈를 선택해 주세요.");
    return; // 사이즈가 선택되지 않았을 경우 처리
  }

  const purchaseItem = {
    itemId: product.id,
    itemQuantity: parseInt(quantityInput.value),
    itemSize: selectedSize,
    itemColor: colorSelect.value,
  };

  // 로컬스토리지에 저장하고 구매 페이지로 이동
  localStorage.setItem('purchase', JSON.stringify(purchaseItem));
  window.location.href = '/order'; // 구매 페이지로 이동
}

// 사이즈 선택 확인 함수
function getSelectedSize() {
  const activeSizeButton = document.querySelector('.size-option.active');
  return activeSizeButton ? parseInt(activeSizeButton.value) : null;
}
