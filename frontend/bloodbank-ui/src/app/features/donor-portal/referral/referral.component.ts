import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { DonorPortalService } from '../services/donor-portal.service';
import { ReferralInfo, Referral } from '../models/donor-portal.models';

@Component({
  selector: 'app-referral',
  standalone: true,
  imports: [
    DatePipe,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSnackBarModule,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './referral.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReferralComponent implements OnInit {
  private readonly service = inject(DonorPortalService);
  private readonly snackBar = inject(MatSnackBar);

  readonly referralInfo = signal<ReferralInfo | null>(null);
  readonly referrals = signal<Referral[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly displayedColumns = ['name', 'email', 'status', 'date'];

  ngOnInit(): void {
    this.load();
  }

  async load(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const [info, referrals] = await Promise.all([
        this.service.getReferralCode(),
        this.service.getReferrals(),
      ]);
      this.referralInfo.set(info);
      this.referrals.set(referrals);
    } catch {
      this.error.set('Failed to load referral information.');
    } finally {
      this.loading.set(false);
    }
  }

  async copyLink(): Promise<void> {
    const url = this.referralInfo()?.url;
    if (url) {
      await navigator.clipboard.writeText(url);
      this.snackBar.open('Referral link copied!', 'Close', { duration: 3000 });
    }
  }
}
