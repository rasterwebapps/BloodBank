import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
  ViewChild,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { DonorPortalService } from '../services/donor-portal.service';
import { DonationRecord } from '../models/donor-portal.models';

@Component({
  selector: 'app-donation-history',
  standalone: true,
  imports: [
    DatePipe,
    MatTableModule,
    MatPaginatorModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './donation-history.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DonationHistoryComponent implements OnInit {
  private readonly service = inject(DonorPortalService);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  readonly donations = signal<DonationRecord[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly pageSize = signal(10);
  readonly pageIndex = signal(0);

  readonly displayedColumns = ['date', 'location', 'volume', 'bloodGroup', 'status', 'actions'];

  ngOnInit(): void {
    this.load();
  }

  async load(page = 0, size = 10): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.service.getDonationHistory(page, size);
      this.donations.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load donation history.');
    } finally {
      this.loading.set(false);
    }
  }

  onPage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load(event.pageIndex, event.pageSize);
  }

  downloadCertificate(donation: DonationRecord): void {
    if (donation.certificateUrl) {
      window.open(donation.certificateUrl, '_blank');
    }
  }
}
