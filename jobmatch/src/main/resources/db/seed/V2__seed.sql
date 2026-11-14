-- Первинне заповнення БД JobMatch (п. 4.1.2 ТЗ).
-- Пароль усіх демонстраційних облікових записів: Demo2026!
-- Кожна таблиця, дані якої використовує критичний UC R2, містить записи
-- «мінімального», «майже мінімального», «типового», «майже максимального» і «максимального» розміру.

INSERT INTO user_account (id, email, password_hash, role, created_at) VALUES
 (1,  'olena.koval@profikadr.ua',     '$2b$10$tXapSFIo3P04teNVzv0ZAO/nVq6FUxQCEAgsvj8EyfS57ZuOMV856', 'RECRUITER', '2026-09-01 09:00:00'),
 (2,  'andrii.melnyk@profikadr.ua',   '$2b$10$tXapSFIo3P04teNVzv0ZAO/nVq6FUxQCEAgsvj8EyfS57ZuOMV856', 'RECRUITER', '2026-09-01 09:05:00'),
 (3,  'r@pk.ua',                      '$2b$10$tXapSFIo3P04teNVzv0ZAO/nVq6FUxQCEAgsvj8EyfS57ZuOMV856', 'RECRUITER', '2026-09-01 09:10:00'),
 (4,  'ivan.petrenko@example.com',    '$2b$10$tXapSFIo3P04teNVzv0ZAO/nVq6FUxQCEAgsvj8EyfS57ZuOMV856', 'CANDIDATE', '2026-09-10 12:00:00'),
 (5,  'dmytro.kovalenko@example.com', '$2b$10$tXapSFIo3P04teNVzv0ZAO/nVq6FUxQCEAgsvj8EyfS57ZuOMV856', 'CANDIDATE', '2026-09-12 12:00:00'),
 (6,  'maria.bondar@example.com',     '$2b$10$tXapSFIo3P04teNVzv0ZAO/nVq6FUxQCEAgsvj8EyfS57ZuOMV856', 'CANDIDATE', '2026-09-15 12:00:00'),
 (7,  'ya@i.ua',                      '$2b$10$tXapSFIo3P04teNVzv0ZAO/nVq6FUxQCEAgsvj8EyfS57ZuOMV856', 'CANDIDATE', '2026-09-20 12:00:00'),
 (8,  'oleksandra-anastasiia.verkhovynets-zalizniak.cv.portfolio@example-long-domain-for-tests.com.ua', '$2b$10$tXapSFIo3P04teNVzv0ZAO/nVq6FUxQCEAgsvj8EyfS57ZuOMV856', 'CANDIDATE', '2026-09-21 12:00:00'),
 (9,  'hr@technosoft.ua',             '$2b$10$tXapSFIo3P04teNVzv0ZAO/nVq6FUxQCEAgsvj8EyfS57ZuOMV856', 'EMPLOYER',  '2026-09-05 10:00:00'),
 (10, 'jobs@nova-logistyka.ua',       '$2b$10$tXapSFIo3P04teNVzv0ZAO/nVq6FUxQCEAgsvj8EyfS57ZuOMV856', 'EMPLOYER',  '2026-09-06 10:00:00'),
 (11, 'hr@agroplus.ua',               '$2b$10$tXapSFIo3P04teNVzv0ZAO/nVq6FUxQCEAgsvj8EyfS57ZuOMV856', 'EMPLOYER',  '2026-09-07 10:00:00'),
 (12, 'kadry@medservis.ua',           '$2b$10$tXapSFIo3P04teNVzv0ZAO/nVq6FUxQCEAgsvj8EyfS57ZuOMV856', 'EMPLOYER',  '2026-09-08 10:00:00');

