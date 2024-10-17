import * as Api from '/api.js';

let currentKeyword = '';
let currentSort = 'default';
let currentPage = 0;

// 초기 검색 시 설정
async function init() {
    const urlParams = new URLSearchParams(window.location.search);
    currentKeyword = urlParams.get('keyword');
    if (currentKeyword) {
        displaySearchInfo(currentKeyword);
        await fetchSearchResults(currentKeyword, currentSort, currentPage);
        setupSortingOptions();
    } else {
        console.error('검색어가 없습니다.');
        document.getElementById('search-title').textContent = '검색어를 입력해주세요.';
    }
}

// 검색 제목
function displaySearchInfo(keyword) {
    const searchTitle = document.getElementById('search-title');
    searchTitle.textContent = `'${keyword}'에 대한 검색 결과`;
}

// 정렬된 검색 결과 - 페이지네이션
async function fetchSearchResults(keyword, sort = 'default', page = 0) {
    try {
        const endpoint = `/api/items/search?keyword=${encodeURIComponent(keyword)}&sort=${sort}&page=${page}&size=8`;
        const searchResults = await Api.get(endpoint);

        const productCount = searchResults.totalElements;
        document.getElementById('product-count').textContent = `${productCount}개의 상품이 검색되었습니다.`;

        if (productCount === 0) {
            document.getElementById('product-list').innerHTML = '<p>검색 결과가 없습니다.</p>';
            document.getElementById('pagination').innerHTML = '';
        } else {
            displayProducts(searchResults.content);
            displayPagination(searchResults);
        }
    } catch (error) {
        console.error('검색 결과를 가져오는 데 실패했습니다:', error);
        document.getElementById('product-count').textContent = '검색 결과를 불러올 수 없습니다.';
        if (error.response) {
            console.error('서버 응답:', error.response.status, error.response.data);
        } else if (error.request) {
            console.error('네트워크 오류: 서버로부터 응답이 없습니다.');
        } else {
            console.error('오류:', error.message);
        }
    }
}

/*function displayPagination(pageData) {
    const paginationElement = document.getElementById('pagination');
    paginationElement.innerHTML = '';

    for (let i = 0; i < pageData.totalPages; i++) {
        const pageLink = document.createElement('a');
        pageLink.href = '#';
        pageLink.textContent = i + 1;
        pageLink.onclick = (e) => {
            e.preventDefault();
            currentPage = i;
            fetchSearchResults(currentKeyword, currentSort, currentPage);
        };
        if (i === pageData.number) {
            pageLink.classList.add('active');
        }
        paginationElement.appendChild(pageLink);
    }
}*/

function displayPagination(pageData) {
    const paginationList = document.querySelector('.pagination-list');
    paginationList.innerHTML = '';

    // 이전 페이지 버튼
    const prevButton = document.createElement('li');
    prevButton.innerHTML = `<a class="pagination-previous" ${pageData.first ? 'disabled' : ''}> < </a>`;
    if (!pageData.first) {
        prevButton.onclick = () => fetchSearchResults(currentKeyword, currentSort, pageData.number - 1);
    }
    paginationList.appendChild(prevButton);

    // 페이지 번호
    for (let i = 0; i < pageData.totalPages; i++) {
        const pageItem = document.createElement('li');
        const pageLink = document.createElement('a');
        pageLink.classList.add('pagination-link');
        pageLink.textContent = i + 1;
        if (i === pageData.number) {
            pageLink.classList.add('is-current');
            pageLink.setAttribute('aria-current', 'page');
        } else {
            pageLink.onclick = () => fetchSearchResults(currentKeyword, currentSort, i);
        }
        pageItem.appendChild(pageLink);
        paginationList.appendChild(pageItem);
    }

    // 다음 페이지 버튼
    const nextButton = document.createElement('li');
    nextButton.innerHTML = `<a class="pagination-next" ${pageData.last ? 'disabled' : ''}> > </a>`;
    if (!pageData.last) {
        nextButton.onclick = () => fetchSearchResults(currentKeyword, currentSort, pageData.number + 1);
    }
    paginationList.appendChild(nextButton);
}

function displayProducts(products) {
    const productList = document.getElementById('product-list');
    productList.innerHTML = '';

    const productContainer = document.createElement('div');
    productContainer.className = 'product-container';

    products.forEach(product => {
        const productElement = createProductElement(product);
        productContainer.appendChild(productElement);
    });

    productList.appendChild(productContainer);
}

// TODO: 임시 상품 요소 작성
function createProductElement(product) {
    const productDiv = document.createElement('div');
    productDiv.className = 'product-item';
    productDiv.innerHTML = `
        <div class="product-image-container">
            <img src="${product.imageUrl}" alt="${product.itemName}" class="product-image">
        </div>
        <div class="product-info">
            <h3 class="product-name">${product.itemName}</h3>
            <p class="product-price">₩${product.itemPrice.toLocaleString()}</p>
        </div>
    `;
    return productDiv;
}

function setupSortingOptions() {
    const sortSelect = document.getElementById('sort-options');
    sortSelect.addEventListener('change', handleSortChange);
}

function handleSortChange(event) {
    currentSort = event.target.value;
    currentPage = 0;   // 정렬 변경 시마다 첫 페이지로 리셋
    fetchSearchResults(currentKeyword, currentSort, currentPage);
}

document.addEventListener('DOMContentLoaded', init);

