import { Directive, ElementRef, inject, afterNextRender } from '@angular/core';

/**
 * Directive that auto-focuses the host element after render.
 *
 * Usage:
 * ```html
 * <input matInput appAutoFocus />
 * ```
 */
@Directive({
  selector: '[appAutoFocus]',
  standalone: true,
})
export class AutoFocusDirective {
  private readonly el = inject(ElementRef<HTMLElement>);

  constructor() {
    afterNextRender(() => {
      this.el.nativeElement.focus();
    });
  }
}
