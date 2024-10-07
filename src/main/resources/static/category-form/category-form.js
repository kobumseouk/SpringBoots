import { loadHeader } from "../../common.js";
import { addImageToS3 } from "../../aws-s3.js";
import * as Api from "../../api.js";
import { checkLogin, createNavbar } from "../../useful-functions.js";

// 요소(element), input 혹은 상수
const titleInput = document.querySelector("#titleInput");
const descriptionInput = document.querySelector("#descriptionInput");
const themeSelectBox = document.querySelector("#themeSelectBox");
const imageInput = document.querySelector("#imageInput");
const fileNameSpan = document.querySelector("#fileNameSpan");
const submitButton = document.querySelector("#submitCategoryButton");
const categoryForm = document.querySelector("#categoryForm");

const categoryId = new URLSearchParams(window.location.search).get('id');
const isEditMode = !!categoryId;

async function initializePage() {
  await loadHeader();
  checkLogin();
  addAllElements();
  addAllEvents();
  if (isEditMode) {
    await fetchCategoryData();
  }
}

async function addAllElements() {
  createNavbar();
}

function addAllEvents() {
  submitButton.addEventListener("click", handleSubmit);
  imageInput.addEventListener("change", handleImageUpload);
}

async function fetchCategoryData() {
  try {
    const category = await Api.get('/categories', categoryId);
    titleInput.value = category.categoryName;
    descriptionInput.value = category.categoryContent;
    themeSelectBox.value = category.categoryThema;
    fileNameSpan.innerText = category.imageUrl ? category.imageUrl.split('/').pop() : '사진파일 (png, jpg, jpeg)';
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

  if (!title || !description || theme === "") {
    return alert("모든 필드를 채워주세요.");
  }

  if (image && image.size > 3e6) {
    return alert("사진은 최대 2.5MB 크기까지 가능합니다.");
  }

  try {
    const imageKey = image ? await addImageToS3(imageInput, "category") : null;
    const data = { categoryName: title, categoryContent: description, categoryThema: theme };
    if (imageKey) {
      data.imageUrl = imageKey;
    }

    if (isEditMode) {
      await Api.patch(`/categories/${categoryId}`, data);
      alert(`정상적으로 ${title} 카테고리가 수정되었습니다.`);
      window.location.href = '/admin/categories';
    } else {
      await Api.post("/categories", data);
      alert(`정상적으로 ${title} 카테고리가 등록되었습니다.`);
      categoryForm.reset();
      fileNameSpan.innerText = "";
    }
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