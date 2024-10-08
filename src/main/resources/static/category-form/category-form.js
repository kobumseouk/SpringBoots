import { loadHeader } from "../../common/common-header.js";
import { addImageToS3 } from "../../aws-s3.js";
import * as Api from "../../api.js";
import { checkLogin, createNavbar } from "../../useful-functions.js";

// 요소(element), input 혹은 상수
const titleInput = document.querySelector("#titleInput");
const descriptionInput = document.querySelector("#descriptionInput");
const themeSelectBox = document.querySelector("#themeSelectBox");
const imageUploadField = document.querySelector("#imageUploadField");
const imageInput = document.querySelector("#imageInput");
const fileNameSpan = document.querySelector("#fileNameSpan");
const submitButton = document.querySelector("#submitCategoryButton");
const categoryForm = document.querySelector("#categoryForm");

const categoryId = new URLSearchParams(window.location.search).get('id');
const isEditMode = !!categoryId;

async function initializePage() {
  await loadHeader();
  addAllElements();
  addAllEvents();
  if (isEditMode) {
    await fetchCategoryData();
  }
  toggleImageUploadField(); // 초기 상태 설정
}

async function addAllElements() {
  createNavbar();
}

function addAllEvents() {
  submitButton.addEventListener("click", handleSubmit);
  imageInput.addEventListener("change", handleImageUpload);
  themeSelectBox.addEventListener("change", toggleImageUploadField);
}

function toggleImageUploadField() {
  if (themeSelectBox.value === "HOW TO") {
    imageUploadField.style.display = "block";
  } else {
    imageUploadField.style.display = "none";
    imageInput.value = ""; // 이미지 입력 초기화
    fileNameSpan.innerText = "사진파일 (png, jpg, jpeg)";
  }
}

async function fetchCategoryData() {
  try {
    const category = await Api.get('/api/admin/categories', categoryId);
    titleInput.value = category.categoryName;
    descriptionInput.value = category.categoryContent;
    themeSelectBox.value = category.categoryThema;
    fileNameSpan.innerText = category.imageUrl ? category.imageUrl.split('/').pop() : '사진파일 (png, jpg, jpeg)';
    toggleImageUploadField(); // 데이터 로드 후 이미지 필드 상태 업데이트
  } catch (err) {
    console.error(err);
    alert('카테고리 정보를 불러오는데 실패했습니다.');
  }
}

async function handleSubmit(e) {
  e.preventDefault();

  const title = titleInput.value;
  const description = descriptionInput.value;
  const theme = themeSelectBox.value;
  const image = imageInput.files[0];

  if (!title || theme === "") {
    return alert("카테고리 이름과 테마는 필수 입력 항목입니다.");
  }

  if (image && image.size > 3e6) {
    return alert("사진은 최대 2.5MB 크기까지 가능합니다.");
  }

  try {
    let imageKey = null;
    if (image) {
      imageKey = await addImageToS3(imageInput, "category");
    }

    const data = { categoryName: title, categoryContent: description, categoryThema: theme };
    if (imageKey) {
      data.imageUrl = imageKey;
    }

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
window.addEventListener('DOMContentLoaded', initializePage);

