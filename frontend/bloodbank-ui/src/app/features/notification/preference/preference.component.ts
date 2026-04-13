import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar } from '@angular/material/snack-bar';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '../services/notification.service';
import { Preference } from '../models/notification.model';

@Component({
  selector: 'app-preference',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSlideToggleModule,
    MatDividerModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './preference.component.html',
  styleUrl: './preference.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PreferenceComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly submitting = signal(false);

  readonly form = this.fb.nonNullable.group({
    emailEnabled: [true],
    smsEnabled: [true],
    pushEnabled: [false],
    inAppEnabled: [true],
    donationReminders: [true],
    appointmentReminders: [true],
    systemAlerts: [true],
    campaignMessages: [false],
  });

  ngOnInit(): void {
    void this.loadPreferences();
  }

  async loadPreferences(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const prefs = await this.notificationService.getPreferences();
      this.form.patchValue(prefs);
    } catch {
      this.error.set('Failed to load preferences.');
    } finally {
      this.loading.set(false);
    }
  }

  async savePreferences(): Promise<void> {
    this.submitting.set(true);
    try {
      const value = this.form.getRawValue();
      await this.notificationService.updatePreferences(value);
      this.snackBar.open('Preferences saved', 'Dismiss', { duration: 3000 });
    } catch {
      this.snackBar.open('Failed to save preferences', 'Dismiss', { duration: 3000 });
    } finally {
      this.submitting.set(false);
    }
  }
}
