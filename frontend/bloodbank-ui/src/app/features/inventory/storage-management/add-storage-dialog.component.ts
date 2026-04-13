import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, FormControl } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { STORAGE_TYPE_OPTIONS, StorageType } from '../models/inventory.model';

export interface AddStorageDialogData {
  branchId: string | null;
}

/**
 * Dialog for creating a new storage location.
 */
@Component({
  selector: 'app-add-storage-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>Add Storage Location</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="flex flex-col gap-4 mt-2">
        <mat-form-field appearance="outline">
          <mat-label>Name</mat-label>
          <input matInput formControlName="name" placeholder="e.g. Refrigerator A" />
          @if (nameCtrl.hasError('required') && nameCtrl.touched) {
            <mat-error>Name is required</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Storage Type</mat-label>
          <mat-select formControlName="type">
            @for (opt of storageTypeOptions; track opt.value) {
              <mat-option [value]="opt.value">{{ opt.label }}</mat-option>
            }
          </mat-select>
          @if (typeCtrl.hasError('required') && typeCtrl.touched) {
            <mat-error>Storage type is required</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Capacity (units)</mat-label>
          <input matInput type="number" formControlName="capacity" min="1" />
          @if (capacityCtrl.hasError('required') && capacityCtrl.touched) {
            <mat-error>Capacity is required</mat-error>
          }
          @if (capacityCtrl.hasError('min') && capacityCtrl.touched) {
            <mat-error>Minimum capacity is 1</mat-error>
          }
        </mat-form-field>

        <div class="flex gap-4">
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Min Temp (°C)</mat-label>
            <input matInput type="number" formControlName="minTempCelsius" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="flex-1">
            <mat-label>Max Temp (°C)</mat-label>
            <input matInput type="number" formControlName="maxTempCelsius" />
          </mat-form-field>
        </div>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button mat-dialog-close>Cancel</button>
      <button
        mat-raised-button
        color="primary"
        [disabled]="saving()"
        (click)="onSave()">
        @if (saving()) {
          <mat-progress-spinner diameter="18" mode="indeterminate" />
        } @else {
          Add Location
        }
      </button>
    </mat-dialog-actions>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddStorageDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<AddStorageDialogComponent>);
  readonly data = inject<AddStorageDialogData>(MAT_DIALOG_DATA);

  readonly saving = signal(false);
  readonly storageTypeOptions = STORAGE_TYPE_OPTIONS;

  readonly form = this.fb.group({
    name: this.fb.nonNullable.control('', [Validators.required]),
    type: this.fb.nonNullable.control<StorageType>(StorageType.REFRIGERATOR, [Validators.required]),
    capacity: this.fb.nonNullable.control(100, [Validators.required, Validators.min(1)]),
    minTempCelsius: this.fb.nonNullable.control(2),
    maxTempCelsius: this.fb.nonNullable.control(6),
  });

  get nameCtrl(): FormControl<string> {
    return this.form.controls.name;
  }
  get typeCtrl(): FormControl<StorageType> {
    return this.form.controls.type;
  }
  get capacityCtrl(): FormControl<number> {
    return this.form.controls.capacity;
  }

  onSave(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.dialogRef.close({
      ...value,
      currentUnits: 0,
      branchId: this.data.branchId ?? '',
    });
  }
}
