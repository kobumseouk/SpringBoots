// 헤더 로드 함수
async function loadHeader() {
  try {
    const response = await fetch('/header.html');
    const headerHtml = await response.text();
    document.getElementById('header-placeholder').innerHTML = headerHtml;
  } catch (error) {
    console.error('헤더를 로드하는 중 오류가 발생했습니다:', error);
  }
}

// 페이지 로드 시 헤더 삽입
window.addEventListener('DOMContentLoaded', loadHeader);

export { loadHeader };
