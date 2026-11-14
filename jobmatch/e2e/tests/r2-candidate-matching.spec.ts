import { test } from '@playwright/test';
import { login, meta, shot, success, error, expect, RECRUITER } from './helpers';

/**
 * Таблиця 2. Тести функцій на підтримку критичного UC «R2 Підбір кандидатів на вакансію».
 * Назва тесту: <ID> | <потік> | <тип даних> | <сценарій>.
 */
test.describe.configure({ mode: 'serial' });

test.beforeEach(async ({ page }) => {
  await login(page, RECRUITER);
});

const row = (page, name: string) => page.locator('#candidates tbody tr', { hasText: name });

async function propose(page, vacancy: number, name: string, minScore: number, comment: string) {
  await page.goto(`/recruiter/vacancies/${vacancy}/matches?minScore=${minScore}`);
  await row(page, name).getByRole('link', { name: 'Запропонувати' }).click();
  await expect(page.getByRole('heading', { name: 'Пропозиція кандидата' })).toBeVisible();
  await page.getByLabel('Коментар для компанії').fill(comment);
  await page.getByRole('button', { name: 'Підтвердити' }).click();
}

// ---------- Основний потік, коректні дані ----------
test('T2-01 | Основний | Коректні | мінімальний: коментар порожній', async ({ page }) => {
  meta('Панель рекрутера → «Підібрати кандидатів» біля «Java-розробник» → «Запропонувати» біля «Петренко Іван Олегович» → поле «Коментар» порожнє → «Підтвердити»',
    'Повідомлення «Кандидатуру Петренко Іван Олегович запропоновано на вакансію «Java-розробник» (ТехноСофт)»; у рядку — «Запропоновано: Нова»', 'коментар: 0 символів');
  await page.goto('/recruiter');
  await shot(page, 'r2-01-dashboard');
  await page.locator('tr', { hasText: 'Java-розробник' }).getByRole('link', { name: 'Підібрати кандидатів' }).click();
  await expect(page.getByRole('heading', { name: 'Підбір кандидатів' })).toBeVisible();
  await expect(page.locator('#candidates tbody tr').first()).toContainText('Петренко');
  await shot(page, 'r2-02-matches', true);
  await row(page, 'Петренко').getByRole('link', { name: 'Запропонувати' }).click();
  await shot(page, 'r2-03-propose-form');
  await page.getByRole('button', { name: 'Підтвердити' }).click();
  await expect(success(page)).toContainText('Кандидатуру Петренко Іван Олегович запропоновано на вакансію «Java-розробник» (ТехноСофт)');
  await expect(row(page, 'Петренко')).toContainText('Запропоновано: Нова');
  await shot(page, 'r2-04-proposed');
});

test('T2-02 | Основний | Коректні | майже мінімальний: коментар 1 символ', async ({ page }) => {
  meta('Підбір для «Java-розробник» → «Запропонувати» біля «Бондар Марія» → коментар «+» → «Підтвердити»',
    'Повідомлення про пропозицію; статус «Запропоновано: Нова»', 'коментар: 1 символ');
  await propose(page, 1, 'Бондар', 50, '+');
  await expect(success(page)).toContainText('Бондар Марія Сергіївна');
  await expect(row(page, 'Бондар')).toContainText('Запропоновано: Нова');
});

test('T2-03 | Основний | Коректні | типовий: коментар 96 символів', async ({ page }) => {
  meta('Підбір для «Java-розробник» → «Запропонувати» біля «Григоренко-Задорожна» → типовий коментар → «Підтвердити»',
    'Повідомлення про пропозицію; статус «Запропоновано: Нова»', 'коментар: 96 символів');
  await propose(page, 1, 'Григоренко', 50, 'Досвід Java і Kotlin 8 років, мікросервіси, CI/CD. Готова до співбесіди наступного тижня.');
  await expect(success(page)).toContainText('Григоренко-Задорожна');
});

test('T2-04 | Основний | Коректні | майже максимальний: коментар 499 символів', async ({ page }) => {
  meta('Підбір для «QA-інженер» → «Запропонувати» біля «Лі Ія» → коментар 499 символів → «Підтвердити»',
    'Повідомлення про пропозицію; статус «Запропоновано: Нова»', 'коментар: 499 символів');
  await propose(page, 2, 'Лі Ія', 50, 'к'.repeat(499));
  await expect(success(page)).toContainText('Лі Ія');
});

