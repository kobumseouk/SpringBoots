import * as Api from '/api.js';

let eventDetail;

async function init() {
  try {
    await loadEventDetail();
    updateBreadcrumb();
    updateEventDetail();
  } catch (error) {
    console.error('이벤트 정보를 불러오는 데 실패했습니다:', error);
  }
}

async function loadEventDetail() {
  const eventId = getEventIdFromUrl();
  eventDetail = await Api.get(`/api/events/${eventId}`);
}

function updateBreadcrumb() {
  const secondBreadcrumb = document.getElementById('second-breadcrumb');
  secondBreadcrumb.querySelector('a').textContent = 'EVENT';
  secondBreadcrumb.querySelector('a').href = '/events';

  if (eventDetail) {
    const thirdBreadcrumb = document.getElementById('third-breadcrumb');
    thirdBreadcrumb.querySelector('a').textContent = eventDetail.eventTitle;
    thirdBreadcrumb.querySelector('a').href = `#${eventDetail.id}`;
    thirdBreadcrumb.classList.add('is-active');
  }
}

function updateEventDetail() {
  if (!eventDetail) return;

  document.getElementById('event-title').textContent = eventDetail.eventTitle;
  document.getElementById('event-start-date').textContent = formatDate(eventDetail.startDate);
  document.getElementById('event-end-date').textContent = formatDate(eventDetail.endDate);

  const contentImageContainer = document.getElementById('event-content-image');
  contentImageContainer.innerHTML = ''; // 기존 내용 초기화
  eventDetail.contentImageUrl.forEach(url => {
    const contentImage = document.createElement('img');
    contentImage.src = url;
    contentImage.alt = eventDetail.eventTitle;
    contentImageContainer.appendChild(contentImage);
  });

  document.getElementById('event-description').textContent = eventDetail.eventContent;
}

function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });
}

function getEventIdFromUrl() {
  const pathParts = window.location.pathname.split('/');
  return pathParts[pathParts.length - 1];
}


document.addEventListener('DOMContentLoaded', init);