import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
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
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { NotificationService } from '../services/notification.service';
import {
  NotificationTemplate,
  NotificationTypeEnum,
  TemplateTypeEnum,
} from '../models/notification.model';

@Component({
  selector: 'app-template-management',
  standalone: true,
  imports: [
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
    DataTableComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './template-management.component.html',
  styleUrl: './template-management.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TemplateManagementComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly templates = signal<NotificationTemplate[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(20);
  readonly showForm = signal(false);
  readonly submitting = signal(false);

  readonly isEmpty = computed(
    () => this.templates().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns = ['code', 'name', 'type', 'channel', 'isActive', 'actions'];
  readonly templateTypes = Object.values(TemplateTypeEnum);
  readonly channels = Object.values(NotificationTypeEnum);

  readonly form = this.fb.nonNullable.group({
    code: ['', Validators.required],
    name: ['', Validators.required],
    type: [TemplateTypeEnum.CUSTOM as string, Validators.required],
    channel: [NotificationTypeEnum.EMAIL as string, Validators.required],
    subject: [''],
    body: ['', Validators.required],
  });

  ngOnInit(): void {
    void this.loadTemplates();
  }

  async loadTemplates(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.notificationService.listTemplates(this.currentPage(), this.pageSize());
      this.templates.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load templates.');
    } finally {
      this.loading.set(false);
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadTemplates();
  }

  toggleForm(): void {
    this.showForm.update((v) => !v);
    if (!this.showForm()) this.form.reset();
  }

  async saveTemplate(): Promise<void> {
    if (this.form.invalid) return;
    this.submitting.set(true);
    try {
      const value = this.form.getRawValue();
      await this.notificationService.createTemplate({
        code: value.code,
        name: value.name,
        type: value.type as TemplateTypeEnum,
        channel: value.channel as NotificationTypeEnum,
        subject: value.subject || undefined,
        body: value.body,
      });
      this.snackBar.open('Template created', 'Dismiss', { duration: 3000 });
      this.form.reset();
      this.showForm.set(false);
      void this.loadTemplates();
    } catch {
      this.snackBar.open('Failed to create template', 'Dismiss', { duration: 3000 });
    } finally {
      this.submitting.set(false);
    }
  }

  async deleteTemplate(template: NotificationTemplate): Promise<void> {
    try {
      await this.notificationService.deleteTemplate(template.id);
      this.snackBar.open('Template deleted', 'Dismiss', { duration: 3000 });
      void this.loadTemplates();
    } catch {
      this.snackBar.open('Failed to delete template', 'Dismiss', { duration: 3000 });
    }
  }
}
