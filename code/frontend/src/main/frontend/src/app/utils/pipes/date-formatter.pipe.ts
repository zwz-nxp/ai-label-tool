import { Pipe, PipeTransform } from "@angular/core";
import { acceptedDateTypes, DateUtils } from "../date-utils";

@Pipe({
  name: "dateFormatter",
  standalone: false,
})
export class DateFormatterPipe implements PipeTransform {
  private dateUtils = new DateUtils();

  public transform(value?: acceptedDateTypes): string {
    return this.dateUtils.display(value);
  }
}
