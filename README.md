Тема — постановка №8 «Кадрова агенція». Веб-застосунок JobMatch: вакансії, резюме, підбір кандидатів на вакансію.

- `lab1` — ТЗ, склад команди
- `lab2` — SRS (RUP), use case діаграма
- `lab3` — User Flow, Wireflow
- `lab4` — архітектура, UML
- `lab5` — код і тестування
- `lab6` — настанова користувача
- `jobmatch` — сам застосунок (Java 21, Spring Boot)

У кожній папці лаби лежить звіт у docx і pdf та його вихідний md.

## Запуск

```bash
cd jobmatch
mvn -DskipTests package
java -jar target/jobmatch-1.0.0.jar
```

Відкрити http://localhost:8090. Тестові акаунти є в `jobmatch/src/main/resources/db/seed/V2__seed.sql`.

## Тести

```bash
cd jobmatch && mvn test
cd jobmatch/e2e && npm install && ./run-all.sh
```
