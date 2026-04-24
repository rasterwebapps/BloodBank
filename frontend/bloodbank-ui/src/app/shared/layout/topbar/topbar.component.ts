import {
  Component,
  ChangeDetectionStrategy,
  inject,
  input,
  output,
} from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '@core/auth/auth.service';
import { ThemeService } from '@core/services/theme.service';
import { BranchContextService, Branch } from '@core/services/branch-context.service';
import { LanguageService, AppLocale } from '@core/services/language.service';
import { User } from '@core/models/user.model';

/**
 * Top toolbar with branch selector, notification bell, theme toggle, language switcher, and user menu.
 */
@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatBadgeModule,
    MatDividerModule,
    MatTooltipModule,
    TranslatePipe,
  ],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TopbarComponent {
  private readonly authService = inject(AuthService);
  private readonly themeService = inject(ThemeService);
  private readonly branchContext = inject(BranchContextService);
  readonly languageService = inject(LanguageService);

  readonly currentBranch = input<Branch | null>(null);
  readonly user = input<User | null>(null);
  readonly toggleSidenav = output<void>();

  readonly branches = this.branchContext.branches;
  readonly isDark = this.themeService.isDark;
  readonly displayName = this.authService.displayName;

  onToggleSidenav(): void {
    this.toggleSidenav.emit();
  }

  onSelectBranch(branchId: string): void {
    this.branchContext.selectBranch(branchId);
  }

  onToggleTheme(): void {
    this.themeService.toggle();
  }

  onSelectLanguage(code: AppLocale): void {
    this.languageService.setLanguage(code);
  }

  onManageAccount(): void {
    this.authService.manageAccount();
  }

  onLogout(): void {
    this.authService.logout();
  }
}
