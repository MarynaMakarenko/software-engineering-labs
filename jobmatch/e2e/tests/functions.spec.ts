import { test } from '@playwright/test';
import { login, meta, shot, success, error, expect, RECRUITER, CANDIDATE, EMPLOYER, PASSWORD } from './helpers';

/** Таблиця 3. Тести решти функцій Веб-застосунку JobMatch. Назва: <ID> | <UC> <функція> | <тип даних> | <сценарій>. */
test.describe.configure({ mode: 'serial' });

// ---------------- U1 Вхід ----------------
test('F1.1 | U1 Вхід | Коректні | мінімальний: найкоротший e-mail (r@pk.ua)', async ({ page }) => {
  meta('«Увійти» → E-mail r@pk.ua, пароль Demo2026! → «Увійти»', 'Відкрито «Панель рекрутера»; у шапці — «r@pk.ua · Рекрутер»');
  await login(page, 'r@pk.ua');
  await expect(page).toHaveURL(/\/recruiter$/);
  await expect(page.locator('.user-email')).toContainText('r@pk.ua · Рекрутер');
});
test('F1.2 | U1 Вхід | Коректні | типовий: шукач', async ({ page }) => {
  meta('«Увійти» → E-mail ivan.petrenko@example.com, пароль → «Увійти»', 'Відкрито «Моє резюме»');
  await page.goto('/login'); await shot(page, 'u1-login');
  await login(page, CANDIDATE);
  await expect(page).toHaveURL(/\/me\/resume$/);
});
test('F1.3 | U1 Вхід | Коректні | максимальний: e-mail 94 символи', async ({ page }) => {
  meta('«Увійти» → E-mail довжиною 94 символи (шукач Верховинець-Залізняк), пароль → «Увійти»', 'Відкрито «Моє резюме» з ПІБ максимальної довжини');
  await login(page, 'oleksandra-anastasiia.verkhovynets-zalizniak.cv.portfolio@example-long-domain-for-tests.com.ua');
  await expect(page).toHaveURL(/\/me\/resume$/);
  await expect(page.locator('main')).toContainText('Верховинець-Залізняк-Святополк-Мирська-Островерх');
});
test('F1.4 | U1 Вхід | Некоректні | типовий: неправильний пароль', async ({ page }) => {
  meta('«Увійти» → E-mail olena.koval@profikadr.ua, пароль wrong-password → «Увійти»', 'Повідомлення «Неправильний e-mail або пароль.» з трьома частинами; залишається сторінка «Вхід»');
  await login(page, RECRUITER, 'wrong-password');
  await expect(error(page)).toContainText('Неправильний e-mail або пароль');
  await shot(page, 'u1-login-error');
});

// ---------------- U2 Вихід ----------------
test('F2.1 | U2 Вихід | Коректні | типовий', async ({ page }) => {
  meta('Увійти як рекрутер → «Вийти» у шапці', 'Головна сторінка з повідомленням «Ви вийшли із застосунку.»; у шапці — «Увійти», «Реєстрація»');
  await login(page, RECRUITER);
  await page.getByRole('button', { name: 'Вийти' }).click();
  await expect(success(page)).toContainText('Ви вийшли із застосунку');
  await expect(page.getByRole('link', { name: 'Увійти' })).toBeVisible();
});
test('F2.2 | U2 Вихід | Некоректні | типовий: кабінет після виходу', async ({ page }) => {
  meta('Увійти як рекрутер → «Вийти» → відкрити /recruiter', 'Перенаправлення на сторінку «Вхід»');
  await login(page, RECRUITER);
  await page.getByRole('button', { name: 'Вийти' }).click();
  await page.goto('/recruiter');
  await expect(page).toHaveURL(/\/login$/);
});

// ---------------- U3 Довідка ----------------
test('F3.1 | U3 Довідка | Коректні | типовий: гість', async ({ page }) => {
  meta('Головна → «Допомога»', 'Сторінка «Допомога» з частими питаннями і посиланням «Завантажити Настанову користувача (PDF)»');
  await page.goto('/'); await shot(page, 'g0-home', true);
  await page.getByRole('navigation', { name: 'Головне меню' }).getByRole('link', { name: 'Допомога' }).click();
  await expect(page.getByRole('link', { name: /Настанову користувача/ })).toBeVisible();
  await shot(page, 'u3-help', true);
});

