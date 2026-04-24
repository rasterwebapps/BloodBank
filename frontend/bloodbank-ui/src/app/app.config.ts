import {
  ApplicationConfig,
  APP_INITIALIZER,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import {
  provideHttpClient,
  withInterceptors,
  withFetch,
} from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { KeycloakService } from 'keycloak-angular';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';

import { routes } from './app.routes';
import { initializeKeycloak } from '@core/auth/auth.init';
import { AuthService } from '@core/auth/auth.service';
import { LanguageService } from '@core/services/language.service';
import { authInterceptor } from '@core/auth/auth.interceptor';
import { branchInterceptor } from '@core/interceptors/branch.interceptor';
import { errorInterceptor } from '@core/interceptors/error.interceptor';

function initializeUserProfile(authService: AuthService): () => Promise<void> {
  return () => authService.loadUserProfile();
}

function initializeLanguage(languageService: LanguageService): () => void {
  return () => languageService.initialize();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(
      withFetch(),
      withInterceptors([authInterceptor, branchInterceptor, errorInterceptor]),
    ),
    provideAnimationsAsync(),
    provideTranslateService({
      defaultLanguage: 'en',
    }),
    provideTranslateHttpLoader({
      prefix: './assets/i18n/',
      suffix: '.json',
    }),
    KeycloakService,
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService],
    },
    {
      provide: APP_INITIALIZER,
      useFactory: initializeUserProfile,
      multi: true,
      deps: [AuthService],
    },
    {
      provide: APP_INITIALIZER,
      useFactory: initializeLanguage,
      multi: true,
      deps: [LanguageService],
    },
  ],
};

