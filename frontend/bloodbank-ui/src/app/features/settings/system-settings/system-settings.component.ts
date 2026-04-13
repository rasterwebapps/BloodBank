import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DatePipe } from '@angular/common';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { SettingsService } from '../services/settings.service';
import { SystemSetting, SettingCategory } from '../models/settings.model';

/**
 * System settings view grouped by category with inline editing.
 */
@Component({
  selector: 'app-system-settings',
  standalone: true,
  imports: [
    FormsModule,
    DatePipe,
    MatTabsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatTooltipModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './system-settings.component.html',
  styleUrl: './system-settings.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SystemSettingsComponent implements OnInit {
  private readonly settingsService = inject(SettingsService);
  private readonly notification = inject(NotificationService);

  readonly settings = signal<SystemSetting[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  /** ID of setting currently being edited, or null */
  readonly editing = signal<string | null>(null);
  /** Temporary value being edited */
  readonly editValue = signal('');

  readonly categories = signal<SettingCategory[]>([]);

  readonly settingColumns: string[] = ['key', 'description', 'value', 'updatedAt', 'actions'];

  readonly settingsByCategory = computed(() => {
    const all = this.settings();
    const map = new Map<SettingCategory, SystemSetting[]>();
    for (const s of all) {
      const list = map.get(s.category) ?? [];
      list.push(s);
      map.set(s.category, list);
    }
    return map;
  });

  ngOnInit(): void {
    void this.loadSettings();
  }

  async loadSettings(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.settingsService.getSettings();
      this.settings.set(result);
      const cats = [...new Set(result.map(s => s.category))];
      this.categories.set(cats);
    } catch {
      this.error.set('Failed to load settings. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }

  getSettingsByCategory(category: SettingCategory): SystemSetting[] {
    return this.settingsByCategory().get(category) ?? [];
  }

  startEdit(setting: SystemSetting): void {
    this.editing.set(setting.id);
    this.editValue.set(setting.value);
  }

  cancelEdit(): void {
    this.editing.set(null);
    this.editValue.set('');
  }

  async saveEdit(setting: SystemSetting): Promise<void> {
    const newValue = this.editValue();
    if (newValue === setting.value) {
      this.cancelEdit();
      return;
    }
    try {
      const updated = await this.settingsService.updateSetting(setting.id, { value: newValue });
      this.settings.update(items =>
        items.map(s => (s.id === updated.id ? updated : s)),
      );
      this.notification.success(`Setting "${setting.key}" updated.`);
    } catch {
      this.notification.error(`Failed to update setting "${setting.key}".`);
    } finally {
      this.editing.set(null);
      this.editValue.set('');
    }
  }

  onEditKeydown(event: KeyboardEvent, setting: SystemSetting): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      void this.saveEdit(setting);
    } else if (event.key === 'Escape') {
      this.cancelEdit();
    }
  }
}
