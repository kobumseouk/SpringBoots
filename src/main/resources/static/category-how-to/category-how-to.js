import * as Api from "/api.js";

async function loadCategories() {
    try {
        const categories = await Api.get("/api/categories/themas/how-to");
        const container = document.getElementById("categories-container");
        const categoryTitle = document.getElementById("category-title");
        const secondBreadcrumb = document.getElementById("second-breadcrumb");

        // 제목 및 브레드크럼 설정
        categoryTitle.textContent = "HOW TO";
        secondBreadcrumb.querySelector('a').textContent = "HOW TO";
        secondBreadcrumb.querySelector('a').href = "/categories/how-to";
        secondBreadcrumb.classList.add('is-active');

        categories.forEach(category => {
            const column = document.createElement("div");
            column.className = "column is-one-third";
            column.innerHTML = `
                <div class="card">
                    <div class="card-image">
                        <figure class="image is-4by3">
                            <img src="${category.imageUrl || '/images/default-category-image.jpg'}" alt="${category.categoryName}" onerror="this.src='/images/default-category-image.jpg'">
                        </figure>
                    </div>
                    <div class="card-content">
                        <h2 class="title is-4">${category.categoryName}</h2>
                        <p class="subtitle is-6">${category.categoryContent}</p>
                        <a href="/items/search?keyword=${encodeURIComponent(category.categoryName)}" class="button is-primary">상세히 보기</a>
                    </div>
                </div>
            `;
            container.appendChild(column);
        });
    } catch (error) {
        console.error("카테고리를 불러오는 중 오류가 발생했습니다:", error);
        const container = document.getElementById("categories-container");
        container.innerHTML = '<p class="has-text-centered">카테고리를 불러오는 데 실패했습니다. 나중에 다시 시도해 주세요.</p>';
    }
}

document.addEventListener("DOMContentLoaded", loadCategories);