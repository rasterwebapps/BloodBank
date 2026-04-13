import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { HospitalPortalService } from '../services/hospital-portal.service';
import { HospitalDashboardStats } from '../models/hospital-portal.model';

@Component({
  selector: 'app-hospital-dashboard',
  standalone: true,
  imports: [
    DatePipe,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatTableModule,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './hospital-dashboard.component.html',
  styleUrl: './hospital-dashboard.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HospitalDashboardComponent implements OnInit {
  private readonly hospitalPortalService = inject(HospitalPortalService);
  private readonly router = inject(Router);

  readonly stats = signal<HospitalDashboardStats | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly recentDeliveriesColumns = [
    'requestNumber',
    'bloodGroup',
    'componentType',
    'quantity',
    'status',
  ];
  readonly pendingFeedbackColumns = [
    'requestNumber',
    'bloodGroup',
    'componentType',
    'quantity',
    'status',
  ];

  ngOnInit(): void {
    this.loadStats();
  }

  async loadStats(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.hospitalPortalService.getDashboardStats();
      this.stats.set(result);
    } catch {
      this.error.set('Failed to load dashboard statistics. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  navigateTo(path: string): void {
    this.router.navigate(['/hospital', path]);
  }
}