INSERT INTO company (id, owner_user_id, name, city, address, phone, email, website, description) VALUES
 (1, 9,  'ТехноСофт', 'Київ', 'вул. Здолбунівська, 7Д', '+380441112233', 'hr@technosoft.ua', 'https://technosoft.example.ua',
  'Розробка корпоративного програмного забезпечення для банків і логістики. 250 співробітників, гібридний формат роботи.'),
 (2, 10, 'Нова Логістика', 'Львів', 'вул. Городоцька, 355', '+380322223344', 'jobs@nova-logistyka.ua', 'https://nova-logistyka.example.ua',
  'Складська та транспортна логістика по західній Україні. Власний автопарк 120 машин.'),
 (3, 11, 'АгроПлюс', 'Вінниця', 'вул. Келецька, 78', '+380432334455', 'hr@agroplus.ua', NULL,
  'Вирощування зернових і олійних культур на 12 000 га.'),
 (4, 12, 'МедСервіс', 'Київ', 'просп. Берестейський, 40', '+380445556677', 'kadry@medservis.ua', 'https://medservis.example.ua',
  'Мережа приватних клінік сімейної медицини: 9 клінік у Києві.'),
 (5, NULL, 'Ок', 'Дніпро', 'вул. Січеславська, 1', '+380560000001', 'ok@ok.ua', NULL, NULL),
 (6, NULL, 'Об''єднання підприємств фінансового консалтингу, аудиту, бухгалтерського обліку та податкового планування «ФінансГруп»',
  'Дніпро', 'просп. Дмитра Яворницького, 101, бізнес-центр «Січеславський», 14 поверх, офіс 1408 (вхід з боку вул. Старокозацької), м. Дніпро, Дніпропетровська область, 49000, Україна',
  '+380567778899', 'careers.department.dnipro.office@finansgrup-ukraina-consulting-audit.example.com.ua',
  'https://finansgrup-ukraina-consulting-audit.example.com.ua/careers/open-positions/dnipro-office/all-departments/index.html',
  LEFT(REPEAT('Аудит, консалтинг, бухгалтерський супровід і податкове планування для середнього бізнесу. ', 22), 2000));

INSERT INTO vacancy (id, company_id, position, salary, required_education, required_experience_years, requirements, city, status, created_at) VALUES
 (1,  1, 'Java-розробник', 85000, 'BACHELOR', 3,
  'Java 17+, Spring Boot, PostgreSQL, REST, Git. Досвід комерційної розробки від 3 років. Англійська B1.', 'Київ', 'OPEN', '2026-11-01 10:00:00'),
 (2,  1, 'QA-інженер', 45000, NULL, 1,
  'Тест-дизайн, Postman, SQL, базові знання Selenium.', 'Київ', 'OPEN', '2026-11-02 10:00:00'),
 (3,  1, 'Frontend-розробник', 70000, 'BACHELOR', 2,
  'TypeScript, React, HTML/CSS, доступність WCAG 2.1.', 'Київ', 'OPEN', '2026-11-03 10:00:00'),
 (4,  1, 'Project manager', 75000, 'MASTER', 4, 'Scrum, Jira, управління командою 8+ осіб.', 'Київ', 'CLOSED', '2026-09-15 10:00:00'),
 (5,  2, 'Логіст', 30000, 'VOCATIONAL', 1, 'Планування маршрутів, 1С:Логістика, Excel.', 'Львів', 'OPEN', '2026-11-04 10:00:00'),
 (6,  2, 'Водій-експедитор', 28000, 'SECONDARY', 2, 'Посвідчення категорії C, CE.', 'Львів', 'OPEN', '2026-11-05 10:00:00'),
 (7,  3, 'Агроном', 35000, 'BACHELOR', 3, 'Агрономія, захист рослин, точне землеробство.', 'Вінниця', 'OPEN', '2026-11-06 10:00:00'),
 (8,  4, 'Медична сестра', 22000, 'VOCATIONAL', 1, 'Диплом молодшого спеціаліста, сертифікат.', 'Київ', 'OPEN', '2026-11-07 10:00:00'),
 (9,  4, 'Лікар-терапевт', 45000, 'MASTER', 5, 'Сертифікат лікаря-спеціаліста, НМТ.', 'Київ', 'OPEN', '2026-11-08 10:00:00'),
 (10, 5, 'Бухгалтер', 32000, 'BACHELOR', 2, NULL, 'Дніпро', 'OPEN', '2026-11-09 10:00:00'),
 (11, 6, 'Аналітик даних', 60000, 'MASTER', 2,
  REPEAT('SQL, Python, Power BI, статистика, побудова фінансових звітів. ', 30), 'Дніпро', 'OPEN', '2026-11-10 10:00:00'),
 (12, 5, 'ІТ', 1000, NULL, 0, NULL, 'Дніпро', 'OPEN', '2026-11-11 10:00:00');

