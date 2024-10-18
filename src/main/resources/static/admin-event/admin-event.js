import { addCommas, checkAdmin } from "../../useful-functions.js";
import { loadHeader } from "../../common/header.js";
import * as Api from "../../api.js";

const eventsContainer = document.querySelector("#eventsContainer");
const modal = document.querySelector("#modal");
const modalBackground = document.querySelector("#modalBackground");
const modalCloseButton = document.querySelector("#modalCloseButton");
const deleteCompleteButton = document.querySelector("#deleteCompleteButton");
const deleteCancelButton = document.querySelector("#deleteCancelButton");
const addEventButton = document.querySelector("#addEventButton");

let eventIdToDelete;

// 초기 설정
async function initialize() {
  try {
      await loadHeader();
      // checkAdmin();
      await insertEvents(0);  // 첫 페이지 로드
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
  deleteCompleteButton.addEventListener("click", deleteEventData);
  deleteCancelButton.addEventListener("click", cancelDelete);

  if (addEventButton) {
    addEventButton.addEventListener("click", () => {
      window.location.href = "/admin/events/create";
    });
  }
}

// 이벤트 데이터를 받아서 테이블에 추가
async function insertEvents(page = 0, size = 10) {
  try {
    const response = await Api.get(`/api/admin/events?page=${page}&size=${size}`);
    console.log('API 응답:', response);

    if (!response || !response.content) {
      throw new Error('예상치 못한 API 응답 형식');
    }

    const events = response.content;
    const totalPages = response.totalPages;
    const currentPage = response.number;

    const eventsContainer = document.querySelector("#eventsContainer");
    if (!eventsContainer) {
      console.error('eventsContainer 요소를 찾을 수 없습니다.');
      return;
    }

    const existingList = eventsContainer.querySelector('.event-list');
    if (existingList) {
      existingList.remove();
    }

    const eventList = document.createElement('div');
    eventList.className = 'event-list';

    for (const event of events) {
        const { id, eventTitle, startDate, endDate, isActive } = event;

        eventList.insertAdjacentHTML(
          "beforeend",
          `
            <div class="columns orders-item is-vcentered" id="event-${id}">
              <div class="column is-3 has-text-centered">${eventTitle}</div>
              <div class="column is-2 has-text-centered">${new Date(startDate).toLocaleDateString()}</div>
              <div class="column is-2 has-text-centered">${new Date(endDate).toLocaleDateString()}</div>
              <div class="column is-2 has-text-centered">
                <span class="event-status ${isActive ? 'active' : 'inactive'}">
                  ${isActive ? '진행중' : '만료'}
                </span>
              </div>
              <div class="column is-2 has-text-centered">
                <button class="button is-outlined is-small mr-2" id="editButton-${id}">수정</button>
                <button class="button is-outlined is-small" id="deleteButton-${id}">삭제</button>
              </div>
            </div>
          `
        );
    }

    eventsContainer.appendChild(eventList);

    events.forEach(event => {
      const { id } = event;
      document.querySelector(`#editButton-${id}`).addEventListener("click", () => {
        window.location.href = `/admin/events/edit?id=${id}`;
      });
      document.querySelector(`#deleteButton-${id}`).addEventListener("click", () => {
        eventIdToDelete = id;
        openModal();
      });
    });

    createPagination(currentPage, totalPages);

  } catch (error) {
    console.error('이벤트 데이터 로딩 실패:', error);
    alert('이벤트 데이터를 불러오는 데 실패했습니다.');
  }
}

async function deleteEventData() {
  try {
    const response = await Api.delete(`/api/admin/events/${eventIdToDelete}`);
    if (response.status === 204) {  // No Content
      const deletedItem = document.querySelector(`#event-${eventIdToDelete}`);
      if (deletedItem) {
        deletedItem.remove();
      }
      alert("이벤트가 삭제되었습니다.");
    } else {
      throw new Error('이벤트 삭제 실패');
    }
    closeModal();
  } catch (err) {
    console.error(`Error: ${err.message}`);
    alert(`이벤트 삭제 과정에서 오류가 발생하였습니다: ${err.message}`);
  }
}

// 페이지네이션 생성 함수
function createPagination(currentPage, totalPages) {
  // 기존 페이지네이션 제거
  const existingPagination = document.querySelector('.pagination');
  if (existingPagination) {
    existingPagination.remove();
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
      pageLink.addEventListener('click', () => insertCategories(i));  // 또는 insertEvents(i)
    }

    pageItem.appendChild(pageLink);
    paginationList.appendChild(pageItem);
  }

  paginationContainer.appendChild(paginationList);
  document.querySelector("#categoriesContainer").after(paginationContainer);  // 또는 "#eventsContainer"
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
  eventIdToDelete = "";
  closeModal();
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', initialize);