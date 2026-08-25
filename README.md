# JavaRush Online Shop

Итоговый проект модуля 4 «Работа с БД. Hibernate» курса JavaRush.

Альтернативный (относительно базового) проект, в котором JPA/Hibernate используются осознанно: обычный CRUD — через Spring Data JPA, а несколько выбранных прикладных функций демонстрируют HQL/JPQL, DTO projections, join fetch, правильные границы транзакций, индексы и проверку плана запроса в PostgreSQL.

## MVP

* Каталог: фильтр, поиск, сортировка, страницы, Redis cache-aside.
* Demo user, корзина, заказы, отзывы — все сущности исходной схемы сохраняются.
* Payment-service: pay, cancel, controlled 503.
* UI: каталог → корзина → заказ → оплата/отмена → история заказов.
* Analytics: хотя бы top products и revenue by category.
* Postgres + Redis + два сервиса + frontend через Docker Compose.
* Swagger, README, Liquibase, smoke tests.

## Запуск

В разработке.

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
| Тесты       | JUnit 5, Mockito, Spring Boot Test; Testcontainers — только если останется время |

## JPA-инструменты

| Сценарий                                | Инструмент                            | Причина                                                |
| --------------------------------------- | ------------------------------------- | ------------------------------------------------------ |
| Простой CRUD по товару, корзине, отзыву | JpaRepository                         | Меньше кода, читаемо, достаточно                       |
| Фильтры каталога                        | Specification / Criteria API          | Динамический набор фильтров                            |
| Детали заказа                           | JOIN FETCH или @EntityGraph           | Предотвращение N+1 ровно в одном use case              |
| Аналитика                               | JPQL GROUP BY + DTO projection        | Агрегаты выполняются в БД, без загрузки всех заказов   |
| Сложная Postgres-specific задача        | Native SQL — только при необходимости | Не имитируем сложность, используем по реальной причине |

## Примерная структура

online-shop/
├── backend/
│   ├── pom.xml
│   ├── shop-service/         # Spring MVC + JPA/Hibernate + Redis cache-aside
│   ├── payment-service/      # Spring MVC, pay/cancel/failure mode
├── frontend/                 # React/Vite
├── api/
│   └── payment-api.yaml
├── db/
│   └── source/
│       └── online-shop.sql   # исходная схема, только как reference
├── docs/
│   ├── architecture.md
│   ├── demo-scenarios.md
│   └── database-schema.md
├── infra/
│   └── nginx/
├── docker-compose.yml
└── README.md