INSERT INTO person (id, user_id, last_name, first_name, middle_name, birth_date, phone, email, city, home_address,
                    job_status, desired_position, desired_salary, about, updated_at) VALUES
 -- мінімальний: лише обов'язкові поля
 (1, 7, 'Бо', 'Ян', NULL, NULL, '+380500000001', 'ya@i.ua', 'Київ', NULL, 'SEARCHING', NULL, NULL, NULL, '2026-09-20 12:00:00'),
 -- майже мінімальний
 (2, NULL, 'Лі', 'Ія', NULL, NULL, '+380500000002', 'li@i.ua', 'Львів', NULL, 'SEARCHING', 'QA', 15000, NULL, '2026-09-22 12:00:00'),
 -- типові
 (3, 4, 'Петренко', 'Іван', 'Олегович', '1997-04-12', '+380671234567', 'ivan.petrenko@example.com', 'Київ', 'вул. Антоновича, 15, кв. 8',
  'SEARCHING', 'Java-розробник', 80000, 'Backend-розробник: Java, Spring Boot, PostgreSQL, Docker. Вів міграцію монолітного застосунку на мікросервіси.', '2026-11-10 12:00:00'),
 (4, NULL, 'Шевченко', 'Оксана', 'Миколаївна', '1998-09-03', '+380672345678', 'oksana.shevchenko@example.com', 'Київ', 'вул. Тарасівська, 3, кв. 21',
  'SEARCHING', 'Java-розробник', 95000, 'Java, Kotlin, Kafka, AWS. Тімлід групи з 4 розробників.', '2026-11-08 12:00:00'),
 (5, 5, 'Коваленко', 'Дмитро', 'Ігорович', '2000-01-25', '+380633456789', 'dmytro.kovalenko@example.com', 'Львів', 'вул. Наукова, 10, кв. 44',
  'SEARCHING', 'QA-інженер', 40000, 'Мануальне та автоматизоване тестування вебзастосунків.', '2026-11-05 12:00:00'),
 (6, 6, 'Бондар', 'Марія', 'Сергіївна', '1999-06-17', '+380934567890', 'maria.bondar@example.com', 'Київ', 'вул. Лук''янівська, 7, кв. 2',
  'SEARCHING', 'Frontend-розробник', 65000, 'React, TypeScript, дизайн-системи.', '2026-11-04 12:00:00'),
 (7, NULL, 'Ткаченко', 'Андрій', 'Петрович', '1990-11-30', '+380965678901', 'andrii.tkachenko@example.com', 'Львів', 'вул. Стрийська, 45, кв. 9',
  'SEARCHING', 'Логіст', 28000, NULL, '2026-10-30 12:00:00'),
 (8, NULL, 'Кравчук', 'Олена', 'Василівна', '1988-02-14', '+380976789012', 'olena.kravchuk@example.com', 'Вінниця', 'вул. Соборна, 60',
  'SEARCHING', 'Агроном', 33000, 'Агроном-технолог, 10 років досвіду з зерновими.', '2026-10-28 12:00:00'),
 (9, NULL, 'Мельник', 'Сергій', 'Анатолійович', '1992-07-08', '+380987890123', 'serhii.melnyk@example.com', 'Дніпро', 'вул. Робоча, 23, кв. 101',
  'SEARCHING', 'Бухгалтер', 30000, NULL, '2026-10-25 12:00:00'),
 (10, NULL, 'Олійник', 'Віктор', 'Романович', '1985-12-01', '+380508901234', 'viktor.oliinyk@example.com', 'Київ', NULL,
  'NOT_SEARCHING', 'Java-розробник', 120000, 'Працевлаштований, не розглядає пропозиції.', '2026-10-20 12:00:00'),
 -- майже максимальний
 (11, NULL, 'Григоренко-Задорожна', 'Анастасія-Вікторія', 'Олександрівна', '1995-03-22', '+380679012345',
  'anastasiia.hryhorenko-zadorozhna.developer@example-mail.com.ua', 'Кам''янець-Подільський',
  REPEAT('вул. Князів Коріатовичів, 25, кв. 17; ', 5), 'SEARCHING',
  'Розробник програмного забезпечення (Java, Kotlin, Spring, мікросервісна архітектура)', 999999,
  REPEAT('Розробляю серверні застосунки на Java і Kotlin, проєктую REST API, налаштовую CI/CD. ', 11), '2026-11-11 12:00:00'),
 -- максимальний
 (12, 8, 'Верховинець-Залізняк-Святополк-Мирська-Островерх', 'Олександра-Анастасія-Вікторія-Марія-Катерина', 'Володимирівна-Олександрівна-Святославівна',
  '1993-08-30', '+380680123456', 'oleksandra-anastasiia.verkhovynets-zalizniak.cv.portfolio@example-long-domain-for-tests.com.ua',
  'Новоград-Волинський (Звягель), Житомирська область, Україна',
  REPEAT('просп. Миру, 100, корпус 2, під''їзд 3, кв. 255; ', 4), 'SEARCHING',
  'Провідна інженерка з якості програмного забезпечення та автоматизації тестування (QA Lead)', 1000000,
  LEFT(REPEAT('QA Lead: стратегія тестування, автоматизація на Playwright і Selenium, менторство команди. ', 11), 1000), '2026-11-12 12:00:00');

