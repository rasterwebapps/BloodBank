import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { BranchService } from '../services/branch.service';
import { BranchCreateRequest, BranchUpdateRequest } from '../models/branch.model';

/**
 * Branch create/edit form.
 * Mode is determined by route: '/new' → create, '/:id/edit' → edit.
 */
@Component({
  selector: 'app-branch-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './branch-form.component.html',
  styleUrl: './branch-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BranchFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly branchService = inject(BranchService);
  private readonly notification = inject(NotificationService);

  readonly isEditMode = signal(false);
  readonly branchId = signal<string | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly pageTitle = computed(() => (this.isEditMode() ? 'Edit Branch' : 'Add Branch'));
  readonly submitLabel = computed(() => (this.isEditMode() ? 'Update' : 'Create'));

  readonly form = this.fb.group({
    name: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(200)]),
    code: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.maxLength(20),
      Validators.pattern(/^[A-Z0-9_-]+$/),
    ]),
    address: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(500)]),
    city: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(100)]),
    country: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(100)]),
    phone: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/^\+?[\d\s-]{7,20}$/),
    ]),
    email: this.fb.nonNullable.control('', [Validators.required, Validators.email]),
    capacity: this.fb.nonNullable.control(0, [Validators.required, Validators.min(1)]),
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.branchId.set(id);
      void this.loadBranch(id);
    }
  }

  async loadBranch(id: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const branch = await this.branchService.getById(id);
      this.form.patchValue({
        name: branch.name,
        code: branch.code,
        address: branch.address,
        city: branch.city,
        country: branch.country,
        phone: branch.phone,
        email: branch.email,
        capacity: branch.capacity,
      });
    } catch {
      this.error.set('Failed to load branch data.');
    } finally {
      this.loading.set(false);
    }
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    try {
      if (this.isEditMode()) {
        await this.updateBranch();
      } else {
        await this.createBranch();
      }
    } catch {
      const action = this.isEditMode() ? 'update' : 'create';
      this.error.set(`Failed to ${action} branch. Please try again.`);
      this.notification.error(`Failed to ${action} branch.`);
    } finally {
      this.saving.set(false);
    }
  }

  private async createBranch(): Promise<void> {
    const fv = this.form.getRawValue();
    const request: BranchCreateRequest = {
      name: fv.name,
      code: fv.code,
      address: fv.address,
      city: fv.city,
      country: fv.country,
      phone: fv.phone,
      email: fv.email,
      capacity: fv.capacity,
    };
    const branch = await this.branchService.create(request);
    this.notification.success('Branch created successfully.');
    void this.router.navigate(['/staff/branches', branch.id]);
  }

  private async updateBranch(): Promise<void> {
    const fv = this.form.getRawValue();
    const request: BranchUpdateRequest = {
      name: fv.name,
      address: fv.address,
      city: fv.city,
      country: fv.country,
      phone: fv.phone,
      email: fv.email,
      capacity: fv.capacity,
    };
    const id = this.branchId()!;
    await this.branchService.update(id, request);
    this.notification.success('Branch updated successfully.');
    void this.router.navigate(['/staff/branches', id]);
  }

  cancel(): void {
    if (this.isEditMode() && this.branchId()) {
      void this.router.navigate(['/staff/branches', this.branchId()]);
    } else {
      void this.router.navigate(['/staff/branches']);
    }
  }
}
