import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { AuthService } from '@core/auth/auth.service';
import { BranchContextService } from '@core/services/branch-context.service';
import { SidenavComponent } from '../sidenav/sidenav.component';
import { TopbarComponent } from '../topbar/topbar.component';
import { BreadcrumbComponent } from '../breadcrumb/breadcrumb.component';
import { FooterComponent } from '../footer/footer.component';

/**
 * Main layout shell with sidenav + topbar + breadcrumb + router-outlet + footer.
 * Responsive: side mode on desktop, overlay on mobile.
 */
@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [
    RouterOutlet,
    SidenavComponent,
    TopbarComponent,
    BreadcrumbComponent,
    FooterComponent,
  ],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShellComponent {
  private readonly breakpointObserver = inject(BreakpointObserver);
  private readonly authService = inject(AuthService);
  private readonly branchContext = inject(BranchContextService);

  readonly sidenavCollapsed = signal(false);
  readonly isMobile = signal(false);
  readonly currentUser = this.authService.currentUser;
  readonly currentBranch = this.branchContext.currentBranch;

  constructor() {
    this.breakpointObserver
      .observe(['(max-width: 1024px)'])
      .subscribe((result) => {
        this.isMobile.set(result.matches);
        if (result.matches) {
          this.sidenavCollapsed.set(true);
        }
      });
  }

  toggleSidenav(): void {
    this.sidenavCollapsed.update((v) => !v);
  }
}
