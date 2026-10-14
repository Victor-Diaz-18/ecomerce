import { useEffect, useState } from 'react';

type Theme = 'light' | 'dark';
const KEY = 'university-store-theme';

// Without a saved choice the page follows the operating system's light or dark mode
function currentTheme(): Theme {
  const chosen = document.documentElement.getAttribute('data-theme');
  if (chosen === 'light' || chosen === 'dark') return chosen;
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

export function useTheme() {
  const [theme, setTheme] = useState<Theme>(currentTheme);

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  const toggle = () => {
    const next: Theme = theme === 'dark' ? 'light' : 'dark';
    setTheme(next);
    try {
      localStorage.setItem(KEY, next);
    } catch {
      // storage unavailable: the choice lasts until the page is reloaded
    }
  };

  return { theme, toggle };
}
