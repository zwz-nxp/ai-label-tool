import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
  name: "actionInterpreter",
  standalone: false,
})
export class ActionInterpreterPipe implements PipeTransform {
  transform(value: boolean): string {
    return value ? "Enable" : "Disable";
  }
}
