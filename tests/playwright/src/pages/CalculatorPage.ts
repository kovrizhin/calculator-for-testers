import type { Locator, Page } from '@playwright/test';

export type Priority = 'HIGH' | 'MEDIUM' | 'LOW';

export class CalculatorPage {
  readonly root: Locator;
  readonly currentUser: Locator;
  readonly logoutButton: Locator;
  readonly equationInput: Locator;
  readonly prioritySelect: Locator;
  readonly submitButton: Locator;
  readonly submitError: Locator;
  readonly submitResult: Locator;
  readonly submittedId: Locator;
  readonly operationIdInput: Locator;
  readonly getResultButton: Locator;
  readonly resultError: Locator;
  readonly resultBox: Locator;
  readonly resultStatus: Locator;
  readonly resultValue: Locator;
  readonly refreshButton: Locator;
  readonly operationsTable: Locator;
  readonly operationRows: Locator;
  readonly operationsEmpty: Locator;

  constructor(readonly page: Page) {
    this.root = page.getByTestId('calculator-page');
    this.currentUser = page.getByTestId('current-user');
    this.logoutButton = page.getByTestId('logout-button');
    this.equationInput = page.getByTestId('equation-input');
    this.prioritySelect = page.getByTestId('priority-select');
    this.submitButton = page.getByTestId('submit-button');
    this.submitError = page.getByTestId('submit-error');
    this.submitResult = page.getByTestId('submit-result');
    this.submittedId = page.getByTestId('submitted-id');
    this.operationIdInput = page.getByTestId('operation-id-input');
    this.getResultButton = page.getByTestId('get-result-button');
    this.resultError = page.getByTestId('result-error');
    this.resultBox = page.getByTestId('result-box');
    this.resultStatus = page.getByTestId('result-status');
    this.resultValue = page.getByTestId('result-value');
    this.refreshButton = page.getByTestId('refresh-button');
    this.operationsTable = page.getByTestId('operations-table');
    this.operationRows = page.getByTestId('operation-row');
    this.operationsEmpty = page.getByTestId('operations-empty');
  }

  async goto(): Promise<void> {
    await this.page.goto('/calculator.html');
  }

  async submit(equation: string, priority?: Priority): Promise<void> {
    await this.equationInput.fill(equation);
    if (priority) {
      await this.prioritySelect.selectOption(priority);
    }
    await this.submitButton.click();
  }

  async submitAndGetId(equation: string, priority?: Priority): Promise<string> {
    await this.submit(equation, priority);
    await this.submitResult.waitFor({ state: 'visible' });
    return (await this.submittedId.textContent())?.trim() ?? '';
  }

  async getResult(id?: string): Promise<void> {
    if (id !== undefined) {
      await this.operationIdInput.fill(id);
    }
    await this.getResultButton.click();
  }

  rowById(id: string): Locator {
    return this.operationRows.filter({ has: this.page.locator(`.op-id`, { hasText: id }) });
  }
}