// ---------------- G1 Пошук вакансій ----------------
test('F4.1 | G1 Пошук вакансій | Коректні | мінімальний: без фільтрів', async ({ page }) => {
  meta('Головна → «Вакансії»', 'Таблиця 11 відкритих вакансій; закритої «Project manager» немає');
  await page.goto('/vacancies');
  await expect(page.locator('tbody tr')).toHaveCount(11);
  await expect(page.locator('main')).not.toContainText('Project manager');
  await shot(page, 'g1-vacancies', true);
});
test('F4.2 | G1 Пошук вакансій | Коректні | типовий: посада «java», зарплата від 50000', async ({ page }) => {
  meta('«Вакансії» → Посада = java, Зарплата від = 50000 → «Знайти»', '1 вакансія «Java-розробник», ТехноСофт, 85 000');
  await page.goto('/vacancies');
  await page.getByLabel('Посада').fill('java');
  await page.getByLabel('Зарплата від, грн').fill('50000');
  await page.getByRole('button', { name: 'Знайти' }).click();
  await expect(page.locator('tbody tr')).toHaveCount(1);
  await expect(page.locator('tbody tr')).toContainText('Java-розробник');
});
test('F4.3 | G1 Пошук вакансій | Коректні | максимальний: усі 4 фільтри, немає результатів', async ({ page }) => {
  meta('«Вакансії» → Посада = розробник, Компанія = ТехноСофт, Місто = Київ, Зарплата від = 1000000 → «Знайти»', 'Повідомлення «За вашими умовами відкритих вакансій немає.» з порадою прибрати фільтр');
  await page.goto('/vacancies?position=розробник&company=ТехноСофт&city=Київ&salaryFrom=1000000');
  await expect(page.locator('.empty')).toContainText('відкритих вакансій немає');
});
test('F4.4 | G1 Пошук вакансій | Некоректні | типовий: зарплата «abc»', async ({ page }) => {
  meta('Відкрити /vacancies?salaryFrom=abc', 'Діагностичне повідомлення «Фільтр зарплати містить нечислове значення «abc» — фільтр не застосовано.»; показано всі вакансії');
  await page.goto('/vacancies?salaryFrom=abc');
  await expect(error(page)).toContainText('нечислове значення');
  await expect(page.locator('tbody tr')).toHaveCount(11);
});
test('F4.5 | G1 Картка вакансії | Коректні | типовий', async ({ page }) => {
  meta('«Вакансії» → «Java-розробник»', 'Картка: зарплата 85 000 грн/міс, мін. освіта Бакалавр, мін. досвід 3 р., вимоги, опис компанії; для гостя — запрошення увійти');
  await page.goto('/vacancies/1');
  await expect(page.locator('main')).toContainText('85 000 грн/міс');
  await shot(page, 'g1-vacancy');
});

// ---------------- G2 Реєстрація ----------------
test('F5.1 | G2 Реєстрація | Коректні | мінімальний: шукач, пароль 8 символів', async ({ page }) => {
  meta('«Реєстрація» → тип «Шукач роботи», e-mail a@b.ua, пароль і повтор 12345678, прізвище «Ко», ім\'я «Ян», домашня адреса «вул. Й, 1», телефон +380501112233, місто «Київ», згода ✓ → «Створити обліковий запис»',
    'Сторінка «Вхід» з повідомленням «Реєстрацію завершено. Увійдіть з указаними e-mail і паролем.»; вхід з цими даними відкриває «Моє резюме»');
  await page.goto('/register');
  await page.locator('#email').fill('a@b.ua');
  await page.locator('#password').fill('12345678');
  await page.getByLabel('Повторіть пароль').fill('12345678');
  await page.getByLabel('Прізвище').fill('Ко');
  await page.getByLabel('Ім\'я').fill('Ян');
  await page.getByLabel('Домашня адреса').fill('вул. Й, 1');
  await page.getByLabel('Телефон').fill('+380501112233');
  await page.getByLabel('Місто').fill('Київ');
  await page.getByLabel(/Даю згоду/).check();
  await shot(page, 'g2-register', true);
  await page.getByRole('button', { name: 'Створити обліковий запис' }).click();
  await expect(success(page)).toContainText('Реєстрацію завершено');
  await login(page, 'a@b.ua', '12345678');
  await expect(page).toHaveURL(/\/me\/resume$/);
});
test('F5.2 | G2 Реєстрація | Коректні | типовий: представник компанії', async ({ page }) => {
  meta('«Реєстрація» → тип «Представник компанії», e-mail hr@novatech.ua, пароль Demo2026!, назва «НоваТех», адреса «вул. Січових Стрільців, 12», e-mail компанії jobs@novatech.ua, телефон, місто → «Створити обліковий запис»',
    'Повідомлення «Реєстрацію завершено…»; після входу — сторінка «НоваТех» (Моя компанія)');
  await page.goto('/register');
  await page.getByLabel('Представник компанії').check();
  await page.locator('#email').fill('hr@novatech.ua');
  await page.locator('#password').fill(PASSWORD);
  await page.getByLabel('Повторіть пароль').fill(PASSWORD);
  await page.getByLabel('Назва компанії').fill('НоваТех');
  await page.getByLabel('Адреса компанії').fill('вул. Січових Стрільців, 12');
  await page.getByLabel('E-mail компанії').fill('jobs@novatech.ua');
  await page.getByLabel('Телефон').fill('+380441230000');
  await page.getByLabel('Місто').fill('Київ');
  await page.getByRole('button', { name: 'Створити обліковий запис' }).click();
  await expect(success(page)).toContainText('Реєстрацію завершено');
  await login(page, 'hr@novatech.ua');
  await expect(page.getByRole('heading', { name: 'НоваТех' })).toBeVisible();
});
test('F5.3 | G2 Реєстрація | Коректні | максимальний: поля граничної довжини', async ({ page }) => {
  meta('«Реєстрація» → шукач: e-mail 100 символів, пароль 64 символи, прізвище й ім\'я по 50 символів, адреса 200 символів, місто 60 символів, згода ✓ → «Створити обліковий запис»',
    'Повідомлення «Реєстрацію завершено…»');
  const email = 'x'.repeat(64) + '@' + 'd'.repeat(31) + '.com'; // 100 символів, локальна частина — 64 (межа RFC 5321)
  const pwd = 'p1'.repeat(32);
  await page.goto('/register');
  await page.locator('#email').fill(email);
  await page.locator('#password').fill(pwd);
  await page.getByLabel('Повторіть пароль').fill(pwd);
  await page.getByLabel('Прізвище').fill('Ш'.repeat(50));
  await page.getByLabel('Ім\'я').fill('Я'.repeat(50));
  await page.getByLabel('Домашня адреса').fill('а'.repeat(200));
  await page.getByLabel('Телефон').fill('+380509998877');
  await page.getByLabel('Місто').fill('М'.repeat(60));
  await page.getByLabel(/Даю згоду/).check();
  await page.getByRole('button', { name: 'Створити обліковий запис' }).click();
  await expect(success(page)).toContainText('Реєстрацію завершено');
});
test('F5.4 | G2 Реєстрація | Некоректні | типовий: e-mail уже зареєстровано, без згоди', async ({ page }) => {
  meta('«Реєстрація» → шукач з e-mail ivan.petrenko@example.com, згоду не позначено → «Створити обліковий запис»',
    'Повідомлення «Форму не збережено…» зі змістом «Не надано згоду на обробку персональних даних…»; введені дані збережено у формі');
  await page.goto('/register');
  await page.locator('#email').fill(CANDIDATE);
  await page.locator('#password').fill(PASSWORD);
  await page.getByLabel('Повторіть пароль').fill(PASSWORD);
  await page.getByLabel('Прізвище').fill('Петренко');
  await page.getByLabel('Ім\'я').fill('Іван');
  await page.getByLabel('Домашня адреса').fill('вул. Антоновича, 15');
  await page.getByLabel('Телефон').fill('+380671234567');
  await page.getByLabel('Місто').fill('Київ');
  await page.getByRole('button', { name: 'Створити обліковий запис' }).click();
  await expect(error(page)).toContainText('Не надано згоду');
  await expect(page.getByLabel('Прізвище')).toHaveValue('Петренко');
  await page.getByLabel(/Даю згоду/).check();
  await page.locator('#password').fill(PASSWORD);
  await page.getByLabel('Повторіть пароль').fill(PASSWORD);
  await page.getByRole('button', { name: 'Створити обліковий запис' }).click();
  await expect(error(page)).toContainText('вже існує');
  await shot(page, 'g2-register-error');
});
test('F5.5 | G2 Реєстрація | Некоректні | мінімальний: пароль 7 символів, телефон без +380', async ({ page }) => {
  meta('«Реєстрація» → пароль 1234567, телефон 0671234567 → «Створити обліковий запис»', 'Повідомлення під полями: «Пароль має містити від 8 до 64 символів…», «Телефон має неправильний формат…»');
  await page.goto('/register');
  await page.locator('#email').fill('new@example.com');
  await page.locator('#password').fill('1234567');
  await page.getByLabel('Повторіть пароль').fill('1234567');
  await page.getByLabel('Телефон').fill('0671234567');
  await page.getByRole('button', { name: 'Створити обліковий запис' }).click();
  await expect(page.locator('.field-error')).toContainText(['Пароль має містити від 8 до 64 символів']);
  await expect(page.locator('main')).toContainText('Телефон має неправильний формат');
});

