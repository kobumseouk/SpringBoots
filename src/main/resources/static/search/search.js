import * as Api from '/api.js';

const ITEMS_PER_PAGE = 8;

let currentKeyword = '';
let currentSort = 'default';
let currentPage = 1;

// 초기 검색 시 설정
async function init() {
    const urlParams = new URLSearchParams(window.location.search);
    currentKeyword = urlParams.get('keyword');
    if (currentKeyword) {
        displaySearchInfo(currentKeyword);
        await fetchSearchResults(currentKeyword, currentSort, currentPage); // 첫 페이지는 1
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
async function fetchSearchResults(keyword, sort = 'default', page = 1) {
    try {
        currentPage = page;  // 현재 페이지 업데이트
        const endpoint = `/api/items/search?keyword=${encodeURIComponent(keyword)}&sort=${sort}&page=${page - 1}&limit=${ITEMS_PER_PAGE}`; // page - 1로 수정
        const searchResults = await Api.get(endpoint);

        const itemCount = searchResults.totalElements;
        document.getElementById('product-count').textContent = `${itemCount}개의 상품이 검색되었습니다.`;

        if (itemCount === 0) {
            document.getElementById('product-list').innerHTML = '<p>검색 결과가 없습니다.</p>';
            document.getElementById('pagination').innerHTML = '';
        } else {
            displayItems(searchResults.content);
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

function displayPagination(pageData) {
    const paginationList = document.querySelector('.pagination-list');
    paginationList.innerHTML = '';

    for (let i = 1; i <= pageData.totalPages; i++) {
        const pageItem = document.createElement('li');
        const pageLink = document.createElement('a');
        pageLink.classList.add('pagination-link');
        pageLink.textContent = i;
        if (i === currentPage) {
            pageLink.classList.add('is-current');
            pageLink.setAttribute('aria-current', 'page');
        } else {
            pageLink.onclick = () => fetchSearchResults(currentKeyword, currentSort, i);
        }
        pageItem.appendChild(pageLink);
        paginationList.appendChild(pageItem);
    }
}

function displayItems(items) {
    const productList = document.getElementById('product-list');
    productList.innerHTML = '';

    const itemContainer = document.createElement('div');
    itemContainer.className = 'product-container';

    items.forEach(item => {
        const itemElement = createItemElement(item);
        itemContainer.appendChild(itemElement);
    });

    productList.appendChild(itemContainer);
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
    window.location.href = `/items?itemId=${item.id}`;
  });

  return itemDiv;
}

// 정렬 변경 설정
function setupSortingOptions() {
    const sortSelect = document.getElementById('sort-options');
    sortSelect.addEventListener('change', handleSortChange);
}

// 정렬 변경 처리 함수
function handleSortChange(event) {
    currentSort = event.target.value;
    currentPage = 1;   // 정렬 변경 시 첫 페이지로 리셋
    fetchSearchResults(currentKeyword, currentSort, currentPage);
}

document.addEventListener('DOMContentLoaded', init);