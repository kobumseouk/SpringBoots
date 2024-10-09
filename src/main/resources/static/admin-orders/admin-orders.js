import { addCommas, createNavbar } from "../useful-functions.js";
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

// 초기 설정
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

  const summary = {
    ordersCount: 0,
    prepareCount: 0,
    deliveryCount: 0,
    completeCount: 0,
  };

  for (const order of orders) {
    const { ordersId, ordersTotalPrice, createdAt, orderStatus, items } = order;
    const date = new Date(createdAt).toLocaleDateString();
    const summaryTitle = items.map(item => `${item.itemName}/${item.orderitemsQuantity}`).join(", ");

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
              <div class="column is-2">
                <button class="button is-danger" id="deleteButton-${ordersId}">주문 취소</button>
              </div>
            </div>
            `
    );

    const statusSelectBox = document.querySelector(`#statusSelectBox-${ordersId}`);
    const deleteButton = document.querySelector(`#deleteButton-${ordersId}`);

    // 상태 변경 이벤트
    statusSelectBox.addEventListener("change", async () => {
      const newStatus = statusSelectBox.value;
      const data = { orderStatus: newStatus };

      try {
        await Api.patch(`/api/admin/orders/${ordersId}/status`, "", data);
        alert("주문 상태가 성공적으로 업데이트되었습니다.");
      } catch (error) {
        console.error("상태 변경 중 오류 발생:", error);
        alert("상태 변경 중 오류가 발생했습니다.");
      }
    });

    // 주문 취소 버튼 클릭 시 모달 창 열기 및 orderId 설정
    deleteButton.addEventListener("click", () => {
      orderIdToDelete = ordersId; // 선택한 주문의 ID 저장
      openModal(); // 모달 창 열기
    });
  }

  ordersCount.innerText = addCommas(summary.ordersCount);
  prepareCount.innerText = addCommas(summary.prepareCount);
  deliveryCount.innerText = addCommas(summary.deliveryCount);
  completeCount.innerText = addCommas(summary.completeCount);
}

// 주문 취소
async function deleteOrderData(orderIdToDelete) {
  try {
    await Api.delete(`/api/admin/orders`, orderIdToDelete);
    const deletedItem = document.querySelector(`#order-${orderIdToDelete}`);
    deletedItem.remove();
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
