import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { DatePipe } from '@angular/common';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { SettingsService } from '../services/settings.service';
import { FeatureFlag } from '../models/settings.model';

/**
 * Feature flags management as a card grid with slide toggles.
 * Uses toggling signal to track in-flight updates.
 */
@Component({
  selector: 'app-feature-flags',
  standalone: true,
  imports: [
    DatePipe,
    MatCardModule,
    MatSlideToggleModule,
    MatProgressSpinnerModule,
    MatIconModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './feature-flags.component.html',
  styleUrl: './feature-flags.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeatureFlagsComponent implements OnInit {
  private readonly settingsService = inject(SettingsService);
  private readonly notification = inject(NotificationService);

  readonly flags = signal<FeatureFlag[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  /** Set of flag IDs currently being toggled */
  readonly toggling = signal<Set<string>>(new Set());

  ngOnInit(): void {
    void this.loadFlags();
  }

  async loadFlags(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.settingsService.getFeatureFlags();
      this.flags.set(result);
    } catch {
      this.error.set('Failed to load feature flags. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  isToggling(flagId: string): boolean {
    return this.toggling().has(flagId);
  }

  async toggleFlag(flag: FeatureFlag): Promise<void> {
    if (this.isToggling(flag.id)) return;

    this.toggling.update(set => new Set([...set, flag.id]));
    try {
      const updated = await this.settingsService.updateFeatureFlag(flag.id, {
        enabled: !flag.enabled,
      });
      this.flags.update(items =>
        items.map(f => (f.id === updated.id ? updated : f)),
      );
      this.notification.success(
        `Feature "${flag.name}" ${updated.enabled ? 'enabled' : 'disabled'}.`,
      );
    } catch {
      this.notification.error(`Failed to toggle "${flag.name}".`);
    } finally {
      this.toggling.update(set => {
        const next = new Set(set);
        next.delete(flag.id);
        return next;
      });
    }
  }
}
