import { Component, Inject, OnInit } from "@angular/core";
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  Validators,
} from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { ModelParam, ModelType } from "app/models/landingai/model-param.model";

export interface ModelParamFormDialogData {
  modelParam?: ModelParam;
  locationId?: number;
}

@Component({
  selector: "app-model-param-form-dialog",
  templateUrl: "./model-param-form-dialog.component.html",
  styleUrls: ["./model-param-form-dialog.component.scss"],
  standalone: false,
})
export class ModelParamFormDialogComponent implements OnInit {
  form: FormGroup;
  isEditMode: boolean = false;
  hasLocation: boolean = false;
  isJsonValid: boolean = false;
  jsonValidationMessage: string = "";

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ModelParamFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ModelParamFormDialogData
  ) {
    this.isEditMode = !!data.modelParam;
    this.hasLocation = !!data.locationId;

    // Create form with validators
    this.form = this.fb.group({
      modelName: ["", [Validators.required, Validators.maxLength(50)]],
      modelType: ["", Validators.required],
      parameters: ["", [Validators.required, this.jsonValidator]],
    });
  }

  ngOnInit(): void {
    // Pre-populate form for edit mode
    if (this.isEditMode && this.data.modelParam) {
      this.form.patchValue({
        modelName: this.data.modelParam.modelName,
        modelType: this.data.modelParam.modelType,
        parameters: this.formatJson(this.data.modelParam.parameters),
      });
      // Trigger initial validation
      this.onParametersChange();
    }
  }

  /**
   * Custom JSON validator
   */
  jsonValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null; // Let required validator handle empty values
    }

    try {
      JSON.parse(control.value);
      return null;
    } catch (e) {
      return { invalidJson: true };
    }
  }

  /**
   * Format JSON string with indentation for display
   */
  formatJson(jsonString: string): string {
    try {
      const parsed = JSON.parse(jsonString);
      return JSON.stringify(parsed, null, 2);
    } catch (e) {
      return jsonString;
    }
  }

  /**
   * Handle parameters field change for real-time validation feedback
   */
  onParametersChange(): void {
    const parametersControl = this.form.get("parameters");
    const value = parametersControl?.value;

    if (!value || value.trim() === "") {
      this.isJsonValid = false;
      this.jsonValidationMessage = "";
      return;
    }

    try {
      JSON.parse(value);
      this.isJsonValid = true;
      this.jsonValidationMessage = "Valid JSON";
    } catch (e) {
      this.isJsonValid = false;
      if (e instanceof Error) {
        this.jsonValidationMessage = `Invalid JSON: ${e.message}`;
      } else {
        this.jsonValidationMessage = "Invalid JSON format";
      }
    }
  }

  /**
   * Handle save button click
   */
  onSave(): void {
    if (!this.form.valid || !this.hasLocation) {
      return;
    }

    const formValue = this.form.value;

    // Minify JSON before sending
    const minifiedJson = JSON.stringify(JSON.parse(formValue.parameters));

    const result = {
      modelName: formValue.modelName,
      modelType: formValue.modelType as ModelType,
      parameters: minifiedJson,
      id: this.data.modelParam?.id,
    };

    this.dialogRef.close(result);
  }

  /**
   * Handle cancel button click
   */
  onCancel(): void {
    this.dialogRef.close();
  }
}
