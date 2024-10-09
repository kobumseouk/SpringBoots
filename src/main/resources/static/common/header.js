import * as Api from "../../api.js";

// 헤더 로드 함수
async function loadHeader() {
  try {
    const response = await fetch('/common/header.html');
    const headerHtml = await response.text();
    document.getElementById('header-placeholder').innerHTML = headerHtml;

    // 사용자 메뉴 업데이트
    updateUserMenu();

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

    // 카테고리 메뉴 active 상태 관리
    const menuItems = document.querySelectorAll('.secondary-navbar .navbar-item');
    menuItems.forEach(item => {
      item.addEventListener('click', (e) => {
        menuItems.forEach(i => i.classList.remove('is-active'));
        e.target.classList.add('is-active');
      });
    });

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
        <a class="navbar-item" href="/cart">장바구니</a>
        <a class="navbar-item" href="/logout">로그아웃</a>
      `;
    } else {
      // 사용자가 로그인하지 않은 경우
      menuHTML += `
        <a class="navbar-item" href="/login">로그인</a>
        <a class="navbar-item" href="/register">회원가입</a>
      `;
    }

    userMenu.innerHTML = menuHTML;
  } catch (error) {
    console.error('사용자 정보를 가져오는 데 실패했습니다:', error);
    // 에러 발생 시 로그인되지 않은 상태로 처리
    userMenu.innerHTML = `
      <a class="navbar-item" href="/login">로그인</a>
      <a class="navbar-item" href="/register">회원가입</a>
    `;
  }
}

export { loadHeader };