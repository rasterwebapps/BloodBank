import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';

import { LoadingSkeletonComponent } from '@shared/components/loading-skeleton/loading-skeleton.component';
import { ErrorCardComponent } from '@shared/components/error-card/error-card.component';
import { NotificationService } from '@core/services/notification.service';
import { BranchService } from '@features/branch/services/branch.service';
import { Branch } from '@features/branch/models/branch.model';
import { UserService } from '../services/user.service';
import {
  UserCreateRequest,
  UserUpdateRequest,
  UserRole,
  UserStatus,
  ASSIGNABLE_ROLES,
} from '../models/user-management.model';

/**
 * User create/edit form.
 * Mode is determined by route: '/new' → create, '/:id/edit' → edit.
 */
@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    LoadingSkeletonComponent,
    ErrorCardComponent,
  ],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly userService = inject(UserService);
  private readonly branchService = inject(BranchService);
  private readonly notification = inject(NotificationService);

  readonly isEditMode = signal(false);
  readonly userId = signal<string | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly branches = signal<Branch[]>([]);

  readonly assignableRoles = ASSIGNABLE_ROLES;
  readonly statusOptions: UserStatus[] = ['ACTIVE', 'INACTIVE', 'SUSPENDED'];

  readonly pageTitle = computed(() => (this.isEditMode() ? 'Edit User' : 'Add User'));
  readonly submitLabel = computed(() => (this.isEditMode() ? 'Update' : 'Create'));

  readonly form = this.fb.group({
    username: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.minLength(3),
      Validators.maxLength(50),
      Validators.pattern(/^[a-zA-Z0-9._-]+$/),
    ]),
    firstName: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(100)]),
    lastName: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(100)]),
    email: this.fb.nonNullable.control('', [Validators.required, Validators.email]),
    temporaryPassword: this.fb.nonNullable.control('', [Validators.minLength(8)]),
    roles: this.fb.nonNullable.control<UserRole[]>([], Validators.required),
    branchId: this.fb.control<string | null>(null),
    status: this.fb.nonNullable.control<UserStatus>('ACTIVE'),
  });

  ngOnInit(): void {
    void this.loadBranches();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.userId.set(id);
      this.form.controls.username.disable();
      this.form.controls.temporaryPassword.clearValidators();
      this.form.controls.temporaryPassword.updateValueAndValidity();
      void this.loadUser(id);
    } else {
      this.form.controls.temporaryPassword.setValidators([
        Validators.required,
        Validators.minLength(8),
      ]);
      this.form.controls.temporaryPassword.updateValueAndValidity();
    }
  }

  async loadBranches(): Promise<void> {
    try {
      const result = await this.branchService.list(0, 100);
      this.branches.set(result.content);
    } catch {
      // Non-critical
    }
  }

  async loadUser(id: string): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const user = await this.userService.getById(id);
      this.form.patchValue({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        roles: user.roles as UserRole[],
        branchId: user.branchId ?? null,
        status: user.status,
      });
    } catch {
      this.error.set('Failed to load user data.');
    } finally {
      this.loading.set(false);
    }
  }

  isRoleSelected(role: UserRole): boolean {
    return this.form.controls.roles.value.includes(role);
  }

  toggleRole(role: UserRole): void {
    const current = this.form.controls.roles.value;
    if (current.includes(role)) {
      this.form.controls.roles.setValue(current.filter(r => r !== role));
    } else {
      this.form.controls.roles.setValue([...current, role]);
    }
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    try {
      if (this.isEditMode()) {
        await this.updateUser();
      } else {
        await this.createUser();
      }
    } catch {
      const action = this.isEditMode() ? 'update' : 'create';
      this.error.set(`Failed to ${action} user. Please try again.`);
      this.notification.error(`Failed to ${action} user.`);
    } finally {
      this.saving.set(false);
    }
  }

  private async createUser(): Promise<void> {
    const fv = this.form.getRawValue();
    const request: UserCreateRequest = {
      username: fv.username,
      firstName: fv.firstName,
      lastName: fv.lastName,
      email: fv.email,
      temporaryPassword: fv.temporaryPassword,
      roles: fv.roles,
      branchId: fv.branchId ?? undefined,
    };
    await this.userService.create(request);
    this.notification.success('User created successfully.');
    void this.router.navigate(['/staff/users']);
  }

  private async updateUser(): Promise<void> {
    const fv = this.form.getRawValue();
    const request: UserUpdateRequest = {
      firstName: fv.firstName,
      lastName: fv.lastName,
      email: fv.email,
      roles: fv.roles,
      branchId: fv.branchId ?? undefined,
      status: fv.status,
    };
    await this.userService.update(this.userId()!, request);
    this.notification.success('User updated successfully.');
    void this.router.navigate(['/staff/users']);
  }

  cancel(): void {
    void this.router.navigate(['/staff/users']);
  }
}
