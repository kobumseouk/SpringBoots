// home.js
import * as Api from '/api.js';

// TODO: 이미지 경로 따로 지정 필요
const KEY_ITEMS = [
    { name: '부츠', image: 'https://project-springboots.s3.amazonaws.com/20241021175145213187597' },
    { name: '슈즈', image: 'https://project-springboots.s3.amazonaws.com/20241021231840-751640513' },
    { name: '앵클워머', image: 'https://project-springboots.s3.amazonaws.com/20241021175145213187597' },
    { name: '로퍼', image: 'https://project-springboots.s3.amazonaws.com/20241021231840-751640513' },
    { name: '첼시부츠', image: 'https://project-springboots.s3.amazonaws.com/20241021183850-461603425' },
    { name: '롱부츠', image: 'https://project-springboots.s3.amazonaws.com/20241021183850-461603425' }
];

let currentFilter = 'new';
let currentCategory = 'common';
let currentPage = 0;
const ITEMS_PER_PAGE = 5;

document.addEventListener('DOMContentLoaded', async () => {
    loadKeyItems();
    setupFilterListeners();
    await loadProducts();
});

function loadKeyItems() {
    const container = document.getElementById('key-items-container');
    KEY_ITEMS.forEach(item => {
        const column = document.createElement('div');
        column.className = 'column is-2';
        column.innerHTML = `
            <div class="key-item-card" onclick="window.location.href='/items/search?keyword=${encodeURIComponent(item.name)}'">
                <figure class="image is-square key-item-image">
                    <img src="${item.image}" alt="${item.name}">
                </figure>
                <p class="has-text-centered mt-2">${item.name}</p>
            </div>
        `;
        container.appendChild(column);
    });
}

function setupFilterListeners() {
    document.querySelectorAll('#product-filter button').forEach(button => {
        button.addEventListener('click', (e) => {
            const filterType = e.target.dataset.filter || e.target.dataset.category;
            if (e.target.dataset.filter) {
                currentFilter = filterType;
                document.querySelectorAll('[data-filter]').forEach(btn => btn.classList.remove('is-selected'));
            } else if (e.target.dataset.category) {
                currentCategory = filterType;
                document.querySelectorAll('[data-category]').forEach(btn => btn.classList.remove('is-selected'));
            }
            e.target.classList.add('is-selected');
            currentPage = 0;
            loadProducts();
        });
    });

    document.getElementById('prev-button').addEventListener('click', () => {
        if (currentPage > 0) {
            currentPage--;
            loadProducts();
        }
    });

    document.getElementById('next-button').addEventListener('click', () => {
        currentPage++;
        loadProducts();
    });
}

async function loadProducts() {
    try {
        let endpoint = `/api/items/thema/${currentCategory}?sort=${currentFilter === 'new' ? 'newest' : 'default'}&page=${currentPage}&limit=${ITEMS_PER_PAGE}`;
        const response = await Api.get(endpoint);
        displayProducts(response.content);
        updatePaginationButtons(response);
    } catch (error) {
        console.error('상품을 불러오는 데 실패했습니다:', error);
    }
}

function displayProducts(products) {
    const container = document.getElementById('products-container');
    container.innerHTML = '';
    products.forEach(product => {
        const column = document.createElement('div');
        column.className = 'column is-one-fifth';
        column.innerHTML = `
            <div class="card product-card">
                <div class="card-image">
                    <figure class="image">
                        <img src="${product.imageUrl}" alt="${product.itemName}">
                    </figure>
                </div>
                <div class="card-content">
                    <p class="title is-5">${product.itemName}</p>
                    <p class="subtitle is-6">₩${product.itemPrice.toLocaleString()}</p>
                </div>
            </div>
        `;
        column.querySelector('.product-card').addEventListener('click', () => {
            window.location.href = `/items?itemId=${product.id}`;
        });
        container.appendChild(column);
    });
}

function updatePaginationButtons(pageData) {
    const prevButton = document.getElementById('prev-button');
    const nextButton = document.getElementById('next-button');
    prevButton.disabled = currentPage === 0;
    nextButton.disabled = currentPage >= pageData.totalPages - 1;
}