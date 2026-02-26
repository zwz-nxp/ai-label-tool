import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
  name: "orDashes",
  standalone: false,
})
export class OrDashesPipe implements PipeTransform {
  public transform(value: string, ..._args: unknown[]): string {
    if (value) return value;
    else return "--";
  }
}
