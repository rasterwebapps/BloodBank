# /project:add-angular-feature

Generate an Angular 21 feature module with standalone components.

## Arguments

- `$ARGUMENTS` should be: `{feature-name} {BackendService} {Role1} [Role2] ...`

## Steps

1. Read CLAUDE.md for Angular patterns (standalone, signals, zoneless, OnPush)
2. Create feature directory: `frontend/bloodbank-ui/src/app/features/{feature-name}/`
3. Generate routes file: `{feature-name}.routes.ts` with lazy-loaded routes and role guards
4. Generate list component: `components/{feature}-list/`
   - Standalone component with `ChangeDetectionStrategy.OnPush`
   - Use signals for reactive state
   - Use `inject()` for dependency injection
5. Generate detail component: `components/{feature}-detail/`
6. Generate form component: `components/{feature}-form/`
7. Generate API service: `services/{feature}.service.ts`
   - Use `inject(HttpClient)` 
   - Use `firstValueFrom` for HTTP calls
   - Base URL from environment
8. Generate TypeScript models: `models/{feature}.model.ts`
9. Generate role guard: `guards/{feature}-role.guard.ts`
10. Register feature routes in `app.routes.ts` via `loadChildren`
11. Add navigation entry in sidebar (role-filtered)
