-- liquibase formatted sql

-- changeset aberezhnoy:010-seed-catalog-and-demo-user
INSERT INTO categories (name, parent_id, description)
VALUES ('Электроника', NULL, 'Все виды электроники'),
       ('Ноутбуки', 1, 'Ноутбуки и планшеты'),
       ('Смартфоны', 1, 'Мобильные телефоны'),
       ('Аксессуары', NULL, 'Аксессуары для гаджетов'),
       ('Игровые ноутбуки', 2, 'Ноутбуки для игр'),
       ('Бизнес ноутбуки', 2, 'Ноутбуки для работы');

INSERT INTO brands (name, description)
VALUES ('Apple', 'Американская компания, производитель iPhone, MacBook'),
       ('Samsung', 'Южнокорейская компания, производитель Galaxy'),
       ('Dell', 'Американская компания, производитель ноутбуков'),
       ('Lenovo', 'Китайская компания, производитель ноутбуков'),
       ('Xiaomi', 'Китайская компания, производитель смартфонов');

INSERT INTO products (name, description, price, stock_quantity, brand_id, category_id)
VALUES ('MacBook Pro 14', 'Ноутбук Apple с чипом M3', 199990.00, 10, 1, 2),
       ('iPhone 15 Pro Max', 'Смартфон Apple', 149990.00, 25, 1, 3),
       ('Galaxy S24 Ultra', 'Смартфон Samsung', 129990.00, 30, 2, 3),
       ('Dell XPS 16', 'Ноутбук Dell для работы', 189990.00, 15, 3, 2),
       ('Lenovo Legion 7', 'Игровой ноутбук Lenovo', 174990.00, 8, 4, 5),
       ('Xiaomi 14 Ultra', 'Смартфон Xiaomi', 99990.00, 20, 5, 3),
       ('Galaxy Watch 6', 'Умные часы Samsung', 39990.00, 50, 2, 4),
       ('AirPods Pro', 'Наушники Apple', 24990.00, 40, 1, 4),
       ('MacBook Air 13', 'Лёгкий ноутбук Apple', 129990.00, 20, 1, 2),
       ('Dell Latitude 5440', 'Бизнес-ноутбук Dell', 159990.00, 12, 3, 6);

INSERT INTO users (name, email, password_hash, phone, address)
VALUES ('Иван Петров', 'ivan.petrov@example.com', 'hash123', '+7-999-111-22-33', 'г. Москва, ул. Тверская, д. 10'),
       ('Мария Сидорова', 'maria.sidorova@example.com', 'hash456', '+7-999-222-33-44', 'г. Санкт-Петербург, ул. Невская, д. 5'),
       ('Петр Иванов', 'petr.ivanov@example.com', 'hash789', '+7-999-333-44-55', 'г. Казань, ул. Кремлёвская, д. 15'),
       ('Анна Кузнецова', 'anna.kuznetsova@example.com', 'hash111', '+7-999-444-55-66', 'г. Москва, ул. Арбат, д. 20'),
       ('Дмитрий Смирнов', 'dmitry.smirnov@example.com', 'hash222', '+7-999-555-66-77', 'г. Новосибирск, ул. Ленина, д. 30');
