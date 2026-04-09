import { KeycloakService } from 'keycloak-angular';
import { environment } from '@env/environment';
import { AuthService } from './auth.service';
import { inject } from '@angular/core';

/**
 * APP_INITIALIZER factory for Keycloak.
 * Initializes Keycloak with login-required flow and in-memory token storage.
 */
export function initializeKeycloak(keycloak: KeycloakService): () => Promise<boolean> {
  return async () => {
    const authenticated = await keycloak.init({
      config: {
        url: environment.keycloakUrl,
        realm: environment.keycloakRealm,
        clientId: environment.keycloakClientId,
      },
      initOptions: {
        onLoad: 'login-required',
        checkLoginIframe: false,
        silentCheckSsoRedirectUri:
          window.location.origin + '/assets/silent-check-sso.html',
      },
      enableBearerInterceptor: true,
      bearerPrefix: 'Bearer',
      bearerExcludedUrls: ['/assets', '/i18n'],
    });

    if (authenticated) {
      const authService = inject(AuthService);
      await authService.loadUserProfile();
    }

    return authenticated;
  };
}
