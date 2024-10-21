import * as Api from "/api.js";

async function loadCategories() {
    try {
        const categories = await Api.get("/api/categories/themas/recommend");
        const container = document.getElementById("categories-container");
        const categoryTitle = document.getElementById("category-title");
        const secondBreadcrumb = document.getElementById("second-breadcrumb");

        // 제목 및 브레드크럼 설정
        categoryTitle.textContent = "RECOMMEND";
        secondBreadcrumb.querySelector('a').textContent = "RECOMMEND";
        secondBreadcrumb.querySelector('a').href = "/categories/recommend";
        secondBreadcrumb.classList.add('is-active');

        categories.forEach(category => {
          const card = document.createElement("div");
          card.className = "card";
          card.innerHTML = `
            <div class="card-image">
              <figure class="image is-4by3">
                <img src="${category.imageUrl || '/images/default-category-image.jpg'}" alt="${category.categoryName}" onerror="this.src='/images/default-event-image.jpg'">
              </figure>
            </div>
            <div class="card-content">
              <h2>${category.categoryName}</h2>
              <p>${category.categoryContent}</p>
              <a href="/items/search?keyword=${encodeURIComponent(category.categoryName)}" class="button">상세히 보기</a>
            </div>
          `;
          container.appendChild(card);
        });
    } catch (error) {
        console.error("카테고리를 불러오는 중 오류가 발생했습니다:", error);
        const container = document.getElementById("categories-container");
        container.innerHTML = '<p class="has-text-centered">카테고리를 불러오는 데 실패했습니다. 나중에 다시 시도해 주세요.</p>';
    }
}

document.addEventListener("DOMContentLoaded", loadCategories);