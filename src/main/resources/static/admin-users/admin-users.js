import { addCommas, checkAdmin, createNavbar } from "../../useful-functions.js";
import * as Api from "../../api.js";

// 요소(element), input 혹은 상수
const usersCount = document.querySelector("#usersCount");
const adminCount = document.querySelector("#adminCount");
const usersContainer = document.querySelector("#usersContainer");
const modal = document.querySelector("#modal");
const modalBackground = document.querySelector("#modalBackground");
const deleteCompleteButton = document.querySelector("#deleteCompleteButton");
const deleteCancelButton = document.querySelector("#deleteCancelButton");

// 모달 닫기 버튼 (각각 다른 모달 닫기 버튼)
const adminCodeModalCloseButton = document.getElementById('adminCodeModalCloseButton');
const deleteModalCloseButton = document.getElementById('deleteModalCloseButton');

checkAdmin();
addAllElements();
addAllEvents();

// 요소 삽입 함수들을 묶어주어서 코드를 깔끔하게 하는 역할임.
function addAllElements() {
  createNavbar();
  insertUsers();
}

// 여러 개의 addEventListener들을 묶어주어서 코드를 깔끔하게 하는 역할임.
function addAllEvents() {
  modalBackground.addEventListener("click", closeModal);
  document.addEventListener("keydown", keyDownCloseModal);
  deleteCompleteButton.addEventListener("click", deleteUserData);
  deleteCancelButton.addEventListener("click", cancelDelete);
}

// 페이지 로드 시 실행, 삭제할 회원 id를 전역변수로 관리함
let userIdToDelete;
async function insertUsers() {
  const users = await Api.get("/api/admin/users");

  // 총 요약에 활용
  const summary = {
    usersCount: 0,
    adminCount: 0,
  };

  for (const user of users) {
    const { userId, email, username, role, createdAt, userRealId } = user;

    //날짜 포맷팅
    const dateStr = createdAt;
    const date =new Date(dateStr);

    // toLocaleString을 사용해 간단히 날짜와 시간을 출력
    const formattedDate = date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
//        second: '2-digit',
    });

    console.log(formattedDate);  // 예: 2024. 10. 20. 오후 4:32:35

    const id = userId;
    const fullName = username;
    const roles = role;

    summary.usersCount += 1;

    if (roles.includes('ADMIN')) {
      summary.adminCount += 1;
    }

    usersContainer.insertAdjacentHTML(
      "beforeend",
      `
        <div class="columns orders-item" id="user-${id}">
          <div class="column">${formattedDate}</div>
          <div class="column">${userRealId}</div>
          <div class="column">${email}</div>
          <div class="column">${fullName}</div>
          <div class="column">
            <div class="select">
              <select id="roleSelectBox-${id}">
                <option
                  class="has-background-link-light has-text-link"
                  ${roles.includes('ADMIN') === false ? "selected" : ""}
                  value="USER">
                  일반사용자
                </option>
                <option
                  class="has-background-danger-light has-text-danger"
                  ${roles.includes('ADMIN') === true ? "selected" : ""}
                  value="ADMIN">
                  관리자
                </option>
              </select>
            </div>
          </div>
          <!-- <div class="column is-2"> -->
          <!--    <button class="button" id="deleteButton-${id}">회원정보 삭제</button>  -->
          <!--  </div>  -->
        </div>
      `
    );

    // 요소 선택
    const roleSelectBox = document.querySelector(`#roleSelectBox-${id}`);
    const deleteButton = document.querySelector(`#deleteButton-${id}`);

    // 권한 변경 시 모달 띄우기
    roleSelectBox.addEventListener("change", () => {
      // 선택된 userId와 roleSelectBox를 전역 변수에 할당
      selectedUserId = id;
      selectedRoleSelectBox = roleSelectBox;

      const selectedOption = selectedRoleSelectBox.options[selectedRoleSelectBox.selectedIndex];
      if (selectedOption.value === "ADMIN") {
        selectedRoleSelectBox.classList.add("has-background-danger-light", "has-text-danger");
        selectedRoleSelectBox.classList.remove("has-background-link-light", "has-text-link");
      } else {
        selectedRoleSelectBox.classList.add("has-background-link-light", "has-text-link");
        selectedRoleSelectBox.classList.remove("has-background-danger-light", "has-text-danger");
      }
      // 모달 띄우기
      adminCodeModal.classList.add("is-active");
    });
  }

  // 총 요약에 값 삽입
  usersCount.innerText = addCommas(summary.usersCount);
  adminCount.innerText = addCommas(summary.adminCount);
}

// 모달 요소 선택 (루프 밖에서 한 번만 선택)
const adminCodeModal = document.getElementById('adminCodeModal');
const adminCodeInput = document.getElementById('adminCodeInput');
const adminCodeConfirmButton = document.getElementById('adminCodeConfirmButton');
const adminCodeCancelButton = document.getElementById('adminCodeCancelButton');

// 현재 선택된 user id를 저장할 변수
let selectedUserId = null;
let selectedRoleSelectBox = null;

// adminCodeConfirmButton을 한 번만 등록
adminCodeConfirmButton.addEventListener("click", async () => {
  const adminCode = adminCodeInput.value;
//  const response = await Api.post("/api/users/grant", adminCode);
  const response = await fetch("/api/users/grant",{
    method: "Post",
    headers: {
        "Content-Type": "application/json",
    },
    body: JSON.stringify(adminCode),
  });

  const json = await response.json();

  // 관리자 코드 확인
  if (json.message === 'success') {
    const newRole = selectedRoleSelectBox.value;
    const data = { roles: newRole };

    // 선택한 옵션의 배경색 반영
    const index = selectedRoleSelectBox.selectedIndex;
    selectedRoleSelectBox.className = selectedRoleSelectBox[index].className;

    // API 요청 (권한 변경)
    await Api.patch("/api/admin/grant",selectedUserId, data);

    alert("해당 유저의 권한상태를 변경하였습니다.");

    // 모달 닫기
    adminCodeModal.classList.remove("is-active");

    window.location.href = "/admin/users";  //리다이렉트 url
  } else {
    alert("관리자 코드가 올바르지 않습니다.");
    adminCodeInput.value = "";
  }
});

adminCodeCancelButton.addEventListener("click", () => {
  adminCodeModal.classList.remove("is-active");
});
adminCodeModalCloseButton.addEventListener("click", () => {
  adminCodeModal.classList.remove("is-active");
});

// 삭제 모달 닫기 버튼 이벤트 리스너
//deleteModalCloseButton.addEventListener("click", () => {
//  modal.classList.remove("is-active");
//});

// db에서 회원정보 삭제
async function deleteUserData(e) {
  e.preventDefault();

  try {
    await Api.delete(`/api/users/${userIdToDelete}`);

    // 삭제 성공
    alert("회원 정보가 삭제되었습니다.");

    // 삭제한 아이템 화면에서 지우기
    const deletedItem = document.querySelector(`#user-${userIdToDelete}`);
    deletedItem.remove();

    // 전역변수 초기화
    userIdToDelete = "";

    closeModal();
  } catch (err) {
    alert(`회원정보 삭제 과정에서 오류가 발생하였습니다: ${err}`);
  }
}

// Modal 창에서 아니오 클릭할 시, 전역 변수를 다시 초기화함.
function cancelDelete() {
  userIdToDelete = "";
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
