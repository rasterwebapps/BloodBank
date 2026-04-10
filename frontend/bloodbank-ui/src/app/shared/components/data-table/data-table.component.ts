import {
  Component,
  ChangeDetectionStrategy,
  input,
  output,
  viewChild,
  AfterViewInit,
} from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule, MatSort, Sort } from '@angular/material/sort';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';

/**
 * Reusable data table wrapper around mat-table with mat-sort and mat-paginator.
 *
 * Usage:
 * ```html
 * <app-data-table
 *   [dataSource]="donors()"
 *   [columns]="['name', 'bloodGroup', 'status']"
 *   [totalElements]="total()"
 *   [loading]="loading()"
 *   (pageChange)="onPage($event)"
 *   (sortChange)="onSort($event)">
 *
 *   <ng-container matColumnDef="name">
 *     <th mat-header-cell *matHeaderCellDef mat-sort-header>Name</th>
 *     <td mat-cell *matCellDef="let row">{{ row.name }}</td>
 *   </ng-container>
 *   ...
 * </app-data-table>
 * ```
 */
@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatProgressBarModule,
  ],
  templateUrl: './data-table.component.html',
  styleUrl: './data-table.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DataTableComponent<T> implements AfterViewInit {
  /** Data to display in the table */
  readonly dataSource = input<T[]>([]);

  /** Column IDs to display */
  readonly columns = input<string[]>([]);

  /** Total number of elements for pagination */
  readonly totalElements = input(0);

  /** Page size options */
  readonly pageSizeOptions = input<number[]>([10, 25, 50, 100]);

  /** Current page size */
  readonly pageSize = input(10);

  /** Whether data is loading */
  readonly loading = input(false);

  /** Emitted when page changes */
  readonly pageChange = output<PageEvent>();

  /** Emitted when sort changes */
  readonly sortChange = output<Sort>();

  readonly sort = viewChild(MatSort);
  readonly paginator = viewChild(MatPaginator);

  ngAfterViewInit(): void {
    // Sort and paginator are available after view init
  }

  onPageChange(event: PageEvent): void {
    this.pageChange.emit(event);
  }

  onSortChange(event: Sort): void {
    this.sortChange.emit(event);
  }
}
