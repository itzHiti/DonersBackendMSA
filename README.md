# 🥙 Doner na Abaya - Microservices Backend

![Architecture](https://img.shields.io/badge/Architecture-Microservices-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green)
![Java](https://img.shields.io/badge/Java-21-orange)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.7.7-black)
![Keycloak](https://img.shields.io/badge/Keycloak-23.0-red)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![Tests](https://img.shields.io/badge/Tests-92%20passed-success)

## 📋 Описание

**Doner na Abaya** — микросервисное backend-приложение для сети ресторанов доставки дёнер-кебаба, созданное по аналогии с архитектурой "Додо Пицца". 

Проект демонстрирует современные практики разработки enterprise-уровня:
- ✅ Микросервисная архитектура с асинхронной коммуникацией
- ✅ Event-driven подход через Apache Kafka
- ✅ OAuth2/JWT аутентификация через Keycloak
- ✅ Система лояльности (дкоины - 5% кэшбэк с каждого заказа)
- ✅ Полное покрытие тестами (92 теста: unit, integration, Kafka)
- ✅ RESTful API с документацией Swagger/OpenAPI
- ✅ Database migration с Flyway
- ✅ Контейнеризация с Docker Compose

## 🏗️ Архитектура

### Микросервисы

```
┌─────────────────────────────────────────────────────────────────┐
│                         API Gateway / Client                     │
└─────────────────────────────────────────────────────────────────┘
                    │                           │
          ┌─────────▼────────┐       ┌─────────▼──────────┐
          │  Order Service   │       │ Delivery Service   │
          │    (port 8081)   │       │    (port 8082)     │
          └─────────┬────────┘       └─────────┬──────────┘
                    │                           │
          ┌─────────▼────────┐       ┌─────────▼──────────┐
          │   PostgreSQL     │       │   PostgreSQL       │
          │    order_db      │       │   delivery_db      │
          └──────────────────┘       └────────────────────┘
                    │                           │
                    └───────────┬───────────────┘
                                │
                    ┌───────────▼────────────┐
                    │    Apache Kafka        │
                    │  (Event Streaming)     │
                    └────────────────────────┘
                                │
                    ┌───────────▼────────────┐
                    │      Keycloak          │
                    │   (Authentication)     │
                    └────────────────────────┘
```

### 1. **Order Service** (`:8081`)

**Ответственность:**
- Управление каталогом продуктов (CRUD)
- Обработка заказов клиентов
- Система лояльности (дкоины - донер-коины)
- Публикация событий в Kafka

**База данных:** `order_db`
- `products` - Продукты (дёнеры, напитки, десерты, соусы)
- `orders` - Заказы клиентов
- `order_items` - Позиции заказов (many-to-many)
- `coin_balances` - Балансы дкоинов пользователей
- `coin_transactions` - История транзакций дкоинов

**Kafka Topics:**
- `order-created` ➡️ Публикует при создании заказа
- `delivery-completed` ⬅️ Подписан на завершение доставки

### 2. **Delivery Service** (`:8082`)

**Ответственность:**
- Управление курьерами
- Обработка доставок
- Назначение курьеров на доставки
- Отслеживание статусов доставки

**База данных:** `delivery_db`
- `couriers` - Курьеры и их статусы
- `deliveries` - Записи о доставках
- `delivery_zones` - Зоны доставки (опционально)

**Kafka Topics:**
- `order-created` ⬅️ Подписан на новые заказы
- `delivery-completed` ➡️ Публикует при завершении доставки

### Технологический стек

| Компонент | Технология | Вер��ия |
|-----------|-----------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.4.3 |
| ORM | Spring Data JPA | 3.4.3 |
| Security | Spring Security + OAuth2 | 6.4.3 |
| Database | PostgreSQL | 15-alpine |
| Migration | Flyway | 10.20.1 |
| Message Broker | Apache Kafka | 7.7.7 |
| Coordination | Apache Zookeeper | 7.7.7 |
| Identity Provider | Keycloak | 23.0 |
| API Documentation | SpringDoc OpenAPI | 2.7.0 |
| Build Tool | Gradle | 8.14.3 |
| Containerization | Docker Compose | 3.8 |
| Testing | JUnit 5 + Mockito | - |

## 🚀 Быстрый старт

### Предварительные требования

- **Java 21+** (JDK)
- **Docker Desktop** (для Windows)
- **Gradle 8.x** (или используйте включенный `gradlew`)
- **Git**
- **Минимум 4 GB RAM** для Docker контейнеров

### Шаг 1: Клонирование репозитория

```powershell
git clone https://github.com/yourusername/DonersBackendMSA.git
cd DonersBackendMSA
```

### Шаг 2: Запуск инфраструктуры через Docker Compose

```powershell
docker-compose up -d
```

Это запустит следующие контейнеры:
- ✅ `order-db` - PostgreSQL для Order Service (:5432)
- ✅ `delivery-db` - PostgreSQL для Delivery Service (:5433)
- ✅ `keycloak-db` - PostgreSQL для Keycloak
- ✅ `zookeeper` - Координатор для Kafka (:2181)
- ✅ `kafka` - Message broker (:9092)
- ✅ `keycloak` - Identity Provider (:8080)
- ✅ `order-service` - Микросервис заказов (:8081)
- ✅ `delivery-service` - Микросервис доставки (:8082)

**Проверка статуса:**
```powershell
docker ps
```

Все 8 контейнеров должны быть в статусе `Up`.

### Шаг 3: Настройка Keycloak

#### 3.1. Вход в Keycloak Admin Console

Откройте: http://localhost:8080

**Credentials:**
- Username: `admin`
- Password: `admin`

#### 3.2. Создание Realm

1. Нажмите на dropdown "master" (верхний левый угол)
2. Нажмите "Create Realm"
3. Заполните:
   - **Realm name:** `doner-realm`
   - **Enabled:** ON
4. Нажмите "Create"

#### 3.3. Создание ролей

1. Перейдите в **Realm Roles**
2. Нажмите "Create Role" и создайте следующие роли:
   - `USER` - для обычных пользователей
   - `ADMIN` - для администраторов
   - `COURIER` - для курьеров (опционально)

#### 3.4. Создание клиента

1. Перейдите в **Clients** → **Create Client**
2. Заполните:
   - **Client ID:** `doner-client`
   - **Client Protocol:** `openid-connect`
   - **Root URL:** `http://localhost:8081`
3. Нажмите "Next"
4. Включите:
   - ✅ Client authentication: **ON**
   - ✅ Authorization: **ON**
   - ✅ Standard flow
   - ✅ Direct access grants
5. Нажмите "Next", затем "Save"
6. Во вкладке **Credentials** скопируйте **Client Secret**

**Important:** В настройках клиента:
- **Valid Redirect URIs:** `*` (для разработки)
- **Web Origins:** `*` (для разработки)

#### 3.5. Создание тестовых пользователей

**Администратор:**
1. **Users** → **Add User**
2. Заполните:
   - Username: `testadmin`
   - Email: `admin@doner.kz`
   - First Name: `Admin`
   - Last Name: `User`
   - Email Verified: **ON**
3. Сохраните
4. Вкладка **Credentials**:
   - Set Password: `admin123`
   - Temporary: **OFF**
5. Вкладка **Role Mappings**:
   - Assign Roles: `ADMIN`, `USER`

**Обычный пользователь:**
1. Username: `testuser1`
2. Email: `user@example.com`
3. Password: `Test@12345` (Temporary: OFF)
4. Roles: `USER`

### Шаг 4: Проверка работоспособности

#### 4.1. Health Checks

```powershell
# Order Service
curl http://localhost:8081/actuator/health

# Delivery Service  
curl http://localhost:8082/actuator/health
```

Ожидаемый ответ: `{"status":"UP"}`

#### 4.2. Swagger UI

- **Order Service:** http://localhost:8081/swagger-ui.html
- **Delivery Service:** http://localhost:8082/swagger-ui.html

#### 4.3. Получение JWT токена

```powershell
curl -X POST "http://localhost:8080/realms/doner-realm/protocol/openid-connect/token" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "client_id=doner-client" `
  -d "client_secret=YOUR_CLIENT_SECRET" `
  -d "grant_type=password" `
  -d "username=testuser1" `
  -d "password=Test@12345"
```

Сохраните `access_token` из ответа.

#### 4.4. Тестовый запрос

```powershell
# Получить список продуктов (публичный endpoint)
curl http://localhost:8081/api/products

# Получить мои заказы (требуется аутентификация)
curl http://localhost:8081/api/orders/my-orders `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Шаг 5: Локальная разработка (опционально)

Если хотите запускать сервисы локально (не в Docker):

```powershell
# Остановите сервисы в Docker, оставив только инфраструктуру
docker-compose stop order-service delivery-service

# Запустите Order Service локально
cd order-service
.\gradlew.bat bootRun

# В другом терминале - Delivery Service
cd delivery-service
.\gradlew.bat bootRun
```

**Важно:** Измените в `application.properties`:
```properties
# Вместо kafka:9092
spring.kafka.bootstrap-servers=localhost:9092
# Вместо order-db:5432
spring.datasource.url=jdbc:postgresql://localhost:5432/order_db
```

## 📖 API Документация

### Order Service API

#### 🔓 Публичные эндпоинты (без авторизации)

| Method | Endpoint | Описание |
|--------|----------|----------|
| GET | `/api/products` | Список всех продуктов |
| GET | `/api/products/available` | Только доступные продукты |
| GET | `/api/products/category/{category}` | Продукты по категории |
| GET | `/api/products/{id}` | Продукт по ID |

**Категории продуктов:**
- `DONER` - Дёнеры
- `BEVERAGE` - Напитки
- `DESSERT` - Десерты
- `SAUCE` - Соусы

#### 🔐 Пользовательские эндпоинты (ROLE_USER)

| Method | Endpoint | Описание |
|--------|----------|----------|
| POST | `/api/orders` | Создать новый заказ |
| GET | `/api/orders/my-orders` | Мои заказы |
| GET | `/api/orders/{id}` | Заказ по ID (только свой) |
| DELETE | `/api/orders/{id}` | Отменить заказ |
| GET | `/api/coins/balance` | Мой баланс дкоинов |
| GET | `/api/coins/transactions` | История транзакций |

#### 👑 Административные эндпоинты (ROLE_ADMIN)

| Method | Endpoint | Описание |
|--------|----------|----------|
| POST | `/api/products` | Добавить продукт |
| PUT | `/api/products/{id}` | Обновить продукт |
| DELETE | `/api/products/{id}` | Удалить продукт |
| PATCH | `/api/products/{id}/availability` | Изменить доступность |
| GET | `/api/orders` | Все заказы |
| PUT | `/api/orders/{id}/status` | Обновить статус заказа |
| GET | `/api/coins/user/{userId}/balance` | Баланс пользователя |

### Delivery Service API

#### 🔐 Пользовательские эндпоинты (ROLE_USER)

| Method | Endpoint | Описание |
|--------|----------|----------|
| GET | `/api/deliveries/order/{orderId}` | Доставка по ID заказа |

#### 👷 Курьерские эндпоинты (ROLE_COURIER)

| Method | Endpoint | Описание |
|--------|----------|----------|
| GET | `/api/deliveries/courier/my` | Мои доставки |
| PUT | `/api/deliveries/{id}/status` | Обновить статус доставки |
| POST | `/api/deliveries/{id}/pickup` | Забрал заказ |
| POST | `/api/deliveries/{id}/complete` | Доставил заказ |

#### 👑 Административные эндпоинты (ROLE_ADMIN)

| Method | Endpoint | Описание |
|--------|----------|----------|
| GET | `/api/deliveries` | Все доставки |
| GET | `/api/deliveries/{id}` | Доставка по ID |
| GET | `/api/deliveries/status/{status}` | Доставки по статусу |
| POST | `/api/deliveries/{deliveryId}/assign/{courierId}` | Назначить курьера |
| DELETE | `/api/deliveries/{id}/cancel` | Отменить доставку |
| GET | `/api/couriers` | Все курьеры |
| GET | `/api/couriers/available` | Доступные курьеры |
| POST | `/api/couriers` | Добавить курьера |
| PUT | `/api/couriers/{id}` | Обновить курьера |
| PATCH | `/api/couriers/{id}/status` | Изменить статус курьера |
| DELETE | `/api/couriers/{id}` | Удалить курьера |

### Статусы

#### Order Status
- `PENDING` - Ожидает обработки
- `CONFIRMED` - Подтвержден
- `PREPARING` - Готовится
- `READY` - Готов к выдаче
- `IN_DELIVERY` - В доставке
- `DELIVERED` - Доставлен ✅ (начисляются дкоины)
- `CANCELLED` - Отменен

#### Delivery Status
- `PENDING` - Ожидает назначения курьера
- `ASSIGNED` - Курьер назначен
- `PICKED_UP` - Забрано из ресторана
- `IN_TRANSIT` - В пути
- `DELIVERED` - Доставлено
- `CANCELLED` - Отменено

#### Courier Status
- `AVAILABLE` - Доступен
- `BUSY` - Занят
- `OFFLINE` - Не в сети

### 💰 Система дкоинов (Донер-коины)

**Правила начисления:**
- 💵 **5% от суммы заказа** при статусе `DELIVERED`
- 💳 1 дкоин = 1 тенге
- ✅ Автоматическое начисление после доставки

**Пример:**
- Заказ на 10,000 ₸ → 500 дкоинов
- Заказ на 5,500 ₸ → 275 дкоинов

**API для дкоинов:**
```http
# Получить баланс
GET /api/coins/balance
Authorization: Bearer {token}

# История транзакций
GET /api/coins/transactions
Authorization: Bearer {token}

# Использовать дкоины при оплате (будущая фича)
POST /api/orders
{
  "useCoins": 500,
  "items": [...]
}
```

## 🔐 Аутентификация и авторизация

### Получение JWT токена

#### Способ 1: Direct Access Grant (для тестирования)

```powershell
curl -X POST "http://localhost:8080/realms/doner-realm/protocol/openid-connect/token" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "client_id=doner-client" `
  -d "client_secret=YOUR_CLIENT_SECRET" `
  -d "grant_type=password" `
  -d "username=testuser1" `
  -d "password=Test@12345"
```

**Ответ:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "token_type": "Bearer",
  "scope": "profile email"
}
```

#### Способ 2: Authorization Code Flow (production)

Для фронтенда используйте стандартный OAuth2 Authorization Code Flow:

1. Redirect на:
```
http://localhost:8080/realms/doner-realm/protocol/openid-connect/auth
  ?client_id=doner-client
  &redirect_uri=YOUR_CALLBACK_URL
  &response_type=code
  &scope=openid
```

2. Обменять `code` на токен

### Использование токена

Все защищенные endpoints требуют Bearer токен:

```powershell
curl http://localhost:8081/api/orders/my-orders `
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Роли и права доступа

| Роль | Доступ |
|------|--------|
| **USER** | Просмотр продуктов, создание заказов, просмотр своих заказов, управление дкоинами |
| **ADMIN** | Полный доступ к управлению продуктами, заказами, курьерами, доставками |
| **COURIER** | Просмотр назначенных доставок, обновление статусов доставки |

### Структура JWT токена

```json
{
  "exp": 1766435650,
  "iat": 1766435350,
  "iss": "http://localhost:8080/realms/doner-realm",
  "sub": "ea30dcc0-38be-4bd8-be3d-29d3840a7fd4",
  "preferred_username": "testuser1",
  "realm_access": {
    "roles": ["USER"]
  },
  "email": "user@example.com",
  "email_verified": false
}
```

## 📊 Kafka Events & Event Flow

### Topics

| Topic | Producer | Consumer | Описание |
|-------|----------|----------|----------|
| `order-created` | Order Service | Delivery Service | Создан новый заказ |
| `order-status-changed` | Order Service | - | Изменен статус заказа |
| `delivery-completed` | Delivery Service | Order Service | Доставка завершена |

### Event Schemas

#### OrderCreatedEvent
```json
{
  "orderId": 123,
  "customerId": "ea30dcc0-38be-4bd8-be3d-29d3840a7fd4",
  "deliveryAddress": "ул. Абая 150, кв. 25",
  "phone": "+77011234567",
  "totalPrice": 5500.00,
  "createdAt": "2025-12-29T10:30:00"
}
```

#### DeliveryCompletedEvent
```json
{
  "deliveryId": 456,
  "orderId": 123,
  "courierId": 5,
  "completedAt": "2025-12-29T11:15:00"
}
```

### Complete Event Flow

```
┌──────────────────────────────────────────────────────────────────┐
│ 1. Клиент создает заказ                                          │
│    POST /api/orders                                               │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│ 2. Order Service:                                                 │
│    - Сохраняет заказ (status: PENDING)                           │
│    - Публикует OrderCreatedEvent → Kafka                         │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│ 3. Delivery Service:                                              │
│    - Получает OrderCreatedEvent ← Kafka                          │
│    - Создает запись Delivery (status: PENDING)                   │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│ 4. Админ назначает курьера                                       │
│    POST /api/deliveries/{id}/assign/{courierId}                  │
│    - Delivery status: PENDING → ASSIGNED                         │
│    - Courier status: AVAILABLE → BUSY                            │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│ 5. Курьер забирает заказ                                         │
│    PUT /api/deliveries/{id}/status                               │
│    - Delivery status: ASSIGNED → PICKED_UP → IN_TRANSIT         │
│    - Order status: PENDING → IN_DELIVERY                         │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│ 6. Курьер завершает доставку                                     │
│    PUT /api/deliveries/{id}/status (DELIVERED)                   │
│    - Delivery status: IN_TRANSIT → DELIVERED                     │
│    - Публикует DeliveryCompletedEvent → Kafka                    │
└─────────────────────────┬────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│ 7. Order Service:                                                 │
│    - Получает DeliveryCompletedEvent ← Kafka                     │
│    - Order status: IN_DELIVERY → DELIVERED                       │
│    - 💰 Начисляет 5% дкоинов клиенту                             │
│    - Courier status: BUSY → AVAILABLE                            │
└──────────────────────────────────────────────────────────────────┘
```

## 🗄️ База данных

### Order Service Database (`order_db`)

**Таблицы:**

1. **products**
   - `id` (BIGSERIAL) - PK
   - `name` (VARCHAR) - Название
   - `description` (TEXT) - Описание
   - `price` (DECIMAL) - Цена
   - `category` (VARCHAR) - Категория
   - `available` (BOOLEAN) - Доступность
   - `created_at`, `updated_at` - Timestamps

2. **orders**
   - `id` (BIGSERIAL) - PK
   - `customer_id` (VARCHAR) - ID клиента из Keycloak
   - `status` (VARCHAR) - Статус заказа
   - `delivery_address` (VARCHAR) - Адрес доставки
   - `phone` (VARCHAR) - Телефон
   - `notes` (TEXT) - Примечания
   - `total_price` (DECIMAL) - Общая сумма
   - `created_at`, `delivered_at` - Timestamps

3. **order_items**
   - `id` (BIGSERIAL) - PK
   - `order_id` (BIGINT) - FK → orders
   - `product_id` (BIGINT) - FK → products
   - `quantity` (INT) - Количество
   - `price` (DECIMAL) - Цена за единицу

4. **coin_balances** 💰
   - `id` (BIGSERIAL) - PK
   - `customer_id` (VARCHAR) - ID клиента
   - `balance` (DECIMAL) - Текущий баланс дкоинов
   - `updated_at` - Timestamp

5. **coin_transactions** 📊
   - `id` (BIGSERIAL) - PK
   - `customer_id` (VARCHAR) - ID клиента
   - `order_id` (BIGINT) - FK → orders
   - `amount` (DECIMAL) - Сумма транзакции
   - `type` (VARCHAR) - EARNED / SPENT
   - `description` (TEXT) - Описание
   - `created_at` - Timestamp

### Delivery Service Database (`delivery_db`)

**Таблицы:**

1. **couriers**
   - `id` (BIGSERIAL) - PK
   - `name` (VARCHAR) - ФИО
   - `phone` (VARCHAR) - Телефон
   - `vehicle_type` (VARCHAR) - Тип транспорта
   - `status` (VARCHAR) - Статус
   - `active_deliveries` (INT) - Активных доставок
   - `total_deliveries` (INT) - Всего доставок
   - `created_at` - Timestamp

2. **deliveries**
   - `id` (BIGSERIAL) - PK
   - `order_id` (BIGINT) - ID заказа из Order Service
   - `courier_id` (BIGINT) - FK → couriers
   - `status` (VARCHAR) - Статус доставки
   - `delivery_address` (VARCHAR) - Адрес
   - `recipient_phone` (VARCHAR) - Телефон получателя
   - `notes` (TEXT) - Примечания
   - `created_at`, `assigned_at`, `picked_up_at`, `delivered_at` - Timestamps

3. **delivery_zones** (опционально)
   - Зоны доставки с полигонами

### Миграции Flyway

**Order Service:**
- `V1__initial_schema.sql` - Основные таблицы
- `V2__insert_sample_products.sql` - Тестовые продукты
- `V3__add_user_coins_system.sql` - Система дкоинов
- `V4__fix_coin_tables_id_type.sql` - Фикс типов ID

**Delivery Service:**
- `V1__initial_schema.sql` - Основные таблицы
- `V2__insert_sample_data.sql` - Тестовые данные

## 📝 Примеры запросов

### 1. Получить JWT токен

```powershell
$response = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/realms/doner-realm/protocol/openid-connect/token" `
  -ContentType "application/x-www-form-urlencoded" `
  -Body @{
    client_id = "doner-client"
    client_secret = "YOUR_CLIENT_SECRET"
    grant_type = "password"
    username = "testuser1"
    password = "Test@12345"
  }

$token = $response.access_token
```

### 2. Получить список продуктов

```powershell
curl http://localhost:8081/api/products
```

**Ответ:**
```json
[
  {
    "id": 1,
    "name": "Классический донер с курицей",
    "description": "Сочный донер с куриным мясом",
    "price": 1500.00,
    "category": "DONER",
    "available": true
  },
  {
    "id": 9,
    "name": "Coca-Cola 0.5л",
    "price": 300.00,
    "category": "BEVERAGE",
    "available": true
  }
]
```

### 3. Создать заказ

```powershell
curl -X POST http://localhost:8081/api/orders `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "deliveryAddress": "ул. Сатпаева 101, кв. 15",
    "phone": "+77771234567",
    "notes": "Домофон 15, 3 этаж",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      },
      {
        "productId": 9,
        "quantity": 1
      }
    ]
  }'
