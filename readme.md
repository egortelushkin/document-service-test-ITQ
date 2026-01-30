## ITQ DOCUMENT SERVICE EXPLANATION

#### - Как начать использовать ITQ Document Service step by step

## Step 0. Clone repository
- Clone the repository
```bash
git clone https://github.com/egortelushkin/document-service-test-ITQ.git
```
- Go to the project folder:
```bash
cd document-service-test-ITQ
```

## Step one. Start docker compose file
```bash
docker-compose up -- build
```
Вы великолепны! Сервис запущен на http://localhost:8080

## Step two. Automatic batch processing (workers)
- Документы автоматически подтягиваются и обновляются каждые 10–15 секунд через Spring @Scheduled.
- Draft → Submitted → Approved, с логированием прогресса.
- Логи выглядят так:
```026-01-30T13:06:02] SUBMIT batch started. Docs in batch: 5
SUBMIT result: docId=252 -> SUCCESS
SUBMIT result: docId=253 -> SUCCESS
...
[2026-01-30T13:06:02] SUBMIT batch finished
[2026-01-30T13:06:12] APPROVE batch started. Docs in batch: 5
APPROVE result: docId=252 -> SUCCESS
APPROVE result: docId=253 -> SUCCESS
...
[2026-01-30T13:06:12] APPROVE batch finished
```
- locked_at гарантирует, что один документ не обрабатывается одновременно несколькими батчами
- Интервал блокировки — 10 минут (можно менять в коде промежуток, а в конфиге размер батча)

## Step three. Creating documents

### 1. Creating new doc
POST: http://localhost:8080/documents

raw input:
```json
{
  "author": "Ivan",
  "title": "Second Test Doc Ivan"
}
```
response:
```json
{
  "1"
}
```

### 2. Get one doc with history
GET: http://localhost:8080/documents/1

raw input:
```json

```
response:
```json
{
  "document": {
    "id": 1,
    "number": "88febe83-7eca-49aa-8788-44c004594d76",
    "author": "Ivan",
    "title": "Test doc",
    "status": "DRAFT",
    "createdAt": "2026-01-30T11:57:22.857958",
    "updatedAt": "2026-01-30T11:57:22.85798"
  },
  "history": []
}
```

### 3. Get bach of docs by ids
GET: http://localhost:8080/documents?ids=1,2&page=0&size=10

raw input:
```json

```
response:
```json
{
  "content": [
    {
      "id": 1,
      "number": "88febe83-7eca-49aa-8788-44c004594d76",
      "author": "Ivan",
      "title": "Test doc",
      "status": "DRAFT",
      "createdAt": "2026-01-30T11:57:22.857958",
      "updatedAt": "2026-01-30T11:57:22.85798"
    },
    {
      "id": 2,
      "number": "7bb073ae-b2d6-459d-8e20-c7bc700070a5",
      "author": "Egor",
      "title": "Second Test Doc",
      "status": "DRAFT",
      "createdAt": "2026-01-30T11:58:17.598863",
      "updatedAt": "2026-01-30T11:58:17.598879"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "unsorted": true,
      "sorted": false,
      "empty": true
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 1,
  "totalElements": 2,
  "last": true,
  "numberOfElements": 2,
  "size": 10,
  "number": 0,
  "sort": {
    "unsorted": true,
    "sorted": false,
    "empty": true
  },
  "first": true,
  "empty": false
}
```

### 4. Submit docs (for approval)
POST: http://localhost:8080/documents/submit?actor=Ivan

raw input:
```json
[1, 2, 3]
```
response:
```json
[
  {
    "id": 1,
    "result": "SUCCESS"
  },
  {
    "id": 2,
    "result": "SUCCESS"
  },
  {
    "id": 3,
    "result": "SUCCESS"
  }
]
```

### 5. Approve docs
POST: http://localhost:8080/documents/approve?actor=Manager

raw input:
```json
[1, 2, 3]
```
response:
```json
[
  {
    "id": 1,
    "result": "SUCCESS"
  },
  {
    "id": 2,
    "result": "SUCCESS"
  },
  {
    "id": 3,
    "result": "SUCCESS"
  }
]
```

### 6. Search docs
POST: http://localhost:8080/documents/search

raw input:
```json
{
  "author": "Egor",
  "status": "DRAFT",
  "titleContains": "Test"
}
```
response:
```json
"content": [],
"pageable": {
"pageNumber": 0,
"pageSize": 20,
"sort": {
"unsorted": true,
"sorted": false,
"empty": true
},
"offset": 0,
"paged": true,
"unpaged": false
},
"totalPages": 0,
"totalElements": 0,
"last": true,
"numberOfElements": 0,
"size": 20,
"number": 0,
"sort": {
"unsorted": true,
"sorted": false,
"empty": true
},
"first": true,
"empty": true
}
```

# Рекомендации по улучшению системы:
Сейчас ApprovalRegistry хранится в той же БД, что и документы

Для масштабируемости и отказоустойчивости можно вынести его:

- Отдельная база данных, потому что микросервисный подход позволит горизонтально масштабировать.

- Отдельный сервис. DocumentService отправляет событие (например, через REST или Kafka) на сервис реестра, который пишет данные в свою бд


Хендлинг больших батчей(5000 + айди) можно улучшить:
- Использовать пагинацию на стороне репозитория и отправлять отдельно
- Использовать очереди сообщения для обработки больших обхемов асинхронно
