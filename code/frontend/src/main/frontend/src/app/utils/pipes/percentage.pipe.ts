import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
  name: "percentage",
  standalone: false,
})
export class PercentagePipe implements PipeTransform {
  // accept number or string, so percentage can be combined with for instance round5
  public transform(
    value: number | string,
    isFraction = false,
    precision = 2
  ): string {
    if (value === 0 || value) {
      value = +value;
      if (isFraction) {
        value = value * 100;
      }
      return `${value.toFixed(precision)}%`;
    }
    return "";
  }
}
