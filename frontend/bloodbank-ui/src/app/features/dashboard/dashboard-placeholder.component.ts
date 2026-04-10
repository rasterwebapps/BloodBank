import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-dashboard-placeholder',
  standalone: true,
  template: `
    <div class="flex items-center justify-center h-full p-8">
      <div class="text-center">
        <h1 class="text-2xl font-bold mb-2">BloodBank Dashboard</h1>
        <p class="text-gray-500">Dashboard feature coming soon.</p>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardPlaceholderComponent {}
