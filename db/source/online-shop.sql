-- Товары Категории товаров Пользователи Заказы Отзывы
-- Cart CartItem Order OrderItem Payment Product Category (иерархия) User Review Brand
-- User name, email, password_hash, phone, address, created_at, updated_at
-- Category  id, name, parent_id, description, created_at

-- Электроника (1, null)
--      Ноутбуки (2, 1)
--     Игровые (4, 2)
--     Офисные (5, 2)
--      ПК (3, 1)
--     Комплектующие (6, 3)
--    Материнские платы
--    Куллер
--    HDD
--     В сборе (7, 3)

-- Brand name, description, created_at
-- Product name, price, stock_quantity, description, brand_id, category_id, created_at, updated_at
-- Review product_id, comment, rating, user_id, created_at, updated_at
-- Payment order_id, payment_method, amount, transaction_id, status, payment_date, created_at, updated_at
-- Order user_id, total_amount, status, created_at, updated_at
-- OrderItem order_id, product_id, price_at_time, quantity, created_at
-- Cart  user_id, created_at, updated_at
-- CartItem cart_id, product_id, quantity, created_at
---------------------------------------------------------------
-- User -> Card 1:1
-- User -> Order 1:M
-- Cart -> CartItem 1:M
-- Order -> OrderItem 1:M
-- Product -> CartItem 1:1
-- Product -> OrderItem 1:1
-----------------------------------------------------------------
CREATE table users
(
    id            SERIAL PRIMARY KEY,
    name          varchar(100) not null,
    email         varchar(100) not null unique,
    password_hash varchar      not null,
    phone         varchar(50),
    address       varchar,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories
(
    id          SERIAL PRIMARY KEY,
    name        varchar not null,
    parent_id   integer references categories (id) on delete cascade,
    description varchar not null,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table brand
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description varchar,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create TABLE products
(
    id             SERIAL PRIMARY KEY,
    name           varchar(250)   not null,
    price          decimal(10, 2) not null check ( price >= 0 ),
    stock_quantity integer        not null default 0 check ( stock_quantity >= 0),
    description    varchar,
    brand_id       integer references brand (id),
    category_id    integer references categories (id),
    created_at     TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP               DEFAULT CURRENT_TIMESTAMP
);

CREATE table cart
(
    id         serial primary key,
    user_id    integer references users (id) unique not null,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table card_items
(
    id         serial primary key,
    card_id    integer references cart (id) on DELETE cascade not null,
    product_id integer references products (id)               not null,
    quantity   integer check ( quantity > 0 )                 not null,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unique (card_id, product_id)
);

create table orders(
    id serial primary key ,
    user_id integer references users(id) not null ,
    total_amount decimal(10,2) not null check ( total_amount > 0 ),
    status varchar(20) DEFAULT 'NEW' CHECK ( status in ('NEW', 'PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table order_items(
    id serial primary key ,
    order_id integer references orders(id) on DELETE cascade not null ,
    product_id integer references products(id) not null,
    price_at_time decimal(10,2) not null check ( price_at_time > 0),
    quantity integert not null check ( quantity > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table payments(
    id serial primary key ,
    order_id integer references orders(id) not null unique ,
    amount decimal (10, 2) not null check ( amount > 0 ),
    transaction_id varchar not null,
    status varchar DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    payment_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method varchar not null
);

create table reviews(
    id serial primary key ,
    product_id integer references products(id) not null,
    comment varchar,
    rating integer not null check ( rating between 1 and 5) ,
    user_id integer references users(id) not null ,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unique (product_id, user_id)
);

-- ============================================================
-- ТЕСТОВЫЕ ДАННЫЕ
-- ============================================================

-- ===== 1. КАТЕГОРИИ =====
INSERT INTO categories (name, parent_id, description) VALUES
                                                          ('Электроника', NULL, 'Все виды электроники'),
                                                          ('Ноутбуки', 1, 'Ноутбуки и планшеты'),
                                                          ('Смартфоны', 1, 'Мобильные телефоны'),
                                                          ('Аксессуары', NULL, 'Аксессуары для гаджетов'),
                                                          ('Игровые ноутбуки', 2, 'Ноутбуки для игр'),
                                                          ('Бизнес ноутбуки', 2, 'Ноутбуки для работы');

-- ===== 2. БРЕНДЫ =====
INSERT INTO brands (name, description) VALUES
                                           ('Apple', 'Американская компания, производитель iPhone, MacBook'),
                                           ('Samsung', 'Южнокорейская компания, производитель Galaxy'),
                                           ('Dell', 'Американская компания, производитель ноутбуков'),
                                           ('Lenovo', 'Китайская компания, производитель ноутбуков'),
                                           ('Xiaomi', 'Китайская компания, производитель смартфонов');

-- ===== 3. ТОВАРЫ =====
INSERT INTO products (name, description, price, stock_quantity, brand_id, category_id) VALUES
                                                                                           ('MacBook Pro 14', 'Ноутбук Apple с чипом M3', 199990.00, 10, 1, 2),
                                                                                           ('iPhone 15 Pro Max', 'Смартфон Apple', 149990.00, 25, 1, 3),
                                                                                           ('Galaxy S24 Ultra', 'Смартфон Samsung', 129990.00, 30, 2, 3),
                                                                                           ('Dell XPS 16', 'Ноутбук Dell для работы', 189990.00, 15, 3, 2),
                                                                                           ('Lenovo Legion 7', 'Игровой ноутбук Lenovo', 174990.00, 8, 4, 5),
                                                                                           ('Xiaomi 14 Ultra', 'Смартфон Xiaomi', 99990.00, 20, 5, 3),
                                                                                           ('Galaxy Watch 6', 'Умные часы Samsung', 39990.00, 50, 2, 4),
                                                                                           ('AirPods Pro', 'Наушники Apple', 24990.00, 40, 1, 4),
                                                                                           ('MacBook Air 13', 'Лёгкий ноутбук Apple', 129990.00, 20, 1, 2),
                                                                                           ('Dell Latitude 5440', 'Бизнес-ноутбук Dell', 159990.00, 12, 3, 6);

-- ===== 4. ПОЛЬЗОВАТЕЛИ =====
INSERT INTO users (name, email, password_hash, phone, address) VALUES
                                                                   ('Иван Петров', 'ivan.petrov@example.com', 'hash123', '+7-999-111-22-33', 'г. Москва, ул. Тверская, д. 10'),
                                                                   ('Мария Сидорова', 'maria.sidorova@example.com', 'hash456', '+7-999-222-33-44', 'г. Санкт-Петербург, ул. Невская, д. 5'),
                                                                   ('Петр Иванов', 'petr.ivanov@example.com', 'hash789', '+7-999-333-44-55', 'г. Казань, ул. Кремлёвская, д. 15'),
                                                                   ('Анна Кузнецова', 'anna.kuznetsova@example.com', 'hash111', '+7-999-444-55-66', 'г. Москва, ул. Арбат, д. 20'),
                                                                   ('Дмитрий Смирнов', 'dmitry.smirnov@example.com', 'hash222', '+7-999-555-66-77', 'г. Новосибирск, ул. Ленина, д. 30');

-- ===== 5. КОРЗИНЫ =====
INSERT INTO carts (user_id) VALUES
                                (1), (2), (3), (4), (5);

-- ===== 6. ПОЗИЦИИ КОРЗИНЫ =====
INSERT INTO cart_items (cart_id, product_id, quantity) VALUES
                                                           (1, 1, 1),  -- Иван: MacBook Pro
                                                           (1, 8, 2),  -- Иван: 2 AirPods
                                                           (2, 3, 1),  -- Мария: Galaxy S24
                                                           (2, 7, 1),  -- Мария: Galaxy Watch
                                                           (3, 4, 1),  -- Пётр: Dell XPS
                                                           (4, 2, 1),  -- Анна: iPhone
                                                           (4, 8, 1),  -- Анна: AirPods
                                                           (5, 9, 1);  -- Дмитрий: MacBook Air

-- ===== 7. ЗАКАЗЫ =====
INSERT INTO orders (user_id, status, total_amount) VALUES
                                                       (1, 'NEW', 0),           -- Иван: новый заказ (сумма будет рассчитана позже)
                                                       (2, 'PAID', 169980.00),  -- Мария: оплачен
                                                       (3, 'DELIVERED', 189990.00), -- Пётр: доставлен
                                                       (4, 'PROCESSING', 174980.00), -- Анна: в обработке
                                                       (1, 'DELIVERED', 249970.00); -- Иван: старый заказ (доставлен)

-- ===== 8. ПОЗИЦИИ ЗАКАЗА =====
INSERT INTO order_items (order_id, product_id, quantity, price_at_time) VALUES
                                                                            (1, 1, 1, 199990.00),   -- Иван: MacBook Pro
                                                                            (1, 8, 2, 24990.00),    -- Иван: 2 AirPods
                                                                            (2, 3, 1, 129990.00),   -- Мария: Galaxy S24
                                                                            (2, 7, 1, 39990.00),    -- Мария: Galaxy Watch
                                                                            (3, 4, 1, 189990.00),   -- Пётр: Dell XPS
                                                                            (4, 2, 1, 149990.00),   -- Анна: iPhone
                                                                            (4, 8, 1, 24990.00),    -- Анна: AirPods
                                                                            (5, 1, 1, 189990.00),   -- Иван: старый MacBook (цена была ниже)
                                                                            (5, 2, 1, 59980.00);    -- Иван: старый iPhone

-- ===== 9. ОПЛАТЫ =====
INSERT INTO payments (order_id, amount, payment_method, status, transaction_id, payment_date) VALUES
                                                                                                  (1, 249970.00, 'CARD', 'PENDING', NULL, NULL),
                                                                                                  (2, 169980.00, 'PAYPAL', 'COMPLETED', 'txn_123456', '2024-01-15 14:30:00'),
                                                                                                  (3, 189990.00, 'CARD', 'COMPLETED', 'txn_789012', '2024-01-10 10:15:00'),
                                                                                                  (4, 174980.00, 'CRYPTO', 'COMPLETED', 'txn_345678', '2024-01-20 09:00:00'),
                                                                                                  (5, 249970.00, 'CARD', 'COMPLETED', 'txn_901234', '2023-12-01 12:00:00');

-- ===== 10. ОТЗЫВЫ =====
INSERT INTO reviews (user_id, product_id, rating, comment) VALUES
                                                               (1, 1, 5, 'Отличный ноутбук! Очень быстрый и качественный'),
                                                               (1, 8, 4, 'Хорошие наушники, но дороговато'),
                                                               (2, 3, 5, 'Лучший смартфон на рынке!'),
                                                               (3, 4, 4, 'Хороший ноутбук, но шумный под нагрузкой'),
                                                               (4, 2, 5, 'iPhone — это просто космос!'),
                                                               (5, 9, 4, 'Отличный ноутбук для работы, лёгкий и быстрый');