// ---------------- C1 Ведення резюме ----------------
test('F6.1 | C1 Ведення резюме | Коректні | типовий: зміна бажаної зарплати', async ({ page }) => {
  meta('Шукач → «Моє резюме» → «Редагувати» → Бажана зарплата = 82000 → «Зберегти»', 'Повідомлення «Резюме збережено.»; у резюме — «82 000 грн»');
  await login(page, CANDIDATE);
  await shot(page, 'c1-resume', true);
  await page.getByRole('link', { name: 'Редагувати' }).click();
  await page.getByLabel('Бажана зарплата, грн/міс').fill('82000');
  await shot(page, 'c1-resume-form', true);
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(success(page)).toContainText('Резюме збережено');
  await expect(page.locator('main')).toContainText('82 000 грн');
});
test('F6.2 | C1 Ведення резюме | Коректні | мінімальний/максимальний: додати й видалити освіту', async ({ page }) => {
  meta('«Моє резюме» → «+ Додати освіту» → Рівень «Магістр», заклад «КПІ», спеціальність «ІПЗ», рік 2035 → «Додати»; потім «Видалити» біля запису → підтвердити',
    'Повідомлення «Запис про освіту додано.» (рядок 2035 у таблиці освіти), потім «Запис про освіту видалено.»');
  await login(page, CANDIDATE);
  await page.getByRole('button', { name: '+ Додати освіту' }).click();
  await page.getByLabel('Рівень').selectOption({ label: 'Магістр' });
  await page.getByLabel('Навчальний заклад').fill('КПІ');
  await page.getByLabel('Спеціальність').fill('ІПЗ');
  await page.getByLabel('Рік закінчення').fill('2035');
  await page.locator('#education-form').getByRole('button', { name: 'Додати', exact: true }).click();
  await expect(success(page)).toContainText('Запис про освіту додано');
  page.once('dialog', d => d.accept());
  await page.locator('tr', { hasText: '2035' }).getByRole('button', { name: 'Видалити' }).click();
  await expect(success(page)).toContainText('Запис про освіту видалено');
});
test('F6.3 | C1 Ведення резюме | Некоректні | типовий: телефон і зарплата поза межами', async ({ page }) => {
  meta('«Моє резюме» → «Редагувати» → Телефон = 067-123-45-67, Бажана зарплата = 999 → «Зберегти»',
    'Форма не зберігається; під полями — «Телефон має неправильний формат…» і «Бажана зарплата менша за 1000 грн…»');
  await login(page, CANDIDATE);
  await page.goto('/me/resume/edit');
  await page.getByLabel('Телефон').fill('067-123-45-67');
  await page.getByLabel('Бажана зарплата, грн/міс').fill('999');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(page.locator('main')).toContainText('Телефон має неправильний формат');
  await expect(page.locator('main')).toContainText('Бажана зарплата менша за 1000 грн');
  await shot(page, 'c1-resume-error', true);
});

