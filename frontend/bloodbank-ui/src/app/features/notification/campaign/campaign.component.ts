import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { DatePipe } from '@angular/common';
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
import { StatusBadgeComponent } from '@shared/components/status-badge/status-badge.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { NotificationService } from '../services/notification.service';
import { Campaign, NotificationTemplate } from '../models/notification.model';

@Component({
  selector: 'app-campaign',
  standalone: true,
  imports: [
    DatePipe,
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
    StatusBadgeComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './campaign.component.html',
  styleUrl: './campaign.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CampaignComponent implements OnInit {
  private readonly notificationService = inject(NotificationService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly campaigns = signal<Campaign[]>([]);
  readonly templates = signal<NotificationTemplate[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(10);
  readonly showForm = signal(false);
  readonly submitting = signal(false);

  readonly isEmpty = computed(
    () => this.campaigns().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns = ['name', 'templateName', 'status', 'totalRecipients', 'sentCount', 'scheduledAt', 'actions'];

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
    templateId: ['', Validators.required],
    scheduledAt: [''],
    targetCriteria: [''],
  });

  ngOnInit(): void {
    void this.loadCampaigns();
    void this.loadTemplates();
  }

  async loadCampaigns(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.notificationService.listCampaigns(this.currentPage(), this.pageSize());
      this.campaigns.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load campaigns.');
    } finally {
      this.loading.set(false);
    }
  }

  async loadTemplates(): Promise<void> {
    try {
      const result = await this.notificationService.listTemplates(0, 100);
      this.templates.set(result.content);
    } catch {
      // non-critical
    }
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadCampaigns();
  }

  toggleForm(): void {
    this.showForm.update((v) => !v);
    if (!this.showForm()) this.form.reset();
  }

  async saveCampaign(): Promise<void> {
    if (this.form.invalid) return;
    this.submitting.set(true);
    try {
      const value = this.form.getRawValue();
      await this.notificationService.createCampaign({
        name: value.name,
        description: value.description || undefined,
        templateId: value.templateId,
        scheduledAt: value.scheduledAt || undefined,
        targetCriteria: value.targetCriteria || undefined,
      });
      this.snackBar.open('Campaign created', 'Dismiss', { duration: 3000 });
      this.form.reset();
      this.showForm.set(false);
      void this.loadCampaigns();
    } catch {
      this.snackBar.open('Failed to create campaign', 'Dismiss', { duration: 3000 });
    } finally {
      this.submitting.set(false);
    }
  }

  async launchCampaign(campaign: Campaign): Promise<void> {
    try {
      await this.notificationService.launchCampaign(campaign.id);
      this.snackBar.open('Campaign launched', 'Dismiss', { duration: 3000 });
      void this.loadCampaigns();
    } catch {
      this.snackBar.open('Failed to launch campaign', 'Dismiss', { duration: 3000 });
    }
  }
}
