import { Pipe, PipeTransform } from "@angular/core";
import { MassUploadType } from "app/models/mass-upload-type";

@Pipe({
  name: "filterMassUploadTypes",
  standalone: false,
})
export class FilterMassUploadTypesPipe implements PipeTransform {
  public transform(
    value: MassUploadType[] | null,
    ...args: string[]
  ): MassUploadType[] {
    return value
      ? value.filter((massUploadType) =>
          args.includes(massUploadType.massUploadName ?? "")
        )
      : [];
  }
}
