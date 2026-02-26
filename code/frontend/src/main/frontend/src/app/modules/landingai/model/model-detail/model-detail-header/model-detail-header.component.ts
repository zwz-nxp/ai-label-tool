import {
  Component,
  OnInit,
  Output,
  EventEmitter,
  ChangeDetectionStrategy,
} from "@angular/core";
import { FormControl, Validators } from "@angular/forms";
import { Store } from "@ngrx/store";
import { Observable } from "rxjs";
import { Model } from "app/models/landingai/model";
import * as ModelDetailActions from "app/state/landingai/model/model-detail/model-detail.actions";
import * as ModelDetailSelectors from "app/state/landingai/model/model-detail/model-detail.selectors";

/**
 * Model Detail Header Component
 * Displays model metadata and provides rename functionality
 * Requirements: 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 21.1
 */
@Component({
  selector: "app-model-detail-header",
  standalone: false,
  templateUrl: "./model-detail-header.component.html",
  styleUrls: ["./model-detail-header.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ModelDetailHeaderComponent implements OnInit {
  @Output() public close = new EventEmitter<void>();
  @Output() public toggleWidth = new EventEmitter<void>();

  public modelData$: Observable<Model | null>;
  public panelWidth$: Observable<"normal" | "expanded">;
  public isEditingName = false;
  public modelAliasControl: FormControl;
  public renameError$: Observable<string | null>;

  constructor(private store: Store) {
    this.modelData$ = this.store.select(ModelDetailSelectors.selectModelData);
    this.panelWidth$ = this.store.select(ModelDetailSelectors.selectPanelWidth);
    this.renameError$ = this.store.select(
      ModelDetailSelectors.selectRenameError
    );

    // Model alias validation: 1-36 characters, no control characters
    this.modelAliasControl = new FormControl("", [
      Validators.required,
      Validators.minLength(1),
      Validators.maxLength(36),
      this.noControlCharactersValidator,
    ]);
  }

  public ngOnInit(): void {}

  /**
   * Start editing model name
   * Requirements: 4.1
   */
  public startEditingName(currentAlias: string): void {
    this.isEditingName = true;
    this.modelAliasControl.setValue(currentAlias);
  }

  /**
   * Cancel editing model name
   */
  public cancelEditingName(): void {
    this.isEditingName = false;
    this.modelAliasControl.reset();
  }

  /**
   * Save new model name
   * Requirements: 4.2, 4.3, 4.4
   */
  public saveModelName(modelId: number): void {
    if (this.modelAliasControl.valid) {
      const newAlias = this.modelAliasControl.value.trim();
      this.store.dispatch(
        ModelDetailActions.renameModel({ modelId, newAlias })
      );
      this.isEditingName = false;
    }
  }

  /**
   * Validator to check for control characters (ASCII 0-31, 127)
   * Requirements: 4.3
   */
  private noControlCharactersValidator(
    control: FormControl
  ): { [key: string]: boolean } | null {
    if (!control.value) {
      return null;
    }

    const value = control.value as string;
    // Check for control characters (ASCII 0-31 and 127)
    const hasControlChars = /[\x00-\x1F\x7F]/.test(value);

    return hasControlChars ? { controlCharacters: true } : null;
  }

  /**
   * Get validation error message
   */
  public getErrorMessage(): string {
    if (this.modelAliasControl.hasError("required")) {
      return "Model name is required";
    }
    if (this.modelAliasControl.hasError("minlength")) {
      return "Model name must be at least 1 character";
    }
    if (this.modelAliasControl.hasError("maxlength")) {
      return "Model name must not exceed 36 characters";
    }
    if (this.modelAliasControl.hasError("controlCharacters")) {
      return "Model name cannot contain control characters";
    }
    return "";
  }

  /**
   * Format date for display
   */
  public formatDate(date: Date | null): string {
    if (!date) {
      return "N/A";
    }
    return new Date(date).toLocaleString();
  }

  /**
   * Close panel
   * Requirements: 21.1
   */
  public onClose(): void {
    this.close.emit();
  }

  /**
   * Toggle panel width
   * Requirements: 21.1
   */
  public onToggleWidth(): void {
    this.toggleWidth.emit();
  }
}
