# JavaRush Online Shop

Итоговый проект модуля 4 «Работа с БД. Hibernate» курса JavaRush.

Альтернативный (относительно базового) проект, в котором JPA/Hibernate используются осознанно: обычный CRUD — через Spring Data JPA, а несколько выбранных прикладных функций демонстрируют HQL/JPQL, DTO projections, join fetch, правильные границы транзакций, индексы и проверку плана запроса в PostgreSQL.

## Текущее состояние

Foundation + database baseline + catalog + cart/orders: demo user, корзина, оформление заказа со snapshot позиций. Оплата ещё не реализована.

## Цель / MVP

* Каталог: фильтр, поиск, сортировка, страницы, Redis cache-aside.
* Demo user, корзина, заказы, отзывы — все сущности исходной схемы сохраняются.
* Payment-service: pay, cancel, controlled 503.
* UI: каталог → корзина → заказ → оплата/отмена → история заказов.
* Analytics: хотя бы top products и revenue by category.
* Postgres + Redis + два сервиса + frontend через Docker Compose.
* Swagger, README, Liquibase, smoke tests.

## Запуск

Из корня репозитория:

```bash
docker compose up --build
```

Остановка и очистка volumes:

```bash
docker compose down -v --remove-orphans
```

Локальная сборка backend. Тесты `shop-service` поднимают одноразовый PostgreSQL через Testcontainers — нужен запущенный Docker, стендовый Compose-Postgres для этого не используется:

```bash
./mvnw -B test
```

На Docker Desktop 29 docker-java по умолчанию ходит в `/v1.32/info` и получает HTTP 400. В `shop-service` для тестов задан Docker API 1.44.

CI на GitHub Actions гоняет те же Maven-тесты (Testcontainers на ubuntu-latest) и `npm run build` для frontend.

Frontend в режиме разработки (нужен запущенный shop-service на порту 8080):

```bash
cd frontend && npm install && npm run dev
```

## Адреса

| Что | URL |
|---|---|
| UI | http://localhost |
| shop-service health | http://localhost:8080/actuator/health |
| payment-service health | http://localhost:8081/actuator/health |
| Catalog API | http://localhost:8080/api/products |
| Cart API | http://localhost:8080/api/cart |
| Orders API | http://localhost:8080/api/orders |
| PostgreSQL | `localhost:5432`, db/user/password: `online_shop` |
| Redis | `localhost:6379` |

Учётные данные Postgres только для локального стенда, не для production.

## Demo user

Авторизация не реализуется. Приложение работает с предзаполненным пользователем учебного seed:

```text
userId = 1
email  = ivan.petrov@example.com
```

## База данных

За основу взята SQL-схема интернет-магазина из учебного репозитория JavaRush:

- репозиторий: [ArtemAlt/JRU-2025-11-05-Module-3](https://github.com/ArtemAlt/JRU-2025-11-05-Module-3)
- ветка: `Module_4_Lesson_18`
- файл: [`online-shop.sql`](https://github.com/ArtemAlt/JRU-2025-11-05-Module-3/blob/Module_4_Lesson_18/online-shop.sql)

В исходном скрипте были несоответствия между DDL и тестовыми данными: разные имена таблиц и внешних ключей, опечатка в SQL-типе, данные, нарушающие constraints.

Исправления задокументированы в issue [#1](https://github.com/Aberezhnoy1980/javarush-online-shop/issues/1) и применены в Liquibase. Исходный файл хранится как reference в `db/source/online-shop.sql`. Подробности — в [`docs/database-schema.md`](docs/database-schema.md).

## Стек

| Слой        | Решение                                                                          |
| ----------- | -------------------------------------------------------------------------------- |
| Java        | JDK 21                                                                           |
| Backend     | Spring Boot 3, Servlet MVC                                                       |
| Persistence | Spring Data JPA + Hibernate                                                      |
| БД          | PostgreSQL 16                                                                    |
| Миграции    | Liquibase                                                                        |
| Кэш         | Redis 7, cache-aside для каталога                                                |
| HTTP        | RestClient между shop-service и payment-service                                  |
| API         | OpenAPI / Swagger UI                                                             |
| UI          | React + Vite + TypeScript                                                        |
| Runtime     | Docker Compose + Nginx для frontend                                              |
| Тесты       | JUnit 5, Mockito, Spring Boot Test, Testcontainers PostgreSQL                    |

## JPA-инструменты

| Сценарий                                | Инструмент                            | Причина                                                |
| --------------------------------------- | ------------------------------------- | ------------------------------------------------------ |
| Простой CRUD по товару, корзине, отзыву | JpaRepository                         | Меньше кода, читаемо, достаточно                       |
| Фильтры каталога                        | Specification / Criteria API          | Динамический набор фильтров                            |
| Детали заказа                           | JOIN FETCH или @EntityGraph           | Предотвращение N+1 ровно в одном use case              |
| Аналитика                               | JPQL GROUP BY + DTO projection        | Агрегаты выполняются в БД, без загрузки всех заказов   |
| Сложная Postgres-specific задача        | Native SQL — только при необходимости | Не имитируем сложность, используем по реальной причине |

## Структура

```text
online-shop/
├── backend/
│   ├── shop-service/         # Spring MVC + JPA/Hibernate + Redis cache-aside
│   └── payment-service/      # Spring MVC, pay/cancel/failure mode
├── frontend/                 # React + Vite + TypeScript
├── db/
│   └── source/
│       └── online-shop.sql   # исходная схема, только как reference
├── docs/
│   └── database-schema.md
├── infra/
│   └── nginx/
├── docker-compose.yml
└── pom.xml                   # Maven aggregator / parent
```
