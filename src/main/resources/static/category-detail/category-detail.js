import { loadHeader } from '/common/header.js';
import * as Api from '/api.js';

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

  // 정렬 및 필터 이벤트 리스너 추가
  document.getElementById('sort-options').addEventListener('change', handleSortChange);
  document.getElementById('filter-options').addEventListener('change', handleFilterChange);
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
       , categories[0]); // 초기값으로 첫 번째 카테고리 사용
    }

    await displayCategoryInfo(categoryToDisplay.id);

    // 브레드크럼 업데이트
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

  // 테마 설정
  secondBreadcrumb.querySelector('a').textContent = theme;
  secondBreadcrumb.querySelector('a').href = `/categories/${translateKoreanToEnglish(theme)}/1`;

  // category 객체가 존재하고 배치 순서가 '1'이 아닌 경우에만 third-breadcrumb 표시
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
      displayCategoryInfo(category.id);
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

    // URL에서 테마 정보 추출
    const pathParts = window.location.pathname.split('/');
    const englishTheme = pathParts[2];
    const theme = translateEnglishToKorean(englishTheme);

    // 브레드크럼 업데이트
    updateBreadcrumb(theme, category);

    // 상품 개수 표시
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

    // URL 업데이트
    const currentEnglishTheme = translateKoreanToEnglish(theme);
    history.pushState(null, '', `/categories/${currentEnglishTheme}/${category.id}`);

  } catch (error) {
    console.error('카테고리 정보를 가져오는 데 실패했습니다:', error);
  }
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

function createProductElement(product) {
  // 상품 요소 생성 로직

}

function handleSortChange(event) {
  const sortValue = event.target.value;
  const categoryId = window.location.pathname.split('/')[3];
  const filterValue = document.getElementById('filter-options').value;
  // TODO: 정렬 로직
  console.log('정렬 기준:', sortValue);
}

function handleFilterChange(event) {
  const filterValue = event.target.value;
  const categoryId = window.location.pathname.split('/')[3];
  const sortValue = document.getElementById('sort-options').value;
  // TODO: 필터 로직
  console.log('필터 기준:', sortValue);
}


// 영어 테마를 한글로 변환하는 함수 수정
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

// 한글 테마를 영어로 변환하는 함수 수정
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

// 페이지 로드 시 초기화 함수 실행
document.addEventListener('DOMContentLoaded', init);