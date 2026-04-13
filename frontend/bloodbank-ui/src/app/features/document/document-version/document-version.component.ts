import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar } from '@angular/material/snack-bar';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { DocumentService } from '../services/document.service';
import { DocumentVersion, formatFileSize } from '../models/document.model';

/**
 * Document version history — lists all versions with download capability.
 */
@Component({
  selector: 'app-document-version',
  standalone: true,
  imports: [
    DatePipe,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './document-version.component.html',
  styleUrl: './document-version.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DocumentVersionComponent implements OnInit {
  private readonly documentService = inject(DocumentService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly versions = signal<DocumentVersion[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly documentId = signal('');

  readonly isEmpty = computed(
    () => this.versions().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns = ['versionNumber', 'uploadedBy', 'fileSize', 'mimeType', 'changeNotes', 'createdAt', 'actions'];

  readonly formatFileSize = formatFileSize;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.documentId.set(id);
      void this.loadVersions(id);
    }
  }

  async loadVersions(id: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const versions = await this.documentService.listVersions(id);
      this.versions.set(versions);
    } catch {
      this.error.set('Failed to load version history.');
    } finally {
      this.loading.set(false);
    }
  }

  async downloadVersion(version: DocumentVersion): Promise<void> {
    try {
      window.open(version.fileUrl, '_blank');
    } catch {
      this.snackBar.open('Failed to download version', 'Dismiss', { duration: 3000 });
    }
  }

  goBack(): void {
    void this.router.navigate(['/staff/documents']);
  }
}
