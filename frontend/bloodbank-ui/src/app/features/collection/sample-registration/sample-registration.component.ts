import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
} from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { CollectionService } from '../services/collection.service';
import {
  SampleRegistrationRequest,
  TubeType,
  TUBE_TYPE_OPTIONS,
} from '../models/collection.model';

/**
 * Form for registering lab samples from a collection.
 */
@Component({
  selector: 'app-sample-registration',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    ErrorCardComponent,
  ],
  templateUrl: './sample-registration.component.html',
  styleUrl: './sample-registration.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SampleRegistrationComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly collectionService = inject(CollectionService);
  private readonly notification = inject(NotificationService);

  // ── State ──────────────────────────────────────────────────────
  readonly collectionId = signal<string>('');
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  // ── Options ────────────────────────────────────────────────────
  readonly tubeTypeOptions = TUBE_TYPE_OPTIONS;

  // ── Form ───────────────────────────────────────────────────────
  readonly form = this.fb.group({
    sampleCode: this.fb.nonNullable.control('', Validators.required),
    tubeType: this.fb.nonNullable.control('', Validators.required),
    volumeMl: this.fb.nonNullable.control(5, [
      Validators.required,
      Validators.min(1),
      Validators.max(50),
    ]),
  });

  // ── Lifecycle ──────────────────────────────────────────────────

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.collectionId.set(id);
    }
  }

  // ── Form submission ────────────────────────────────────────────

  async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    try {
      const v = this.form.getRawValue();
      const request: SampleRegistrationRequest = {
        collectionId: this.collectionId(),
        sampleCode: v.sampleCode,
        tubeType: v.tubeType as TubeType,
        volumeMl: v.volumeMl,
      };

      await this.collectionService.registerSample(request);
      this.notification.success('Sample registered successfully.');
      void this.router.navigate(['/staff/collections']);
    } catch {
      this.error.set('Failed to register sample. Please try again.');
      this.notification.error('Failed to register sample.');
    } finally {
      this.saving.set(false);
    }
  }

  // ── Navigation ─────────────────────────────────────────────────

  cancel(): void {
    void this.router.navigate(['/staff/collections']);
  }
}
