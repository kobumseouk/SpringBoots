import { loadHeader } from '/common/header.js';
import * as Api from '/api.js';

const ITEMS_PER_PAGE = 8;

let currentCategoryId = '';
let currentSort = 'default';
let currentPage = 0;

async function init() {
  await loadHeader();

  const path = window.location.pathname;
  const pathParts = path.split('/');

  if (pathParts.length >= 3 && pathParts[1] === 'categories') {
    const englishTheme = pathParts[2];
    const categoryId = pathParts[3] || null;  // categoryId가 없으면 null
    await handleThemeClick(englishTheme, categoryId);
  } else {
    window.location.href = '/';
  }

  setupSortingOptions();
}

async function handleThemeClick(theme, selectedCategoryId = null) {
  try {
    const categories = await Api.get(`/api/categories/themas/${theme}`);
    const allViewCategory = createAllViewCategory(categories);
    const allCategories = [allViewCategory, ...categories];

    displayCategoryButtons(allCategories, theme);

    let categoryToDisplay;
    if (selectedCategoryId) {
      categoryToDisplay = allCategories.find(c => c.id.toString() === selectedCategoryId);
    }
    if (!categoryToDisplay) {
      categoryToDisplay = allViewCategory;
    }

    currentCategoryId = categoryToDisplay.id;
    await displayCategoryInfo(categoryToDisplay);
    await fetchCategoryItems(categoryToDisplay.id, currentSort, currentPage);

    const koreanTheme = translateEnglishToKorean(theme);
    updateBreadcrumb(koreanTheme, categoryToDisplay);

    history.pushState(null, '', `/categories/${theme}/${categoryToDisplay.id}`);
  } catch (error) {
    console.error('테마 카테고리를 가져오는 데 실패했습니다:', error);
  }
}

function createAllViewCategory(categories) {
  const themeContent = categories.length > 0 ? categories[0].categoryContent : '';
  return {
    id: 'all',
    categoryName: '전체보기',
    categoryThema: categories.length > 0 ? categories[0].categoryThema : '',
    displayOrder: 0
  };
}

function displayCategoryButtons(categories, currentEnglishTheme) {
  const categoryButtons = document.getElementById('category-buttons');
  categoryButtons.innerHTML = '';

  // 전체보기 카테고리를 찾아 먼저 추가
  const allViewCategory = categories.find(category => category.categoryName === '전체보기');
  if (allViewCategory) {
    addCategoryButton(allViewCategory, currentEnglishTheme, categoryButtons);
  }

  // 나머지 카테고리들을 정렬하여 추가
  categories
    .filter(category => category.categoryName !== '전체보기')
    .sort((a, b) => a.displayOrder - b.displayOrder)
    .forEach(category => {
      addCategoryButton(category, currentEnglishTheme, categoryButtons);
    });
}

function addCategoryButton(category, currentEnglishTheme, container) {
  const button = document.createElement('button');
  button.textContent = category.categoryName;
  button.classList.add('button', 'is-primary', 'mr-2', 'mb-2');
  if (category.categoryName === '전체보기') {
    button.classList.add('is-outlined');
  }
  button.addEventListener('click', () => {
    currentCategoryId = category.id;
    currentPage = 0;
    displayCategoryInfo(category);
    fetchCategoryItems(category.id, currentSort, currentPage);
    history.pushState(null, '', `/categories/${currentEnglishTheme}/${category.id}`);
    updateActiveButton(category.categoryName);
  });
  container.appendChild(button);
}

function displayCategoryInfo(category) {
  document.getElementById('category-title').textContent = category.categoryName;
  document.getElementById('category-description').textContent = category.categoryContent;

  updateActiveButton(category.categoryName);

  const pathParts = window.location.pathname.split('/');
  const englishTheme = pathParts[2];
  const theme = translateEnglishToKorean(englishTheme);
  updateBreadcrumb(theme, category);
}

