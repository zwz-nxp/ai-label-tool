import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
  name: "translateTimezone",
  standalone: false,
})
export class TranslateTimezonePipe implements PipeTransform {
  public transform(timezone: string): string {
    if (timezone === "Europe/Amsterdam") return "NL";
    return timezone;
  }
}
