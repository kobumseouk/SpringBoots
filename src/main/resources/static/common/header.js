import * as Api from "../../api.js";

async function loadHeader() {
  try {
    const response = await fetch('/common/header.html');
    const headerHtml = await response.text();
    document.getElementById('header-placeholder').innerHTML = headerHtml;

    // 사용자 메뉴 업데이트
    updateUserMenu();

    // 검색 기능 설정
    setupSearchFunction();

    // Bulma navbar toggle script
    const $navbarBurgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'), 0);
    if ($navbarBurgers.length > 0) {
      $navbarBurgers.forEach(el => {
        el.addEventListener('click', () => {
          const target = el.dataset.target;
          const $target = document.getElementById(target);
          el.classList.toggle('is-active');
          $target.classList.toggle('is-active');
        });
      });
    }

    // 네비게이션 메뉴 활성화 처리
    setActiveNavItem();

  } catch (error) {
    console.error('헤더를 로드하는 중 오류가 발생했습니다:', error);
  }
}

async function updateUserMenu() {
  const userMenu = document.getElementById('user-menu');

  try {
    // API를 사용하여 사용자 정보 가져오기
    const userInfo = await Api.get('/api/users-info');

    let menuHTML = '';

    // 사용자가 로그인한 경우
    if (userInfo) {
      // 사용자 역할 확인
      if (userInfo.role === 'ADMIN') {
        menuHTML += '<a class="navbar-item" href="/admin">관리자 페이지</a>';
      }
      menuHTML += `
        <a class="navbar-item" href="/mypage">마이 페이지</a>
        <a class="navbar-item" href="/cart/cart.html">장바구니</a>
        <a class="navbar-item" href="#" id="logout">로그아웃</a>
      `;
    } else {
      // 사용자가 로그인하지 않은 경우
      menuHTML += `
        <a class="navbar-item" href="/login">로그인</a>
        <a class="navbar-item" href="/register">회원가입</a>
      `;
    }
    userMenu.innerHTML = menuHTML;

    // 로그아웃 버튼 클릭 이벤트 처리
    document.getElementById('logout')?.addEventListener('click', async (event) => {
      event.preventDefault();
      try {
        const response = await fetch('/api/logout', {
          method: 'POST',
          credentials: 'include'
        });

        if (response.ok) {
          userMenu.innerHTML = `
            <a class="navbar-item" href="/login">로그인</a>
            <a class="navbar-item" href="/register">회원가입</a>
          `;
          window.location.href = '/';
        } else {
          console.error('로그아웃 실패:', response.statusText);
        }
      } catch (error) {
        console.error('로그아웃 중 에러 발생:', error);
      }
    });

  } catch (error) {
    console.error('사용자 정보를 가져오는 데 실패했습니다:', error);
    userMenu.innerHTML = `
      <a class="navbar-item" href="/login">로그인</a>
      <a class="navbar-item" href="/register">회원가입</a>
    `;
  }
}

// 검색 기능 설정
function setupSearchFunction() {
  const searchForm = document.querySelector('.search-container .field');
  const searchInput = document.querySelector('.search-input');
  const searchButton = document.querySelector('.search-button');

  function performSearch(event) {
    event.preventDefault();
    const keyword = searchInput.value.trim();
    if (keyword) {
      window.location.href = `/items/search?keyword=${encodeURIComponent(keyword)}`;
    }
  }

  // 폼 제출 이벤트 리스너
  searchForm.addEventListener('submit', performSearch);
  // 검색 버튼 클릭 이벤트 리스너
  searchButton.addEventListener('click', performSearch);

  // 입력 필드에서 엔터키 입력 처리
  searchInput.addEventListener('keypress', function(event) {
    if (event.key === 'Enter') {
      performSearch(event);
    }
  });
}

// 카테고리 메뉴 활성화 설정
function setActiveNavItem() {
  const currentPath = window.location.pathname;
  const navItems = document.querySelectorAll('.secondary-navbar .navbar-item');

  navItems.forEach(item => {
    const itemPath = item.getAttribute('href');
    if (currentPath === itemPath || (itemPath !== '/' && currentPath.startsWith(itemPath))) {
      item.classList.add('is-active');
    } else {
      item.classList.remove('is-active');
    }
  });

  // 현재 활성 메뉴 저장
  localStorage.setItem('activeNavItem', currentPath);
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', () => {
  loadHeader().then(() => {
    const activeNavItem = localStorage.getItem('activeNavItem');
    if (activeNavItem) {
      const navItem = document.querySelector(`.secondary-navbar .navbar-item[href="${activeNavItem}"]`);
      if (navItem) {
        navItem.classList.add('is-active');
      }
    }
  });
});

// 네비게이션 메뉴 클릭 이벤트 처리
document.addEventListener('click', (event) => {
  if (event.target.matches('.secondary-navbar .navbar-item')) {
    const navItems = document.querySelectorAll('.secondary-navbar .navbar-item');
    navItems.forEach(item => item.classList.remove('is-active'));
    event.target.classList.add('is-active');
    localStorage.setItem('activeNavItem', event.target.getAttribute('href'));
  }
});

// 히스토리 변경 시 실행 (SPA에서 페이지 전환 시)
window.addEventListener('popstate', setActiveNavItem);

export { loadHeader };