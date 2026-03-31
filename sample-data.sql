-- Sample data for Tour_Travel_BackEnd
-- Target database: defaultdb
-- Safe to run multiple times (it clears only seeded IDs first).

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM favorite_tours WHERE id IN (9001, 9002);
DELETE FROM review WHERE review_id IN (8001, 8002, 8003);
DELETE FROM images WHERE image_id IN (7001, 7002, 7003, 7004, 7005, 7006);
DELETE FROM tour_destinations WHERE tour_id IN (3001, 3002, 3003);
DELETE FROM tour_pricing WHERE tour_pricing_id IN (5001, 5002, 5003, 5004, 5005, 5006);
DELETE FROM departures WHERE departure_id IN (4001, 4002, 4003, 4004);
DELETE FROM tours WHERE tour_id IN (3001, 3002, 3003);
DELETE FROM destinations WHERE destination_id IN (2001, 2002, 2003, 2004, 2005);
DELETE FROM users WHERE user_id IN (1001, 1002, 1003);

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users (user_id, email, full_name, phone_number) VALUES
(1001, 'provider1@tour.local', 'Tour Provider One', '0901000001'),
(1002, 'customer1@tour.local', 'Customer One', '0901000002'),
(1003, 'customer2@tour.local', 'Customer Two', '0901000003');

INSERT INTO destinations (destination_id, name, description, province) VALUES
(2001, 'Da Nang', 'Bien dep, am thuc phong phu va nhieu diem check-in.', 'Da Nang'),
(2002, 'Hoi An', 'Pho co voi den long va van hoa dac sac.', 'Quang Nam'),
(2003, 'Ha Noi', 'Thu do ngan nam van hien, am thuc da dang.', 'Ha Noi'),
(2004, 'Ha Long', 'Vinh Ha Long ky quan thien nhien the gioi.', 'Quang Ninh'),
(2005, 'Phu Quoc', 'Dao ngoc voi bien xanh va khu nghi duong.', 'Kien Giang');

INSERT INTO tours
(tour_id, tour_name, tour_description, duration, start_location, is_active, created_date, tour_type, user_id)
VALUES
(3001, 'Da Nang - Hoi An 3N2D', 'Tour nghi duong ket hop tham quan pho co Hoi An.', 3, 'Ho Chi Minh', 1, '2026-03-01 08:00:00', 'FAMILY_CENTRAL', 1001),
(3002, 'Ha Noi - Ha Long 4N3D', 'Kham pha thu do va ky quan Vinh Ha Long.', 4, 'Ho Chi Minh', 1, '2026-03-02 08:30:00', 'GROUP_NORTHERN', 1001),
(3003, 'Phu Quoc Relax 3N2D', 'Tour nghi duong bien dao cho gia dinh.', 3, 'Can Tho', 1, '2026-03-03 09:00:00', 'FAMILY_RELAXATION', 1001);

INSERT INTO departures
(departure_id, tour_id, start_date, end_date, available_seats, max_participants, is_active)
VALUES
(4001, 3001, '2026-04-15 06:00:00', '2026-04-17 20:00:00', 18, 20, 1),
(4002, 3001, '2026-05-10 06:00:00', '2026-05-12 20:00:00', 15, 20, 1),
(4003, 3002, '2026-04-20 05:30:00', '2026-04-23 21:00:00', 22, 25, 1),
(4004, 3003, '2026-04-25 07:00:00', '2026-04-27 19:00:00', 10, 15, 1);

INSERT INTO tour_pricing
(tour_pricing_id, departure_id, price, participant_type, modified_date)
VALUES
(5001, 4001, 3290000, 'ADULTS', '2026-03-20 10:00:00'),
(5002, 4001, 2490000, 'CHILDREN', '2026-03-20 10:00:00'),
(5003, 4002, 3390000, 'ADULTS', '2026-03-22 10:00:00'),
(5004, 4003, 4590000, 'ADULTS', '2026-03-18 10:00:00'),
(5005, 4003, 3190000, 'CHILDREN', '2026-03-18 10:00:00'),
(5006, 4004, 3890000, 'ADULTS', '2026-03-19 10:00:00');

INSERT INTO tour_destinations (tour_id, destination_id, duration, sequence_order) VALUES
(3001, 2001, 2, 1),
(3001, 2002, 1, 2),
(3002, 2003, 2, 1),
(3002, 2004, 2, 2),
(3003, 2005, 3, 1);

INSERT INTO images (image_id, image_url, tour_id, destination_id) VALUES
(7001, 'https://images.unsplash.com/photo-1559592413-7cec4d0cae2b', 3001, NULL),
(7002, 'https://images.unsplash.com/photo-1528127269322-539801943592', 3001, NULL),
(7003, 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e', 3003, NULL),
(7004, 'https://images.unsplash.com/photo-1534447677768-be436bb09401', 3002, NULL),
(7005, 'https://images.unsplash.com/photo-1540202404-1b927e27fa8b', NULL, 2002),
(7006, 'https://images.unsplash.com/photo-1537996194471-e657df975ab4', NULL, 2004);

INSERT INTO review (review_id, user_id, tour_id, rating, comment, review_date, is_active) VALUES
(8001, 1002, 3001, 5, 'Tour rat vui, huong dan vien nhiet tinh.', '2026-03-25 20:00:00', 1),
(8002, 1003, 3002, 4, 'Lich trinh on, do an ngon.', '2026-03-26 18:30:00', 1),
(8003, 1002, 3003, 5, 'Nghi duong tuyet voi, bien dep.', '2026-03-27 19:45:00', 1);

INSERT INTO favorite_tours (id, user_id, tour_id, added_date) VALUES
(9001, 1002, 3001, '2026-03-28 09:00:00'),
(9002, 1003, 3003, '2026-03-28 09:30:00');

