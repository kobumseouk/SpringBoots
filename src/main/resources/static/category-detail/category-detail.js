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

  if (pathParts.length >= 4 && pathParts[1] === 'categories') {
    const englishTheme = pathParts[2];
    const categoryId = pathParts[3];
    await handleThemeClick(englishTheme, categoryId);
  } else {
    window.location.href = '/';
  }

  setupSortingOptions();
}

async function handleThemeClick(theme, selectedCategoryId = null) {
  try {
    const categories = await Api.get(`/api/categories/themas/${theme}`);
    displayCategoryButtons(categories, theme);

    let categoryToDisplay;
    if (selectedCategoryId) {
      categoryToDisplay = categories.find(c => c.id.toString() === selectedCategoryId);
    }
    if (!categoryToDisplay) {
      categoryToDisplay = categories.reduce((prev, current) =>
        (prev.displayOrder < current.displayOrder) ? prev : current
      , categories[0]);
    }

    currentCategoryId = categoryToDisplay.id;
    await displayCategoryInfo(categoryToDisplay.id);
    await fetchCategoryItems(categoryToDisplay.id, currentSort, currentPage);

    const koreanTheme = translateEnglishToKorean(theme);
    updateBreadcrumb(koreanTheme, categoryToDisplay);

    history.pushState(null, '', `/categories/${theme}/${categoryToDisplay.id}`);
  } catch (error) {
    console.error('테마 카테고리를 가져오는 데 실패했습니다:', error);
  }
}

function updateBreadcrumb(theme, category) {
  const secondBreadcrumb = document.getElementById('second-breadcrumb');
  const thirdBreadcrumb = document.getElementById('third-breadcrumb');

  secondBreadcrumb.querySelector('a').textContent = theme;
  secondBreadcrumb.querySelector('a').href = `/categories/${translateKoreanToEnglish(theme)}/1`;

  if (category?.displayOrder !== '1') {
    thirdBreadcrumb.querySelector('a').textContent = category.categoryName;
    thirdBreadcrumb.querySelector('a').href = `/categories/${translateKoreanToEnglish(theme)}/${category.id}`;
    thirdBreadcrumb.classList.add('is-active');
    thirdBreadcrumb.style.display = '';
  } else {
    thirdBreadcrumb.style.display = 'none';
  }
}

function displayCategoryButtons(categories, currentEnglishTheme) {
  const categoryButtons = document.getElementById('category-buttons');
  categoryButtons.innerHTML = '';

  categories.sort((a, b) => a.displayOrder - b.displayOrder).forEach(category => {
    const button = document.createElement('button');
    button.textContent = category.categoryName;
    button.classList.add('button', 'is-primary', 'mr-2', 'mb-2');
    button.addEventListener('click', () => {
      currentCategoryId = category.id;
      currentPage = 0;
      displayCategoryInfo(category.id);
      fetchCategoryItems(category.id, currentSort, currentPage);
      history.pushState(null, '', `/categories/${currentEnglishTheme}/${category.id}`);
    });
    categoryButtons.appendChild(button);
  });
}

async function displayCategoryInfo(categoryId) {
  try {
    const category = await Api.get(`/api/categories/${categoryId}`);
    document.getElementById('category-title').textContent = category.categoryName;
    document.getElementById('category-description').textContent = category.categoryContent;

    updateActiveButton(category.categoryName);

    const pathParts = window.location.pathname.split('/');
    const englishTheme = pathParts[2];
    const theme = translateEnglishToKorean(englishTheme);
    updateBreadcrumb(theme, category);
  } catch (error) {
    console.error('카테고리 정보를 가져오는 데 실패했습니다:', error);
  }
}

async function fetchCategoryItems(categoryId, sort, page) {
  try {
    const response = await Api.get(`/api/items/categories/${categoryId}?sort=${sort}&page=${page}&limit=${ITEMS_PER_PAGE}`);
    displayItems(response.content);
    displayPagination(response);
    document.getElementById('product-count').textContent = `${response.totalElements}개의 상품이 있습니다.`;
  } catch (error) {
    console.error('상품을 가져오는 데 실패했습니다:', error);
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


// 정렬 설정
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
    } else {
      button.classList.remove('active');
    }
  });
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