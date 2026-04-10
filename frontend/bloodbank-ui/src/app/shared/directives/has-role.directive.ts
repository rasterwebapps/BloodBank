import { Directive, inject, input, TemplateRef, ViewContainerRef, effect } from '@angular/core';
import { AuthService } from '@core/auth/auth.service';

/**
 * Structural directive that shows/hides elements based on user roles.
 *
 * Usage:
 * ```html
 * <div *appHasRole="'BRANCH_ADMIN'">Admin only content</div>
 * <div *appHasRole="['DOCTOR', 'NURSE']">Doctor or Nurse content</div>
 * ```
 */
@Directive({
  selector: '[appHasRole]',
  standalone: true,
})
export class HasRoleDirective {
  private readonly templateRef = inject(TemplateRef<unknown>);
  private readonly viewContainer = inject(ViewContainerRef);
  private readonly authService = inject(AuthService);

  readonly appHasRole = input.required<string | string[]>();

  private hasView = false;

  constructor() {
    effect(() => {
      const roles = this.appHasRole();
      const roleArray = Array.isArray(roles) ? roles : [roles];
      const hasRole = this.authService.hasAnyRole(roleArray);

      if (hasRole && !this.hasView) {
        this.viewContainer.createEmbeddedView(this.templateRef);
        this.hasView = true;
      } else if (!hasRole && this.hasView) {
        this.viewContainer.clear();
        this.hasView = false;
      }
    });
  }
}
