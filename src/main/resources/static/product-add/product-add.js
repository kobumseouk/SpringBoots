import { checkLogin, randomId, createNavbar } from "../../useful-functions.js";

// 요소(element)들과 상수들
const titleInput = document.querySelector("#titleInput");
const categorySelectBox = document.querySelector("#categorySelectBox");
const manufacturerInput = document.querySelector("#manufacturerInput");
const shortDescriptionInput = document.querySelector("#shortDescriptionInput");
const detailDescriptionInput = document.querySelector(
  "#detailDescriptionInput"
);
const imageInput = document.querySelector("#imageInput");
const inventoryInput = document.querySelector("#inventoryInput");
const priceInput = document.querySelector("#priceInput");
const searchKeywordInput = document.querySelector("#searchKeywordInput");
const addKeywordButton = document.querySelector("#addKeywordButton");
const keywordsContainer = document.querySelector("#keywordContainer");
const submitButton = document.querySelector("#submitButton");
const registerProductForm = document.querySelector("#registerProductForm");


document.addEventListener('DOMContentLoaded', () => {
// 여기에서 모든 기존 코드를 넣습니다.
    const titleInput = document.querySelector("#titleInput");
    const categorySelectBox = document.querySelector("#categorySelectBox");
    const manufacturerInput = document.querySelector("#manufacturerInput");
    const shortDescriptionInput = document.querySelector("#shortDescriptionInput");
    const detailDescriptionInput = document.querySelector("#detailDescriptionInput");
    const imageInput = document.querySelector("#imageInput");
    const inventoryInput = document.querySelector("#inventoryInput");
    const priceInput = document.querySelector("#priceInput");
    const searchKeywordInput = document.querySelector("#searchKeywordInput");
    const addKeywordButton = document.querySelector("#addKeywordButton");
    const keywordsContainer = document.querySelector("#keywordContainer");
    const submitButton = document.querySelector("#submitButton");
    const registerProductForm = document.querySelector("#registerProductForm");

// 카테고리 변경 이벤트
    window.categoryChang = function(selectElement) {
        const selectedTheme = selectElement.value;
        const selectBox = categorySelectBox;

        // 기존 카테고리 옵션 제거
        selectBox.innerHTML = '<option value="">카테고리를 선택해 주세요.</option>';

        if (selectedTheme) {
            fetchCategories(selectedTheme);
        }
    };

// 키워드 배열
    let keywords = []; // 키워드 저장 배열

    addKeywordButton.addEventListener('click', function() {
        const input = searchKeywordInput;
        const keyword = input.value.trim();

        if (keyword) {
            if (keywords.length < 5) { // 키워드 개수 제한
                keywords.push(keyword); // 키워드 추가
                input.value = ''; // 입력 필드 초기화
                renderKeywordList(); // 키워드 리스트 갱신
            } else {
                alert('키워드는 최대 5개까지 추가할 수 있습니다.'); // 최대 개수 알림
            }
        }
});

function renderKeywordList() {
  const keywordList = document.getElementById('keywordList');
  keywordList.innerHTML = ''; // 리스트 초기화

  keywords.forEach((keyword, index) => {
      const li = document.createElement('li');
      li.textContent = keyword; // 키워드 텍스트 추가
      keywordList.appendChild(li);
  });
}

function categoryChang(selectElement) {
    const selectedTheme = selectElement.value;
    const selectBox = document.getElementById('categorySelectBox');

    // 기존 카테고리 옵션 제거
    selectBox.innerHTML = '<option value="">카테고리를 선택해 주세요.</option>';

    if (selectedTheme) {
        fetchCategories(selectedTheme);
    }
}

// 카테고리 동적 할당
function fetchCategories(theme) {
    fetch(`/api/categories/themas/${theme}`) // 선택한 테마에 따라 카테고리 API 호출
        .then(response => response.json())
        .then(categories => {
            const selectBox = document.getElementById('categorySelectBox');
            categories.forEach(category => {
                const option = document.createElement('option');
                option.value = category.id; // 카테고리 ID 설정
                option.textContent = category.categoryName; // 카테고리 이름 설정
                selectBox.appendChild(option); // select 박스에 추가
            });
        })
        .catch(error => console.error('Error fetching categories:', error));
}

// 이미지 미리보기 동적 할당ㄹ
function showPreviewImage(input) {
const file = input.files[0]; // 선택한 파일 가져오기
const preview = document.getElementById('imagePreview'); // 미리보기 이미지 요소

if (file) {
  const reader = new FileReader(); // FileReader 객체 생성

  reader.onload = function (e) {
    preview.src = e.target.result; // 파일의 데이터 URL을 미리보기 이미지의 src에 설정
    preview.style.display = 'block'; // 이미지를 표시
    };

  reader.readAsDataURL(file); // 파일을 데이터 URL로 읽기
} else {
preview.src = '#'; // 파일이 없을 경우 초기화
preview.style.display = 'none'; // 이미지를 숨김
}
const fileNameSpan = document.getElementById("fileNameSpan");
if (input.files && input.files[0]) {
  const fileName = input.files[0].name; // 선택된 파일의 이름 가져오기
  fileNameSpan.textContent = fileName; // 파일 이름을 span에 설정
} else {
  fileNameSpan.textContent = "사진파일 (png, jpg, jpeg)"; // 초기 텍스트로 되돌리기
  }
}

// 상품 추가 버튼
document.getElementById('registerProductForm').addEventListener('submit', function(event) {
  event.preventDefault(); // 기본 제출 이벤트 방지
  if (keywords.length === 0) {
    alert('키워드를 추가하세요.');
    return;
    }

  const formData = new FormData(this); // 현재 폼의 데이터를 FormData 객체로 생성

  keywords.forEach((keyword, index) => {
    formData.append('keywords', keyword);
  });

  fetch('/api/admin/items', {
      method: 'POST',
      body: formData,
  })
  .then(response => {
      if (!response.ok) {
          throw new Error('Network response was not ok');
      }
      return response.json();
  })
  .then(data => {
      console.log ('Success', data);
      // 성공적으로 추가된 후의 처리
      alert('상품이 성공적으로 추가되었습니다.'); // 알림 메시지 추가
      window.location.href = '/admin/items/list'; // 페이지 리다이렉트
  })
  .catch((error) => {
      console.error('Error', error);
      // 오류 처리
  });
});
