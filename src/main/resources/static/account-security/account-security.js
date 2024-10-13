import { checkLogin, createNavbar } from "../../useful-functions.js";
import * as Api from "../../api.js";

// 요소(element), input 혹은 상수
const securityTitle = document.querySelector("#securityTitle");
const userRealIdInput = document.querySelector("#userRealIdInput");
//const userRealIdToggle = document.querySelector("#userRealIdToggle");
const fullNameInput = document.querySelector("#fullNameInput");
//const fullNameToggle = document.querySelector("#fullNameToggle");
const emailInput = document.querySelector("#userEmailInput");
const emailToggle = document.querySelector("#userEmailToggle");
const passwordInput = document.querySelector("#passwordInput");
const passwordToggle = document.querySelector("#passwordToggle");
const passwordConfirmInput = document.querySelector("#passwordConfirmInput");
const postalCodeInput = document.querySelector("#postalCodeInput");
const searchAddressButton = document.querySelector("#searchAddressButton");
const addressToggle = document.querySelector("#addressToggle");
const address1Input = document.querySelector("#address1Input");
const address2Input = document.querySelector("#address2Input");
const phoneNumberInput = document.querySelector("#phoneNumberInput");
const phoneNumberToggle = document.querySelector("#phoneNumberToggle");
const saveButton = document.querySelector("#saveButton");
const modal = document.querySelector("#modal");
const modalBackground = document.querySelector("#modalBackground");
const modalCloseButton = document.querySelector("#modalCloseButton");
const currentPasswordInput = document.querySelector("#currentPasswordInput");
const saveCompleteButton = document.querySelector("#saveCompleteButton");

checkLogin();
addAllElements();
addAllEvents();

// 요소 삽입 함수들을 묶어주어서 코드를 깔끔하게 하는 역할임.
function addAllElements() {
  createNavbar();
  insertUserData();
}

// 여러 개의 addEventListener들을 묶어주어서 코드를 깔끔하게 하는 역할임.
function addAllEvents() {
//  fullNameToggle.addEventListener("change", toggleTargets);
  emailToggle.addEventListener("change", toggleTargets);
  passwordToggle.addEventListener("change", toggleTargets);
  addressToggle.addEventListener("change", toggleTargets);
  phoneNumberToggle.addEventListener("change", toggleTargets);
  searchAddressButton.addEventListener("click", searchAddress);
  saveButton.addEventListener("click", openModal);
  modalBackground.addEventListener("click", closeModal);
  modalCloseButton.addEventListener("click", closeModal);
  document.addEventListener("keydown", keyDownCloseModal);
  saveCompleteButton.addEventListener("click", saveUserData);
}

// input 및 주소찾기 버튼의 disabled <-> abled 상태를 토글함.
function toggleTargets(e) {
  const toggleId = e.target.id;
  const isChecked = e.target.checked;

  // 어떤 요소들의 토글인지 확인
  let targets = []; // 기본적으로 빈 배열로 초기화

  if (toggleId.includes("email")) {
    targets = [emailInput];
  }

  if (toggleId.includes("password")) {
    targets = [passwordInput, passwordConfirmInput];
  }
  if (toggleId.includes("address")) {
    targets = [
      postalCodeInput,
      address1Input,
      address2Input,
      searchAddressButton,
    ];
  }
  if (toggleId.includes("phoneNumber")) {
    targets = [phoneNumberInput];
  }

  // 여러 개의 타겟이 있을 때, 첫 타겟만 focus 시키기 위한 flag
  let isFocused = false;

  // 토글 진행
  for (const target of targets) {
    if (isChecked) {
      target.removeAttribute("disabled");

      if (!isFocused) {
        target.focus();
        isFocused = true;
      }
    } else {
      target.setAttribute("disabled", ""); // 비활성화
    }
  }
}


// 페이지 로드 시 실행
// 나중에 사용자가 데이터를 변경했는지 확인하기 위해, 전역 변수로 userData 설정
let userData;
let userId; //userId 정보 갖고오기(Pathvariable 로 유저정보 엔티티를 가져오기 위해 설정)
async function insertUserData() {
  userData = await Api.get("/api/users-info");
  userId= userData.userId;  //userId 정보 추출하기

  // 객체 destructuring
  const { username, userRealId, email, userInfoList } = userData;

  // 서버에서 온 비밀번호는 해쉬 문자열인데, 이를 빈 문자열로 바꿈
  // 나중에 사용자가 비밀번호 변경을 위해 입력했는지 확인하기 위함임.
  userData.password = "";

  securityTitle.innerText = `회원정보 관리 (${email})`;
  fullNameInput.value = username;
  userRealIdInput.value = userRealId;
  if(email){
    emailInput.value = email;
  }

  // userInfoList 배열에서 address와 phoneNumber 추출
  if (userInfoList && userInfoList.length > 0) {
    const { address, streetAddress, detailedAddress, phone } = userInfoList[0]; // 첫 번째 객체에서 정보 추출

    if (address) {
      const { address, streetAddress, detailedAddress, phone } = userInfoList[0]; // 첫 번째 객체에서 정보 추출

      if (address || streetAddress || detailedAddress) {
        // 주소 정보 설정
        postalCodeInput.value = address || ""; // address가 없을 경우 빈 문자열
        address1Input.value = streetAddress || ""; // streetAddress가 없을 경우 빈 문자열
        address2Input.value = detailedAddress || ""; // detailedAddress가 없을 경우 빈 문자열
      } else {
        // 나중에 입력 여부를 확인하기 위해 설정함
        userData.address = { address: "", streetAddress: "", detailedAddress: "" };
      }

      if (phone) {
        phoneNumberInput.value = phone;
      }
  }
}

  // 크롬 자동완성 삭제함.
  passwordInput.value = "";

  // 기본적으로 disabled 상태로 만듦
  disableForm();
}

