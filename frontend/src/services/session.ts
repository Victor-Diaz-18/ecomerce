import type { Session } from '../types';

// The login is kept in the browser so that reloading the page does not log the user out
const KEY = 'university-store-session';

// Fired when the saved session changes (login, logout, or the server rejecting an expired token)
export const SESSION_EVENT = 'university-store-session-changed';

export function loadSession(): Session | null {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as Session) : null;
  } catch {
    return null;
  }
}

export function saveSession(session: Session | null) {
  if (session) localStorage.setItem(KEY, JSON.stringify(session));
  else localStorage.removeItem(KEY);
  window.dispatchEvent(new Event(SESSION_EVENT));
}