// ---------------- C2 Історія роботи ----------------
test('F7.1 | C2 Історія роботи | Коректні | типовий: додати, редагувати, видалити запис', async ({ page }) => {
  meta('«Історія роботи» → «+ Додати запис» → компанія «Стартап», посада «Стажер», 2018-06-01 – 2018-08-31 → «Зберегти»; «Редагувати» → посада «Java-стажер» → «Зберегти»; «Видалити» → підтвердити',
    'Повідомлення «Запис історії роботи додано.», «…збережено.», «…видалено.»; стаж змінюється відповідно');
  await login(page, CANDIDATE);
  await page.goto('/me/work');
  await shot(page, 'c2-work');
  await page.getByRole('link', { name: '+ Додати запис' }).click();
  await page.getByLabel('Компанія').fill('Стартап');
  await page.getByLabel('Посада').fill('Стажер');
  await page.getByLabel('Дата початку').fill('2018-06-01');
  await page.getByLabel('Дата завершення').fill('2018-08-31');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(success(page)).toContainText('Запис історії роботи додано');
  await page.locator('tr', { hasText: 'Стартап' }).getByRole('link', { name: 'Редагувати' }).click();
  await page.getByLabel('Посада').fill('Java-стажер');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(page.locator('tr', { hasText: 'Стартап' })).toContainText('Java-стажер');
  page.once('dialog', d => d.accept());
  await page.locator('tr', { hasText: 'Стартап' }).getByRole('button', { name: 'Видалити' }).click();
  await expect(success(page)).toContainText('видалено');
});
test('F7.2 | C2 Історія роботи | Некоректні | типовий: дата завершення раніша за початок', async ({ page }) => {
  meta('«Історія роботи» → «+ Додати запис» → 2024-05-01 – 2024-04-01 → «Зберегти»', 'Повідомлення «Дата завершення раніша за дату початку. Виправте одну з дат…»; запис не додано');
  await login(page, CANDIDATE);
  await page.goto('/me/work/new');
  await page.getByLabel('Компанія').fill('Тест');
  await page.getByLabel('Посада').fill('Тест');
  await page.getByLabel('Дата початку').fill('2024-05-01');
  await page.getByLabel('Дата завершення').fill('2024-04-01');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(error(page)).toContainText('Дата завершення раніша за дату початку');
});

// ---------------- C3 / C4 / C5 ----------------
test('F8.1 | C3 Відповідні вакансії | Коректні | типовий', async ({ page }) => {
  meta('Шукач Петренко → «Відповідні вакансії»', 'Першою у списку — «Java-розробник» з оцінкою 100 %; показано складові оцінки');
  await login(page, CANDIDATE);
  await page.goto('/me/matches');
  await expect(page.locator('tbody tr').first()).toContainText('Java-розробник');
  await expect(page.locator('tbody tr').first().locator('.score')).toHaveText('100%');
  await shot(page, 'c3-matches', true);
});
test('F8.2 | C3 Відповідні вакансії | Некоректні | типовий: мін. оцінка «x»', async ({ page }) => {
  meta('«Відповідні вакансії» → Мін. оцінка = x → «Оновити»', 'Діагностичне повідомлення «…має бути цілим числом від 0 до 100»; список з порогом 50 %');
  await login(page, CANDIDATE);
  await page.goto('/me/matches?minScore=x');
  await expect(error(page)).toContainText('від 0 до 100');
});
test('F9.1 | C4 Відгук на вакансію | Коректні | типовий', async ({ page }) => {
  meta('Шукач Петренко → «Вакансії» → «QA-інженер» → «Відгукнутися»', 'Повідомлення «Відгук надіслано…»; на картці — «Ваш відгук: Нова»');
  await login(page, CANDIDATE);
  await page.goto('/vacancies/2');
  await page.getByRole('button', { name: 'Відгукнутися' }).click();
  await expect(success(page)).toContainText('Відгук надіслано');
  await expect(page.locator('main')).toContainText('Ваш відгук:');
  await shot(page, 'c4-applied');
});
test('F9.2 | C4 Відгук на вакансію | Некоректні | типовий: закрита вакансія', async ({ page }) => {
  meta('Шукач → відкрити /vacancies/4 (закрита «Project manager»)', 'Повідомлення «Вакансію закрито — подання кандидатур недоступне.»; кнопки «Відгукнутися» немає');
  await login(page, CANDIDATE);
  await page.goto('/vacancies/4');
  await expect(page.locator('.alert-info')).toContainText('Вакансію закрито');
  await expect(page.getByRole('button', { name: 'Відгукнутися' })).toHaveCount(0);
});
test('F10.1 | C5 Мої відгуки | Коректні | типовий', async ({ page }) => {
  meta('Шукач Петренко → «Мої відгуки»', 'Таблиця кандидатур з рядком «QA-інженер», ТехноСофт, джерело «Відгук шукача», статус «Нова»');
  await login(page, CANDIDATE);
  await page.goto('/me/applications');
  await expect(page.locator('tbody tr')).toHaveCount(1);
  await expect(page.locator('tbody tr')).toContainText('Відгук шукача');
  await shot(page, 'c5-applications');
});
test('F10.2 | C5 Мої відгуки | Коректні | мінімальний: немає відгуків', async ({ page }) => {
  meta('Увійти як шукач ya@i.ua → «Мої відгуки»', 'Повідомлення «Ви ще не відгукувалися на вакансії.» і посилання на відповідні вакансії');
  await login(page, 'ya@i.ua');
  await page.goto('/me/applications');
  await expect(page.locator('.empty')).toContainText('Ви ще не відгукувалися');
});

