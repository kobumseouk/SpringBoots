// 문자열+숫자로 이루어진 랜덤 5글자 반환
export const randomId = () => {
  return Math.random().toString(36).substring(2, 7);
};

//원하는 쿠키 가지고오기 HttpOnly false 일 경우
const getCookie = (cookieName) => {
  const name = cookieName + "=";
  const decodedCookie = decodeURIComponent(document.cookie);  // 쿠키 값을 URI 디코딩
  const cookieArray = decodedCookie.split(';');  // 쿠키를 세미콜론(;) 기준으로 나눔

  for (let i = 0; i < cookieArray.length; i++) {
    let cookie = cookieArray[i].trim();  // 공백 제거
    if (cookie.indexOf(name) === 0) {
      return cookie.substring(name.length, cookie.length);  // 원하는 쿠키 값 반환
    }
  }
  return null;  // 해당 쿠키가 없을 경우 null 반환
};

// 이메일 형식인지 확인 (true 혹은 false 반환)
export const validateEmail = (email) => {
  return String(email)
    .toLowerCase()
    .match(
      /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
    );
};

// 주소창의 url로부터 params를 얻어 객체로 만듦
export const getUrlParams = () => {
  const queryString = window.location.search;
  const urlParams = new URLSearchParams(queryString);

  const result = {};

  for (const [key, value] of urlParams) {
    result[key] = value;
  }

  return result;
};

// 숫자에 쉼표를 추가함. (10000 -> 10,000)
export const addCommas = (n) => {
  return n.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
};

// 로그인 여부(토큰 존재 여부) 확인
export const checkLogin = () => {
  const accessToken = getCookie("accessToken");
  const refreshToken = getCookie("refreshToken");

  if (!accessToken && !refreshToken) {
    // 현재 페이지의 url 주소 추출하기
    const pathname = window.location.pathname;
    const search = window.location.search;

    // 로그인 후 다시 지금 페이지로 자동으로 돌아가도록 하기 위한 준비작업임.
    window.location.replace(`/login?previouspage=${pathname + search}`);
    return false;
  }
  return true;
};

// 관리자 여부 확인
export const checkAdmin = async () => {
  // 쿠키에서 accessToken을 가져오기
  const accessToken = getCookie("accessToken");
  const refreshToken = getCookie("refreshToken");

  // 토큰 존재 여부 확인
  if (!accessToken && !refreshToken) {
    const pathname = window.location.pathname;
    const search = window.location.search;
    window.location.replace(`/login?previouspage=${pathname + search}`);
    return;
  }

  // 쿠키에 토큰을 저장하는 로직 (이미 저장되어 있다면 불필요)
//  document.cookie = `accessToken=${accessToken}; path=/; Secure; HttpOnly; SameSite=Strict`;

  // 쿠키가 포함된 상태로 요청 보내기 (쿠키는 자동으로 포함됨)
  try {
    const res = await fetch("/api/users/admin-check", {
      credentials: 'include', // 쿠키를 자동으로 포함하게 하는 옵션
    });

    if (!res.ok) {
      throw new Error("서버 응답 오류");
    }

    const { message } = await res.json();

    if (message === "관리자 인증 성공") {
      window.document.body.style.display = "block";
      return;
    } else {
      alert("관리자 전용 페이지입니다.");
      window.location.replace("/");
    }
  } catch (error) {
    console.error("관리자 체크 중 오류 발생:", error);
    alert("관리자 확인 중 문제가 발생했습니다. 다시 시도해주세요.");
    window.location.replace("/login");
  }
};

// 로그인 상태일 때에는 접근 불가한 페이지로 만듦. (회원가입 페이지 등)
export const blockIfLogin = () => {
  const accessToken = getCookie("accessToken");
  const refreshToken = getCookie("refreshToken");


  if(accessToken){
    console.log("access Token: ",accessToken);
  }else{
    console.log("access 실행안됨");
  }

  if(refreshToken){
    console.log("refresh Token: ",refreshToken)
  }else{
       console.log("access 실행안됨");
  }

  if (accessToken || refreshToken) {
    alert("로그인 상태에서는 접근할 수 없는 페이지입니다.");
    window.location.replace("/");
  }
};

// 해당 주소로 이동하는 콜백함수를 반환함.
// 이벤트 핸들 함수로 쓰면 유용함
export const navigate = (pathname) => {
  return function () {
    window.location.href = pathname;
  };
};

// 13,000원, 2개 등의 문자열에서 쉼표, 글자 등 제외 후 숫자만 뺴냄
// 예시: 13,000원 -> 13000, 20,000개 -> 20000
export const convertToNumber = (string) => {
  return parseInt(string.replace(/(,|개|원)/g, ""));
};

// ms만큼 기다리게 함.
export const wait = (ms) => {
  return new Promise((r) => setTimeout(r, ms));
};

// 긴 문자열에서 뒷부분을 ..으로 바꿈
export const compressString = (string) => {
  if (string.length > 10) {
    return string.substring(0, 9) + "..";
  }
  return string;
};

// 주소에 특정 params가 없다면 잘못된 접근으로 하고 싶은 경우 사용.
export const checkUrlParams = (key) => {
  const { [key]: params } = getUrlParams();

  if (!params) {
    window.location.replace("/page-not-found");
  }
};

// 배열 혹은 객체에서 랜덤으로 1개 고름
export const randomPick = (items) => {
  const isArray = Array.isArray(items);

  // 배열인 경우
  if (isArray) {
    const randomIndex = [Math.floor(Math.random() * items.length)];

    return items[randomIndex];
  }

  // 객체인 경우
  const keys = Object.keys(items);
  const randomIndex = [Math.floor(Math.random() * keys.length)];
  const randomKey = keys[randomIndex];

  return items[randomKey];
};

// 주변 다른 파일 것도 여기서 일괄 export 함
export { createNavbar } from "./navbar.js"; // 얘는 또 navbar 랑 연결되어있넹
