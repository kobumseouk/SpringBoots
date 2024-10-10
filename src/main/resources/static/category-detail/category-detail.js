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
  } catch (error) {
    console.error('카테고리 정보를 가져오는 데 실패했습니다:', error);
  }
}


// 페이지 로드 시 초기화 함수 실행
document.addEventListener('DOMContentLoaded', init);
