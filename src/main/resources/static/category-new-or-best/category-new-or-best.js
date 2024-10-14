import * as Api from '/api.js';

async function init() {
    const path = window.location.pathname;
    const category = path.split('/').pop(); // 'new-in' 또는 'best'

    updateBreadcrumb(category);
    displayCategoryInfo(category);
    await fetchProductCount(category);
    setupSortingOptions();
}

const SMALL_NEW_IN = 'new-in';
const SMALL_BEST = 'best';
const BIG_NEW_IN = 'NEW-IN';
const BIG_BEST = 'BEST';


function updateBreadcrumb(category) {
    const secondBreadcrumb = document.getElementById('second-breadcrumb');
    let categoryName = category === SMALL_NEW_IN ? 'BIG_NEW_IN' :
                       category === SMALL_BEST ? 'BIG_BEST' : 'Categories';

    secondBreadcrumb.querySelector('a').textContent = categoryName;
    secondBreadcrumb.querySelector('a').href = `/categories/${category}`;
    secondBreadcrumb.classList.add('is-active');
}

function displayCategoryInfo(category) {
    let title, description;

    if (category === SMALL_NEW_IN) {
        title = 'BIG_NEW_IN';
        description = '새롭게 업데이트되는 뛰어난 최신 디자인과 내구성의 신상품을 만나보세요.';
    } else if (category === SMALL_BEST) {
        title = 'BIG_BEST';
        description = '시대와 상관없이 클래식한 디자인부터 엣지있는 스타일, 과감한 스타일까지 베스트셀러를 만나보세요.';
    } else {
        title = '카테고리';
        description = '카테고리 설명';
    }

    document.getElementById('category-title').textContent = title;
    document.getElementById('category-description').textContent = description;
}

async function fetchProductCount(category) {
    try {
        const endpoint = `/api/products/count/${category}`;
        const countInfo = await Api.get(endpoint);

        const productCount = countInfo.count || 0;
        document.getElementById('product-count').textContent = `${productCount}개의 ${category === 'SMALL_NEW_IN' ? 'BIG_NEW_IN' : 'BIG_NEW_IN'} 상품이 있습니다.`;
    } catch (error) {
        console.error('상품 개수를 가져오는 데 실패했습니다:', error);
        document.getElementById('product-count').textContent = '상품 개수를 불러올 수 없습니다.';
    }
}

function setupSortingOptions() {
    const sortSelect = document.getElementById('sort-options');
    sortSelect.addEventListener('change', handleSortChange);
}

function handleSortChange(event) {
    const sortValue = event.target.value;
    // TODO: 정렬 로직
    console.log('정렬 기준:', sortValue);
}

document.addEventListener('DOMContentLoaded', init);