test('T2-05 | Основний | Коректні | максимальний: коментар 500 символів', async ({ page }) => {
  meta('Підбір для «QA-інженер» (мін. оцінка 0) → «Запропонувати» біля «Верховинець-Залізняк…» → коментар 500 символів → «Підтвердити»',
    'Повідомлення про пропозицію; статус «Запропоновано: Нова»', 'коментар: 500 символів');
  await propose(page, 2, 'Верховинець', 0, 'к'.repeat(500));
  await expect(success(page)).toContainText('Верховинець-Залізняк');
});

// ---------- Основний потік, некоректні дані ----------
test('T2-06 | Основний | Некоректні | типовий: підмінений ідентифікатор кандидата', async ({ page }) => {
  meta('Відкрити адресу /recruiter/vacancies/1/propose?personId=abc', 'Сторінка «404 — Не знайдено», кандидатура не створюється', 'personId = abc');
  const resp = await page.goto('/recruiter/vacancies/1/propose?personId=abc');
  expect(resp?.status()).toBe(404);
  await expect(page.getByRole('heading', { name: '404 — Не знайдено' })).toBeVisible();
});

// ---------- A1: немає кандидатів з оцінкою ≥ порогу ----------
test('T2-07 | A1 | Коректні | типовий: порожній список → часткові збіги', async ({ page }) => {
  meta('Компанії → «Ок» → «Підібрати кандидатів» біля «ІТ» (поріг 50) → «Показати часткові збіги (від 25 %)»',
    'Повідомлення «Немає кандидатів з оцінкою від 50 %.» і кнопка; після натискання — список з порогом 25 % (усі оцінки ≥ 25 %)');
  await page.goto('/recruiter/vacancies/12/matches');
  await expect(page.locator('#no-candidates')).toContainText('Немає кандидатів з оцінкою від 50 %');
  await shot(page, 'r2-05-empty');
  await page.getByRole('link', { name: 'Показати часткові збіги (від 25 %)' }).click();
  await expect(page).toHaveURL(/minScore=25/);
  const scores = await page.locator('#candidates .score').allTextContents();
  expect(scores.length).toBeGreaterThan(0);
  for (const s of scores) expect(parseInt(s)).toBeGreaterThanOrEqual(25);
  await shot(page, 'r2-06-partial');
});

test('T2-08 | A1 | Коректні | максимальний поріг 100 % без кандидатів', async ({ page }) => {
  meta('Відкрити підбір для вакансії «ІТ» з порогом 100', 'Повідомлення «Немає кандидатів з оцінкою від 100 %.» і кнопка «Показати часткові збіги»', 'мін. оцінка = 100');
  await page.goto('/recruiter/vacancies/12/matches?minScore=100');
  await expect(page.locator('#no-candidates')).toContainText('від 100 %');
  await expect(page.getByRole('link', { name: /часткові збіги/ })).toBeVisible();
});

// ---------- A2: зміна порогу ----------
for (const [id, value, label] of [['T2-09', '0', 'мінімальний'], ['T2-10', '1', 'майже мінімальний'], ['T2-11', '50', 'типовий'], ['T2-12', '99', 'майже максимальний'], ['T2-13', '100', 'максимальний']] as const) {
  test(`${id} | A2 | Коректні | ${label}: мін. оцінка ${value}`, async ({ page }) => {
    meta(`Підбір для «Java-розробник» → поле «Мін. оцінка, %» = ${value} → «Оновити»`,
      `Список кандидатів з оцінкою ≥ ${value} %, відсортований за спаданням; повідомлень про помилку немає`, `мін. оцінка = ${value}`);
    await page.goto('/recruiter/vacancies/1/matches');
    await page.getByLabel('Мін. оцінка, %').fill(value);
    await page.getByRole('button', { name: 'Оновити' }).click();
    await expect(error(page)).toHaveCount(0);
    const scores = (await page.locator('#candidates .score').allTextContents()).map(s => parseInt(s));
    expect(scores.length).toBeGreaterThan(0);
    for (const s of scores) expect(s).toBeGreaterThanOrEqual(parseInt(value));
    expect([...scores].sort((a, b) => b - a)).toEqual(scores);
  });
}

