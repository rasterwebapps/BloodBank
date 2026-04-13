import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatStepperModule } from '@angular/material/stepper';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { DatePipe } from '@angular/common';
import { DonorPortalService } from '../services/donor-portal.service';
import { Branch, Appointment } from '../models/donor-portal.models';

@Component({
  selector: 'app-appointment-booking',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatStepperModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
  ],
  templateUrl: './appointment-booking.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentBookingComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(DonorPortalService);

  readonly branches = signal<Branch[]>([]);
  readonly loading = signal(false);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly booked = signal<Appointment | null>(null);
  readonly minDate = new Date();

  readonly timeSlots = [
    '09:00', '09:30', '10:00', '10:30', '11:00',
    '14:00', '14:30', '15:00', '15:30',
  ];

  readonly branchForm = this.fb.nonNullable.group({
    branchId: ['', Validators.required],
  });

  readonly dateForm = this.fb.nonNullable.group({
    date: [null as Date | null, Validators.required],
  });

  readonly slotForm = this.fb.nonNullable.group({
    timeSlot: ['', Validators.required],
  });

  ngOnInit(): void {
    this.loadBranches();
  }

  async loadBranches(): Promise<void> {
    this.loading.set(true);
    try {
      const branches = await this.service.getBranches();
      this.branches.set(branches);
    } catch {
      this.error.set('Failed to load branches.');
    } finally {
      this.loading.set(false);
    }
  }

  get selectedBranch(): Branch | undefined {
    return this.branches().find((b) => b.id === this.branchForm.value.branchId);
  }

  async confirmBooking(): Promise<void> {
    this.submitting.set(true);
    this.error.set(null);
    try {
      const dateVal = this.dateForm.getRawValue().date;
      const dateStr = dateVal ? (dateVal as Date).toISOString().split('T')[0] : '';
      const result = await this.service.bookAppointment({
        branchId: this.branchForm.getRawValue().branchId,
        date: dateStr,
        timeSlot: this.slotForm.getRawValue().timeSlot,
      });
      this.booked.set(result);
    } catch {
      this.error.set('Failed to book appointment. Please try again.');
    } finally {
      this.submitting.set(false);
    }
  }
}
