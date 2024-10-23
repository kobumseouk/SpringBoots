import { addCommas, createNavbar } from "../useful-functions.js";
import { loadHeader } from "../../common/header.js";
import * as Api from "../api.js";

// 요소 선택
const ordersCount = document.querySelector("#ordersCount");
const prepareCount = document.querySelector("#prepareCount");
const deliveryCount = document.querySelector("#deliveryCount");
const completeCount = document.querySelector("#completeCount");
const ordersContainer = document.querySelector("#ordersContainer");
const modal = document.querySelector("#modal");
const modalBackground = document.querySelector("#modalBackground");
const modalCloseButton = document.querySelector("#modalCloseButton");
const deleteCompleteButton = document.querySelector("#deleteCompleteButton");
const deleteCancelButton = document.querySelector("#deleteCancelButton");

let orderIdToDelete;

// summary 변수를 전역에서 접근할 수 있도록 선언
let summary = {
  ordersCount: 0,
  prepareCount: 0,
  deliveryCount: 0,
  completeCount: 0,
};

// 초기 설정
loadHeader();
createNavbar();
insertOrders();
addAllEvents();

// 이벤트 설정
function addAllEvents() {
  modalBackground.addEventListener("click", closeModal);
  modalCloseButton.addEventListener("click", closeModal);
  document.addEventListener("keydown", keyDownCloseModal);
  deleteCompleteButton.addEventListener("click", () => {
    deleteOrderData(orderIdToDelete); // 모달에서 "네" 클릭 시 주문 삭제 실행
  });
  deleteCancelButton.addEventListener("click", cancelDelete);
}

// 주문 데이터를 받아서 테이블에 추가
async function insertOrders() {
  const orders = await Api.get("/api/admin/orders");

  summary = { // summary 객체 초기화
    ordersCount: 0,
    prepareCount: 0,
    deliveryCount: 0,
    completeCount: 0,
  };

  for (const order of orders) {
    const { ordersId, ordersTotalPrice, createdAt, orderStatus, items } = order;
    const date = new Date(createdAt).toLocaleDateString();
    const summaryTitle = items.map(item => `${item.itemName}/${item.orderItemsQuantity}`).join(", ");

    summary.ordersCount += 1;

    if (orderStatus === "주문완료") {
      summary.prepareCount += 1;
    } else if (orderStatus === "상품배송중") {
      summary.deliveryCount += 1;
    } else if (orderStatus === "배송완료") {
      summary.completeCount += 1;
    }

    ordersContainer.insertAdjacentHTML(
        "beforeend",
        `
        <div class="columns orders-item" id="order-${ordersId}">
          <div class="column is-2">${date}</div>
          <div class="column is-4 order-summary">${summaryTitle}</div>
          <div class="column is-2">${addCommas(ordersTotalPrice)}원</div>
          <div class="column is-2">
            <select id="statusSelectBox-${ordersId}" class="select">
              <option value="주문완료" ${orderStatus === "주문완료" ? "selected" : ""}>주문완료</option>
              <option value="상품배송중" ${orderStatus === "상품배송중" ? "selected" : ""}>상품배송중</option>
              <option value="배송완료" ${orderStatus === "배송완료" ? "selected" : ""}>배송완료</option>
            </select>
          </div>
          <div class="column is-2" id="cancelButtonContainer-${ordersId}">
            ${orderStatus === "주문완료" ? `<button class="button is-danger" id="deleteButton-${ordersId}">주문 취소</button>` : ""}
          </div>
        </div>
      `
    );

    const statusSelectBox = document.querySelector(`#statusSelectBox-${ordersId}`);
    const deleteButton = document.querySelector(`#deleteButton-${ordersId}`);
    const cancelButtonContainer = document.querySelector(`#cancelButtonContainer-${ordersId}`);

    // 상태 변경 이벤트
    let oldStatus = orderStatus; // 변경 전 상태 저장

    // 상태 변경 이벤트
    statusSelectBox.addEventListener("change", async () => {
      const newStatus = statusSelectBox.value;
      const data = { orderStatus: newStatus };

      try {
        await Api.patch(`/api/admin/orders/${ordersId}/status`, "", data);
        alert("주문 상태가 성공적으로 업데이트되었습니다.");

        // 상태가 '주문완료'일 때 취소 버튼 다시 표시
        if (newStatus === "주문완료") {
          cancelButtonContainer.innerHTML = `<button class="button is-danger" id="deleteButton-${ordersId}">주문 취소</button>`;
          // 새로 추가된 취소 버튼에도 이벤트 리스너 추가
          const newDeleteButton = document.querySelector(`#deleteButton-${ordersId}`);
          newDeleteButton.addEventListener("click", () => {
            orderIdToDelete = ordersId; // 선택한 주문의 ID 저장
            openModal(); // 모달 창 열기
          });
        } else {
          // 다른 상태일 때 취소 버튼 제거
          cancelButtonContainer.innerHTML = "";
        }

        // 상태 변경 후 요약 정보 업데이트
        if (newStatus === "상품배송중") {
          if (oldStatus === "주문완료" && summary.prepareCount > 0) {
            summary.prepareCount -= 1;
          } else if (oldStatus === "배송완료" && summary.completeCount > 0) {
            summary.completeCount -= 1;
          }
          summary.deliveryCount += 1;
        } else if (newStatus === "배송완료") {
          if (oldStatus === "상품배송중" && summary.deliveryCount > 0) {
            summary.deliveryCount -= 1;
          } else if (oldStatus === "주문완료" && summary.prepareCount > 0) {
            summary.prepareCount -= 1;
          }
          summary.completeCount += 1;
        } else if (newStatus === "주문완료") {
          if (oldStatus === "배송완료" && summary.completeCount > 0) {
            summary.completeCount -= 1;
          } else if (oldStatus === "상품배송중" && summary.deliveryCount > 0) {
            summary.deliveryCount -= 1;
          }
          summary.prepareCount += 1;
        }

        oldStatus = newStatus; // 상태가 변경되었으므로 oldStatus 업데이트

        updateSummaryCounts();
      } catch (error) {
        console.error("상태 변경 중 오류 발생:", error);
        alert("상태 변경 중 오류가 발생했습니다.");
      }
    });

    // 주문 취소 버튼 클릭 시 모달 창 열기 및 orderId 설정
    if (deleteButton) {
      deleteButton.addEventListener("click", () => {
        orderIdToDelete = ordersId; // 선택한 주문의 ID 저장
        openModal(); // 모달 창 열기
      });
    }
  }

  // 초기 요약 정보 업데이트
  updateSummaryCounts();
}