for (const [id, value, label] of [['T2-14', '-1', 'мінімальний'], ['T2-15', 'abc', 'типовий'], ['T2-16', '101', 'максимальний']] as const) {
  test(`${id} | A2 | Некоректні | ${label}: мін. оцінка «${value}»`, async ({ page }) => {
    meta(`Підбір для «Java-розробник» → поле «Мін. оцінка, %» = ${value} → «Оновити»`,
      'Діагностичне повідомлення «Мінімальна оцінка … має бути цілим числом від 0 до 100» з трьома частинами; список побудовано з порогом 50 %', `мін. оцінка = ${value}`);
    await page.goto('/recruiter/vacancies/1/matches');
    await page.getByLabel('Мін. оцінка, %').fill(value);
    await page.getByRole('button', { name: 'Оновити' }).click();
    await expect(error(page)).toContainText('має бути цілим числом від 0 до 100');
    await expect(error(page)).toContainText('Як виправити зараз');
    await expect(error(page)).toContainText('Як запобігти надалі');
    await expect(page.getByLabel('Мін. оцінка, %')).toHaveValue('50');
    if (value === 'abc') await shot(page, 'r2-07-threshold-error');
  });
}

// ---------- A3: кандидатуру вже створено ----------
test('T2-17 | A3 | Коректні | типовий: кандидата вже запропоновано', async ({ page }) => {
  meta('Підбір для «Java-розробник» → рядок «Шевченко Оксана»', 'Кнопки «Запропонувати» немає; показано «Запропоновано: Нова»');
  await page.goto('/recruiter/vacancies/1/matches');
  await expect(row(page, 'Шевченко')).toContainText('Запропоновано: Нова');
  await expect(row(page, 'Шевченко').getByRole('link', { name: 'Запропонувати' })).toHaveCount(0);
});

test('T2-18 | A3 | Коректні | кандидат сам відгукнувся', async ({ page }) => {
  meta('Підбір для «QA-інженер» → рядок «Коваленко Дмитро»', 'Кнопки «Запропонувати» немає; показано «Відгукнувся: Нова»');
  await page.goto('/recruiter/vacancies/2/matches');
  await expect(row(page, 'Коваленко')).toContainText('Відгукнувся: Нова');
});

test('T2-19 | A3 | Некоректні | типовий: повторне підтвердження з другої вкладки', async ({ page, context }) => {
  meta('Відкрити форму пропозиції «Кравчук Олена» → «Агроном» у двох вкладках → «Підтвердити» в першій, потім у другій',
    'Друга спроба: повідомлення «Кандидатура … вже існує» з трьома частинами; дублікат не створено', 'дві вкладки');
  const url = '/recruiter/vacancies/7/propose?personId=8&minScore=50';
  await page.goto(url);
  const second = await context.newPage();
  await second.goto(url);
  await page.getByRole('button', { name: 'Підтвердити' }).click();
  await expect(success(page)).toBeVisible();
  await second.getByRole('button', { name: 'Підтвердити' }).click();
  await expect(error(second)).toContainText('вже існує');
  await shot(second, 'r2-08-duplicate');
  await second.goto('/recruiter/people/8');
  await expect(second.locator('section', { hasText: 'Кандидатури' }).locator('tbody tr', { hasText: 'Агроном' })).toHaveCount(1);
});

// ---------- A4: вакансію закрито ----------
test('T2-20 | A4 | Коректні | типовий: підбір для закритої вакансії', async ({ page }) => {
  meta('Відкрити підбір для закритої вакансії «Project manager»', 'Повідомлення «Вакансію закрито — підбір недоступний» і посилання «До відкритих вакансій»; таблиці кандидатів немає');
  await page.goto('/recruiter/vacancies/4/matches');
  await expect(page.locator('#vacancy-closed')).toContainText('Вакансію закрито — підбір недоступний');
  await expect(page.locator('#candidates')).toHaveCount(0);
  await shot(page, 'r2-09-closed');
});

test('T2-21 | A4 | Некоректні | типовий: вакансію закрили під час заповнення форми', async ({ page, context }) => {
  meta('Відкрити форму пропозиції «Мельник Сергій» → «Бухгалтер»; в іншій вкладці закрити вакансію; натиснути «Підтвердити»',
    'Повідомлення «Вакансію «Бухгалтер» закрито — кандидатури не приймаються»; кандидатура не створюється');
  await page.goto('/recruiter/vacancies/10/propose?personId=9&minScore=50');
  const other = await context.newPage();
  await other.goto('/recruiter/companies/5');
  await other.locator('tr', { hasText: 'Бухгалтер' }).getByRole('button', { name: 'Закрити' }).click();
  await page.getByRole('button', { name: 'Підтвердити' }).click();
  await expect(error(page)).toContainText('закрито');
});

