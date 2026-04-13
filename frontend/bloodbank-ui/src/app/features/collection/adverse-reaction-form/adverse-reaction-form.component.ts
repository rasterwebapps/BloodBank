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
  AdverseReactionCreateRequest,
  ReactionSeverity,
  REACTION_TYPE_OPTIONS,
  SEVERITY_OPTIONS,
} from '../models/collection.model';

/**
 * Form for reporting an adverse reaction during blood collection.
 */
@Component({
  selector: 'app-adverse-reaction-form',
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
  templateUrl: './adverse-reaction-form.component.html',
  styleUrl: './adverse-reaction-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdverseReactionFormComponent implements OnInit {
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
  readonly reactionTypeOptions = REACTION_TYPE_OPTIONS;
  readonly severityOptions = SEVERITY_OPTIONS;

  // ── Form ───────────────────────────────────────────────────────
  readonly form = this.fb.group({
    reactionType: this.fb.nonNullable.control('', Validators.required),
    severity: this.fb.nonNullable.control('', Validators.required),
    description: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.minLength(10),
      Validators.maxLength(1000),
    ]),
    actionTaken: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.minLength(10),
      Validators.maxLength(1000),
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
      const request: AdverseReactionCreateRequest = {
        collectionId: this.collectionId(),
        reactionType: v.reactionType,
        severity: v.severity as ReactionSeverity,
        description: v.description,
        actionTaken: v.actionTaken,
      };

      await this.collectionService.reportAdverseReaction(request);
      this.notification.success('Adverse reaction reported successfully.');
      void this.router.navigate(['/staff/collections']);
    } catch {
      this.error.set('Failed to report adverse reaction. Please try again.');
      this.notification.error('Failed to report adverse reaction.');
    } finally {
      this.saving.set(false);
    }
  }

  // ── Navigation ─────────────────────────────────────────────────

  cancel(): void {
    void this.router.navigate(['/staff/collections']);
  }
}
