import { expect, test } from '../src/fixtures';
import { users } from '../src/users';

test.describe('login page', () => {
  test('shows an error for a wrong password', async ({ loginPage, page }) => {
    await loginPage.goto();
    await loginPage.login(users.alice.username, 'wrong-password');

    await expect(loginPage.error).toHaveText('Invalid username or password');
    await expect(page).toHaveURL(/\/$/);
  });

  test('requires both username and password', async ({ loginPage }) => {
    await loginPage.goto();
    await loginPage.loginButton.click();

    await expect(loginPage.error).toHaveText('Username and password are required');
  });

  test('redirects to the calculator after a valid login', async ({ loginPage, calculatorPage, page }) => {
    await loginPage.goto();
    await loginPage.login(users.alice.username, users.alice.password);

    await expect(page).toHaveURL(/\/calculator\.html$/);
    await expect(calculatorPage.currentUser).toHaveText(users.alice.username);
  });

  test('redirects to login when the calculator is opened without logging in', async ({ calculatorPage, loginPage, page }) => {
    await calculatorPage.goto();

    await expect(page).toHaveURL(/\/$/);
    await expect(loginPage.loginButton).toBeVisible();
  });
});
