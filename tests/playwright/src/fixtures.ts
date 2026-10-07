import { test as base, expect } from '@playwright/test';
import { CalculatorPage } from './pages/CalculatorPage';
import { LoginPage } from './pages/LoginPage';
import { users } from './users';

interface Fixtures {
  loginPage: LoginPage;
  calculatorPage: CalculatorPage;
  loggedIn: CalculatorPage;
}

export const test = base.extend<Fixtures>({
  loginPage: async ({ page }, use) => {
    await use(new LoginPage(page));
  },
  calculatorPage: async ({ page }, use) => {
    await use(new CalculatorPage(page));
  },
  loggedIn: async ({ loginPage, calculatorPage }, use) => {
    await loginPage.goto();
    await loginPage.login(users.alice.username, users.alice.password);
    await expect(calculatorPage.root).toBeVisible();
    await use(calculatorPage);
  },
});

export { expect };
