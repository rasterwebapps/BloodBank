import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';

import { DataTableComponent } from '@shared/components/data-table/data-table.component';
import { SearchBarComponent } from '@shared/components/search-bar/search-bar.component';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { EmptyStateComponent } from '@shared/components/empty-state/empty-state.component';
import { DocumentService } from '../services/document.service';
import { Document, DocumentCategoryEnum, formatFileSize } from '../models/document.model';

/**
 * Document browser — file explorer with folder navigation and search.
 */
@Component({
  selector: 'app-document-browser',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    RouterLink,
    DataTableComponent,
    SearchBarComponent,
    LoadingSkeletonComponent,
    ErrorCardComponent,
    EmptyStateComponent,
  ],
  templateUrl: './document-browser.component.html',
  styleUrl: './document-browser.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DocumentBrowserComponent implements OnInit {
  private readonly documentService = inject(DocumentService);
  readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly documents = signal<Document[]>([]);
  readonly totalElements = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly currentPage = signal(0);
  readonly pageSize = signal(20);
  readonly searchQuery = signal('');
  readonly categoryFilter = signal<DocumentCategoryEnum | undefined>(undefined);

  readonly isEmpty = computed(
    () => this.documents().length === 0 && !this.loading() && !this.error(),
  );

  readonly displayedColumns = ['name', 'category', 'folder', 'currentVersion', 'fileSize', 'uploadedBy', 'updatedAt', 'actions'];
  readonly categories = Object.values(DocumentCategoryEnum);

  readonly formatFileSize = formatFileSize;

  ngOnInit(): void {
    void this.loadDocuments();
  }

  async loadDocuments(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.documentService.listDocuments(
        this.currentPage(),
        this.pageSize(),
        undefined,
        this.categoryFilter(),
      );
      this.documents.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Failed to load documents.');
    } finally {
      this.loading.set(false);
    }
  }

  onSearch(query: string): void {
    this.searchQuery.set(query);
    this.currentPage.set(0);
    if (query) {
      void this.searchDocuments(query);
    } else {
      void this.loadDocuments();
    }
  }

  async searchDocuments(query: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.documentService.searchDocuments(query, this.currentPage(), this.pageSize());
      this.documents.set(result.content);
      this.totalElements.set(result.totalElements);
    } catch {
      this.error.set('Search failed.');
    } finally {
      this.loading.set(false);
    }
  }

  onCategoryChange(category: DocumentCategoryEnum | ''): void {
    this.categoryFilter.set(category === '' ? undefined : category);
    this.currentPage.set(0);
    void this.loadDocuments();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    void this.loadDocuments();
  }

  viewVersions(document: Document): void {
    void this.router.navigate(['/staff/documents', document.id, 'versions']);
  }

  async downloadDocument(document: Document): Promise<void> {
    try {
      const blob = await this.documentService.downloadDocument(document.id);
      const url = URL.createObjectURL(blob);
      const a = window.document.createElement('a');
      a.href = url;
      a.download = document.name;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      this.snackBar.open('Failed to download document', 'Dismiss', { duration: 3000 });
    }
  }
}
