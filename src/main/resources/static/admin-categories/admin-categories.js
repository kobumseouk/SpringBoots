import { loadHeader } from "../../common/common-header.js";
import { addCommas, checkAdmin, createNavbar } from "../../useful-functions.js";
import * as Api from "../../api.js";

// 요소(element), input 혹은 상수
const categoriesCount = document.querySelector("#categoriesCount");
const categoriesContainer = document.querySelector("#categoriesContainer");
const modal = document.querySelector("#modal");
const modalBackground = document.querySelector("#modalBackground");
const deleteCompleteButton = document.querySelector("#deleteCompleteButton");
const deleteCancelButton = document.querySelector("#deleteCancelButton");

// 페이지 초기화 함수
async function initializePage() {
  await loadHeader();
  // checkAdmin();
  addAllElements();
  addAllEvents();
}

// 요소 삽입 함수들을 묶어주어서 코드를 깔끔하게 하는 역할임.
function addAllElements() {
  createNavbar();
  insertCategories();
}

// 여러 개의 addEventListener들을 묶어주어서 코드를 깔끔하게 하는 역할임.
function addAllEvents() {
  modalBackground.addEventListener("click", closeModal);
  document.addEventListener("keydown", keyDownCloseModal);
  deleteCompleteButton.addEventListener("click", deleteCategoryData);
  deleteCancelButton.addEventListener("click", cancelDelete);
}

// 페이지 로드 시 실행, 삭제할 카테고리 id를 전역변수로 관리함
let categoryIdToDelete;
async function insertCategories() {
  const categories = await Api.get("/categories/admin");

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

    // 요소 선택
    const editButton = document.querySelector(`#editButton-${id}`);
    const deleteButton = document.querySelector(`#deleteButton-${id}`);
    const addCategoryButton = document.querySelector("#addCategoryButton");

    // 이벤트 - 추가버튼 클릭 시 추카 페이지 이동
    addCategoryButton.addEventListener("click", () => {
      // 카테고리 추가 페이지로 이동하거나 모달을 여는 로직 구현
      window.location.href = "/admin/categories/create";
    });

    // 이벤트 - 수정버튼 클릭 시 수정 페이지로 이동
    editButton.addEventListener("click", () => {
      window.location.href = `/admin/categories/edit?id=${id}`;
    });

    // 이벤트 - 삭제버튼 클릭 시 Modal 창 띄우고, 동시에, 전역변수에 해당 카테고리의 id 할당
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
    await Api.delete("/categories", categoryIdToDelete);

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