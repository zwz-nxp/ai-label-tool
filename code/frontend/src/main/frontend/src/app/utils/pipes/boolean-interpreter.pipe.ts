import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
  name: "booleanInterpreter",
  standalone: false,
})
export class BooleanInterpreterPipe implements PipeTransform {
  public transform(value: boolean): string {
    return value ? "Yes" : "No";
  }
}
