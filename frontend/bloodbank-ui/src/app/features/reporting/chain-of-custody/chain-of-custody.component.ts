import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar } from '@angular/material/snack-bar';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { ReportingService } from '../services/reporting.service';
import { ChainOfCustody } from '../models/reporting.model';

/**
 * Chain of custody — vein-to-vein blood unit tracking.
 */
@Component({
  selector: 'app-chain-of-custody',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
    MatDividerModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './chain-of-custody.component.html',
  styleUrl: './chain-of-custody.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChainOfCustodyComponent {
  private readonly reportingService = inject(ReportingService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly custody = signal<ChainOfCustody | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly searchForm = this.fb.nonNullable.group({
    unitNumber: ['', Validators.required],
  });

  async search(): Promise<void> {
    if (this.searchForm.invalid) return;
    this.loading.set(true);
    this.error.set(null);
    this.custody.set(null);
    try {
      const result = await this.reportingService.searchChainOfCustody(
        this.searchForm.getRawValue().unitNumber,
      );
      if (result.content.length > 0) {
        this.custody.set(result.content[0]);
      } else {
        this.error.set('No records found for this unit number.');
      }
    } catch {
      this.error.set('Failed to search chain of custody.');
    } finally {
      this.loading.set(false);
    }
  }
}
