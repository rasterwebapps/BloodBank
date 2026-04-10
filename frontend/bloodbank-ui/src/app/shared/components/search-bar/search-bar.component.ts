import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  signal,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

/**
 * Reusable search bar with debounced input.
 *
 * Usage:
 * ```html
 * <app-search-bar
 *   placeholder="Search donors..."
 *   [debounceMs]="300"
 *   (searchChange)="onSearch($event)" />
 * ```
 */
@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [FormsModule, MatFormFieldModule, MatInputModule, MatIconModule, MatButtonModule],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SearchBarComponent implements OnInit, OnDestroy {
  /** Placeholder text */
  readonly placeholder = input('Search...');

  /** Debounce time in milliseconds */
  readonly debounceMs = input(300);

  /** Emitted when the search term changes (after debounce) */
  readonly searchChange = output<string>();

  readonly searchTerm = signal('');

  private readonly searchSubject = new Subject<string>();
  private readonly destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.searchSubject
      .pipe(
        debounceTime(this.debounceMs()),
        distinctUntilChanged(),
        takeUntil(this.destroy$),
      )
      .subscribe((term) => {
        this.searchChange.emit(term);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onInput(value: string): void {
    this.searchTerm.set(value);
    this.searchSubject.next(value);
  }

  clear(): void {
    this.searchTerm.set('');
    this.searchSubject.next('');
  }
}
