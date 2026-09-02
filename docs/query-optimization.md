# Запросы и индексы

Короткий разбор того, как магазин использует PostgreSQL. Каталог, детали заказа и аналитика — прикладные функции, не отдельная лабораторная работа.

## Каталог

Фильтры, сортировка и pagination выполняются `Specification` / Criteria API и `Page`. Java не загружает всю таблицу `products`.

Индексы из `009-add-indexes.sql`:

| Индекс | Зачем |
|---|---|
| `idx_products_category_id` | фильтр по категории |
| `idx_products_brand_id` | фильтр по бренду |
| `idx_products_price` | сортировка и диапазон цены |

Поиск по названию/описанию на текущем seed идёт через `LIKE`. Отдельный GIN/`pg_trgm` индекс не добавлялся: на десятках строк он не даёт поучительного плана, а усложняет схему.

## Детали заказа без N+1

Связи `Order.items`, `OrderItem.product` и `Order.payment` по умолчанию `LAZY`. Для `GET /api/orders/{id}` repository включает `@EntityGraph` с этими путями, поэтому Hibernate строит один `SELECT` с `JOIN`, а не запрос на каждую позицию.

Проверка: `OrderDetailsFetchTest` включает Hibernate Statistics и ожидает не больше одного JDBC statement при чтении seed-заказа с двумя позициями.

## Аналитика

`GET /api/analytics/top-products` и `GET /api/analytics/sales-summary` считают `SUM` / `COUNT` / `AVG` в PostgreSQL через JPQL constructor expression. В Java не грузятся коллекции `Order` / `OrderItem` ради подсчёта.

В выручку входят только `PAID`, `PROCESSING`, `SHIPPED`, `DELIVERED`. `NEW`, `PAYMENT_*` и `CANCELLED` не считаются: оплата ещё не состоялась или отменена.

`012-index-order-items-product-id.sql` добавляет `idx_order_items_product_id`: PostgreSQL не индексирует FK автоматически, а top-products джоинит `order_items` к `products`.

Период: `from` и `to` — даты включительно (`created_at >= from 00:00` и `< to+1 день`).

## EXPLAIN ANALYZE

На учебном seed планировщик всё равно часто ходит seq scan по `order_items` (15 строк дешевле индекса). При этом фильтр заказов уже использует `idx_orders_status_created_at`, а товары — `products_pkey`. Снимок с Compose PostgreSQL после инкремента:

```text
Limit
  -> Sort (SUM(oi.line_total) DESC)
       -> GroupAggregate (p.id)
            -> Nested Loop
                 -> Nested Loop
                      -> Index Scan using idx_orders_status_created_at on orders
                         Index Cond: status IN (PAID, PROCESSING, SHIPPED, DELIVERED)
                                    AND created_at range
                      -> Seq Scan on order_items
                 -> Index Scan using products_pkey on products
```

`idx_order_items_product_id` на этом объёме не выбирается — это ожидаемо, не повод удалять индекс. Он соответствует join path аналитики и станет полезен, когда заказов будет больше, чем влезает в одну страницу таблицы.

