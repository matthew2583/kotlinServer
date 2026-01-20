# Sand & Gravel Mix Shipment Management API

REST API для управления отгрузками песчано-гравийной смеси. Написан на Kotlin с использованием http4k.

## О проекте

Это backend-приложение для компании, занимающейся продажей строительных материалов: песка, щебня и ПГС. Система ведёт учёт отгрузок, самосвалов и сотрудников. Данные хранятся в CSV-файлах и загружаются при старте сервера.

Что умеет:
- CRUD для отгрузок
- Фильтрация по типу материала и периоду
- Месячные отчёты по продажам
- Регистрация накладных с автоматическим созданием самосвала
- JWT-авторизация с разграничением прав

## Стек

- Kotlin 2.2
- http4k (web framework)
- Netty (server)
- Jackson (JSON)
- java-jwt (авторизация)
- kotlin-csv (парсинг данных)
- JCommander (CLI)
- Gradle + ktlint

## Структура проекта

```
src/main/kotlin/ru/yarsu/
├── application/     — запуск сервера, shutdown hook
├── auth/            — работа с JWT токенами
├── cli/             — интерфейс командной строки
│   ├── args/        — аргументы CLI
│   └── commands/    — команды (list, report и т.д.)
├── csv/             — парсинг и запись CSV
├── data/            — модели данных
├── domain/          — бизнес-логика (роли, типы ПГС)
├── storage/         — хранилища (in-memory)
└── web/routes/      — HTTP-обработчики
    ├── dto/         — DTO для запросов
    ├── filter/      — фильтры авторизации
    ├── handlers/    — обработчики эндпоинтов
    ├── lens/        — валидация через http4k lens
    └── util/        — утилиты (пагинация, сортировка)
```

## Запуск

Нужен JDK 17 или выше.

Сборка:
```bash
./gradlew build
```

Запуск сервера:
```bash
./gradlew run --args="--swg-file=sample-data/swg.csv --dump-trucks-file=sample-data/trucks.csv --employees-file=sample-data/employees.csv --port=9000 --secret=my-secret-key"
```

После запуска API доступен на `http://localhost:9000`

## CLI режим

Приложение можно использовать и без сервера — просто для вывода данных в консоль:

```bash
# Вывести все отгрузки
./gradlew run --args="list --swg-file=sample-data/swg.csv --dump-trucks-file=sample-data/trucks.csv --employees-file=sample-data/employees.csv"

# Отгрузка по ID
./gradlew run --args="show-shipment --shipment-id=2b52fcb1-34ec-48d2-95f2-b79f2d9c3c70 ..."

# Фильтр по типу
./gradlew run --args="list-by-swg --type='Песок речной' ..."

# Фильтр по датам
./gradlew run --args="list-by-period --from=2024-01-01 --to=2024-12-31 ..."

# Отчёт за год
./gradlew run --args="report --year=2024 ..."
```

## API

Все эндпоинты кроме `/api/tokens` требуют JWT токен в заголовке:
```
Authorization: Bearer <token>
```

Получить тестовые токены можно через `GET /api/tokens`.

### Эндпоинты

| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/tokens` | Токены для тестирования |
| GET | `/api/shipments` | Список отгрузок с пагинацией |
| GET | `/api/shipments/{id}` | Одна отгрузка |
| POST | `/api/shipments` | Создать отгрузку |
| PUT | `/api/shipments/{id}` | Обновить отгрузку |
| GET | `/api/shipments/by-type` | Фильтр по типу ПГС |
| GET | `/api/shipments/by-period` | Фильтр по датам |
| GET | `/api/shipments/report` | Отчёт по месяцам |
| POST | `/api/register-invoice` | Регистрация накладной |
| GET | `/api/dump-trucks/{id}` | Инфо о самосвале |
| DELETE | `/api/dump-trucks/{id}` | Удалить самосвал |
| GET | `/api/employees` | Список сотрудников |

### Параметры пагинации

- `page` — номер страницы (по умолчанию 1)
- `records-per-page` — записей на странице (5, 10, 20 или 50)

### Роли

| Роль | Что может |
|------|-----------|
| Employee | Работать со своими отгрузками |
| Manager | Полный доступ к отгрузкам |
| UserManager | Просмотр списка сотрудников |

## Формат данных

### swg.csv (отгрузки)

```csv
Id,Title,SWG,Measure,Count,Price,Cost,ShipmentDateTime,DumpTruck,Washing,Manager
de42a00f-7f43-4d10-808d-bee47fdeef49,Песчано-гравийная смесь (ПГС),Песок речной,т,36,640,22.3488,2024-01-01T00:00:00,accfe76f-9a9e-4cb4-8876-d36daa22f924,True,de42a00f-7f43-4d10-808d-bee47fdeef49
```

Поля:
- `Id` — UUID отгрузки
- `Title` — название
- `SWG` — тип материала (см. ниже)
- `Measure` — единица измерения: `т` (тонны) или `м3` (кубометры)
- `Count` — количество
- `Price` — цена за единицу
- `Cost` — итоговая стоимость
- `ShipmentDateTime` — дата и время
- `DumpTruck` — UUID самосвала
- `Washing` — была ли промывка
- `Manager` — UUID ответственного

### trucks.csv (самосвалы)

```csv
Id,Model,Registration,Capacity,Volume
accfe76f-9a9e-4cb4-8876-d36daa22f924,KAMAZ-65951-СА,Д903ЧН,36.0,25
f8741785-4986-41ab-85d3-8b2fe522f9ed,МАЗ 5516А8-336,Э723ВЛ,37.0,26
```

Поля:
- `Id` — UUID
- `Model` — модель машины
- `Registration` — госномер
- `Capacity` — грузоподъёмность в тоннах
- `Volume` — объём кузова в м³

### employees.csv (сотрудники)

```csv
Id,Name,Position,RegistrationDateTime,Email,Role
de42a00f-7f43-4d10-808d-bee47fdeef49,Иванов Иван Иванович,Начальник смены,2024-01-01T00:00:00,IvanovII@crafted.su,Manager
```

Поля:
- `Id` — UUID
- `Name` — ФИО
- `Position` — должность
- `RegistrationDateTime` — дата регистрации в системе
- `Email` — почта
- `Role` — роль (Employee, Manager, UserManager)

## Типы материалов

| Название | Плотность (т/м³) |
|----------|------------------|
| Песок речной | 1.5 |
| Песок карьерный | 1.5 |
| Щебень гранитный | 1.4 |
| Щебень гравийный | 1.43 |
| Щебень шлаковый | 1.17 |
| Песчано-гравийная смесь | 1.6 |

Плотность используется при регистрации накладной для пересчёта веса в объём.

## Примеры запросов

Получить токен и сделать запрос:
```bash
# Получаем токен
curl http://localhost:9000/api/tokens

# Используем его
curl http://localhost:9000/api/shipments \
  -H "Authorization: Bearer eyJ..."
```

Создание отгрузки:
```bash
curl -X POST http://localhost:9000/api/shipments \
  -H "Authorization: Bearer eyJ..." \
  -H "Content-Type: application/json" \
  -d '{
    "Title": "Доставка щебня",
    "SWG": "Щебень гранитный",
    "Measure": "т",
    "Count": 25,
    "Price": 1800,
    "Cost": 45000,
    "DumpTruck": "accfe76f-9a9e-4cb4-8876-d36daa22f924",
    "Manager": "de42a00f-7f43-4d10-808d-bee47fdeef49"
  }'
```

Регистрация накладной (form-data):
```bash
curl -X POST http://localhost:9000/api/register-invoice \
  -H "Authorization: Bearer eyJ..." \
  -d "InvoiceTitle=Накладная №123" \
  -d "InvoiceType=Песок речной" \
  -d "InvoiceWeight=20" \
  -d "InvoicePrice=800" \
  -d "DumpTruckModel=КАМАЗ" \
  -d "DumpTruckRegistration=А001АА" \
  -d "Manager=de42a00f-7f43-4d10-808d-bee47fdeef49"
```

Отчёт за год:
```bash
curl "http://localhost:9000/api/shipments/report?year=2024" \
  -H "Authorization: Bearer eyJ..."
```

## Сохранение данных

При остановке сервера (Ctrl+C) данные автоматически сохраняются обратно в CSV-файлы. Это реализовано через shutdown hook.
