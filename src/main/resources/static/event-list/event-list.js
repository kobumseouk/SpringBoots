import * as Api from '/api.js';


// 한 페이지당 보여줄 이벤트 수
const EVENTS_PER_PAGE = 10;

let currentPage = 1;
let totalPages = 1;

async function init() {
  updateBreadcrumb();
  await fetchEvents(currentPage);
}

function updateBreadcrumb() {
  const secondBreadcrumb = document.getElementById('second-breadcrumb');
  secondBreadcrumb.querySelector('a').textContent = 'EVENT';
  secondBreadcrumb.querySelector('a').href = '/event-list';
  secondBreadcrumb.classList.add('is-active');
}

async function fetchEvents(page) {
  try {
    const response = await Api.get(`/api/events?page=${page - 1}&limit=${EVENTS_PER_PAGE}`);
    const events = response.content.filter(event => {   // 만료된 이벤트 확인
      const endDate = new Date(event.endDate);
      return endDate >= new Date();
    });
    totalPages = response.totalPages;
    displayEvents(events);
    setupPagination();
  } catch (error) {
    console.error('이벤트를 가져오는 데 실패했습니다:', error);
  }
}

function displayEvents(events) {
  const eventListContainer = document.getElementById('event-list');
  eventListContainer.innerHTML = '';

  events.forEach(event => {
    const eventElement = createEventElement(event);
    eventListContainer.appendChild(eventElement);
  });
}

// event-list.js의 createEventElement 함수 수정
function createEventElement(event) {
  const column = document.createElement('div');
  column.className = 'column is-one-fifth'; // 5개의 열로 표시

  const card = document.createElement('div');
  card.className = 'card';

  // 카드 전체를 클릭 가능하게 만들기
  card.style.cursor = 'pointer';
  card.addEventListener('click', () => {
    window.location.href = `/events/${event.id}`;
  });

  const cardImage = document.createElement('div');
  cardImage.className = 'card-image';

  const figure = document.createElement('figure');
  figure.className = 'image';

  const img = document.createElement('img');
  img.src = event.thumbnailImageUrl || '/images/default-event-image.jpg';
  img.alt = event.eventTitle;
  img.onerror = function() {
    this.src = '/images/default-event-image.jpg';
  };

  figure.appendChild(img);
  cardImage.appendChild(figure);

  const cardContent = document.createElement('div');
  cardContent.className = 'card-content';

  const title = document.createElement('p');
  title.className = 'title';
  title.textContent = event.eventTitle;

  const date = document.createElement('p');
  date.className = 'subtitle';
  date.textContent = `${formatDate(event.startDate)} ~ ${formatDate(event.endDate)}`;

  cardContent.appendChild(title);
  cardContent.appendChild(date);

  card.appendChild(cardImage);
  card.appendChild(cardContent);

  column.appendChild(card);

  return column;
}


// 날짜 형식을 변환하는 헬퍼 함수
function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });
}

// 페이지 버튼 설정
function setupPagination() {
  const paginationList = document.querySelector('.pagination-list');
  paginationList.innerHTML = '';

  // 이전 페이지 버튼
  const prevButton = document.createElement('li');
  prevButton.innerHTML = `<a class="pagination-previous" ${currentPage === 1 ? 'disabled' : ''}> < </a>`;
  if (currentPage > 1) {
    prevButton.querySelector('a').onclick = () => fetchEvents(currentPage - 1);
  }
  paginationList.appendChild(prevButton);

  // 페이지 번호
  for (let i = 1; i <= totalPages; i++) {
    const pageItem = document.createElement('li');
    const pageLink = document.createElement('a');
    pageLink.classList.add('pagination-link');
    pageLink.textContent = i;
    if (i === currentPage) {
      pageLink.classList.add('is-current');
      pageLink.setAttribute('aria-current', 'page');
    } else {
      pageLink.onclick = () => fetchEvents(i);
    }
    pageItem.appendChild(pageLink);
    paginationList.appendChild(pageItem);
  }

  // 다음 페이지 버튼
  const nextButton = document.createElement('li');
  nextButton.innerHTML = `<a class="pagination-next" ${currentPage === totalPages ? 'disabled' : ''}> > </a>`;
  if (currentPage < totalPages) {
    nextButton.querySelector('a').onclick = () => fetchEvents(currentPage + 1);
  }
  paginationList.appendChild(nextButton);
}

document.addEventListener('DOMContentLoaded', init);