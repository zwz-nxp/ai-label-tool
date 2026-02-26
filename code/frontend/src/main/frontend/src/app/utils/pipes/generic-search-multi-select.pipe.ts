import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
  name: "genericSearchMultiSelect",
  standalone: false,
})
export class GenericSearchMultiSelectPipe implements PipeTransform {
  public transform(value: any): string {
    let result = value as string;
    if (result === undefined) {
      return value;
    }
    result = result.toLowerCase().replaceAll("_", " ");
    return result.charAt(0).toUpperCase() + result.slice(1);
  }
}
