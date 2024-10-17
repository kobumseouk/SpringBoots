import * as Api from '/api.js';

async function init() {
    const urlParams = new URLSearchParams(window.location.search);
    const keyword = urlParams.get('keyword');
    if (keyword) {
        displaySearchInfo(keyword);
        await fetchSearchResults(keyword);
        setupSortingOptions();
    } else {
        console.error('검색어가 없습니다.');
    }
}

function displaySearchInfo(keyword) {
    const searchTitle = document.getElementById('search-title');
    searchTitle.textContent = `'${keyword}'에 대한 검색 결과`;
}

async function fetchSearchResults(keyword) {
    try {
        const endpoint = `/api/items/search?keyword=${encodeURIComponent(keyword)}`;
        const searchResults = await Api.get(endpoint);

        const productCount = searchResults.length;
        document.getElementById('product-count').textContent = `${productCount}개의 상품이 검색되었습니다.`;

        displayProducts(searchResults);
    } catch (error) {
        console.error('검색 결과를 가져오는 데 실패했습니다:', error);
        document.getElementById('product-count').textContent = '검색 결과를 불러올 수 없습니다.';
    }
}

function displayProducts(products) {
    const productList = document.getElementById('product-list');
    productList.innerHTML = '';

    products.forEach(product => {
        const productElement = createProductElement(product);
        productList.appendChild(productElement);
    });
}

function createProductElement(product) {
    // TODO: 상품 요소 생성 로직

}

function setupSortingOptions() {
    const sortSelect = document.getElementById('sort-options');
    sortSelect.addEventListener('change', handleSortChange);
}

async function handleSortChange(event) {
    const sortValue = event.target.value;
    const keyword = new URLSearchParams(window.location.search).get('keyword');

    try {
        const endpoint = `/api/items/search?keyword=${encodeURIComponent(keyword)}&sort=${sortValue}`;
        const sortedResults = await Api.get(endpoint);
        displayProducts(sortedResults);
    } catch (error) {
        console.error('정렬된 검색 결과를 가져오는 데 실패했습니다:', error);
    }
}

document.addEventListener('DOMContentLoaded', init);