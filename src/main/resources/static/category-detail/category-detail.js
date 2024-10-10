import { loadHeader } from '/common/header.js';
import * as Api from '/api.js';

async function init() {
  await loadHeader();

  const path = window.location.pathname;
  const pathParts = path.split('/');

  if (pathParts.length >= 4 && pathParts[1] === 'categories') {
    const encodedThema = pathParts[2];
    const categoryId = pathParts[3];
    const thema = decodeURIComponent(encodedThema);
    await handleThemaClick(thema, categoryId);
  } else {
    window.location.href = '/';
  }

  // 정렬 및 필터 이벤트 리스너 추가
  document.getElementById('sort-options').addEventListener('change', handleSortChange);
  document.getElementById('filter-options').addEventListener('change', handleFilterChange);
}

async function handleThemaClick(thema, selectedCategoryId = null) {
  try {
    const categories = await Api.get(`/api/categories/themas/${encodeURIComponent(thema)}`);
    displayCategoryButtons(categories);

    let categoryToDisplay;
    if (selectedCategoryId) {
      categoryToDisplay = categories.find(c => c.id.toString() === selectedCategoryId);
    }
    if (!categoryToDisplay) {
      categoryToDisplay = categories.reduce((prev, current) =>
        (prev.displayOrder < current.displayOrder) ? prev : current
      );
    }

    await displayCategoryInfo(categoryToDisplay.id);

    const encodedThema = encodeURIComponent(thema);
    history.pushState(null, '', `/categories/${encodedThema}/${categoryToDisplay.id}`);
  } catch (error) {
    console.error('테마 카테고리를 가져오는 데 실패했습니다:', error);
  }
}

function displayCategoryButtons(categories) {
  const categoryButtons = document.getElementById('category-buttons');
  categoryButtons.innerHTML = '';

  categories.sort((a, b) => a.displayOrder - b.displayOrder).forEach(category => {
    const button = document.createElement('button');
    button.textContent = category.categoryName;
    button.classList.add('button', 'is-primary', 'mr-2', 'mb-2');
    button.addEventListener('click', () => {
      displayCategoryInfo(category.id);
      const thema = window.location.pathname.split('/')[2];
      const encodedThema = encodeURIComponent(decodeURIComponent(thema));
      history.pushState(null, '', `/categories/${encodedThema}/${category.id}`);
    });
    categoryButtons.appendChild(button);
  });
}

async function displayCategoryInfo(categoryId) {
  try {
    const category = await Api.get(`/api/categories/${categoryId}`);
    document.getElementById('category-title').textContent = category.categoryName;
    document.getElementById('category-description').textContent = category.categoryContent;

    // 상품 개수 표시 (이 부분은 실제 API 응답에 따라 조정 필요)
    const productCount = category.productCount || 0;
    document.getElementById('product-count').textContent = `${productCount}개의 상품이 있습니다.`;

    // 현재 선택된 카테고리 버튼에 active 클래스 추가
    const buttons = document.querySelectorAll('#category-buttons button');
    buttons.forEach(button => {
      if (button.textContent === category.categoryName) {
        button.classList.add('active');
      } else {
        button.classList.remove('active');
      }
    });

    // 필터 옵션 업데이트
    updateFilterOptions(category.filters || []);

    // 상품 목록 표시
    await displayProducts(categoryId);
  } catch (error) {
    console.error('카테고리 정보를 가져오는 데 실패했습니다:', error);
  }
}

function updateFilterOptions(filters) {
  const filterSelect = document.getElementById('filter-options');
  filterSelect.innerHTML = '<option value="">필터</option>';
  filters.forEach(filter => {
    const option = document.createElement('option');
    option.value = filter.value;
    option.textContent = filter.name;
    filterSelect.appendChild(option);
  });
}

async function displayProducts(categoryId, sortOption = 'default', filterOption = '') {
  try {
    const products = await Api.get(`/api/products?categoryId=${categoryId}&sort=${sortOption}&filter=${filterOption}`);
    const productList = document.getElementById('product-list');
    productList.innerHTML = '';

    products.forEach(product => {
      const productElement = createProductElement(product);
      productList.appendChild(productElement);
    });
  } catch (error) {
    console.error('상품 목록을 가져오는 데 실패했습니다:', error);
  }
}

function createProductElement(product) {
  // 상품 요소 생성 로직

}

function handleSortChange(event) {
  const sortValue = event.target.value;
  const categoryId = window.location.pathname.split('/')[3];
  const filterValue = document.getElementById('filter-options').value;
  displayProducts(categoryId, sortValue, filterValue);
}

function handleFilterChange(event) {
  const filterValue = event.target.value;
  const categoryId = window.location.pathname.split('/')[3];
  const sortValue = document.getElementById('sort-options').value;
  displayProducts(categoryId, sortValue, filterValue);
}

// 페이지 로드 시 초기화 함수 실행
document.addEventListener('DOMContentLoaded', init);