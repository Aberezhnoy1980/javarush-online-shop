# JavaRush Online Shop

Итоговый проект модуля 4 «Работа с БД. Hibernate» курса JavaRush.

Альтернативный (относительно базового) проект, в котором JPA/Hibernate используются осознанно: обычный CRUD — через Spring Data JPA, а несколько выбранных прикладных функций демонстрируют HQL/JPQL, DTO projections, join fetch, правильные границы транзакций, индексы и проверку плана запроса в PostgreSQL.

## Текущее состояние

Foundation: два backend-сервиса с health-проверками, React-скелет, PostgreSQL и Redis поднимаются одной командой Docker Compose. Каталог, корзина, заказы и оплата ещё не реализованы.

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

Локальная сборка backend без Docker:

```bash
./mvnw -B test
```

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
| shop-service API (позже) | http://localhost:8080/api |
| PostgreSQL | `localhost:5432`, db/user/password: `online_shop` |
| Redis | `localhost:6379` |

Учётные данные Postgres только для локального стенда, не для production.

## Demo user

Авторизация не реализуется. Приложение будет работать с предзаполненным пользователем учебного seed:

```text
userId = 1
email  = ivan.petrov@example.com
```

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

## Структура

```text
online-shop/
├── backend/
│   ├── shop-service/         # Spring MVC + JPA/Hibernate + Redis cache-aside
│   └── payment-service/      # Spring MVC, pay/cancel/failure mode
├── frontend/                 # React + Vite + TypeScript
├── infra/
│   └── nginx/
├── docker-compose.yml
└── pom.xml                   # Maven aggregator / parent
```
