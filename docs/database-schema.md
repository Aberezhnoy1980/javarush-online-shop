# Схема базы данных

Исполняемая схема создаётся Liquibase в `shop-service`. Исходный учебный SQL сохранён без изменений как reference:

```text
db/source/online-shop.sql
```

Источник: [ArtemAlt/JRU-2025-11-05-Module-3](https://github.com/ArtemAlt/JRU-2025-11-05-Module-3), ветка `Module_4_Lesson_18`.

Дефекты исходного скрипта и принятые исправления зафиксированы в issue [#1](https://github.com/Aberezhnoy1980/javarush-online-shop/issues/1).

## Таблицы

`users`, `categories`, `brands`, `products`, `carts`, `cart_items`, `orders`, `order_items`, `payments`, `reviews`.

Hibernate проверяет соответствие mapping через `spring.jpa.hibernate.ddl-auto=validate`. Liquibase — единственный способ менять схему.

## Отличия от исходного SQL

- Единые имена: `brands`, `carts`, `cart_items`, колонка `cart_id`.
- Тип `integer` вместо опечатки `integert`.
- `orders.total_amount > 0` сохранён; у seed-заказа `NEW` сумма `249970.00`.
- `payments.transaction_id` допускает `NULL` в статусе `PENDING`.
- В `order_items` добавлены snapshot-поля `product_name_snapshot` и `line_total`.
- В статусы заказа добавлены `PAYMENT_PENDING`, `PAYMENT_FAILED`, `PAYMENT_CANCELLED`.

## Demo user

```text
id    = 1
email = ivan.petrov@example.com
```

У пользователя есть корзина. Авторизация в приложении не реализуется.
