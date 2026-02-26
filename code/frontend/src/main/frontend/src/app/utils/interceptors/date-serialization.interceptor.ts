import { Injectable } from "@angular/core";
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from "@angular/common/http";
import { Observable } from "rxjs";

/* eslint-disable @typescript-eslint/no-explicit-any */

@Injectable()
export class DateSerializationInterceptor implements HttpInterceptor {
  public intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    if (req.body instanceof FormData) return next.handle(req);

    const modifiedReq = req.clone({
      body: this.serializeDates(req.body),
    });
    return next.handle(modifiedReq);
  }

  private serializeDates(body: any): any {
    if (!body) {
      return body;
    }

    if (body instanceof Date) {
      return this.formatDate(body);
    }

    if (Array.isArray(body)) {
      return body.map((item) => this.serializeDates(item));
    }

    if (typeof body === "object") {
      const copy = { ...body };
      Object.keys(copy).forEach((key) => {
        if (key !== "lastUpdated") {
          copy[key] = this.serializeDates(copy[key]);
        }
      });
      return copy;
    }

    return body;
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, "0");
    const day = date.getDate().toString().padStart(2, "0");
    return `${year}-${month}-${day}`;
  }
}
