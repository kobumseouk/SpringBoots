import * as Api from "../api.js";
import {
  blockIfLogin,
  getUrlParams,
  validateEmail,
  createNavbar,
} from "../useful-functions.js";
//useful-functions.js 파일에 있는 blockIfLogin,... 함수를 사용하겠다.

// 요소(element), input 혹은 상수
const usernameInput = document.querySelector("#usernameInput");
const passwordInput = document.querySelector("#passwordInput");
const submitButton = document.querySelector("#submitButton");

blockIfLogin();
addAllElements();
addAllEvents();

// html에 요소를 추가하는 함수들을 묶어주어서 코드를 깔끔하게 하는 역할임.
async function addAllElements() {
  createNavbar();
}

// 여러 개의 addEventListener들을 묶어주어서 코드를 깔끔하게 하는 역할임.
function addAllEvents() {
  submitButton.addEventListener("click", handleSubmit);
}

// 로그인 진행
async function handleSubmit(e) {
  e.preventDefault();

  const username = usernameInput.value;
  const password = passwordInput.value;

  // 잘 입력했는지 확인
  const isEmailValid = validateEmail(username);
  const isPasswordValid = password.length >= 4;

  if (!isPasswordValid) {
    return alert(
      "비밀번호가 4글자 이상인지 확인해주세요"
    );
  }

  // 로그인 api 요청
  try {
    const userRealId=usernameInput.value;
    const password=passwordInput.value;
    const data = { userRealId, password };

//    const result = await Api.post("/api/login", data);
    const result = await fetch("/api/login",{
      method: "POST",
      headers : { "Content-Type": "application/json" },
      body: JSON.stringify(data),
      credentials: "include"
    });

    const resultData = await result.json(); // JSON 형식으로 응답을 파싱

    // 로그인 성공
    if(result.ok){
      alert('정상적으로 로그인하였습니다.');
    }else if(result.status ===401){
      alert('비밀번호를 잘못입력하셨습니다. 다시 입력해주세요');
      window.location.href = "/login";
      return;
    }else if(result.status ===404){
      alert('삭제된 유저거나 유저정보를 확인할수없습니다. 아이디를 다시입력해주세요.');
      window.location.href = "/login";
      return;
    }

    // admin(관리자) 일 경우, sessionStorage 에 기록함
    if (result.admin) {
      sessionStorage.setItem("admin", "admin");
    }

    // 기존 다른 페이지에서 이 로그인 페이지로 온 경우, 다시 돌아가도록 해 줌.
    const { previouspage } = getUrlParams();

    if (previouspage) {
      window.location.href = previouspage;

      return;
    }

    // 기존 다른 페이지가 없었던 경우, 그냥 기본 페이지로 이동
    window.location.href = "/";
  } catch (err) {
    console.error(err.stack);
    alert(`문제가 발생하였습니다. 확인 후 다시 시도해 주세요: ${err.message}`);
  }

}
