import { addImageToS3 } from "../../aws-s3.js";
import * as Api from "../../api.js";
import { checkLogin, createNavbar } from "../../useful-functions.js";

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

  if (themeSelectBox.value !== "HOW TO") {
    imageUploadField.style.display = "none";
    imageInput.value = ""; // 이미지 입력 초기화
    fileNameSpan.innerText = "사진파일 (png, jpg, jpeg)";
    console.log("Image upload field hidden.");
  } else {
    imageUploadField.style.display = "block";
    console.log("Image upload field shown.");
    alert("'HOW TO' 테마가 선택되었습니다. 이미지 업로드 필드가 활성화됩니다.");
  }
}

async function handleThemeChange() {
  const selectedTheme = themeSelectBox.value;
  console.log("Selected theme:", selectedTheme);

  if (selectedTheme) {
    try {
      const categories = await Api.get(`/api/categories/themes/${selectedTheme}`);
      updateDisplayOrderOptions(categories.length);
    } catch (err) {
      console.error("Error fetching categories:", err);
      alert('카테고리 정보를 불러오는데 실패했습니다.');
    }
  } else {
    // 테마가 선택되지 않았을 때 배치 선택 옵션 초기화
    updateDisplayOrderOptions(0);
  }

  toggleImageUploadField();
}

function updateDisplayOrderOptions(count) {
  displaySelectBox.innerHTML = '<option value="">배치할 위치를 골라주세요.</option>';
  for (let i = 1; i <= count + 1; i++) {
    const option = document.createElement('option');
    option.value = i;
    option.textContent = `${i}번째`;
    displaySelectBox.appendChild(option);
  }
}

async function fetchCategoryData() {
  console.log("Fetching category data...");
  try {
    const category = await Api.get('/api/admin/categories', categoryId);
    console.log("Category data received:", category);

    titleInput.value = category.categoryName;
    descriptionInput.value = category.categoryContent;
    themeSelectBox.value = category.categoryThema;

    console.log("Theme set to:", category.categoryThema);

    // 테마에 맞는 배치 옵션 업데이트
    const categories = await Api.get(`/api/categories/themes/${category.categoryThema}`);
    updateDisplayOrderOptions(categories.length);
    displaySelectBox.value = category.displayOrder;

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
  const displayOrder = displaySelectBox.value;
  const image = imageInput.files[0];

  if (!title || !theme || !displayOrder) {
    return alert("카테고리 이름, 테마, 배치 위치는 필수 입력 항목입니다.");
  }

  if (image && image.size > 3e6) {
    return alert("사진은 최대 2.5MB 크기까지 가능합니다.");
  }

  try {
    let imageUrl = null;
    if (image) {
      const imageKey = await addImageToS3(imageInput, "category");
      imageUrl = await getImageUrl(imageKey);
    }

    const data = {
      categoryName: title,
      categoryContent: description,
      categoryThema: theme,
      imageUrl: imageUrl,
      displayOrder: parseInt(displayOrder)
    };

    if (isEditMode) {
      await Api.patch(`/api/admin/categories/${categoryId}`, data);
      alert(`정상적으로 ${title} 카테고리가 수정되었습니다.`);
    } else {
      await Api.post("/api/admin/categories", data);
      alert(`정상적으로 ${title} 카테고리가 등록되었습니다.`);
    }
    window.location.href = '/admin/categories';
  } catch (err) {
    console.error(err.stack);
    alert(`문제가 발생하였습니다. 확인 후 다시 시도해 주세요: ${err.message}`);
  }
}

function handleImageUpload() {
  const file = imageInput.files[0];
  fileNameSpan.innerText = file ? file.name : "";
}

// 페이지 초기화
window.addEventListener('DOMContentLoaded', () => {
  console.log("DOM content loaded. Initializing page...");
  initializePage();
});