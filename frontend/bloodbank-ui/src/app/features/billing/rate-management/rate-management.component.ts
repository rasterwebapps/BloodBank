import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { BillingService } from '../services/billing.service';
import { RateMaster, RateTypeEnum } from '../models/billing.model';

/**
 * Rate management — create and manage service pricing rates.
 */
@Component({
  selector: 'app-rate-management',
  standalone: true,
  imports: [
    DatePipe,
    CurrencyPipe,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatChipsModule,
    MatDialogModule,
    DataTableComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './rate-management.component.html',
  styleUrl: './rate-management.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RateManagementComponent implements OnInit {
  private readonly billingService = inject(BillingService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly rates = signal<RateMaster[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(20);
  readonly showForm = signal(false);
  readonly submitting = signal(false);

  readonly isEmpty = computed(
    () => this.rates().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns = [
    'serviceCode',
    'serviceName',
    'rateType',
    'unitPrice',
    'effectiveFrom',
    'effectiveTo',
    'isActive',
    'actions',
  ];

  readonly rateTypes = Object.values(RateTypeEnum);

  readonly form = this.fb.nonNullable.group({
    serviceCode: ['', Validators.required],
    serviceName: ['', Validators.required],
    rateType: [RateTypeEnum.PER_UNIT as string, Validators.required],
    unitPrice: [0, [Validators.required, Validators.min(0)]],
    currency: ['USD', Validators.required],
    effectiveFrom: [new Date().toISOString().split('T')[0], Validators.required],
    effectiveTo: [''],
  });

  ngOnInit(): void {
    void this.loadRates();
  }

  async loadRates(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.billingService.listRates(this.currentPage(), this.pageSize());
      this.rates.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load rates.');
    } finally {
      this.loading.set(false);
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadRates();
  }

  toggleForm(): void {
    this.showForm.update((v) => !v);
    if (!this.showForm()) this.form.reset();
  }

  async saveRate(): Promise<void> {
    if (this.form.invalid) return;
    this.submitting.set(true);
    try {
      const value = this.form.getRawValue();
      await this.billingService.createRate({
        serviceCode: value.serviceCode,
        serviceName: value.serviceName,
        rateType: value.rateType as RateTypeEnum,
        unitPrice: value.unitPrice,
        currency: value.currency,
        effectiveFrom: value.effectiveFrom,
        effectiveTo: value.effectiveTo || undefined,
      });
      this.snackBar.open('Rate created successfully', 'Dismiss', { duration: 3000 });
      this.form.reset();
      this.showForm.set(false);
      void this.loadRates();
    } catch {
      this.snackBar.open('Failed to create rate', 'Dismiss', { duration: 3000 });
    } finally {
      this.submitting.set(false);
    }
  }

  async deactivateRate(rate: RateMaster): Promise<void> {
    try {
      await this.billingService.deactivateRate(rate.id);
      this.snackBar.open('Rate deactivated', 'Dismiss', { duration: 3000 });
      void this.loadRates();
    } catch {
      this.snackBar.open('Failed to deactivate rate', 'Dismiss', { duration: 3000 });
    }
  }
}
