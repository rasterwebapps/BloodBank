import { Injectable, signal, computed } from '@angular/core';

/**
 * Represents a branch in the system.
 */
export interface Branch {
  id: string;
  name: string;
  code: string;
  regionId: string | null;
  active: boolean;
}

/**
 * Branch context service — manages the currently selected branch.
 * Uses Angular Signals for reactive state management.
 * Populated from JWT claims on login or from user branch selection.
 */
@Injectable({ providedIn: 'root' })
export class BranchContextService {
  /** Currently selected branch */
  readonly currentBranch = signal<Branch | null>(null);

  /** List of branches the user has access to */
  readonly branches = signal<Branch[]>([]);

  /** Loading state for branch data */
  readonly loading = signal(false);

  /** Derived: current branch ID */
  readonly branchId = computed(() => this.currentBranch()?.id ?? null);

  /** Derived: current branch name */
  readonly branchName = computed(() => this.currentBranch()?.name ?? '');

  /** Derived: whether a branch is selected */
  readonly hasBranch = computed(() => this.currentBranch() !== null);

  /** Derived: number of available branches */
  readonly branchCount = computed(() => this.branches().length);

  /** Select a branch by ID */
  selectBranch(branchId: string): void {
    const branch = this.branches().find((b) => b.id === branchId) ?? null;
    this.currentBranch.set(branch);
  }

  /** Set the list of available branches */
  setBranches(branches: Branch[]): void {
    this.branches.set(branches);
  }

  /** Set the current branch directly (e.g., from JWT claims) */
  setCurrentBranch(branch: Branch): void {
    this.currentBranch.set(branch);
  }

  /** Clear the branch context */
  clear(): void {
    this.currentBranch.set(null);
    this.branches.set([]);
  }
}
