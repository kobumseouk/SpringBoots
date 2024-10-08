-- Users 테이블에 테스트 데이터 삽입
INSERT INTO users (user_id, username, user_real_id, email, password, is_deleted, delete_reason, role, provider, created_at, updated_at)
VALUES (1, 'test_user', 'test_real_id', 'test_user@example.com', '$2a$10$V1hEUNfKSMDNC2lkh58L0uebtdMuwl0tCf2mwlmz4j47Vt.7OgZNe', 0, NULL, 'USER', 'GOOGLE', NOW(), NOW());

INSERT INTO users (user_id, username, user_real_id, email, password, is_deleted, delete_reason, role, provider, created_at, updated_at)
VALUES (2, 'second_user', 'second_real_id', 'second_user@example.com', '$2a$10$WKGvXJc/mTeYBvCTHu0t/uAxuh3NS5u.Tv4BqhR8Geby1DVchWLxO', 0, NULL, 'USER', 'GOOGLE', NOW(), NOW());

INSERT INTO users (user_id, username, user_real_id, email, password, is_deleted, delete_reason, role, provider, created_at, updated_at)
VALUES (3, 'admin', 'admin', 'admin@example.com', '$2a$10$V1hEUNfKSMDNC2lkh58L0uebtdMuwl0tCf2mwlmz4j47Vt.7OgZNe', 0, NULL, 'ADMIN', 'GOOGLE', NOW(), NOW());

-- UsersInfo 테이블에 테스트 데이터 삽입
INSERT INTO users_info (
    user_info_id, user_id, address, phone, created_at, updated_at
) VALUES
    (1, 1, '경기도 성남시 분당구 대왕판교로 670', '010-9876-5432', NOW(), NOW());

INSERT INTO users_info (
    user_info_id, user_id, address, phone, created_at, updated_at
) VALUES
    (2, 2, '서울특별시 마포구 월드컵북로 396', '010-8765-4321', NOW(), NOW());


-- Category 테이블에 테스트 데이터 삽입
INSERT INTO category (
    category_name, category_thema, category_content, display_order, created_at, updated_at, image_url
) VALUES
    ('Shoes', '공용', 'All types of shoes', 1, NOW(), NOW(), NULL),
    ('Shoes', '여성', 'All types of shoes', 2, NOW(), NOW(), NULL),
    ('Shoes', '남성', 'All types of shoes', 3, NOW(), NOW(), NULL),
    ('24SS SUMMER RECOMEND STYLING', 'HOW TO', 'Styling Guide', 5, NOW(), NOW(), 'https://example.com/summer_styling_guide.jpg');

-- Item 테이블에 테스트 데이터 삽입
INSERT INTO item (item_id, category_id, item_name, item_price, item_description, item_maker, item_color, created_at, updated_at, image_url, item_size)
VALUES (101, 1, 'Running Shoes', 5000, 'Comfortable running shoes', 'Brand A', 'Red', NOW(), NOW(), 'https://example.com/shoes.jpg', 230);

INSERT INTO item (item_id, category_id, item_name, item_price, item_description, item_maker, item_color, created_at, updated_at, image_url, item_size)
VALUES (102, 2, 'Walking Shoes', 7000, 'Comfortable walking shoes', 'Brand B', 'Blue', NOW(), NOW(), 'https://example.com/walking_shoes.jpg', 240);

-- Orders 테이블에 테스트 데이터 삽입
INSERT INTO orders (
    orders_id, user_id, quantity, orders_total_price, order_status, is_canceled, created_at, updated_at
) VALUES
    (1, 1, 3, 15000, '주문완료', false, NOW(), NOW());

-- Orders 테이블에 테스트 데이터 삽입 (유저 2의 주문 추가)
INSERT INTO orders (
    orders_id, user_id, quantity, orders_total_price, order_status, is_canceled, created_at, updated_at
) VALUES
    (2, 2, 2, 14000, '주문완료', false, NOW(), NOW());
INSERT INTO orders (
    orders_id, user_id, quantity, orders_total_price, order_status, is_canceled, created_at, updated_at
) VALUES
    (3, 1, 3, 21000, '배송중', false, NOW(), NOW());


-- OrderItems 테이블에 테스트 데이터 삽입
INSERT INTO order_items (
    order_items_id, orders_id, item_id, orderitems_total_price, orderitems_quantity,
    shipping_address, recipient_name, recipient_contact, delivery_message,
    created_at, updated_at
) VALUES
      (1, 1, 101, 5000, 1, '서울특별시 강남구 선릉로 433, 신관 6층', '엘리스', '010-4234-3424', '문앞에 배송해주세요', NOW(), NOW()),
      (2, 1, 101, 10000, 2, '서울특별시 강남구 선릉로 433, 신관 6층', 'John Doe', '031-434-223', '부재시 경비실에 맡겨주세요', NOW(), NOW());

INSERT INTO order_items (
    order_items_id, orders_id, item_id, orderitems_total_price, orderitems_quantity,
    shipping_address, recipient_name, recipient_contact, delivery_message,
    created_at, updated_at
) VALUES
      (3, 2, 102, 7000, 1, '서울특별시 마포구 월드컵북로 396', 'Second User', '010-8765-4321', '부재 시 경비실에 맡겨주세요', NOW(), NOW()),
      (4, 2, 102, 7000, 1, '서울특별시 마포구 월드컵북로 396', 'Second User', '010-8765-4321', '조심히 다뤄주세요', NOW(), NOW());

INSERT INTO order_items (
    order_items_id, orders_id, item_id, orderitems_total_price, orderitems_quantity,
    shipping_address, recipient_name, recipient_contact, delivery_message,
    created_at, updated_at
) VALUES
      (5, 3, 102, 7000, 1, '서울특별시 강남구 선릉로 433, 신관 6층', '엘리스', '010-4234-3424', '부재 시 경비실에 맡겨주세요', NOW(), NOW()),
      (6, 3, 102, 7000, 1, '서울특별시 강남구 선릉로 433, 신관 6층', '엘리스', '010-4234-3424', '조심히 다뤄주세요', NOW(), NOW()),
      (7, 3, 102, 7000, 1, '서울특별시 강남구 선릉로 433, 신관 6층', '엘리스', '010-4234-3424', '조심히 다뤄주세요', NOW(), NOW());

-- Event 테이블에 테스트 데이터 삽입
INSERT INTO event (
    event_id,  event_title, event_content, thumbnail_image_url,
    content_image_url, start_date, end_date, is_active, created_at, updated_at
) VALUES
    (1, '여름 신발 세일', '모든 여름 신발 20% 할인! 더운 여름을 시원하게 보내세요.',
    'https://example.com/summer_sale_thumb.jpg', 'https://example.com/summer_sale_content.jpg',
    '2024-07-01', '2024-07-31', true, NOW(), NOW()),

    (2, '새 학기 특별전', '학생들을 위한 신발 특별 할인. 새 학기를 새 신발과 함께 시작하세요!',
    'https://example.com/back_to_school_thumb.jpg', 'https://example.com/back_to_school_content.jpg',
    '2024-08-15', '2024-09-15', true, NOW(), NOW()),

    (3, '겨울 부츠 프리뷰', '다가오는 겨울 시즌 부츠 미리보기. 따뜻하고 스타일리시한 겨울을 준비하세요.',
    'https://example.com/winter_preview_thumb.jpg', 'https://example.com/winter_preview_content.jpg',
    '2024-10-01', '2024-10-31', true, NOW(), NOW());