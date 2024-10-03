-- Users 테이블에 테스트 데이터 삽입
INSERT INTO users (user_id, username, user_real_id, email, password, is_deleted, delete_reason, role, provider, created_at, updated_at)
VALUES (1, 'test_user', 'test_real_id', 'test_user@example.com', 'password123', 0, NULL, 'USER', 'GOOGLE', NOW(), NOW());

-- Category 테이블에 테스트 데이터 삽입
INSERT INTO category (category_id, category_name, category_content, display_order, created_at, updated_at)
VALUES (1, 'Shoes', 'All types of shoes', 1, NOW(), NOW());

-- Item 테이블에 테스트 데이터 삽입
INSERT INTO item (item_id, category_id, item_name, item_price, item_description, item_maker, item_color, created_at, updated_at, image_url, item_size)
VALUES (101, 1, 'Running Shoes', 5000, 'Comfortable running shoes', 'Brand A', 'Red', NOW(), NOW(), 'https://example.com/shoes.jpg', 42);

-- Orders 테이블에 테스트 데이터 삽입
INSERT INTO orders (orders_id, user_id, quantity, orders_total_price, created_at, updated_at)
VALUES (1, 1, 3, 15000, NOW(), NOW());

-- OrderItems 테이블에 테스트 데이터 삽입
INSERT INTO order_items (
    order_items_id, orders_id, item_id, orderitems_total_price, orderitems_quantity,
    order_status, shipping_address, recipient_name, recipient_contact, delivery_message,
    created_at, updated_at, is_canceled
) VALUES
      (1, 1, 101, 5000, 1, 'Pending', '서울특별시 강남구 선릉로 433, 신관 6층', '엘리스', '010-4234-3424', '문앞에 배송해주세요', NOW(), NOW(), false),
      (2, 1, 101, 10000, 2, 'Pending', '서울특별시 강남구 선릉로 433, 신관 8층', 'John Doe', '031-434-223', '부재시 경비실에 맡겨주세요', NOW(), NOW(), false);