// ---------------- E1 / E2 / E3 / E4 ----------------
test('F11.1 | E1 Профіль компанії | Коректні | типовий', async ({ page }) => {
  meta('Представник ТехноСофт → «Моя компанія» → «Редагувати профіль» → Сайт = https://technosoft.ua → «Зберегти»', 'Повідомлення «Профіль компанії збережено.»; у профілі — новий сайт');
  await login(page, EMPLOYER);
  await shot(page, 'e1-company');
  await page.getByRole('link', { name: 'Редагувати профіль' }).click();
  await page.getByLabel('Сайт').fill('https://technosoft.ua');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(success(page)).toContainText('Профіль компанії збережено');
  await expect(page.locator('main')).toContainText('https://technosoft.ua');
});
test('F11.2 | E1 Профіль компанії | Некоректні | типовий: сайт без https, назва зайнята', async ({ page }) => {
  meta('«Редагувати профіль» → Сайт = technosoft.ua → «Зберегти»; потім Назва = «АгроПлюс» → «Зберегти»', 'Повідомлення «Адреса сайту має неправильний формат…»; потім «Компанія з назвою «АгроПлюс» вже існує…»');
  await login(page, EMPLOYER);
  await page.goto('/company/edit');
  await page.getByLabel('Сайт').fill('technosoft.ua');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(page.locator('main')).toContainText('Адреса сайту має неправильний формат');
  await page.getByLabel('Сайт').fill('');
  await page.getByLabel('Назва').fill('АгроПлюс');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(error(page)).toContainText('вже існує');
});
test('F12.1 | E2 Ведення вакансій | Коректні | типовий: додати, редагувати, закрити, видалити', async ({ page }) => {
  meta('«Вакансії компанії» → «+ Нова вакансія» → посада «DevOps-інженер», 90000, Бакалавр, 3 р., Київ → «Зберегти»; «Редагувати» → зарплата 95000; «Закрити»; «Відкрити»; «Видалити» → підтвердити',
    'Повідомлення «Вакансію «DevOps-інженер» опубліковано.», «Вакансію збережено.», «Вакансію закрито.», «Вакансію відкрито.», «Вакансію видалено.»');
  await login(page, EMPLOYER);
  await page.goto('/company/vacancies');
  await page.getByRole('link', { name: '+ Нова вакансія' }).click();
  await page.getByLabel('Посада').fill('DevOps-інженер');
  await page.getByLabel('Зарплата, грн/міс').fill('90000');
  await page.getByLabel('Мінімальна освіта').selectOption({ label: 'Бакалавр' });
  await page.getByLabel('Мінімальний досвід, років').fill('3');
  await page.getByLabel('Місто роботи').fill('Київ');
  await shot(page, 'e2-vacancy-form', true);
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(success(page)).toContainText('опубліковано');
  await shot(page, 'e2-vacancies');
  const r = page.locator('tr', { hasText: 'DevOps-інженер' });
  await r.getByRole('link', { name: 'Редагувати' }).click();
  await page.getByLabel('Зарплата, грн/міс').fill('95000');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(r).toContainText('95 000');
  await r.getByRole('button', { name: 'Закрити' }).click();
  await expect(success(page)).toContainText('Вакансію закрито');
  await r.getByRole('button', { name: 'Відкрити' }).click();
  await expect(success(page)).toContainText('Вакансію відкрито');
  page.once('dialog', d => d.accept());
  await r.getByRole('button', { name: 'Видалити' }).click();
  await expect(success(page)).toContainText('Вакансію видалено');
});
test('F12.2 | E2 Ведення вакансій | Некоректні | мінімальний/максимальний: зарплата 999 і досвід 41', async ({ page }) => {
  meta('«+ Нова вакансія» → посада «Х», зарплата 999, досвід 41 → «Зберегти»', 'Повідомлення «Назва посади має бути від 2 до 100 символів…», «Зарплата менша за 1000 грн…», «Досвід більший за 40 років…»');
  await login(page, EMPLOYER);
  await page.goto('/company/vacancies/new');
  await page.getByLabel('Посада').fill('Х');
  await page.getByLabel('Зарплата, грн/міс').fill('999');
  await page.getByLabel('Мінімальний досвід, років').fill('41');
  await page.getByLabel('Місто роботи').fill('Київ');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(page.locator('main')).toContainText('Зарплата менша за 1000 грн');
  await expect(page.locator('main')).toContainText('Досвід більший за 40 років');
  await expect(page.locator('main')).toContainText('від 2 до 100 символів');
});
test('F13.1 | E3 Відповідні резюме | Коректні | типовий', async ({ page }) => {
  meta('«Вакансії компанії» → «Відповідні резюме» біля «Java-розробник»', 'Список кандидатів у форматі «Ім\'я П.» без контактів, з оцінками і складовими');
  await login(page, EMPLOYER);
  await page.goto('/company/vacancies/1/matches');
  await expect(page.locator('tbody tr').first()).toContainText('Іван П.');
  await expect(page.locator('main')).not.toContainText('+380');
  await shot(page, 'e3-matches', true);
});
test('F14.1 | E4 Розгляд кандидатур | Коректні | типовий: запросити й відхилити', async ({ page }) => {
  meta('«Вакансії компанії» → «Кандидатури» біля «Java-розробник» → «Запросити» біля «Оксана Ш.»; «Кандидатури» біля «QA-інженер» → «Відхилити» біля «Іван П.» → підтвердити',
    'Повідомлення «Кандидата запрошено на співбесіду…» (статус «Запрошено»), потім «Кандидатуру відхилено.» (статус «Відхилено»); кнопок рішення для них більше немає');
  await login(page, EMPLOYER);
  await page.goto('/company/vacancies/1/applications');
  await shot(page, 'e4-applications', true);
  await page.locator('tr', { hasText: 'Оксана Ш.' }).getByRole('button', { name: 'Запросити' }).click();
  await expect(success(page)).toContainText('Кандидата запрошено');
  await expect(page.locator('tr', { hasText: 'Оксана Ш.' })).toContainText('Запрошено');
  await page.goto('/company/vacancies/2/applications');
  page.once('dialog', d => d.accept());
  await page.locator('tr', { hasText: 'Іван П.' }).getByRole('button', { name: 'Відхилити' }).click();
  await expect(success(page)).toContainText('Кандидатуру відхилено');
  await expect(page.locator('tr', { hasText: 'Іван П.' })).toContainText('Відхилено');
  await expect(page.locator('tr', { hasText: 'Іван П.' }).getByRole('button')).toHaveCount(0);
});
test('F14.2 | E4 Розгляд кандидатур | Некоректні | типовий: чужа вакансія', async ({ page }) => {
  meta('Представник ТехноСофт відкриває /company/vacancies/5/applications (вакансія «Нова Логістика»)', 'Сторінка «404 — Не знайдено»');
  await login(page, EMPLOYER);
  const resp = await page.goto('/company/vacancies/5/applications');
  expect(resp?.status()).toBe(404);
});

