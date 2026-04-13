import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { BloodGroupBadgeComponent } from '@shared/components/blood-group-badge/blood-group-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { DonorPortalService } from '../services/donor-portal.service';
import { DonorProfile, LoyaltyTier } from '../models/donor-portal.models';

@Component({
  selector: 'app-digital-donor-card',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    BloodGroupBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './digital-donor-card.component.html',
  styleUrl: './digital-donor-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DigitalDonorCardComponent implements OnInit {
  private readonly service = inject(DonorPortalService);

  readonly profile = signal<DonorProfile | null>(null);
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
    this.loadProfile();
  }

  async loadProfile(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      this.profile.set(await this.service.getProfile());
    } catch {
      this.error.set('Failed to load donor card.');
    } finally {
      this.loading.set(false);
    }
  }

  printCard(): void {
    window.print();
  }
}
