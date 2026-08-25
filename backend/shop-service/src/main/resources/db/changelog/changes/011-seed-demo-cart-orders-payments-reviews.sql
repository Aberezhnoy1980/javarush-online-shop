-- liquibase formatted sql

-- changeset aberezhnoy:011-seed-demo-cart-orders-payments-reviews
INSERT INTO carts (user_id)
VALUES (1),
       (2),
       (3),
       (4),
       (5);

INSERT INTO cart_items (cart_id, product_id, quantity)
VALUES (1, 1, 1),
       (1, 8, 2),
       (2, 3, 1),
       (2, 7, 1),
       (3, 4, 1),
       (4, 2, 1),
       (4, 8, 1),
       (5, 9, 1);

INSERT INTO orders (user_id, status, total_amount)
VALUES (1, 'NEW', 249970.00),
       (2, 'PAID', 169980.00),
       (3, 'DELIVERED', 189990.00),
       (4, 'PROCESSING', 174980.00),
       (1, 'DELIVERED', 249970.00);

INSERT INTO order_items (order_id, product_id, product_name_snapshot, quantity, price_at_time, line_total)
VALUES (1, 1, 'MacBook Pro 14', 1, 199990.00, 199990.00),
       (1, 8, 'AirPods Pro', 2, 24990.00, 49980.00),
       (2, 3, 'Galaxy S24 Ultra', 1, 129990.00, 129990.00),
       (2, 7, 'Galaxy Watch 6', 1, 39990.00, 39990.00),
       (3, 4, 'Dell XPS 16', 1, 189990.00, 189990.00),
       (4, 2, 'iPhone 15 Pro Max', 1, 149990.00, 149990.00),
       (4, 8, 'AirPods Pro', 1, 24990.00, 24990.00),
       (5, 1, 'MacBook Pro 14', 1, 189990.00, 189990.00),
       (5, 2, 'iPhone 15 Pro Max', 1, 59980.00, 59980.00);

INSERT INTO payments (order_id, amount, payment_method, status, transaction_id, payment_date)
VALUES (1, 249970.00, 'CARD', 'PENDING', NULL, NULL),
       (2, 169980.00, 'PAYPAL', 'COMPLETED', 'txn_123456', '2024-01-15 14:30:00'),
       (3, 189990.00, 'CARD', 'COMPLETED', 'txn_789012', '2024-01-10 10:15:00'),
       (4, 174980.00, 'CRYPTO', 'COMPLETED', 'txn_345678', '2024-01-20 09:00:00'),
       (5, 249970.00, 'CARD', 'COMPLETED', 'txn_901234', '2023-12-01 12:00:00');

INSERT INTO reviews (user_id, product_id, rating, comment)
VALUES (1, 1, 5, 'Отличный ноутбук! Очень быстрый и качественный'),
       (1, 8, 4, 'Хорошие наушники, но дороговато'),
       (2, 3, 5, 'Лучший смартфон на рынке!'),
       (3, 4, 4, 'Хороший ноутбук, но шумный под нагрузкой'),
       (4, 2, 5, 'iPhone — это просто космос!'),
       (5, 9, 4, 'Отличный ноутбук для работы, лёгкий и быстрый');
