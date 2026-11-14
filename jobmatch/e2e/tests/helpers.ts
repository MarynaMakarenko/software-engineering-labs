import { Page, expect, test } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

export const PASSWORD = 'Demo2026!';
export const RECRUITER = 'olena.koval@profikadr.ua';
export const CANDIDATE = 'ivan.petrenko@example.com';
export const EMPLOYER = 'hr@technosoft.ua';

const SHOTS = path.resolve(__dirname, '../screenshots');
fs.mkdirSync(SHOTS, { recursive: true });

export async function login(page: Page, email: string, password = PASSWORD) {
  await page.goto('/login');
  await page.getByLabel('E-mail').fill(email);
  await page.getByLabel('Пароль').fill(password);
  await page.getByRole('button', { name: 'Увійти' }).click();
}

export async function shot(page: Page, name: string, fullPage = false) {
  await page.screenshot({ path: path.join(SHOTS, name + '.png'), fullPage });
}

/** Опис тесту для звіту: кроки та очікувана реакція (потрапляють у results.json). */
export function meta(steps: string, expected: string, data = '') {
  test.info().annotations.push({ type: 'steps', description: steps });
  test.info().annotations.push({ type: 'expected', description: expected });
  if (data) test.info().annotations.push({ type: 'data', description: data });
}

export const success = (page: Page) => page.locator('.alert-success');
export const error = (page: Page) => page.locator('.alert-error');
export { expect };