async function fetchCategoryItems(categoryId, sort, page) {
  try {
    let endpoint;
    if (categoryId === 'all') {
      const thema = window.location.pathname.split('/')[2];
      endpoint = `/api/items/thema/${thema}?sort=${sort}&page=${page}&limit=${ITEMS_PER_PAGE}`;
    } else {
      endpoint = `/api/items/categories/${categoryId}?sort=${sort}&page=${page}&limit=${ITEMS_PER_PAGE}`;
    }
    console.log('Fetching from endpoint:', endpoint); // 요청 URL 로깅
    const response = await Api.get(endpoint);
    console.log('Server response:', response); // 서버 응답 로깅
    displayItems(response.content);
    displayPagination(response);
    document.getElementById('product-count').textContent = `${response.totalElements}개의 상품이 있습니다.`;
  } catch (error) {
    console.error('상품을 가져오는 데 실패했습니다:', error);
    if (error.response) {
      console.error('서버 응답:', error.response.status, error.response.data);
    } else if (error.request) {
      console.error('응답을 받지 못했습니다. 네트워크 문제일 수 있습니다.');
    } else {
      console.error('요청 설정 중 오류 발생:', error.message);
    }
    document.getElementById('product-count').textContent = '상품을 불러올 수 없습니다.';
  }
}

function displayItems(items) {
  const itemList = document.getElementById('product-list');
  itemList.innerHTML = '';

  const itemContainer = document.createElement('div');
  itemContainer.className = 'product-container';

  items.forEach(item => {
    const itemElement = createItemElement(item);
    itemContainer.appendChild(itemElement);
  });

  itemList.appendChild(itemContainer);
}

function createItemElement(item) {
  const itemDiv = document.createElement('div');
  itemDiv.className = 'product-item';
  itemDiv.innerHTML = `
    <div class="product-image-container">
      <img src="${item.imageUrl}" alt="${item.itemName}" class="product-image">
    </div>
    <div class="product-info">
      <h3 class="product-name">${item.itemName}</h3>
      <p class="product-price">₩${item.itemPrice.toLocaleString()}</p>
    </div>
  `;

  // 상품 전체를 클릭 가능하게 만들기
  itemDiv.style.cursor = 'pointer';
  itemDiv.addEventListener('click', () => {
    window.location.href = `/items?item_id=${item.id}`;
  });

  return itemDiv;
}

function displayPagination(pageData) {
  const paginationList = document.querySelector('.pagination-list');
  paginationList.innerHTML = '';

  for (let i = 0; i < pageData.totalPages; i++) {
    const pageItem = document.createElement('li');
    const pageLink = document.createElement('a');
    pageLink.classList.add('pagination-link');
    pageLink.textContent = i + 1;
    if (i === currentPage) {
      pageLink.classList.add('is-current');
      pageLink.setAttribute('aria-current', 'page');
    } else {
      pageLink.onclick = () => {
        currentPage = i;
        fetchCategoryItems(currentCategoryId, currentSort, currentPage);
      };
    }
    pageItem.appendChild(pageLink);
    paginationList.appendChild(pageItem);
  }
}

function setupSortingOptions() {
  const sortSelect = document.getElementById('sort-options');
  sortSelect.addEventListener('change', handleSortChange);
}

function handleSortChange(event) {
  currentSort = event.target.value;
  currentPage = 0;
  fetchCategoryItems(currentCategoryId, currentSort, currentPage);
}

function updateActiveButton(categoryName) {
  const buttons = document.querySelectorAll('#category-buttons button');
  buttons.forEach(button => {
    if (button.textContent === categoryName) {
      button.classList.add('active');
      button.classList.remove('is-outlined');
    } else {
      button.classList.remove('active');
      if (button.textContent === '전체보기') {
        button.classList.add('is-outlined');
      }
    }
  });
}

function updateBreadcrumb(theme, category) {
  const secondBreadcrumb = document.getElementById('second-breadcrumb');
  const thirdBreadcrumb = document.getElementById('third-breadcrumb');

  secondBreadcrumb.querySelector('a').textContent = theme;
  secondBreadcrumb.querySelector('a').href = `/categories/${translateKoreanToEnglish(theme)}/1`;

  if (category?.categoryName !== '전체보기') {
    thirdBreadcrumb.querySelector('a').textContent = category.categoryName;
    thirdBreadcrumb.querySelector('a').href = `/categories/${translateKoreanToEnglish(theme)}/${category.id}`;
    thirdBreadcrumb.classList.add('is-active');
    thirdBreadcrumb.style.display = '';
  } else {
    thirdBreadcrumb.style.display = 'none';
  }
}

function translateEnglishToKorean(englishTheme) {
  const themeMap = {
    'common': '공용',
    'women': '여성',
    'men': '남성',
    'accessories': '액세서리'
  };
  return themeMap[englishTheme];
}

function translateKoreanToEnglish(koreanTheme) {
  const themeMap = {
    '공용': 'common',
    '여성': 'women',
    '남성': 'men',
    '액세서리': 'accessories'
  };
  return themeMap[koreanTheme];
}

document.addEventListener('DOMContentLoaded', init);