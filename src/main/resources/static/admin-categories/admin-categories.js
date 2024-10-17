import { addCommas, checkAdmin } from "../../useful-functions.js";
import { loadHeader } from "../../common/header.js";
import * as Api from "../../api.js";

// 요소(element), input 혹은 상수
// const categoriesCount = document.querySelector("#categoriesCount");
const categoriesContainer = document.querySelector("#categoriesContainer");
const modal = document.querySelector("#modal");
const modalBackground = document.querySelector("#modalBackground");
const modalCloseButton = document.querySelector("#modalCloseButton");
const deleteCompleteButton = document.querySelector("#deleteCompleteButton");
const deleteCancelButton = document.querySelector("#deleteCancelButton");
const addCategoryButton = document.querySelector("#addCategoryButton");

let categoryIdToDelete;

// 초기 설정
async function initialize() {
  try {
      await loadHeader();
      // checkAdmin();
      await insertCategories(0);  // 첫 페이지 로드
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
  deleteCompleteButton.addEventListener("click", deleteCategoryData);
  deleteCancelButton.addEventListener("click", cancelDelete);

  if (addCategoryButton) {
    addCategoryButton.addEventListener("click", () => {
      window.location.href = "/admin/categories/create";
    });
  }
}

// 카테고리 데이터를 받아서 테이블에 추가
async function insertCategories(page = 0, size = 10) {
  try {
    const response = await Api.get(`/api/admin/categories?page=${page}&size=${size}`);
    console.log('API 응답:', response);

    if (!response || !response.content) {
      throw new Error('예상치 못한 API 응답 형식');
    }

    const categories = response.content;
    const totalPages = response.totalPages;
    const currentPage = response.number;

    // categoriesContainer가 존재하는지 확인
    const categoriesContainer = document.querySelector("#categoriesContainer");
    if (!categoriesContainer) {
      console.error('categoriesContainer 요소를 찾을 수 없습니다.');
      return;
    }

    // 기존 카테고리 목록만 초기화 (컬럼명은 유지)
    const existingList = categoriesContainer.querySelector('.category-list');
    if (existingList) {
      existingList.remove();
    }

    // 새로운 카테고리 목록 컨테이너 생성
    const categoryList = document.createElement('div');
    categoryList.className = 'category-list';

    for (const category of categories) {
      const { id, categoryName, categoryThema, displayOrder, createdAt, updatedAt } = category;
      const koreanThema = translateEnglishToKorean(categoryThema);

      categoryList.insertAdjacentHTML(
        "beforeend",
        `
          <div class="columns orders-item" id="category-${id}">
            <div class="column is-2">${categoryName}</div>
            <div class="column is-2">${koreanThema}</div>
            <div class="column is-2">${displayOrder}번째</div>
            <div class="column is-2">${new Date(createdAt).toLocaleDateString()}</div>
            <div class="column is-2">${new Date(updatedAt).toLocaleDateString()}</div>
            <div class="column is-2">
              <button class="button is-outlined is-small mr-2" id="editButton-${id}">수정</button>
              <button class="button is-outlined is-small" id="deleteButton-${id}">삭제</button>
            </div>
          </div>
        `
      );
    }

    // 새로운 카테고리 목록을 컨테이너에 추가
    categoriesContainer.appendChild(categoryList);

    // 이벤트 리스너 추가
    categories.forEach(category => {
      const { id } = category;
      document.querySelector(`#editButton-${id}`).addEventListener("click", () => {
        window.location.href = `/admin/categories/edit?id=${id}`;
      });
      document.querySelector(`#deleteButton-${id}`).addEventListener("click", () => {
        categoryIdToDelete = id;
        openModal();
      });
    });

    // 페이지네이션 UI 생성
    createPagination(currentPage, totalPages);

  } catch (error) {
    console.error('카테고리 데이터 로딩 실패:', error);
    alert('카테고리 데이터를 불러오는 데 실패했습니다.');
  }
}

// 카테고리 삭제 + 로그 오류 처리 개선
async function deleteCategoryData() {
  try {
    const response = await Api.delete(`/api/admin/categories/${categoryIdToDelete}`);
    if (response.status === 204) {  // No Content
      const deletedItem = document.querySelector(`#category-${categoryIdToDelete}`);
      if (deletedItem) {
        deletedItem.remove();
      }
      alert("카테고리가 삭제되었습니다.");
    } else {
      throw new Error('카테고리 삭제 실패');
    }
    closeModal();
  } catch (err) {
    console.error(`Error: ${err.message}`);
    alert(`카테고리 삭제 과정에서 오류가 발생하였습니다: ${err.message}`);
  }
}

// 페이지네이션 생성 함수
function createPagination(currentPage, totalPages) {
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
      pageLink.addEventListener('click', () => insertCategories(i));
    }

    pageItem.appendChild(pageLink);
    paginationList.appendChild(pageItem);
  }

  paginationContainer.appendChild(paginationList);
  document.querySelector("#categoriesContainer").after(paginationContainer);
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

