import { Role } from './role.enum';

/**
 * Represents the currently authenticated user.
 * Populated from Keycloak JWT claims.
 */
export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: Role[];
  branchId: string | null;
  branchName: string | null;
  realmRoles: string[];
  clientRoles: string[];
}