// ---------------- R1 / R3 / R4 / R5 / R6 / R7 ----------------
test('F15.1 | R1 Пошук резюме | Коректні | мінімальний: без фільтрів', async ({ page }) => {
  meta('Рекрутер → «Резюме»', 'Таблиця всіх резюме (усі статуси), впорядкована за прізвищем');
  await login(page, RECRUITER);
  await page.goto('/recruiter/people');
  expect(await page.locator('tbody tr').count()).toBeGreaterThanOrEqual(12);
  await shot(page, 'r1-people', true);
});
test('F15.2 | R1 Пошук резюме | Коректні | типовий: освіта не нижче «Магістр», посада «java»', async ({ page }) => {
  meta('«Резюме» → Освіта не нижче = Магістр, Посада = java → «Знайти»', 'Резюме: Олійник Віктор, Шевченко Оксана (+ Григоренко-Задорожна з посадою Java у бажаній)');
  await login(page, RECRUITER);
  await page.goto('/recruiter/people');
  await page.getByLabel('Освіта не нижче').selectOption({ label: 'Магістр' });
  await page.getByLabel('Посада (бажана або з історії)').fill('java');
  await page.getByRole('button', { name: 'Знайти' }).click();
  await expect(page.locator('tbody')).toContainText('Шевченко');
  await expect(page.locator('tbody')).toContainText('Олійник');
  await expect(page.locator('tbody')).not.toContainText('Петренко');
});
test('F15.3 | R1 Пошук резюме | Коректні | максимальний: усі фільтри', async ({ page }) => {
  meta('«Резюме» → ПІБ = пет, Посада = java, Компанія = Глобал, Освіта = Бакалавр, Зарплата 50000–90000, Статус = Шукає роботу → «Знайти»', 'Одне резюме — Петренко Іван Олегович');
  await login(page, RECRUITER);
  await page.goto('/recruiter/people?name=пет&position=java&company=Глобал&minEducation=BACHELOR&salaryFrom=50000&salaryTo=90000&status=SEARCHING');
  await expect(page.locator('tbody tr')).toHaveCount(1);
  await expect(page.locator('tbody tr')).toContainText('Петренко');
});
test('F15.4 | R1 Пошук резюме | Некоректні | типовий: зарплата від «п\'ять»', async ({ page }) => {
  meta('Відкрити /recruiter/people?salaryFrom=п\'ять', 'Діагностичне повідомлення «Фільтр зарплати містить нечислове значення…»; показано всі резюме');
  await login(page, RECRUITER);
  await page.goto('/recruiter/people?salaryFrom=' + encodeURIComponent("п'ять"));
  await expect(error(page)).toContainText('нечислове значення');
});
test('F16.1 | R3 Підбір вакансій для шукача | Коректні | типовий', async ({ page }) => {
  meta('«Резюме» → «Петренко Іван Олегович» → «Підібрати вакансії»', 'Першою — «Java-розробник», ТехноСофт, 100 % (позначка поданої кандидатури)');
  await login(page, RECRUITER);
  await page.goto('/recruiter/people/3');
  await page.getByRole('link', { name: 'Підібрати вакансії' }).click();
  await expect(page.locator('tbody tr').first()).toContainText('Java-розробник');
  await shot(page, 'r3-vacancy-matches', true);
});
test('F17.1 | R4 Історія роботи | Коректні | максимальний: 3 записи, PhD', async ({ page }) => {
  meta('«Резюме» → «Верховинець-Залізняк…»', 'Картка з 3 записами історії роботи (поточний — «досі»), 3 записами освіти, стажем 12 р. 4 міс.');
  await login(page, RECRUITER);
  await page.goto('/recruiter/people/12');
  await expect(page.locator('section', { hasText: 'Історія роботи' }).locator('tbody tr')).toHaveCount(3);
  await expect(page.locator('main')).toContainText('досі');
});
test('F17.2 | R4 Історія роботи | Коректні | мінімальний: без історії', async ({ page }) => {
  meta('«Резюме» → «Бо Ян»', 'Картка з текстом «Історію роботи не вказано.» і «Освіту не вказано.», стаж 0 р. 0 міс.');
  await login(page, RECRUITER);
  await page.goto('/recruiter/people/1');
  await expect(page.locator('main')).toContainText('Історію роботи не вказано');
  await expect(page.locator('main')).toContainText('0 р. 0 міс.');
});
test('F18.1 | R5 Ведення даних людей | Коректні | типовий: додати, редагувати, додати запис роботи, видалити', async ({ page }) => {
  meta('«Резюме» → «+ Нова людина» → прізвище «Демченко», ім\'я «Олег», телефон, e-mail, місто → «Зберегти»; «+ Додати запис» роботи → «Додати»; «Редагувати» → місто «Львів»; «Видалити» → підтвердити (текст підтвердження з кількістю записів)',
    'Повідомлення «Резюме Демченко Олег додано…», «Запис історії роботи додано.», «Резюме збережено.», «Резюме Демченко Олег видалено…»');
  await login(page, RECRUITER);
  await page.goto('/recruiter/people/new');
  await page.getByLabel('Прізвище').fill('Демченко');
  await page.getByLabel('Ім\'я').fill('Олег');
  await page.getByLabel('Телефон').fill('+380631112244');
  await page.getByLabel('E-mail').fill('oleh.demchenko@example.com');
  await page.getByLabel('Місто').fill('Київ');
  await page.getByLabel('Бажана посада').fill('Логіст');
  await shot(page, 'r5-person-form', true);
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(success(page)).toContainText('Резюме Демченко Олег додано');
  await page.getByRole('button', { name: '+ Додати запис' }).click();
  await page.locator('#work-form').getByLabel('Компанія').fill('Нова Логістика');
  await page.locator('#work-form').getByLabel('Посада').fill('Логіст');
  await page.locator('#work-form').getByLabel('Дата початку').fill('2020-01-15');
  await page.locator('#work-form').getByRole('button', { name: 'Додати' }).click();
  await expect(success(page)).toContainText('Запис історії роботи додано');
  await page.getByRole('link', { name: 'Редагувати' }).click();
  await page.getByLabel('Місто').fill('Львів');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(success(page)).toContainText('Резюме збережено');
  let confirmText = '';
  page.once('dialog', d => { confirmText = d.message(); d.accept(); });
  await page.getByRole('button', { name: 'Видалити' }).first().click();
  await expect(success(page)).toContainText('видалено');
  expect(confirmText).toContain('записів історії роботи — 1');
});
test('F18.2 | R5 Ведення даних людей | Некоректні | типовий: порожнє прізвище, цифри в імені', async ({ page }) => {
  meta('«+ Нова людина» → прізвище порожнє, ім\'я «Олег2» → «Зберегти»', 'Повідомлення «Не вказано прізвище…», «Ім’я містить недопустимі символи…»; людину не додано');
  await login(page, RECRUITER);
  await page.goto('/recruiter/people/new');
  await page.getByLabel('Ім\'я').fill('Олег2');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(page.locator('main')).toContainText('Не вказано прізвище');
  await expect(page.locator('main')).toContainText('Ім’я містить недопустимі символи');
});
test('F19.1 | R6 Компанії та вакансії | Коректні | типовий: додати компанію і вакансію, видалити', async ({ page }) => {
  meta('«Компанії» → «+ Нова компанія» → «БудКом», Одеса, адреса, телефон, e-mail → «Зберегти»; «+ Нова вакансія» → «Виконроб», 40000 → «Зберегти»; «Видалити» компанію → підтвердити',
    'Повідомлення «Компанію «БудКом» додано.», «Вакансію «Виконроб» додано.», «Компанію «БудКом» видалено разом з її вакансіями.»');
  await login(page, RECRUITER);
  await page.goto('/recruiter/companies');
  await shot(page, 'r6-companies');
  await page.getByRole('link', { name: '+ Нова компанія' }).click();
  await page.getByLabel('Назва').fill('БудКом');
  await page.getByLabel('Місто').fill('Одеса');
  await page.getByLabel('Адреса офісу').fill('вул. Дерибасівська, 1');
  await page.getByLabel('Телефон').fill('+380482223344');
  await page.getByLabel('E-mail').fill('hr@budkom.ua');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(success(page)).toContainText('Компанію «БудКом» додано');
  await page.getByRole('link', { name: '+ Нова вакансія' }).click();
  await page.getByLabel('Посада').fill('Виконроб');
  await page.getByLabel('Зарплата, грн/міс').fill('40000');
  await page.getByLabel('Місто роботи').fill('Одеса');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(success(page)).toContainText('Вакансію «Виконроб» додано');
  await shot(page, 'r6-company', true);
  page.once('dialog', d => d.accept());
  await page.locator('.toolbar').getByRole('button', { name: 'Видалити' }).click();
  await expect(success(page)).toContainText('видалено разом з її вакансіями');
});
test('F19.2 | R6 Компанії та вакансії | Некоректні | типовий: назва вже існує', async ({ page }) => {
  meta('«+ Нова компанія» → Назва = «ТехноСофт», решта полів коректні → «Зберегти»', 'Повідомлення «Компанія з назвою «ТехноСофт» вже існує.» з порадою змінити назву');
  await login(page, RECRUITER);
  await page.goto('/recruiter/companies/new');
  await page.getByLabel('Назва').fill('ТехноСофт');
  await page.getByLabel('Місто').fill('Київ');
  await page.getByLabel('Адреса офісу').fill('вул. Тестова, 1');
  await page.getByLabel('Телефон').fill('+380441110000');
  await page.getByLabel('E-mail').fill('x@x.ua');
  await page.getByRole('button', { name: 'Зберегти' }).click();
  await expect(error(page)).toContainText('вже існує');
});
test('F20.1 | R7 Працевлаштування | Некоректні | максимальний: дата початку +91 день', async ({ page }) => {
  meta('«Кандидатури» → фільтр «Запрошено» → «Зареєструвати працевлаштування» біля «Бондар Марія» → дата = сьогодні + 91 день → «Підтвердити» → підтвердити',
    'Повідомлення «Дата початку роботи не вказана або пізніша за …»; статус кандидатури не змінено');
  await login(page, RECRUITER);
  await page.goto('/recruiter/applications?status=INVITED');
  await page.locator('tr', { hasText: 'Бондар' }).getByRole('link', { name: 'Зареєструвати працевлаштування' }).click();
  const d = new Date(); d.setDate(d.getDate() + 91);
  await page.getByLabel('Дата початку роботи').fill(d.toISOString().slice(0, 10));
  page.once('dialog', dl => dl.accept());
  await page.getByRole('button', { name: 'Підтвердити' }).click();
  await expect(error(page)).toContainText('Дата початку роботи');
});
test('F20.2 | R7 Працевлаштування | Коректні | типовий: дата за замовчуванням', async ({ page }) => {
  meta('«Кандидатури» → «Запрошено» → «Зареєструвати працевлаштування» біля «Бондар Марія» → дата сьогодні → «Підтвердити» → підтвердити',
    'Повідомлення «Працевлаштування Бондар Марія Сергіївна на вакансію «Frontend-розробник» (ТехноСофт) зареєстровано.»; вакансія закрита; статус людини «Не шукає»; в історії роботи — новий запис «досі»');
  await login(page, RECRUITER);
  await page.goto('/recruiter/applications?status=INVITED');
  await shot(page, 'r7-applications', true);
  await page.locator('tr', { hasText: 'Бондар' }).getByRole('link', { name: 'Зареєструвати працевлаштування' }).click();
  await shot(page, 'r7-hire', true);
  page.once('dialog', dl => dl.accept());
  await page.getByRole('button', { name: 'Підтвердити' }).click();
  await expect(success(page)).toContainText('Працевлаштування Бондар Марія Сергіївна на вакансію «Frontend-розробник» (ТехноСофт) зареєстровано');
  await page.goto('/recruiter/people/6');
  await expect(page.locator('main')).toContainText('Не шукає');
  await expect(page.locator('section', { hasText: 'Історія роботи' }).locator('tbody tr').first()).toContainText('ТехноСофт');
});
test('F20.3 | R7 Працевлаштування | Некоректні | типовий: кандидатура не «Запрошено»', async ({ page }) => {
  meta('Відкрити форму працевлаштування для кандидатури зі статусом «Нова» (Коваленко → QA-інженер) і натиснути «Підтвердити»', 'Повідомлення «Працевлаштування можна зареєструвати лише для кандидатури зі статусом «Запрошено»…»');
  await login(page, RECRUITER);
  await page.goto('/recruiter/applications?status=NEW');
  const href = await page.locator('tr', { hasText: 'Коваленко' }).locator('a', { hasText: 'QA-інженер' }).count();
  expect(href).toBe(1);
  await page.goto('/recruiter/applications/2/hire');
  page.once('dialog', dl => dl.accept());
  await page.getByRole('button', { name: 'Підтвердити' }).click();
  await expect(error(page)).toContainText('лише для кандидатури зі статусом «Запрошено»');
});

// ---------------- Безпека / ролі ----------------
test('F21.1 | Доступ за ролями | Некоректні | типовий: шукач відкриває панель рекрутера', async ({ page }) => {
  meta('Увійти як шукач → відкрити /recruiter', 'Сторінка «403 — Доступ заборонено»');
  await login(page, CANDIDATE);
  const resp = await page.goto('/recruiter');
  expect(resp?.status()).toBe(403);
  await expect(page.getByRole('heading', { name: /403/ })).toBeVisible();
});
test('F21.2 | Неіснуюча сторінка | Некоректні | типовий', async ({ page }) => {
  meta('Відкрити /no-such-page', 'Сторінка «404 — Не знайдено» з посиланнями «На головну», «До вакансій»');
  const resp = await page.goto('/no-such-page');
  expect(resp?.status()).toBe(404);
  await shot(page, 'x-404');
});
