import * as Api from "../../api.js";
import { checkLogin, checkAdmin } from "../../useful-functions.js";
import { loadHeader } from "../../common/header.js";

// 요소(element), input 혹은 상수
const titleInput = document.querySelector("#titleInput");
const contentInput = document.querySelector("#contentInput");
const startDateInput = document.querySelector("#startDateInput");
const endDateInput = document.querySelector("#endDateInput");
const thumbnailInput = document.querySelector("#thumbnailInput");
const contentImageInput = document.querySelector("#contentImageInput");
const thumbnailNameSpan = document.querySelector("#thumbnailNameSpan");
const contentImageNameSpan = document.querySelector("#contentImageNameSpan");
const thumbnailPreview = document.querySelector("#thumbnailPreview");
const contentImagePreview = document.querySelector("#contentImagePreview");
const thumbnailPreviewContainer = document.querySelector("#thumbnailPreviewContainer");
const contentImagePreviewContainer = document.querySelector("#contentImagePreviewContainer");
const submitButton = document.querySelector("#submitButton");
const cancelButton = document.querySelector("#cancelButton");
const eventForm = document.querySelector("#eventForm");
const formTitle = document.querySelector("#formTitle");

const eventId = new URLSearchParams(window.location.search).get('id');
const isEditMode = !!eventId;

// 페이지 로드 시 초기 상태 설정
async function initializePage() {
    console.log("Initializing page...");
    await loadHeader();
    // checkLogin();
    addAllEvents();
    if (isEditMode) {
        await fetchEventData();
        formTitle.textContent = "이벤트 수정하기";
        submitButton.textContent = "수정";
    } else {
        formTitle.textContent = "이벤트 추가하기";
        submitButton.textContent = "작성";
    }
    console.log("Page initialization complete.");
}

function addAllEvents() {
    console.log("Adding all events...");
    eventForm.addEventListener("submit", handleSubmit);
    thumbnailInput.addEventListener("change", (e) => handleImageUpload(e, thumbnailNameSpan, thumbnailPreview, thumbnailPreviewContainer));
    contentImageInput.addEventListener("change", (e) => handleImageUpload(e, contentImageNameSpan, contentImagePreview, contentImagePreviewContainer));
    cancelButton.addEventListener("click", handleCancel);
    console.log("All events added.");
}

async function fetchEventData() {
    try {
        const event = await Api.get('/api/admin/events', eventId);
        console.log("Event data received:", event);

        titleInput.value = event.eventTitle;
        contentInput.value = event.eventContent;
        startDateInput.value = event.startDate;
        endDateInput.value = event.endDate;

        if (event.thumbnailUrl) {
            thumbnailNameSpan.innerText = event.thumbnailUrl.split('/').pop();
            thumbnailPreview.src = event.thumbnailUrl;
            thumbnailPreviewContainer.style.display = 'block';
            console.log("Thumbnail set:", thumbnailNameSpan.innerText);
        }

        if (event.contentImageUrl) {
            contentImageNameSpan.innerText = event.contentImageUrl.split('/').pop();
            contentImagePreview.src = event.contentImageUrl;
            contentImagePreviewContainer.style.display = 'block';
            console.log("Content image set:", contentImageNameSpan.innerText);
        }

        console.log("Event data loaded successfully");
    } catch (err) {
        console.error("Error fetching event data:", err);
        alert('이벤트 정보를 불러오는데 실패했습니다.');
    }
}

// 날짜 포맷팅 함수 추가
function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// 현재 날짜 구하기
function getCurrentDate() {
    return formatDate(new Date());
}

// 1년 후 날짜 구하기
function getOneYearLater() {
    const date = new Date();
    date.setFullYear(date.getFullYear() + 1);
    return formatDate(date);
}

async function handleSubmit(e) {
    e.preventDefault();

    const title = titleInput.value;
    const content = contentInput.value;
    let startDate = startDateInput.value;
    let endDate = endDateInput.value;
    const thumbnailImage = thumbnailInput.files[0];
    const contentImages = contentImageInput.files;

    if (!title || !content) {
        return alert("모든 필드를 입력해주세요.");
    }

    // 시작일이 비어있으면 현재 날짜로 설정
    if (!startDate) {
        startDate = getCurrentDate();
        startDateInput.value = startDate;
    }

    // 종료일이 비어있으면 1년 후로 설정
    if (!endDate) {
        endDate = getOneYearLater();
        endDateInput.value = endDate;
    }

    if (thumbnailImage && thumbnailImage.size > 3e6) {
        return alert("썸네일 이미지는 최대 2.5MB 크기까지 가능합니다.");
    }

    for (let i = 0; i < contentImages.length; i++) {
        if (contentImages[i].size > 3e6) {
            return alert(`내용 이미지 ${i+1}은(는) 최대 2.5MB 크기까지 가능합니다.`);
        }
    }

    try {
        const formData = new FormData();

        // JSON 데이터를 문자열로 변환하여 추가
        const eventData = {
            eventTitle: title,
            eventContent: content,
            startDate: startDate,
            endDate: endDate
        };
        formData.append('event', new Blob([JSON.stringify(eventData)], {type: 'application/json'}));

        // 썸네일 이미지 추가 - 필드명 수정
        if (thumbnailImage) {
            formData.append('thumbnailFile', thumbnailImage);
        }

        // 내용 이미지들 추가 - 필드명 수정
        for (let i = 0; i < contentImages.length; i++) {
            formData.append('contentFiles', contentImages[i]);
        }

        let response;
        if (isEditMode) {
            response = await Api.patchFormData(`/api/admin/events/${eventId}`, formData);
            alert(`정상적으로 ${title} 이벤트가 수정되었습니다.`);
        } else {
            response = await Api.postFormData("/api/admin/events", formData);
            alert(`정상적으로 ${title} 이벤트가 등록되었습니다.`);
        }

        window.location.href = '/admin/events';
    } catch (err) {
        console.error("Error details:", err);
        alert(`문제가 발생하였습니다. 확인 후 다시 시도해 주세요: ${err.message}`);
    }
}


function handleImageUpload(e, nameSpan, preview, previewContainer) {
    const files = e.target.files;
    if (files.length > 0) {
        nameSpan.innerText = Array.from(files).map(file => file.name).join(', ');
        previewContainer.innerHTML = ''; // 기존 미리보기 삭제

        Array.from(files).forEach(file => {
            const reader = new FileReader();
            reader.onload = function(e) {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.className = 'image-preview';
                previewContainer.appendChild(img);
            }
            reader.readAsDataURL(file);
        });

        previewContainer.style.display = 'block';
    } else {
        nameSpan.innerText = "사진 파일 (png, jpg, jpeg)";
        previewContainer.style.display = 'none';
        previewContainer.innerHTML = '';
    }
}

function handleCancel() {
    window.location.href = '/admin/events';
}

// 페이지 로드 시 페이지 초기화 실행
document.addEventListener('DOMContentLoaded', initializePage);