function disableForm() {
  emailInput.setAttribute("disabled", "");
  emailToggle.checked = false;
  userRealIdInput.setAttribute("disabled","");
  fullNameInput.setAttribute("disabled", "");
//  fullNameToggle.checked = false;
  passwordInput.setAttribute("disabled", "");
  passwordToggle.checked = false;
  passwordConfirmInput.setAttribute("disabled", "");
  postalCodeInput.setAttribute("disabled", "");
  addressToggle.checked = false;
  searchAddressButton.setAttribute("disabled", "");
  address1Input.setAttribute("disabled", "");
  address2Input.setAttribute("disabled", "");
  phoneNumberToggle.checked = false;
  phoneNumberInput.setAttribute("disabled", "");
}

// Daum 주소 API (사용 설명 https://postcode.map.daum.net/guide)
function searchAddress(e) {
  e.preventDefault();

  new daum.Postcode({
    oncomplete: function (data) {
      let addr = "";
      let extraAddr = "";

      if (data.userSelectedType === "R") {
        addr = data.roadAddress;
      } else {
        addr = data.jibunAddress;
      }

      if (data.userSelectedType === "R") {
        if (data.bname !== "" && /[동|로|가]$/g.test(data.bname)) {
          extraAddr += data.bname;
        }
        if (data.buildingName !== "" && data.apartment === "Y") {
          extraAddr +=
            extraAddr !== "" ? ", " + data.buildingName : data.buildingName;
        }
        if (extraAddr !== "") {
          extraAddr = " (" + extraAddr + ")";
        }
      } else {
      }

      postalCodeInput.value = data.zonecode;
      address1Input.value = `${addr} ${extraAddr}`;
      address2Input.placeholder = "상세 주소를 입력해 주세요.";
      address2Input.focus();
    },
  }).open();
}

// db에 정보 저장
async function saveUserData(e) {
  e.preventDefault();

//  const username = fullNameInput.value;
  const updatePassword = passwordInput.value;
  const passwordConfirm = passwordConfirmInput.value;
  const postalCode = postalCodeInput.value;
  const address1 = address1Input.value;
  const address2 = address2Input.value;
  const phoneNumber = phoneNumberInput.value;
  const currentPassword = currentPasswordInput.value;
  const email =emailInput.value;

  const isPasswordLong = updatePassword.length >= 4;
  const isPasswordSame = updatePassword === passwordConfirm;
  const isPostalCodeChanged = postalCode !== (userData.address?.postalCode || "");
  const isAddress2Changed = address2 !== (userData.address?.address2 || "");
  const isAddressChanged = isPostalCodeChanged || isAddress2Changed;
  const isEmailChanged = email !== userData.email;

  // 비밀번호를 새로 작성한 경우
  if (updatePassword && !isPasswordLong) {
    closeModal();
    return alert("비밀번호는 4글자 이상이어야 합니다.");
  }
  if (updatePassword && !isPasswordSame) {
    closeModal();
    return alert("비밀번호와 비밀번호확인이 일치하지 않습니다.");
  }

  // Prepare the data object with currentPassword
  const data = { currentPassword };

  // 초기값과 다를 경우 api 요청에 사용할 data 객체에 넣어줌
//  if (username !== userData.username) {
//    data.username = username;
//  }

  if (updatePassword !== userData.password) {
    data.updatePassword = updatePassword;
  }

  // 주소를 변경했는데, 덜 입력한 경우
  if (isAddressChanged && !address2) {
    closeModal();
    return alert("주소를 모두 입력해 주세요.");
  }

  data.email = email;

  data.address = [{
    address: postalCode,
    streetAddress: address1,
    detailedAddress: address2,
    phone: phoneNumber,
  }];

  // 만약 업데이트할 것이 없다면 종료
  const toUpdate = Object.keys(data);
  if (toUpdate.length === 1) {
    disableForm();
    closeModal();
    return alert("업데이트된 정보가 없습니다");
  }

  try {
    const { id } = userData;
    console.log(data);
    // db에 수정된 정보 저장
    await Api.patch("/api/users",userId, data);  //첫번째의 유저 정보를 가지고옴

    alert("회원정보가 안전하게 저장되었습니다.");
    disableForm();
    closeModal();
  } catch (err) {
    alert(`회원정보 저장 과정에서 오류가 발생하였습니다: ${err}`);
  }
}

// Modal 창 열기
function openModal(e) {
  e.preventDefault();

  modal.classList.add("is-active");
  currentPasswordInput.focus();
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
