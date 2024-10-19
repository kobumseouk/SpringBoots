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
  console.log("All events added.");
}

function toggleImageUploadField() {
  console.log("Toggling image upload field...");
  console.log("Current theme:", themeSelectBox.value);

  if (themeSelectBox.value !== "how-to") {
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

  if (selectedTheme) {
    try {
      const categories = await Api.get(`/api/categories/themas/${selectedTheme}`);
      const nonAllViewCategories = categories.filter(c => c.categoryName !== '전체보기' && (!isEditMode || c.id !== categoryId));
      updateDisplayOrderOptions(nonAllViewCategories.length);
    } catch (err) {
      console.error("Error fetching categories:", err);
      alert('카테고리 정보를 불러오는데 실패했습니다: ' + err.message);
    }
  } else {
    updateDisplayOrderOptions(0);
  }

  toggleImageUploadField();
}

function updateDisplayOrderOptions(count) {
  displaySelectBox.innerHTML = '<option value="">배치할 위치를 골라주세요.</option>';

  if (titleInput.value.trim().toLowerCase() === '전체보기') {
    displaySelectBox.disabled = true;
    return;
  }

  displaySelectBox.disabled = false;

  if (isEditMode) {
    // 수정 모드: 카테고리 숫자와 동일하게 배치 순서 결정
    for (let i = 1; i <= count; i++) {
      const option = document.createElement('option');
      option.value = i;
      option.textContent = `${i}번째`;
      displaySelectBox.appendChild(option);
    }
  } else {   // 등록 모드: 배치 가능한 모든 위치에 대한 옵션 추가
    for (let i = 1; i <= count + 1; i++) {
      const option = document.createElement('option');
      option.value = i;
      option.textContent = `${i}번째`;
      displaySelectBox.appendChild(option);
    }
  }
}

// 카테고리 이름 입력 필드에 이벤트 리스너 추가
titleInput.addEventListener('input', function() {
  const selectedTheme = themeSelectBox.value;
  if (selectedTheme) {
    handleThemeChange();
  }
});

async function fetchCategoryData() {
  try {
    const category = await Api.get('/api/admin/categories', categoryId);
    console.log("Category data received:", category);

    titleInput.value = category.categoryName;
    descriptionInput.value = category.categoryContent;
    themeSelectBox.value = category.categoryThema;

    const koreanTheme = translateEnglishToKorean(category.categoryThema);
    console.log("Theme set to:", koreanTheme);

    // 테마에 맞는 배치 옵션 업데이트
    const categories = await Api.get(`/api/categories/themas/${category.categoryThema}`);
    const nonAllViewCategories = categories.filter(c => c.categoryName !== '전체보기');
    updateDisplayOrderOptions(nonAllViewCategories.length);

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

  if (image && image.size > 3e6) {
    return alert("사진은 최대 2.5MB 크기까지 가능합니다.");
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
    'how-to': 'HOW TO'
  };
  return themeMap[englishTheme];
}

function translateKoreanToEnglish(koreanTheme) {
  const themeMap = {
    '공용': 'common',
    '여성': 'women',
    '남성': 'men',
    '액세서리': 'accessories',
    'HOW TO': 'how-to'
  };
  return themeMap[koreanTheme];
}

// 페이지 로드 시 페이지 초기화 실행
document.addEventListener('DOMContentLoaded', initializePage);
