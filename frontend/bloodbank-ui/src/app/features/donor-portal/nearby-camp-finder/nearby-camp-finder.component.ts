import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { DonorPortalService } from '../services/donor-portal.service';
import { Camp } from '../models/donor-portal.models';

@Component({
  selector: 'app-nearby-camp-finder',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
  ],
  templateUrl: './nearby-camp-finder.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NearbyCampFinderComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(DonorPortalService);

  readonly camps = signal<Camp[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly geoDenied = signal(false);

  readonly cityForm = this.fb.nonNullable.group({
    city: ['', Validators.required],
  });

  ngOnInit(): void {
    this.tryGeolocation();
  }

  tryGeolocation(): void {
    if (!navigator.geolocation) {
      this.geoDenied.set(true);
      return;
    }
    this.loading.set(true);
    navigator.geolocation.getCurrentPosition(
      async (pos) => {
        try {
          const camps = await this.service.getNearbyCamps(
            pos.coords.latitude,
            pos.coords.longitude,
          );
          this.camps.set(camps);
        } catch {
          this.error.set('Failed to load nearby camps.');
        } finally {
          this.loading.set(false);
        }
      },
      () => {
        this.geoDenied.set(true);
        this.loading.set(false);
      },
    );
  }

  async searchByCity(): Promise<void> {
    if (this.cityForm.invalid) return;
    this.loading.set(true);
    this.error.set(null);
    try {
      const camps = await this.service.getCampsByCity(this.cityForm.getRawValue().city);
      this.camps.set(camps);
    } catch {
      this.error.set('Failed to load camps. Please try again.');
    } finally {
      this.loading.set(false);
    }
  }
}
