import * as Api from "../api.js";
import { validateEmail, createNavbar } from "../useful-functions.js";

// 요소(element), input 혹은 상수
const fullNameInput = document.querySelector("#fullNameInput"); //이름
const userRealIdInput = document.querySelector("#userRealId");   //ID
const emailInput = document.querySelector("#emailInput");   //이메일
const passwordInput = document.querySelector("#passwordInput"); //비밀번호
const passwordConfirmInput = document.querySelector("#passwordConfirmInput");   //비밀번호 확인
const submitButton = document.querySelector("#submitButton");   //제출버튼

addAllElements();
addAllEvents();

// html에 요소를 추가하는 함수들을 묶어주어서 코드를 깔끔하게 하는 역할임.
async function addAllElements() {
  createNavbar();
}

// 여러 개의 addEventListener들을 묶어주어서 코드를 깔끔하게 하는 역할임.
function addAllEvents() {
  submitButton.addEventListener("click", handleSubmit);
  document
    .getElementById("checkDuplicateIdButton")
    .addEventListener("click", checkDuplicateId); // 여기서 이벤트 리스너를 추가합니다.
}

async function checkDuplicateId() {
  const userRealId = document.getElementById("userRealId").value;

  if (!userRealId) {
    alert("ID를 입력해주세요.");
    return;
  }

  // 쿼리 파라미터를 포함한 URL로 이동
  try {
      // ID 중복 확인 요청
      const response = await fetch(`/api/signup/check-id?userRealId=${encodeURIComponent(userRealId)}`, {
        method: 'GET',
        credentials: 'include' // 쿠키 전송을 원하면 이 설정을 추가합니다.
      });

      // 응답 상태 코드 확인
      if (response.status === 200) {
        const messageElement = document.getElementById("duplicateIdMessage");
        messageElement.textContent = "사용 가능한 ID입니다."; // 성공 메시지 업데이트
        messageElement.classList.remove('has-text-danger'); // 경고 색상 제거
        messageElement.classList.add('has-text-success'); // 성공 색상 추가
        return true;
      } else {
        const messageElement = document.getElementById("duplicateIdMessage");
        messageElement.textContent = "ID가 이미 존재합니다."; // 중복일 경우 경고 메시지
        messageElement.classList.remove('has-text-success'); // 성공 색상 제거
        messageElement.classList.add('has-text-danger'); // 경고 색상 추가
        return false;
      }
    } catch (error) {
      console.error("오류 발생:", error);
      alert("서버와의 통신 중 문제가 발생했습니다.");
    }
}


// 회원가입 진행
async function handleSubmit(e) {
  e.preventDefault();

  const username = fullNameInput.value;
  const userRealId = userRealIdInput.value;
  const email = emailInput.value;
  const password = passwordInput.value;
  const passwordConfirm = passwordConfirmInput.value;

  // 잘 입력했는지 확인
  const isFullNameValid = username.length >= 2;
  const isEmailValid = validateEmail(email);
  const isPasswordValid = password.length >= 4;
  const isPasswordSame = password === passwordConfirm;

  if (!isFullNameValid || !isPasswordValid) {
    return alert("이름은 2글자 이상, 비밀번호는 4글자 이상이어야 합니다.");
  }

  if (!isEmailValid) {
    return alert("이메일 형식이 맞지 않습니다.");
  }

  if (!isPasswordSame) {
    return alert("비밀번호가 일치하지 않습니다.");
  }

  const isIdAvailable = await checkDuplicateId(userRealId); // ID 중복 확인 함수 호출
  if (!isIdAvailable) {
    return alert("이미 사용중인 ID 입니다.");
  }

  // 회원가입 api 요청
  try {
    const data = { username, userRealId, email, password };

    await Api.post("/api/signup", data);

    alert(`정상적으로 회원가입되었습니다.`);
    // 로그인 페이지 이동
    window.location.href = "/";
  } catch (err) {
    console.error(err.stack);
    alert(`문제가 발생하였습니다. 확인 후 다시 시도해 주세요: ${err.message}`);
  }
}
