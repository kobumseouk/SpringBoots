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
     } catch (error) {
         console.error('헤더를 로드하는 중 오류가 발생했습니다:', error);
     }
}

function updateUserMenu() {
  const userMenu = document.getElementById('user-menu');

  // TODO: 서버에서 사용자 정보를 가져오기
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
  const isAdmin = localStorage.getItem('isAdmin') === 'true';

  let menuHTML = '';

  if (isLoggedIn) {
    if (isAdmin) {
      menuHTML += '<a class="navbar-item" href="/admin">관리자 페이지</a>';
    }
    menuHTML += `
      <a class="navbar-item" href="/mypage">마이 페이지</a>
      <a class="navbar-item" href="/cart">장바구니</a>
      <a class="navbar-item" href="/logout">로그아웃</a>
    `;
  } else {
    menuHTML += `
      <a class="navbar-item" href="/login">로그인</a>
      <a class="navbar-item" href="/register">회원가입</a>
    `;
  }

  userMenu.innerHTML = menuHTML;
}

export { loadHeader };