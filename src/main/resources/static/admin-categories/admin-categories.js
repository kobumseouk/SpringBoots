import { addCommas, checkAdmin } from "../../useful-functions.js";
import { loadHeader } from "../../common/header.js";
import * as Api from "../../api.js";

// 요소(element), input 혹은 상수
const categoriesCount = document.querySelector("#categoriesCount");
const categoriesContainer = document.querySelector("#categoriesContainer");
const modal = document.querySelector("#modal");
const modalBackground = document.querySelector("#modalBackground");
const deleteCompleteButton = document.querySelector("#deleteCompleteButton");
const deleteCancelButton = document.querySelector("#deleteCancelButton");
const addCategoryButton = document.querySelector("#addCategoryButton");

// 페이지 초기화 함수
async function initializePage() {
  // checkAdmin();
  await addAllElements();
  addAllEvents();
}

// 요소 삽입 함수들을 묶어주어서 코드를 깔끔하게 하는 역할임.
async function addAllElements() {
  await loadHeader();
  await insertCategories();
}

// 여러 개의 addEventListener들을 묶어주어서 코드를 깔끔하게 하는 역할임.
function addAllEvents() {
  modalBackground.addEventListener("click", closeModal);
  document.addEventListener("keydown", keyDownCloseModal);
  deleteCompleteButton.addEventListener("click", deleteCategoryData);
  deleteCancelButton.addEventListener("click", cancelDelete);

  // 카테고리 등록 버튼 이벤트 리스너 추가
  if (addCategoryButton) {
    addCategoryButton.addEventListener("click", () => {
      window.location.href = "/admin/categories/create";
    });
  }
}

// 페이지 로드 시 실행, 삭제할 카테고리 id를 전역변수로 관리함
let categoryIdToDelete;
async function insertCategories() {
  const response = await Api.get("/api/admin/categories");
    console.log('API 응답:', response);

    let categories = [];
    if (Array.isArray(response)) {
      categories = response;
    } else if (response && typeof response === 'object' && response.categories) {
      categories = response.categories;
    } else {
      console.error('예상치 못한 API 응답 형식:', response);
      return;
    }

  // 총 요약에 활용
  const summary = {
    categoriesCount: categories.length,
  };

  for (const category of categories) {
    const { id, categoryName, categoryThema, displayOrder, createdAt, updatedAt } = category;

    categoriesContainer.insertAdjacentHTML(
      "beforeend",
      `
        <div class="columns orders-item" id="category-${id}">
          <div class="column is-2">${categoryName}</div>
          <div class="column is-2">${categoryThema}</div>
          <div class="column is-2">${displayOrder}</div>
          <div class="column is-2">${new Date(createdAt).toLocaleDateString()}</div>
          <div class="column is-2">${new Date(updatedAt).toLocaleDateString()}</div>
          <div class="column is-2">
            <button class="button is-info" id="editButton-${id}">수정</button>
            <button class="button is-danger" id="deleteButton-${id}">삭제</button>
          </div>
        </div>
      `
    );

    // 수정 버튼 이벤트 리스너
    const editButton = document.querySelector(`#editButton-${id}`);
    editButton.addEventListener("click", () => {
      window.location.href = `/admin/categories/edit?id=${id}`;
    });

    // 삭제 버튼 이벤트 리스너
    const deleteButton = document.querySelector(`#deleteButton-${id}`);
    deleteButton.addEventListener("click", () => {
      categoryIdToDelete = id;
      openModal();
    });
  }

  // 총 요약에 값 삽입
  categoriesCount.innerText = addCommas(summary.categoriesCount);
}

// db에서 카테고리 삭제
async function deleteCategoryData(e) {
  e.preventDefault();

  try {
    await Api.delete(`/api/admin/categories/${categoryIdToDelete}`);
    // 삭제 성공
    alert("카테고리가 삭제되었습니다.");

    // 삭제한 아이템 화면에서 지우기
    const deletedItem = document.querySelector(`#category-${categoryIdToDelete}`);
    deletedItem.remove();

    // 전역변수 초기화
    categoryIdToDelete = "";

    closeModal();
  } catch (err) {
    alert(`카테고리 삭제 과정에서 오류가 발생하였습니다: ${err}`);
  }
}

// Modal 창에서 아니오 클릭할 시, 전역 변수를 다시 초기화함.
function cancelDelete() {
  categoryIdToDelete = "";
  closeModal();
}

// Modal 창 열기
function openModal() {
  modal.classList.add("is-active");
}

// Modal 창 닫기
function closeModal() {
  modal.classList.remove("is-active");
}

// 키보드로 Modal 창 닫기
function keyDownCloseModal(e) {
  // Esc 키
  if (e.keyCode === 27) {
    closeModal();
  }
}

// 페이지 초기화
window.addEventListener('DOMContentLoaded', initializePage);