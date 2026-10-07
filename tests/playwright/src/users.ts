export interface User {
  username: string;
  password: string;
}

export const users = {
  alice: { username: 'alice', password: 'alice123' },
  bob: { username: 'bob', password: 'bob123' },
  carol: { username: 'carol', password: 'carol123' },
} as const satisfies Record<string, User>;
