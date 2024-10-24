import * as Api from "../../api.js";
import { checkLogin, checkAdmin } from "../../useful-functions.js";
import { loadHeader } from "../../common/header.js";

// 요소(element), input 혹은 상수
const titleInput = document.querySelector("#titleInput");
const descriptionInput = document.querySelector("#descriptionInput");
const themeSelectBox = document.querySelector("#themeSelectBox");
const displaySelectBox = document.querySelector("#displaySelectBox");
const imageUploadField = document.querySelector("#imageUploadField");
const imageInput = document.querySelector("#imageInput");
const fileNameSpan = document.querySelector("#fileNameSpan");
const submitButton = document.querySelector("#submitCategoryButton");
const categoryForm = document.querySelector("#categoryForm");
const formTitle = document.querySelector("#formTitle");

const categoryId = new URLSearchParams(window.location.search).get('id');
const isEditMode = !!categoryId;
let originalTheme = '';


// 페이지 로드 시 초기 상태 설정
async function initializePage() {
  console.log("Initializing page...");
  await loadHeader();
  // checkLogin();
  addAllEvents();
  toggleImageUploadField(); // 초기 상태 설정
  if (isEditMode) {
    await fetchCategoryData();
    formTitle.textContent = "카테고리 수정하기";
    submitButton.textContent = "카테고리 수정하기";
  } else {
    formTitle.textContent = "카테고리 추가하기";
    submitButton.textContent = "카테고리 추가하기";
  }
  console.log("Page initialization complete.");
}

function addAllEvents() {
  console.log("Adding all events...");
  categoryForm.addEventListener("submit", handleSubmit);
  imageInput.addEventListener("change", handleImageUpload);
  themeSelectBox.addEventListener("change", handleThemeChange);
  /*descriptionInput.addEventListener('input', handleNotThemeChange);  // 카테고리 내용 변경 이벤트
  displaySelectBox.addEventListener('focus', handleNotThemeChange);  // 배치 순서 선택 시점 이벤트*/
  console.log("All events added.");
}

function toggleImageUploadField() {
  console.log("Toggling image upload field...");
  console.log("Current theme:", themeSelectBox.value);

  if (themeSelectBox.value !== "recommend") {
    imageUploadField.style.display = "none";
    imageInput.value = ""; // 이미지 입력 초기화
    fileNameSpan.innerText = "사진파일 (png, jpg, jpeg)";
    console.log("Image upload field hidden.");
  } else {
    imageUploadField.style.display = "block";
    console.log("Image upload field shown.");
  }
}

async function handleThemeChange() {
    const selectedTheme = themeSelectBox.value;
    console.log("Selected theme:", selectedTheme);

    if (!selectedTheme) return;

    try {
        // 현재 선택된 값 저장
        const currentValue = displaySelectBox.value;

        await updateDisplayOrderOptions(selectedTheme);

        // 이전 선택값이 있고 유효한 경우 복원
        if (currentValue && displaySelectBox.querySelector(`option[value="${currentValue}"]`)) {
            displaySelectBox.value = currentValue;
        }
    } catch (err) {
        console.error("Error updating display order options:", err);
        alert('배치 순서 옵션을 업데이트하는데 실패했습니다: ' + err.message);
    }

    toggleImageUploadField();
}

// 배치 순서 선택 시점 이벤트 핸들러
function handleNotThemeChange() {
    const selectedTheme = themeSelectBox.value;
    if (selectedTheme) {
        handleThemeChange();
    }
}

async function updateDisplayOrderOptions(selectedTheme, currentCategoryId = null, currentDisplayOrder = null) {
   // 기본 옵션으로 초기화
   displaySelectBox.innerHTML = '<option value="">배치할 위치를 골라주세요.</option>';

   // '전체보기' 카테고리인 경우 select box 비활성화
   if (titleInput.value.trim() === '전체보기') {
       displaySelectBox.disabled = true;
       return;
   }

   // select box 활성화
   displaySelectBox.disabled = false;

   if (selectedTheme) {
       try {
           // 선택된 테마의 모든 카테고리 조회
           const categories = await Api.get(`/api/categories/themas/${selectedTheme}`);

           // '전체보기'가 아니고 현재 수정 중인 카테고리가 아닌 카테고리만 필터링
           const nonAllViewCategories = categories.filter(c =>
               c.categoryName !== '전체보기' &&
               c.id !== currentCategoryId
           );

           // 최대 순서 결정
           const maxOrder = isEditMode && selectedTheme === originalTheme
               ? nonAllViewCategories.length      // 같은 테마 내에서 수정 시
               : nonAllViewCategories.length + 1; // 새로운 테마로 변경 시

           // maxOrder까지의 옵션 생성
           for (let i = 1; i <= maxOrder; i++) {
               const option = document.createElement('option');
               option.value = i;
               option.textContent = `${i}번째`;

               // 수정 모드면서 현재 카테고리의 displayOrder와 일치하면 선택
               if (isEditMode && i === currentDisplayOrder) {
                   option.selected = true;
               }
               displaySelectBox.appendChild(option);
           }
       } catch (err) {
           console.error("Error fetching categories:", err);
           alert('카테고리 정보를 불러오는데 실패했습니다: ' + err.message);
       }
   }
}

