import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatIconModule],
  template: `
    <div class="flex items-center justify-center h-screen">
      <div class="text-center p-8">
        <mat-icon class="text-6xl text-red-600 mb-4">lock</mat-icon>
        <h1 class="text-3xl font-bold mb-2">Access Denied</h1>
        <p class="text-gray-500 mb-6">You do not have permission to access this page.</p>
        <a mat-flat-button color="primary" routerLink="/">Return to Home</a>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UnauthorizedComponent {}
