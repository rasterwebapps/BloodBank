import {
  Component,
  ChangeDetectionStrategy,
  inject,
  computed,
} from '@angular/core';
import { Router, ActivatedRoute, NavigationEnd, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { filter, map, startWith } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

interface Breadcrumb {
  label: string;
  url: string;
}

/**
 * Auto-generated breadcrumb from route data.
 * Routes should include `data: { breadcrumb: 'Label' }` for proper display.
 */
@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [RouterLink, MatIconModule],
  templateUrl: './breadcrumb.component.html',
  styleUrl: './breadcrumb.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BreadcrumbComponent {
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

  private readonly navigationEnd$ = this.router.events.pipe(
    filter((event) => event instanceof NavigationEnd),
    startWith(null),
    map(() => this.buildBreadcrumbs(this.activatedRoute.root)),
  );

  readonly breadcrumbs = toSignal(this.navigationEnd$, { initialValue: [] as Breadcrumb[] });

  private buildBreadcrumbs(
    route: ActivatedRoute,
    url = '',
    breadcrumbs: Breadcrumb[] = [],
  ): Breadcrumb[] {
    const children = route.children;
    if (children.length === 0) {
      return breadcrumbs;
    }

    for (const child of children) {
      const routeUrl = child.snapshot.url.map((seg) => seg.path).join('/');
      const fullUrl = routeUrl ? `${url}/${routeUrl}` : url;
      const label = child.snapshot.data['breadcrumb'] as string | undefined;

      if (label) {
        breadcrumbs.push({ label, url: fullUrl });
      }

      return this.buildBreadcrumbs(child, fullUrl, breadcrumbs);
    }

    return breadcrumbs;
  }
}
