import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';

import { BloodGroupBadgeComponent } from '@shared/components/blood-group-badge/blood-group-badge.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { DonorService } from '../services/donor.service';
import {
  Donor,
  DonorHealthRecord,
  DonorConsent,
  GenderEnum,
  getDonorFullName,
} from '../models/donor.model';

/**
 * Donor detail view with profile card and tabbed sections.
 */
@Component({
  selector: 'app-donor-detail',
  standalone: true,
  imports: [
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatTableModule,
    MatChipsModule,
    MatDividerModule,
    MatTooltipModule,
    BloodGroupBadgeComponent,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './donor-detail.component.html',
  styleUrl: './donor-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DonorDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly donorService = inject(DonorService);

  // ── State signals ──────────────────────────────────────────────
  readonly donor = signal<Donor | null>(null);
  readonly healthRecords = signal<DonorHealthRecord[]>([]);
  readonly consents = signal<DonorConsent[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly activeTab = signal(0);

  // ── Computed ───────────────────────────────────────────────────
  readonly fullName = computed(() => {
    const d = this.donor();
    return d ? getDonorFullName(d) : '';
  });

  readonly genderLabel = computed(() => {
    const d = this.donor();
    if (!d) return '';
    switch (d.gender) {
      case GenderEnum.MALE:
        return 'Male';
      case GenderEnum.FEMALE:
        return 'Female';
      case GenderEnum.OTHER:
        return 'Other';
      default:
        return d.gender;
    }
  });

  readonly healthRecordColumns: string[] = [
    'screeningDate',
    'hemoglobinGdl',
    'bloodPressure',
    'pulseRate',
    'weightKg',
    'isEligible',
  ];

  readonly consentColumns: string[] = [
    'consentType',
    'consentGiven',
    'consentDate',
    'expiryDate',
  ];

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      void this.loadDonor(id);
    }
  }

  // ── Data loading ───────────────────────────────────────────────

  async loadDonor(id: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      const donor = await this.donorService.getById(id);
      this.donor.set(donor);

      // Load related data in parallel
      const [healthRecords, consentsData] = await Promise.all([
        this.donorService.getHealthRecords(id),
        this.donorService.getConsents(id),
      ]);
      this.healthRecords.set(healthRecords);
      this.consents.set(consentsData);
    } catch {
      this.error.set('Failed to load donor details. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  // ── Navigation ─────────────────────────────────────────────────

  editDonor(): void {
    const d = this.donor();
    if (d) {
      void this.router.navigate(['/staff/donors', d.id, 'edit']);
    }
  }

  goBack(): void {
    void this.router.navigate(['/staff/donors']);
  }

  onTabChange(index: number): void {
    this.activeTab.set(index);
  }
}