// ---------- A5: коментар довший за 500 ----------
for (const [id, len, label] of [['T2-22', 501, 'типовий'], ['T2-23', 1000, 'максимальний']] as const) {
  test(`${id} | A5 | Некоректні | ${label}: коментар ${len} символів`, async ({ page }) => {
    meta(`Форма пропозиції «Ткаченко Андрій» → «Водій-експедитор» → вставити коментар ${len} символів (обійти maxlength) → «Підтвердити»`,
      'Діагностичне повідомлення «Коментар довший за 500 символів. Скоротіть коментар… Пишіть 2–3 речення…»; введений текст збережено; кандидатура не створюється', `коментар: ${len} символів`);
    await page.goto('/recruiter/vacancies/6/propose?personId=7&minScore=50');
    await page.locator('#comment').evaluate((el: HTMLTextAreaElement, n: number) => { el.removeAttribute('maxlength'); el.value = 'к'.repeat(n); }, len);
    await page.getByRole('button', { name: 'Підтвердити' }).click();
    await expect(error(page)).toContainText('Коментар довший за 500 символів');
    await expect(page.locator('#comment')).toHaveValue('к'.repeat(len));
    if (len === 501) await shot(page, 'r2-10-long-comment');
  });
}

// ---------- A6: скасування ----------
test('T2-24 | A6 | Коректні | типовий: «Скасувати» у формі пропозиції', async ({ page }) => {
  meta('Підбір для «Frontend-розробник» (мін. оцінка 25) → «Запропонувати» біля «Петренко Іван Олегович» → ввести коментар → «Скасувати»',
    'Повернення до списку підбору; кандидатура не створена (кнопка «Запропонувати» доступна)');
  await page.goto('/recruiter/vacancies/3/matches?minScore=25');
  await row(page, 'Петренко').getByRole('link', { name: 'Запропонувати' }).click();
  await page.getByLabel('Коментар для компанії').fill('Тест скасування');
  await page.getByRole('link', { name: 'Скасувати' }).click();
  await expect(page).toHaveURL(/\/recruiter\/vacancies\/3\/matches\?minScore=25/);
  await expect(row(page, 'Петренко').getByRole('link', { name: 'Запропонувати' })).toBeVisible();
});

// ---------- A7: запис видалено ----------
test('T2-25 | A7 | Некоректні | типовий: вакансію видалено', async ({ page }) => {
  meta('Відкрити адресу підбору для неіснуючої вакансії /recruiter/vacancies/999/matches', 'Сторінка «404 — Не знайдено» з посиланням на панель рекрутера');
  const resp = await page.goto('/recruiter/vacancies/999/matches');
  expect(resp?.status()).toBe(404);
  await expect(page.getByRole('link', { name: 'До панелі рекрутера' })).toBeVisible();
  await shot(page, 'r2-11-404');
});

test('T2-26 | A7 | Некоректні | людину видалено', async ({ page }) => {
  meta('Відкрити форму пропозиції для неіснуючої людини /recruiter/vacancies/1/propose?personId=999', 'Сторінка «404 — Не знайдено»');
  const resp = await page.goto('/recruiter/vacancies/1/propose?personId=999');
  expect(resp?.status()).toBe(404);
});

// ---------- крок 4: картка людини ----------
test('T2-27 | Основний (крок 4) | Коректні | перегляд картки й історії кандидата', async ({ page }) => {
  meta('Підбір для «Java-розробник» → натиснути «Петренко Іван Олегович»', 'Картка людини з особистими даними, освітою й історією роботи (2 записи, стаж 7 р. 3 міс.)');
  await page.goto('/recruiter/vacancies/1/matches');
  await page.getByRole('link', { name: 'Петренко Іван Олегович' }).click();
  await expect(page.getByRole('heading', { name: 'Петренко Іван Олегович' })).toBeVisible();
  await expect(page.locator('section', { hasText: 'Історія роботи' }).locator('tbody tr')).toHaveCount(2);
  await shot(page, 'r2-12-person', true);
});
