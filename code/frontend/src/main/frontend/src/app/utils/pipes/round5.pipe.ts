import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
  name: "round5",
  standalone: false,
})
export class RoundFivePipe implements PipeTransform {
  public transform(value: number): string {
    if (value === 0 || value) {
      return value.toFixed(5);
    }
    return "";
  }
}
