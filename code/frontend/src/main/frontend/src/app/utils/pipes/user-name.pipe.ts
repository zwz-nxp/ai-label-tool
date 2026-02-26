import { Pipe, PipeTransform } from "@angular/core";
import { Store } from "@ngrx/store";
import * as SystemSelectors from "app/state/system/system.selectors";

@Pipe({
  name: "userName",
  standalone: false,
})
export class UserNamePipe implements PipeTransform {
  private usernames: Record<string, string> = {};

  public constructor(private store: Store) {
    this.store
      .select(SystemSelectors.selectUserNames)
      .subscribe((value) => (this.usernames = value));
  }

  public transform(wbi: string): string {
    return this.usernames[wbi] ?? wbi;
  }
}
