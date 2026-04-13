import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { ComplianceService } from '../services/compliance.service';
import { License } from '../models/compliance.model';

@Component({
  selector: 'app-license-list',
  standalone: true,
  imports: [
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatChipsModule,
    DataTableComponent,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './license-list.component.html',
  styleUrl: './license-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LicenseListComponent implements OnInit {
  private readonly complianceService = inject(ComplianceService);

  readonly licenses = signal<License[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);

  readonly isEmpty = computed(
    () => this.licenses().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns = [
    'licenseNumber',
    'licenseType',
    'issuingAuthority',
    'issueDate',
    'expiryDate',
    'status',
  ];

  ngOnInit(): void {
    void this.loadLicenses();
  }

  async loadLicenses(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.complianceService.listLicenses(this.currentPage(), this.pageSize());
      this.licenses.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load licenses.');
    } finally {
      this.loading.set(false);
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadLicenses();
  }

  isExpiringSoon(license: License): boolean {
    const daysUntilExpiry = Math.floor(
      (new Date(license.expiryDate).getTime() - Date.now()) / (1000 * 60 * 60 * 24),
    );
    return daysUntilExpiry >= 0 && daysUntilExpiry <= license.renewalReminderDays;
  }
}