// 요약 정보 업데이트 함수
function updateSummaryCounts() {
  ordersCount.innerText = addCommas(summary.ordersCount);
  prepareCount.innerText = addCommas(summary.prepareCount);
  deliveryCount.innerText = addCommas(summary.deliveryCount);
  completeCount.innerText = addCommas(summary.completeCount);
}

// 주문 취소
async function deleteOrderData(orderIdToDelete) {
  try {

    // 삭제하려는 주문의 상태를 추적
    const orderElement = document.querySelector(`#order-${orderIdToDelete}`);
    const statusSelectBox = orderElement.querySelector('select');
    const orderStatus = statusSelectBox.value; // 현재 상태 가져오기

    await Api.delete(`/api/admin/orders`, orderIdToDelete);
    const deletedItem = document.querySelector(`#order-${orderIdToDelete}`);
    deletedItem.remove();

    summary.ordersCount -= 1; // 주문 개수 감소
    if (orderStatus === "주문완료") {
      summary.prepareCount -= 1;
    } else if (orderStatus === "상품배송중") {
      summary.deliveryCount -= 1;
    } else if (orderStatus === "배송완료") {
      summary.completeCount -= 1;
    }

    updateSummaryCounts(); // 요약 정보 업데이트
    alert("주문이 취소되었습니다.");
    closeModal();
  } catch (err) {
    console.error(`Error: ${err.message}`);
    alert(`오류가 발생했습니다: ${err.message}`);
  }
}

// 모달 창 열기
function openModal() {
  modal.classList.add("is-active");
}

// 모달 창 닫기
function closeModal() {
  modal.classList.remove("is-active");
}

// 키보드로 모달 닫기
function keyDownCloseModal(e) {
  if (e.keyCode === 27) {
    closeModal();
  }
}

// 취소 버튼 클릭 시, 모달 창 닫기
function cancelDelete() {
  orderIdToDelete = "";
  closeModal();
}
