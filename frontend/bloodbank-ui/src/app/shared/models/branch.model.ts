/**
 * Branch model for multi-branch operations.
 */
export interface Branch {
  id: string;
  name: string;
  code: string;
  regionId: string | null;
  active: boolean;
}
