import * as Api from "../api.js";
import { loadHeader } from "../../common/header.js";

// 요소(element), input 혹은 상수
const productItemContainer = document.querySelector("#producItemContainer");
const modal = document.querySelector("#modal");
const modalBackground = document.querySelector("#modalBackground");
const modalCloseButton = document.querySelector("#modalCloseButton");
const deleteCompleteButton = document.querySelector("#deleteCompleteButton");
const deleteCancelButton = document.querySelector("#deleteCancelButton");
const addItemButton = document.querySelector("#addItemButton");

let itemIdToDelete;

// 초기 설정
async function initialize() {
  try {
      await loadHeader();
      await insertItems(0);  // 첫 페이지 로드
      addAllEvents();
    } catch (error) {
      console.error('초기화 중 오류 발생:', error);
    }
}

// 이벤트 설정
function addAllEvents() {
  modalBackground.addEventListener("click", closeModal);
  modalCloseButton.addEventListener("click", closeModal);
  document.addEventListener("keydown", keyDownCloseModal);
  deleteCompleteButton.addEventListener("click", deleteItemData);
  deleteCancelButton.addEventListener("click", cancelDelete);

if (addItemButton) {
    addItemButton.addEventListener("click", () => {
        console.log("Add Item Button Clicked");
      window.location.href = "/admin/items/create";
    });
  } else {
        console.error("add Item Button not found");
  }
}



// 상품 데이터를 받아서 테이블에 추가
async function insertItems(page = 0, size = 10) {
  try {
    const response = await Api.get(`/api/items?page=${page}&size=${size}`);
    console.log('API 응답:', response);


    if (!response || !response.content) {
      throw new Error('예상치 못한 API 응답 형식');
    }

    const items = response.content;
    const totalPages = response.totalPages;
    const currentPage = response.number;

    // itemsContainer가 존재하는지 확인
    const itemsContainer = document.querySelector("#itemsContainer");
    if (!itemsContainer) {
      console.error('itemsContainer 요소를 찾을 수 없습니다.');
      return;
    }

    // 기존 카테고리 목록만 초기화 (컬럼명은 유지)
    const existingList = itemsContainer.querySelector('.item-list');
    if (existingList) {
      existingList.remove();
    }

    // 새로운 카테고리 목록 컨테이너 생성
    const itemList = document.createElement('div');
    itemList.className = 'item-list';

    for (const item of items) {
      const { id, itemName, categoryId, createdAt, updatedAt } = item;
        const createdDate = new Date(createdAt);
        const updatedDate = new Date(updatedAt);
        const options = {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false // 24시간 형식
          };
          const categories = await Api.get(`/api/admin/categories/${categoryId}`);


      itemList.insertAdjacentHTML(
        "beforeend",
        `
          <div class="columns orders-item" id="item-${id}">
            <div class="column is-2 has-text-centered">${itemName}</div>
            <div class="column is-2 has-text-centered">${translateEnglishToKorean(categories.categoryThema)}</div>
            <div class="column is-2 has-text-centered">${createdDate.toLocaleDateString('ko-KR', options)}</div>
            <div class="column is-2 has-text-centered">${updatedDate.toLocaleDateString('ko-KR', options)}</div>
            <div class="column is-2 has-text-centered">
              <button class="button is-outlined is-small mr-2" id="editButton-${id}">수정</button>
              <button class="button is-outlined is-small" id="deleteButton-${id}">삭제</button>
            </div>
          </div>
        `
      );
    }

    // 새로운 카테고리 목록을 컨테이너에 추가
    itemsContainer.appendChild(itemList);

    // 이벤트 리스너 추가
    items.forEach(item => {
      const { id } = item;
      document.querySelector(`#editButton-${id}`).addEventListener("click", () => {
        window.location.href = `/admin/items/edit?id=${id}`;
      });
      document.querySelector(`#deleteButton-${id}`).addEventListener("click", () => {
        itemIdToDelete = id;
        openModal();
      });
    });

    // 페이지네이션 UI 생성
    createPagination(currentPage, totalPages);

  } catch (error) {
    console.error('아이템 데이터 로딩 실패:', error);
    alert('상품 데이터를 불러오는 데 실패했습니다.');
  }
}

// 카테고리 삭제 + 로그 오류 처리 개선
async function deleteItemData() {
  try {
    const response = await Api.delete(`/api/items/${itemIdToDelete}`);
    if (response.status === 204) {  // No Content
      const deletedItem = document.querySelector(`#item-${itemIdToDelete}`);
      if (deletedItem) {
        deletedItem.remove();
      }
      alert("상품이 삭제되었습니다.");
    } else {
      throw new Error('상품 삭제 실패');
    }
    closeModal();
  } catch (err) {
    console.error(`Error: ${err.message}`);
    alert(`카테고리 삭제 과정에서 오류가 발생하였습니다: ${err.message}`);
  }
}

// 페이지네이션 생성 함수
function createPagination(currentPage, totalPages) {
  // 기존 페이지네이션 제거
  const existingPagination = document.querySelector('.pagination');
  if (existingPagination) {
    existingPagination.remove();
  }

  // 페이지 수가 0일 경우 처리
  if (totalPages <= 0) {
    return; // 페이지네이션 생성하지 않음
  }

  const paginationContainer = document.createElement('nav');
  paginationContainer.className = 'pagination is-centered';
  paginationContainer.setAttribute('role', 'navigation');
  paginationContainer.setAttribute('aria-label', 'pagination');

  const paginationList = document.createElement('ul');
  paginationList.className = 'pagination-list';

  for (let i = 0; i < totalPages; i++) {
    const pageItem = document.createElement('li');
    const pageLink = document.createElement('a');
    pageLink.className = 'pagination-link';
    pageLink.setAttribute('aria-label', `Goto page ${i + 1}`);
    pageLink.textContent = i + 1;

    if (i === currentPage) {
      pageLink.className += ' is-current';
      pageLink.setAttribute('aria-current', 'page');
    } else {
      pageLink.addEventListener('click', () => {
        insertItems(i);  // 페이지 클릭 시 insertItems 호출
      });
    }

    pageItem.appendChild(pageLink);
    paginationList.appendChild(pageItem);
  }

  paginationContainer.appendChild(paginationList);
  document.querySelector("#itemsContainer").after(paginationContainer);  // 또는 "#eventsContainer"
}

function translateEnglishToKorean(englishTheme) {
  const themeMap = {
    'common': '공용',
    'women': '여성',
    'men': '남성',
    'accessories': '액세서리',
    'how-to': 'HOW TO'
  };

  return themeMap[englishTheme];
}

// Modal 창 관련 함수들
function openModal() {
  modal.classList.add("is-active");
}

function closeModal() {
  modal.classList.remove("is-active");
}

function keyDownCloseModal(e) {
  if (e.keyCode === 27) {
    closeModal();
  }
}

function cancelDelete() {
  categoryIdToDelete = "";
  closeModal();
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', initialize);
