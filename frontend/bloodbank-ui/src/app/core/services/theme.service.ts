import { Injectable, signal, computed, effect, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export type Theme = 'light' | 'dark';

/**
 * Theme service — manages dark/light mode toggle.
 * Persists preference and respects system preference.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly platformId = inject(PLATFORM_ID);

  /** Current theme */
  readonly theme = signal<Theme>(this.getInitialTheme());

  /** Whether dark mode is active */
  readonly isDark = computed(() => this.theme() === 'dark');

  constructor() {
    // Apply theme class to document when theme changes
    effect(() => {
      if (isPlatformBrowser(this.platformId)) {
        const html = document.documentElement;
        if (this.theme() === 'dark') {
          html.classList.add('dark-theme');
        } else {
          html.classList.remove('dark-theme');
        }
        localStorage.setItem('bloodbank-theme', this.theme());
      }
    });
  }

  /** Toggle between light and dark themes */
  toggle(): void {
    this.theme.set(this.isDark() ? 'light' : 'dark');
  }

  /** Set a specific theme */
  setTheme(theme: Theme): void {
    this.theme.set(theme);
  }

  /** Determine initial theme from localStorage or system preference */
  private getInitialTheme(): Theme {
    if (isPlatformBrowser(this.platformId)) {
      const stored = localStorage.getItem('bloodbank-theme') as Theme | null;
      if (stored === 'light' || stored === 'dark') {
        return stored;
      }
      // Respect system preference
      if (window.matchMedia?.('(prefers-color-scheme: dark)').matches) {
        return 'dark';
      }
    }
    return 'light';
  }
}
