import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { AuthService } from '@core/auth/auth.service';
import { HospitalPortalService } from '../services/hospital-portal.service';
import { HospitalContract } from '../models/hospital-portal.model';

@Component({
  selector: 'app-contract-view',
  standalone: true,
  imports: [
    DatePipe,
    DecimalPipe,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule,
    MatDividerModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './contract-view.component.html',
  styleUrl: './contract-view.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContractViewComponent implements OnInit {
  private readonly hospitalPortalService = inject(HospitalPortalService);
  private readonly authService = inject(AuthService);

  readonly contract = signal<HospitalContract | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly utilizationPercent = computed(() => {
    const c = this.contract();
    if (!c || c.contractedUnits === 0) return 0;
    return Math.min(
      100,
      Math.max(0, Math.round((c.usedUnits / c.contractedUnits) * 100)),
    );
  });

  ngOnInit(): void {
    const hospitalId = this.authService.currentUser()?.branchId ?? '';
    this.loadContract(hospitalId);
  }

  async loadContract(hospitalId: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const result = await this.hospitalPortalService.getContract(hospitalId);
      this.contract.set(result);
    } catch {
      this.error.set('Failed to load contract information. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }
}
