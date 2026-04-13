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
import { SopDocument } from '../models/compliance.model';

@Component({
  selector: 'app-sop-list',
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
  templateUrl: './sop-list.component.html',
  styleUrl: './sop-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SopListComponent implements OnInit {
  private readonly complianceService = inject(ComplianceService);

  readonly sops = signal<SopDocument[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);

  readonly isEmpty = computed(
    () => this.sops().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns = ['sopCode', 'title', 'version', 'category', 'effectiveDate', 'reviewDate', 'status'];

  ngOnInit(): void {
    void this.loadSops();
  }

  async loadSops(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.complianceService.listSops(this.currentPage(), this.pageSize());
      this.sops.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load SOPs.');
    } finally {
      this.loading.set(false);
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadSops();
  }
}