// 카테고리 이름 입력 필드에 이벤트 리스너 추가
titleInput.addEventListener('input', function() {
    // 입력값 정규화 (공백 제거)
    const normalizedValue = this.value.trim();

    const selectedTheme = themeSelectBox.value;
    if (!selectedTheme) return;

    // 이전 값과 현재 값이 같으면 중복 실행 방지
    if (this._lastValue === normalizedValue) return;
    this._lastValue = normalizedValue;

    // 비동기 실행을 위한 setTimeout 사용
    clearTimeout(this._timeout);
    this._timeout = setTimeout(() => {
        handleThemeChange();
    }, 100);
});


async function fetchCategoryData() {
  try {
    const category = await Api.get('/api/admin/categories', categoryId);
    console.log("Category data received:", category);

    titleInput.value = category.categoryName;
    descriptionInput.value = category.categoryContent;
    themeSelectBox.value = category.categoryThema;
    originalTheme = category.categoryThema; // 원래 테마 저장

    const koreanTheme = translateEnglishToKorean(category.categoryThema);
    console.log("Theme set to:", koreanTheme);

    // 테마에 맞는 배치 옵션 업데이트
    //await updateDisplayOrderOptions(category.categoryThema, category.id, category.displayOrder);

    // 테마 변경 핸들러 호출하여 배치 옵션 즉시 업데이트
    await handleThemeChange();

    // 전체보기가 아닌 경우에만 displayOrder 설정
    if (category.categoryName !== '전체보기') {
      displaySelectBox.value = category.displayOrder;
    }

    if (category.imageUrl) {
      fileNameSpan.innerText = category.imageUrl.split('/').pop();
      console.log("Image filename set to:", fileNameSpan.innerText);
    } else {
      fileNameSpan.innerText = '사진파일 (png, jpg, jpeg)';
      console.log("No image URL, reset to default text");
    }

    console.log("Updating image field state...");
    toggleImageUploadField();

    console.log("Category data loaded successfully");
  } catch (err) {
    console.error("Error fetching category data:", err);
    alert('카테고리 정보를 불러오는데 실패했습니다.');
  }
}

async function handleSubmit(e) {
  e.preventDefault();

  const title = titleInput.value;
  const description = descriptionInput.value;
  const theme = themeSelectBox.value;
  const displayOrder = title === '전체보기' ? 0 : parseInt(displaySelectBox.value, 10);
  const image = imageInput.files[0];

  if (!title || !theme || (title !== '전체보기' && isNaN(displayOrder))) {
    return alert("카테고리 이름, 테마, 배치 위치는 필수 입력 항목입니다.");
  }

  if (image && image.size > 500 * 1024) {
    return alert("이미지의 크기가 최대 500KB 크기를 초과했습니다.");
  }

  try {
    // JSON 데이터 추가
    const formData = new FormData();
    formData.append('category', new Blob([JSON.stringify({
      categoryName: title,
      categoryContent: description,
      categoryThema: theme,
      displayOrder: displayOrder
    })], {type: 'application/json'}));

    // 이미지 파일 추가
    if (image) {
      formData.append('file', image);
    }

    let response;
    if (isEditMode) {
      response = await Api.patchFormData(`/api/admin/categories/${categoryId}`, formData);
      alert(`정상적으로 ${title} 카테고리가 수정되었습니다.`);
    } else {
      response = await Api.postFormData("/api/admin/categories", formData);
      alert(`정상적으로 ${title} 카테고리가 등록되었습니다.`);
    }

    window.location.href = '/admin/categories';
  } catch (err) {
    console.error("Error details:", err);
    alert(`문제가 발생하였습니다. 확인 후 다시 시도해 주세요: ${err.message}`);
  }
}

function handleImageUpload() {
  const file = imageInput.files[0];
  const imagePreview = document.getElementById('imagePreview');

  if (file) {
    fileNameSpan.innerText = file.name;

    // 이미지 미리보기
    const reader = new FileReader();
    reader.onload = function(e) {
      imagePreview.src = e.target.result;
      imagePreview.style.display = 'block';
    }
    reader.readAsDataURL(file);
  } else {
    fileNameSpan.innerText = "사진파일 (png, jpg, jpeg)";
    imagePreview.style.display = 'none';
    imagePreview.src = '#';
  }
}

function translateEnglishToKorean(englishTheme) {
  const themeMap = {
    'common': '공용',
    'women': '여성',
    'men': '남성',
    'accessories': '액세서리',
    'recommend': 'RECOMMEND'
  };
  return themeMap[englishTheme];
}

function translateKoreanToEnglish(koreanTheme) {
  const themeMap = {
    '공용': 'common',
    '여성': 'women',
    '남성': 'men',
    '액세서리': 'accessories',
    'RECOMMEND': 'recommend'
  };
  return themeMap[koreanTheme];
}

// 페이지 로드 시 페이지 초기화 실행
document.addEventListener('DOMContentLoaded', initializePage);
