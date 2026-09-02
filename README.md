# JavaRush Online Shop

[![CI](https://github.com/Aberezhnoy1980/javarush-online-shop/actions/workflows/ci.yml/badge.svg?branch=dev)](https://github.com/Aberezhnoy1980/javarush-online-shop/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-JPA-59666C?logo=hibernate&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

Итоговый проект модуля 4 «Работа с БД. Hibernate» курса JavaRush.

Альтернативный (относительно базового) вариант: JPA/Hibernate используются осознанно. Обычный CRUD — через Spring Data JPA. Каталог, детали заказа и аналитика показывают Specification/Criteria, `@EntityGraph`, JPQL projections, границы транзакций и индексы PostgreSQL.

Одна команда поднимает UI, два backend-сервиса, PostgreSQL и Redis.

## Запуск

```bash
docker compose up --build
```

UI: [http://localhost](http://localhost)

Остановка и очистка volumes:

```bash
docker compose down -v --remove-orphans
```

## Сценарий проверки

Авторизации нет. Приложение всегда работает как demo user `id=1` (`ivan.petrov@example.com`).

1. Открыть [http://localhost](http://localhost) — каталог с поиском, фильтрами и сортировкой.
2. Найти MacBook, добавить в корзину, оформить заказ.
3. На странице заказа нажать **Оплатить** — статус `PAID`, платёж `COMPLETED`.
4. Нажать **Отменить оплату** — `PAYMENT_CANCELLED` / `REFUNDED`. Повторно оплатить этот заказ нельзя.
5. Открыть **Аналитика** — топ товаров и выручка по категориям (в выручку входят `PAID`, `PROCESSING`, `SHIPPED`, `DELIVERED`).
6. Swagger: [shop-service](http://localhost:8080/swagger-ui.html), [payment-service](http://localhost:8081/swagger-ui.html).

Временный отказ payment-service (заказ станет `PAYMENT_FAILED`, оплату можно повторить):

```bash
PAYMENT_FAILURE_RATE=1 docker compose up -d payment-service
```

Вернуть стабильный режим (`PAYMENT_FAILURE_RATE=0` по умолчанию):

```bash
docker compose up -d payment-service
```

## Что реализовано

* Каталог: поиск, фильтры, сортировка, pagination, Redis cache-aside.
* Корзина и заказ: snapshot позиций, уменьшение `stock_quantity` на checkout.
* Оплата и отмена через отдельный `payment-service`; HTTP к нему **вне** транзакции БД магазина.
* Аналитика: агрегаты `SUM` / `COUNT` / `AVG` в PostgreSQL, не в Java.
* Детали заказа без N+1 (`@EntityGraph`).
* Liquibase + `ddl-auto=validate`, Docker Compose, CI.

Таблица `reviews` есть в схеме и seed (средний рейтинг в аналитике). Отдельного UI отзывов нет — это сознательно вне MVP.

## Адреса

| Что | URL |
|---|---|
| UI | http://localhost |
| shop-service health | http://localhost:8080/actuator/health |
| payment-service health | http://localhost:8081/actuator/health |
| Catalog API | http://localhost:8080/api/products |
| Cart API | http://localhost:8080/api/cart |
| Orders API | http://localhost:8080/api/orders |
| Analytics API | http://localhost:8080/api/analytics/top-products |
| shop-service Swagger | http://localhost:8080/swagger-ui.html |
| payment-service Swagger | http://localhost:8081/swagger-ui.html |
| Контракт payment-service | [`api/payment-api.yaml`](api/payment-api.yaml) |
| PostgreSQL | `localhost:5432`, db/user/password: `online_shop` |
| Redis | `localhost:6379` |

Учётные данные Postgres только для локального стенда, не для production.

## Локальные тесты

Тесты `shop-service` поднимают одноразовый PostgreSQL через Testcontainers. Нужен запущенный Docker; стендовый Compose-Postgres для этого не используется.

```bash
./mvnw -B test
cd frontend && npm install && npm run build
```

На Docker Desktop 29 docker-java по умолчанию ходит в `/v1.32/info` и получает HTTP 400. В `shop-service` для тестов задан Docker API 1.44.

CI на GitHub Actions гоняет те же Maven-тесты и `npm run build`.

Frontend в режиме разработки (нужен shop-service на порту 8080):

```bash
cd frontend && npm install && npm run dev
```

Аналитика: период — query-параметры `from` и `to` (даты включительно). Как устроены запросы и индексы: [`docs/query-optimization.md`](docs/query-optimization.md).

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
├── api/
│   └── payment-api.yaml      # контракт payment-service
├── backend/
│   ├── shop-service/         # Spring MVC + JPA/Hibernate + Redis cache-aside
│   └── payment-service/      # Spring MVC, pay/cancel/failure mode
├── frontend/                 # React + Vite + TypeScript
├── db/
│   └── source/
│       └── online-shop.sql   # исходная схема, только как reference
├── docs/
│   ├── database-schema.md
│   └── query-optimization.md
├── infra/
│   └── nginx/
├── docker-compose.yml
└── pom.xml                   # Maven aggregator / parent
```
