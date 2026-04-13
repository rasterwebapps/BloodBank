import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
} from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar } from '@angular/material/snack-bar';

import { DocumentService } from '../services/document.service';
import {
  DocumentCategoryEnum,
  DOCUMENT_CATEGORY_OPTIONS,
  formatFileSize,
} from '../models/document.model';

/**
 * Document upload — drag-and-drop file upload with metadata form.
 */
@Component({
  selector: 'app-document-upload',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatChipsModule,
  ],
  templateUrl: './document-upload.component.html',
  styleUrl: './document-upload.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DocumentUploadComponent {
  private readonly documentService = inject(DocumentService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly selectedFile = signal<File | null>(null);
  readonly dragOver = signal(false);
  readonly uploading = signal(false);

  readonly categoryOptions = DOCUMENT_CATEGORY_OPTIONS;
  readonly formatFileSize = formatFileSize;

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
    category: [DocumentCategoryEnum.OTHER as string, Validators.required],
    folder: [''],
    tags: [''],
  });

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragOver.set(true);
  }

  onDragLeave(): void {
    this.dragOver.set(false);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragOver.set(false);
    const file = event.dataTransfer?.files[0];
    if (file) this.selectFile(file);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) this.selectFile(file);
  }

  private selectFile(file: File): void {
    this.selectedFile.set(file);
    if (!this.form.controls.name.value) {
      this.form.controls.name.setValue(file.name.replace(/\.[^/.]+$/, ''));
    }
  }

  async upload(): Promise<void> {
    const file = this.selectedFile();
    if (!file || this.form.invalid) return;
    this.uploading.set(true);
    try {
      const value = this.form.getRawValue();
      await this.documentService.uploadDocument(file, {
        name: value.name,
        description: value.description || undefined,
        category: value.category as DocumentCategoryEnum,
        folder: value.folder || undefined,
        tags: value.tags ? value.tags.split(',').map((t) => t.trim()) : undefined,
      });
      this.snackBar.open('Document uploaded successfully', 'Dismiss', { duration: 3000 });
      void this.router.navigate(['/staff/documents']);
    } catch {
      this.snackBar.open('Failed to upload document', 'Dismiss', { duration: 3000 });
    } finally {
      this.uploading.set(false);
    }
  }

  cancel(): void {
    void this.router.navigate(['/staff/documents']);
  }
}
