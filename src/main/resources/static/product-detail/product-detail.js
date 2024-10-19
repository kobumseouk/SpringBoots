// 필요한 모듈 및 함수 import
import { loadHeader } from "../../common/header.js";
import * as Api from "../api.js";
import {
  getUrlParams as getQueryParams,  // 이름 충돌 방지를 위해 변경
  addCommas,
  checkUrlParams,
  createNavbar,
} from "../useful-functions.js";
import { addToDb, putToDb } from "../indexed-db.js";

// URL에서 id 파라미터를 추출하는 함수
function getUrlParams() {
  const urlParams = new URLSearchParams(window.location.search);
  return { id: urlParams.get('id') };
}

// 요소 선택
const productImageTag = document.querySelector("#productImageTag");
const manufacturerTag = document.querySelector("#manufacturerTag");
const titleTag = document.querySelector("#titleTag");
const priceTag = document.querySelector("#priceTag");
const detailDescriptionTag = document.querySelector("#detailDescriptionTag");
const addToCartButton = document.querySelector("#addToCartButton");
const purchaseButton = document.querySelector("#purchaseButton");

// 상품 정보를 API에서 가져와 화면에 표시하는 함수
let currentProduct = null; // API에서 받아온 상품 정보를 저장할 전역 변수

export async function insertProductData() {
  const { id } = getUrlParams(); // URL에서 itemId 추출
  try {
    const product = await Api.get(`/api/items/${id}`); // API 호출로 상품 정보 받아오기
    console.log("받아온 상품 정보:", product); // 받아온 상품 정보 출력
    currentProduct = product; // 전역 변수에 상품 정보 저장

    // 상품 정보 구조 분해 할당
    const { itemName, itemDescription, itemPrice, itemMaker, imageUrl, isRecommended } = product;

    // HTML에 반영
    productImageTag.src = imageUrl;
    titleTag.innerText = itemName;
    detailDescriptionTag.innerText = itemDescription;
    manufacturerTag.innerText = `제조사: ${itemMaker}`;
    priceTag.innerText = `${addCommas(itemPrice)}원`;

    // 추천 상품 표시
    if (isRecommended) {
      titleTag.insertAdjacentHTML(
          "beforeend",
          '<span class="tag is-success is-rounded">추천</span>'
      );
    }
  } catch (error) {
    console.error("상품 정보를 가져오는 중 오류 발생:", error);
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

// 장바구니에 추가하는 함수
function addToCart(product) {
  const cartItem = {
    itemId: product.itemId,
    itemQuantity: 1, // 기본 수량 1
    itemSize: product.itemSize,
    itemColor: product.itemColor,
  };

  // 로컬스토리지에 저장
  const cart = JSON.parse(localStorage.getItem('cart')) || [];
  cart.push(cartItem);
  localStorage.setItem('cart', JSON.stringify(cart));

  alert("장바구니에 추가되었습니다.");
}

// 바로 구매하기 함수
function purchaseNow(product) {
  const purchaseItem = {
    itemId: product.itemId,
    itemQuantity: 1,
    itemSize: product.itemSize,
    itemColor: product.itemColor,
  };

  // 로컬스토리지에 저장하고 구매 페이지로 이동
  localStorage.setItem('purchase', JSON.stringify(purchaseItem));
  window.location.href = '/order'; // 구매 페이지로 이동
}

// indexedDB에 추가하는 함수 (선택적으로 사용 가능)
async function insertDb(product) {
  const { itemId, price } = product;

  await addToDb("cart", { ...product, quantity: 1 }, itemId);

  await putToDb("order", "summary", (data) => {
    const count = data.productsCount ? data.productsCount + 1 : 1;
    const total = data.productsTotal ? data.productsTotal + price : price;
    const ids = data.ids ? [...data.ids, itemId] : [itemId];
    const selectedIds = data.selectedIds ? [...data.selectedIds, itemId] : [itemId];

    data.productsCount = count;
    data.productsTotal = total;
    data.ids = ids;
    data.selectedIds = selectedIds;
  });
}
