/**
 * Error Handler Service
 *
 * Provides centralized error handling for the AI Training module.
 * Transforms HTTP errors into user-friendly messages and displays notifications.
 *
 * Requirement 24.6: THE System SHALL handle HTTP errors and transform them
 * into user-friendly error messages
 */

import { Injectable } from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Observable, throwError } from "rxjs";

/**
 * Error response structure from the backend
 */
export interface ErrorResponse {
  code: string;
  message: string;
  details?: string[];
}

/**
 * Validation error messages mapping
 */
export const VALIDATION_MESSAGES: Record<string, Record<string, string>> = {
  epochs: {
    required: "Epoch 值是必填项",
    min: "Epoch 值不能小于 1",
    max: "Epoch 值不能大于 100",
  },
  distribution: {
    sumNotHundred: "训练集、验证集和测试集的比例之和必须等于 100%",
  },
  modelSize: {
    required: "请选择模型尺寸",
  },
  crop: {
    negativeValue: "裁剪参数不能为负数",
  },
  resize: {
    invalidDimension: "尺寸必须为正整数",
  },
};

@Injectable({ providedIn: "root" })
export class ErrorHandlerService {
  private readonly defaultDuration = 5000;

  constructor(private snackBar: MatSnackBar) {}

  /**
   * Handle HTTP errors and display user-friendly messages
   *
   * @param error The HTTP error response
   * @returns Observable that throws an error with a user-friendly message
   */
  handleHttpError(error: HttpErrorResponse): Observable<never> {
    const message = this.getErrorMessage(error);
    this.showError(message);
    return throwError(() => new Error(message));
  }

  /**
   * Get a user-friendly error message from an HTTP error
   *
   * @param error The HTTP error response
   * @returns User-friendly error message
   */
  getErrorMessage(error: HttpErrorResponse): string {
    // Network error (no response from server)
    if (error.status === 0) {
      return "网络连接失败，请检查网络设置";
    }

    // Client-side error (bad request)
    if (error.status === 400) {
      return this.extractValidationMessage(error) || "请求参数无效";
    }

    // Authentication error
    if (error.status === 401) {
      return "登录已过期，请重新登录";
    }

    // Authorization error
    if (error.status === 403) {
      return "没有权限执行此操作";
    }

    // Not found error
    if (error.status === 404) {
      return "请求的资源不存在";
    }

    // Conflict error
    if (error.status === 409) {
      return error.error?.message || "操作冲突，请稍后重试";
    }

    // Server error
    if (error.status >= 500) {
      return "服务器错误，请稍后重试";
    }

    // Unknown error
    return error.error?.message || "发生未知错误";
  }

  /**
   * Show an error notification
   *
   * @param message Error message to display
   * @param duration Duration in milliseconds (default: 5000)
   */
  showError(message: string, duration: number = this.defaultDuration): void {
    this.snackBar.open(message, "关闭", {
      duration,
      panelClass: ["error-snackbar"],
      horizontalPosition: "center",
      verticalPosition: "bottom",
    });
  }

  /**
   * Show a success notification
   *
   * @param message Success message to display
   * @param duration Duration in milliseconds (default: 3000)
   */
  showSuccess(message: string, duration: number = 3000): void {
    this.snackBar.open(message, "关闭", {
      duration,
      panelClass: ["success-snackbar"],
      horizontalPosition: "center",
      verticalPosition: "bottom",
    });
  }

  /**
   * Show a warning notification
   *
   * @param message Warning message to display
   * @param duration Duration in milliseconds (default: 4000)
   */
  showWarning(message: string, duration: number = 4000): void {
    this.snackBar.open(message, "关闭", {
      duration,
      panelClass: ["warning-snackbar"],
      horizontalPosition: "center",
      verticalPosition: "bottom",
    });
  }

  /**
   * Show an info notification
   *
   * @param message Info message to display
   * @param duration Duration in milliseconds (default: 3000)
   */
  showInfo(message: string, duration: number = 3000): void {
    this.snackBar.open(message, "关闭", {
      duration,
      panelClass: ["info-snackbar"],
      horizontalPosition: "center",
      verticalPosition: "bottom",
    });
  }

  /**
   * Get validation message for a specific field and error type
   *
   * @param field Field name
   * @param errorType Error type
   * @returns Validation message or default message
   */
  getValidationMessage(field: string, errorType: string): string {
    return VALIDATION_MESSAGES[field]?.[errorType] || "输入无效";
  }

  /**
   * Extract validation error message from the error response
   *
   * @param error The HTTP error response
   * @returns Validation error message or null
   */
  private extractValidationMessage(error: HttpErrorResponse): string | null {
    const errorBody = error.error as ErrorResponse;

    if (errorBody?.message) {
      return errorBody.message;
    }

    if (errorBody?.details && errorBody.details.length > 0) {
      return errorBody.details.join("; ");
    }

    return null;
  }
}
