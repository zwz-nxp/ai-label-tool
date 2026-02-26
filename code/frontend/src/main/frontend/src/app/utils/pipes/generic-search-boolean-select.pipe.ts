import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
  name: "genericSearchBooleanSelect",
  standalone: false,
})
export class GenericSearchBooleanSelectPipe implements PipeTransform {
  public transform(value?: boolean): string {
    if (value === undefined) {
      return "Both";
    } else {
      return value ? "Yes" : "No";
    }
  }
}
