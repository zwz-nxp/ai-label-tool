import { CanDeactivateFn } from "@angular/router";

export interface HasUnsavedChanges {
  hasUnsavedChanges(): boolean;
}

export const hasUnsavedChangesGuard: CanDeactivateFn<HasUnsavedChanges> = (
  component: HasUnsavedChanges
): boolean => {
  if (component.hasUnsavedChanges()) {
    // Prompt the user to confirm leaving the page or perform other checks
    return confirm("You have unsaved changes. Are you sure you want to leave?");
  }
  return true;
};
