import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { BloodGroupBadgeComponent } from '@shared/components/blood-group-badge/blood-group-badge.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { DonorPortalService } from '../services/donor-portal.service';
import { DonorProfile, DonationRecord, Appointment, Camp, LoyaltyTier } from '../models/donor-portal.models';

@Component({
  selector: 'app-donor-dashboard',
  standalone: true,
  imports: [
    DatePipe,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
    MatDividerModule,
    BloodGroupBadgeComponent,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './donor-dashboard.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DonorDashboardComponent implements OnInit {
  private readonly service = inject(DonorPortalService);
  private readonly router = inject(Router);

  readonly profile = signal<DonorProfile | null>(null);
  readonly recentDonations = signal<DonationRecord[]>([]);
  readonly upcomingAppointments = signal<Appointment[]>([]);
  readonly nearbyCamps = signal<Camp[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly tierColor = computed(() => {
    const tier = this.profile()?.loyaltyTier;
    const map: Record<LoyaltyTier, string> = {
      [LoyaltyTier.BRONZE]: '#cd7f32',
      [LoyaltyTier.SILVER]: '#c0c0c0',
      [LoyaltyTier.GOLD]: '#ffd700',
      [LoyaltyTier.PLATINUM]: '#e5e4e2',
    };
    return tier ? map[tier] : '#cd7f32';
  });

  ngOnInit(): void {
    this.loadAll();
  }

  async loadAll(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const [profile, history, appointments] = await Promise.all([
        this.service.getProfile(),
        this.service.getDonationHistory(0, 3),
        this.service.getAppointments(),
      ]);
      this.profile.set(profile);
      this.recentDonations.set(history.content);
      this.upcomingAppointments.set(appointments.slice(0, 2));
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          async (pos) => {
            try {
              const camps = await this.service.getNearbyCamps(
                pos.coords.latitude,
                pos.coords.longitude,
              );
              this.nearbyCamps.set(camps.slice(0, 3));
            } catch {
              // geolocation ok but camps failed — non-fatal
            }
          },
          () => { /* denied — skip */ },
        );
      }
    } catch {
      this.error.set('Failed to load dashboard. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  navigateTo(path: string): void {
    this.router.navigate(['/donor', path]);
  }
}
