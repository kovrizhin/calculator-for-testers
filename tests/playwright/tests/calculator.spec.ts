import { expect, test } from '../src/fixtures';

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/;
const SAMPLE_EQUATION = '6+7+(3+4*5) * 3';

test.describe('calculator page', () => {
  test('submits an equation and shows its operation id', async ({ loggedIn: calculator }) => {
    const id = await calculator.submitAndGetId(SAMPLE_EQUATION, 'HIGH');

    expect(id).toMatch(UUID);
    await expect(calculator.operationIdInput).toHaveValue(id);
    await expect(calculator.submitError).toBeHidden();
  });

  test('shows the result once the operation is done', async ({ loggedIn: calculator }) => {
    await calculator.submitAndGetId(SAMPLE_EQUATION, 'HIGH');

    await expect
      .poll(
        async () => {
          await calculator.getResult();
          return calculator.resultStatus.textContent();
        },
        { timeout: 10_000, intervals: [500] },
      )
      .toBe('DONE');
    await expect(calculator.resultValue).toHaveText('82');
  });

  test('shows the server message for an invalid equation', async ({ loggedIn: calculator }) => {
    await calculator.submit('2++3', 'HIGH');

    await expect(calculator.submitError).toContainText("Unexpected token '+'");
    await expect(calculator.submitResult).toBeHidden();
  });

  test('lists the submitted operation in the table', async ({ loggedIn: calculator }) => {
    const id = await calculator.submitAndGetId('2*3', 'HIGH');

    const row = calculator.rowById(id);
    await expect(row).toHaveCount(1);
    await expect(row.locator('.op-equation')).toHaveText('2*3');
    await expect(row.locator('.op-priority')).toHaveText('HIGH');
  });
});
