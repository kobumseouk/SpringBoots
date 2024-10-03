-- Users 테이블에 테스트 데이터 삽입
INSERT INTO users (user_id, username, user_real_id, email, password, is_deleted, delete_reason, role, provider, created_at, updated_at)
VALUES (1, 'test_user', 'test_real_id', 'test_user@example.com', 'password123', 0, NULL, 'USER', 'GOOGLE', NOW(), NOW());

-- Category 테이블에 테스트 데이터 삽입
INSERT INTO category (category_id, category_name, category_thema, category_content, display_order, created_at, updated_at)
VALUES (1, 'Shoes', '공용', 'All types of shoes', 1, NOW(), NOW()),
       (2, 'Shoes', '여성', 'All types of shoes', 2, NOW(), NOW()),
       (3, 'Shoes', '남성', 'All types of shoes', 3, NOW(), NOW());

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


-- Event 테이블에 테스트 데이터 삽입
INSERT INTO event (
    event_id, category_id, event_title, event_content, thumbnail_image_url,
    content_image_url, start_date, end_date, is_active, created_at, updated_at
) VALUES
    (1, 1, '여름 신발 세일', '모든 여름 신발 20% 할인! 더운 여름을 시원하게 보내세요.',
    'https://example.com/summer_sale_thumb.jpg', 'https://example.com/summer_sale_content.jpg',
    '2023-07-01', '2023-07-31', true, NOW(), NOW()),

    (2, 1, '새 학기 특별전', '학생들을 위한 신발 특별 할인. 새 학기를 새 신발과 함께 시작하세요!',
    'https://example.com/back_to_school_thumb.jpg', 'https://example.com/back_to_school_content.jpg',
    '2023-08-15', '2023-09-15', true, NOW(), NOW()),

    (3, 1, '겨울 부츠 프리뷰', '다가오는 겨울 시즌 부츠 미리보기. 따뜻하고 스타일리시한 겨울을 준비하세요.',
    'https://example.com/winter_preview_thumb.jpg', 'https://example.com/winter_preview_content.jpg',
    '2023-10-01', '2023-10-31', true, NOW(), NOW());