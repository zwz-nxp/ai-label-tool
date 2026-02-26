import { DateUtils } from "app/utils/date-utils";

const du = new DateUtils();

export class NxpProductionYear {
  public year = 0;
  public startDate = new Date();
  public endDate?: Date;

  public static clone(input: NxpProductionYear): NxpProductionYear {
    const clone = new NxpProductionYear();
    clone.startDate = du.toDate(input.startDate);
    clone.endDate = du.toDate(input.endDate);
    clone.year = input.year;
    return clone;
  }

  public toJSON(): string {
    const du = new DateUtils();
    return JSON.stringify({
      ...this,
      startDate: du.toJSON(this.startDate),
      endDate: undefined,
    });
  }
}
