import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  workers: 1,
  fullyParallel: false,
  timeout: 30_000,
  reporter: [['list'], ['json', { outputFile: 'results.json' }]],
  use: {
    baseURL: 'http://localhost:8090',
    channel: 'chrome',
    headless: true,
    viewport: { width: 1280, height: 860 },
    locale: 'uk-UA',
  },
});