```

**Ответ:**
```json
{
  "id": 42,
  "customerId": "ea30dcc0-38be-4bd8-be3d-29d3840a7fd4",
  "status": "PENDING",
  "deliveryAddress": "ул. Сатпаева 101, кв. 15",
  "phone": "+77771234567",
  "notes": "Домофон 15, 3 этаж",
  "totalPrice": 3300.00,
  "createdAt": "2025-12-29T10:30:00",
  "items": [
    {
      "productId": 1,
      "productName": "Классический донер с курицей",
      "quantity": 2,
      "price": 1500.00
    },
    {
      "productId": 9,
      "productName": "Coca-Cola 0.5л",
      "quantity": 1,
      "price": 300.00
    }
  ]
}
```

### 4. Проверить баланс дкоинов

```powershell
curl http://localhost:8081/api/coins/balance `
  -H "Authorization: Bearer $token"
```

**Ответ:**
```json
{
  "customerId": "ea30dcc0-38be-4bd8-be3d-29d3840a7fd4",
  "balance": 1250.50,
  "updatedAt": "2025-12-29T10:35:00"
}
```

### 5. История транзакций дкоинов

```powershell
curl http://localhost:8081/api/coins/transactions `
  -H "Authorization: Bearer $token"
```

**Ответ:**
```json
[
  {
    "id": 15,
    "amount": 165.00,
    "type": "EARNED",
    "description": "Начислено за заказ #42",
    "orderId": 42,
    "createdAt": "2025-12-29T11:15:00"
  },
  {
    "id": 14,
    "amount": 275.00,
    "type": "EARNED",
    "description": "Начислено за заказ #38",
    "orderId": 38,
    "createdAt": "2025-12-28T19:20:00"
  }
]
```

### 6. Создать продукт (ADMIN)

```powershell
curl -X POST http://localhost:8081/api/products `
  -H "Authorization: Bearer $adminToken" `
  -H "Content-Type: application/json" `
  -d '{
    "name": "Острый донер с говядиной",
    "description": "Пикантный донер для любителей остренького",
    "price": 1800.00,
    "category": "DONER",
    "available": true
  }'
```

### 7. Назначить курьера на доставку (ADMIN)

```powershell
curl -X POST http://localhost:8082/api/deliveries/1/assign/3 `
  -H "Authorization: Bearer $adminToken"
```

### 8. Обновить статус доставки (COURIER)

```powershell
curl -X PUT http://localhost:8082/api/deliveries/1/status `
  -H "Authorization: Bearer $courierToken" `
  -H "Content-Type: application/json" `
  -d '{
    "status": "DELIVERED",
    "notes": "Доставлено успешно"
  }'
```