INSERT INTO education (person_id, level, institution, specialty, graduation_year) VALUES
 (2,  'SECONDARY',  'Ліцей № 1', 'Загальна середня освіта', 2018),
 (3,  'BACHELOR',   'КПІ ім. Ігоря Сікорського', 'Інженерія програмного забезпечення', 2019),
 (4,  'BACHELOR',   'КНУ ім. Тараса Шевченка', 'Комп''ютерні науки', 2019),
 (4,  'MASTER',     'КНУ ім. Тараса Шевченка', 'Комп''ютерні науки', 2021),
 (5,  'BACHELOR',   'Львівська політехніка', 'Комп''ютерна інженерія', 2022),
 (6,  'BACHELOR',   'НаУКМА', 'Інженерія програмного забезпечення', 2021),
 (7,  'VOCATIONAL', 'Львівський транспортний коледж', 'Організація перевезень', 2010),
 (8,  'MASTER',     'Вінницький національний аграрний університет', 'Агрономія', 2011),
 (9,  'BACHELOR',   'Дніпровський національний університет', 'Облік і оподаткування', 2014),
 (10, 'MASTER',     'КПІ ім. Ігоря Сікорського', 'Комп''ютерна інженерія', 2008),
 (11, 'BACHELOR',   'Хмельницький національний університет', 'Комп''ютерні науки', 2016),
 (11, 'MASTER',     'Хмельницький національний університет', 'Комп''ютерні науки', 2018),
 (12, 'BACHELOR',   'Державний університет «Житомирська політехніка»', 'Інженерія програмного забезпечення', 2014),
 (12, 'MASTER',     'Державний університет «Житомирська політехніка»', 'Інженерія програмного забезпечення', 2016),
 (12, 'PHD',        'Національний технічний університет України «Київський політехнічний інститут імені Ігоря Сікорського»', 'Інженерія програмного забезпечення та якість програмних систем', 2021);

INSERT INTO work_record (person_id, company_id, company_name, position, salary, start_date, end_date) VALUES
 (2,  NULL, 'Фриланс', 'Тестувальник', NULL, '2025-06-01', NULL),
 (3,  NULL, 'Веб Студія', 'Junior Java developer', 35000, '2019-07-01', '2022-03-31'),
 (3,  NULL, 'Глобал ІТ', 'Java-розробник', 70000, '2022-04-01', NULL),
 (4,  NULL, 'Софт Лаб', 'Java-розробник', 75000, '2020-09-01', NULL),
 (5,  NULL, 'Тест Про', 'QA-інженер', 32000, '2023-01-09', NULL),
 (6,  NULL, 'Дизайн Хаб', 'Frontend-розробник', 55000, '2021-08-02', NULL),
 (7,  NULL, 'Швидка доставка', 'Логіст', 24000, '2018-03-01', '2024-12-31'),
 (8,  3,    'АгроПлюс', 'Помічник агронома', 15000, '2011-08-01', '2014-12-31'),
 (8,  NULL, 'Поділля Агро', 'Агроном', 30000, '2015-01-12', NULL),
 (9,  NULL, 'Дніпро Трейд', 'Бухгалтер', 26000, '2017-02-01', NULL),
 (10, 1,    'ТехноСофт', 'Java-розробник', 110000, '2012-05-01', NULL),
 (11, NULL, 'Поділля Софт', 'Розробник програмного забезпечення', 60000, '2018-09-01', '2021-08-31'),
 (11, NULL, 'Карпати Тех', 'Kotlin-розробник', 85000, '2021-09-01', NULL),
 (12, NULL, 'Житомир Софт', 'QA-інженер', 30000, '2014-07-01', '2018-06-30'),
 (12, NULL, 'Полісся ІТ', 'Senior QA-інженер', 70000, '2018-07-01', '2022-12-31'),
 (12, NULL, 'Звягель Лабс', 'QA Lead', 110000, '2023-01-02', NULL);

INSERT INTO application (vacancy_id, person_id, source, status, comment, created_at) VALUES
 (1, 4, 'RECRUITER', 'NEW', 'Сильна в Kafka і AWS, керувала командою. Очікування трохи вищі за бюджет.', '2026-11-12 15:00:00'),
 (2, 5, 'CANDIDATE', 'NEW', NULL, '2026-11-11 09:30:00'),
 (3, 6, 'CANDIDATE', 'INVITED', NULL, '2026-11-06 11:00:00'),
 (5, 7, 'RECRUITER', 'REJECTED', 'Досвід 6 років у логістиці.', '2026-11-05 16:00:00');

ALTER TABLE user_account ALTER COLUMN id RESTART WITH 100;
ALTER TABLE company ALTER COLUMN id RESTART WITH 100;
ALTER TABLE vacancy ALTER COLUMN id RESTART WITH 100;
ALTER TABLE person ALTER COLUMN id RESTART WITH 100;
ALTER TABLE education ALTER COLUMN id RESTART WITH 100;
ALTER TABLE work_record ALTER COLUMN id RESTART WITH 100;
ALTER TABLE application ALTER COLUMN id RESTART WITH 100;
