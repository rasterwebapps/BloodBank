import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

import { TransfusionService } from '../services/transfusion.service';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import {
  CrossMatchRequest,
  CrossMatchResult,
  CrossMatchStatusEnum,
  CROSS_MATCH_STATUS_OPTIONS,
} from '../models/transfusion.model';

/**
 * Component for viewing cross-match results per request.
 */
@Component({
  selector: 'app-cross-match-result',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './cross-match-result.component.html',
  styleUrl: './cross-match-result.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CrossMatchResultComponent implements OnInit {
  private readonly transfusionService = inject(TransfusionService);

  readonly selectedStatus = signal<CrossMatchStatusEnum | null>(null);
  readonly requests = signal<CrossMatchRequest[]>([]);
  readonly selectedRequest = signal<CrossMatchRequest | null>(null);
  readonly results = signal<CrossMatchResult[]>([]);
  readonly loading = signal(false);
  readonly loadingResults = signal(false);
  readonly error = signal<string | null>(null);

  readonly statusOptions = CROSS_MATCH_STATUS_OPTIONS;
  readonly requestColumns: string[] = [
    'requestNumber',
    'patientName',
    'bloodGroup',
    'componentType',
    'status',
    'actions',
  ];
  readonly resultColumns: string[] = [
    'unitNumber',
    'crossMatchType',
    'isCompatible',
    'performedBy',
    'performedAt',
    'notes',
  ];

  ngOnInit(): void {
    void this.loadRequests();
  }

  async loadRequests(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const status = this.selectedStatus() ?? undefined;
      const result = await this.transfusionService.listCrossMatchRequests(
        0,
        20,
        status,
      );
      this.requests.set(result.content);
    } catch {
      this.error.set('Failed to load cross-match requests.');
    } finally {
      this.loading.set(false);
    }
  }

  onStatusChange(status: CrossMatchStatusEnum | null): void {
    this.selectedStatus.set(status);
    void this.loadRequests();
  }

  async onRequestSelect(req: CrossMatchRequest): Promise<void> {
    this.selectedRequest.set(req);
    this.loadingResults.set(true);
    try {
      const results = await this.transfusionService.getCrossMatchResults(req.id);
      this.results.set(results);
    } catch {
      this.results.set([]);
    } finally {
      this.loadingResults.set(false);
    }
  }

  isSelected(req: CrossMatchRequest): boolean {
    return this.selectedRequest()?.id === req.id;
  }
}
