import { Injectable, PLATFORM_ID, inject, signal, computed } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { TranslateService } from '@ngx-translate/core';
import { firstValueFrom } from 'rxjs';

/**
 * Supported application locales.
 */
export type AppLocale = 'en' | 'es' | 'fr';

export interface LanguageOption {
  code: AppLocale;
  label: string;
  nativeLabel: string;
}

/**
 * Service that manages the active UI language.
 * Persists the selection to localStorage so it survives page reloads.
 */
@Injectable({ providedIn: 'root' })
export class LanguageService {
  private readonly translate = inject(TranslateService);
  private readonly platformId = inject(PLATFORM_ID);

  private static readonly STORAGE_KEY = 'bb_lang';
  private static readonly DEFAULT_LOCALE: AppLocale = 'en';

  readonly supportedLanguages: LanguageOption[] = [
    { code: 'en', label: 'English', nativeLabel: 'English' },
    { code: 'es', label: 'Spanish', nativeLabel: 'Español' },
    { code: 'fr', label: 'French', nativeLabel: 'Français' },
  ];

  private readonly _currentLocale = signal<AppLocale>(LanguageService.DEFAULT_LOCALE);

  /** Currently active locale (reactive signal). */
  readonly currentLocale = this._currentLocale.asReadonly();

  /** The current language option object. */
  readonly currentLanguage = computed(() =>
    this.supportedLanguages.find((l) => l.code === this._currentLocale()) ??
    this.supportedLanguages[0],
  );

  /**
   * Initializes the translate service with all supported locales,
   * then applies the persisted (or browser-default) language.
   * Call this once during application startup.
   */
  initialize(): void {
    const codes = this.supportedLanguages.map((l) => l.code);
    this.translate.addLangs(codes);
    this.translate.setDefaultLang(LanguageService.DEFAULT_LOCALE).subscribe();

    const saved = this.loadSavedLocale();
    const browserLang = TranslateService.getBrowserLang() as AppLocale | undefined;
    const resolved =
      saved ?? (browserLang && codes.includes(browserLang) ? browserLang : LanguageService.DEFAULT_LOCALE);

    this.applyLocale(resolved);
  }

  /**
   * Changes the active language immediately and persists the choice.
   */
  setLanguage(code: AppLocale): void {
    this.applyLocale(code);
    this.saveLocale(code);
  }

  // ── Private helpers ────────────────────────────────────────────

  private applyLocale(code: AppLocale): void {
    void firstValueFrom(this.translate.use(code));
    this._currentLocale.set(code);
    if (isPlatformBrowser(this.platformId)) {
      document.documentElement.lang = code;
    }
  }

  private loadSavedLocale(): AppLocale | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }
    const stored = localStorage.getItem(LanguageService.STORAGE_KEY);
    const codes = this.supportedLanguages.map((l) => l.code);
    return stored && codes.includes(stored as AppLocale) ? (stored as AppLocale) : null;
  }

  private saveLocale(code: AppLocale): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(LanguageService.STORAGE_KEY, code);
    }
  }
}
