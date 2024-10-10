import * as Api from "../../api.js";
import { createNavbar } from "../../useful-functions.js";

// 요소(element), input 혹은 상수
const passwordInput = document.querySelector("#passwordInput");
const modal = document.querySelector("#modal");
const modalBackground = document.querySelector("#modalBackground");
const modalCloseButton = document.querySelector("#modalCloseButton");
const deleteCompleteButton = document.querySelector("#deleteCompleteButton");
const deleteCancelButton = document.querySelector("#deleteCancelButton");

addAllElements();
addAllEvents();

// html에 요소를 추가하는 함수들을 묶어주어서 코드를 깔끔하게 하는 역할임.
async function addAllElements() {
  createNavbar();
}

// 여러 개의 addEventListener들을 묶어주어서 코드를 깔끔하게 하는 역할임.
function addAllEvents() {
  submitButton.addEventListener("click", openModal);
  modalBackground.addEventListener("click", closeModal);
  modalCloseButton.addEventListener("click", closeModal);
  document.addEventListener("keydown", keyDownCloseModal);
  deleteCompleteButton.addEventListener("click", deleteUserData);
  deleteCancelButton.addEventListener("click", closeModal);
}

// db에서 회원정보 삭제
async function deleteUserData(e) {
  e.preventDefault();

  const password = passwordInput.value;
  const data = { password };

  try {
    // 우선 입력된 비밀번호가 맞는지 확인 (틀리면 에러 발생함)
    const userToDelete = await Api.post("/api/users/check-password", data);
    const { id } = userToDelete;

    // 삭제 진행
    const response = await fetch(`/api/users-soft/${id}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        // 쿠키에 저장된 토큰은 자동으로 서버로 전송됨
      },
      credentials: 'include' // 쿠키를 포함하여 요청을 보냄
    });

    if (!response.ok) {
      throw new Error('서버와의 통신에서 문제가 발생했습니다.');
    }

    // 삭제 성공
    alert("회원 정보가 안전하게 삭제되었습니다.");

    // 토큰 쿠키 삭제
    document.cookie = "token=; Max-Age=0; path=/"; // 토큰 쿠키 삭제

    window.location.href = "/";
  } catch (err) {
    alert(`회원정보 삭제 과정에서 오류가 발생하였습니다: ${err}`);

    closeModal();
  }
}

// Modal 창 열기
function openModal(e) {
  if (e) {
    e.preventDefault();
  }
  modal.classList.add("is-active");
}

// Modal 창 닫기
function closeModal(e) {
  if (e) {
    e.preventDefault();
  }
  modal.classList.remove("is-active");
}

// 키보드로 Modal 창 닫기
function keyDownCloseModal(e) {
  // Esc 키
  if (e.keyCode === 27) {
    closeModal();
  }
}
