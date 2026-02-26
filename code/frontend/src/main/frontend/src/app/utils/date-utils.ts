import {
  differenceInCalendarDays,
  format,
  isSameDay,
  nextMonday,
} from "date-fns";

export type acceptedDateTypes = Date | string | number | null | undefined;

export class DateUtils {
  private readonly DATE_FORMAT = "yyyy-MM-dd";
  private readonly INFINITE_DATE = "9999-09-09";
  private readonly TIME_SUFFIX = " 00:00:00";

  public display(value: acceptedDateTypes): string {
    if (!value) {
      return "";
    }
    if (this.isSame(value, this.INFINITE_DATE)) {
      return "—";
    }

    return format(this.toDate(value), this.DATE_FORMAT);
  }

  public toJSON(value?: Date): string | null {
    if (!value) {
      return null;
    }
    return format(value, this.DATE_FORMAT);
  }

  /**
   * This function was changed in the scope of IEMDM-2398. The problem in the ticket could be recreated by switching the
   * timezone of your local machine to UCT-n (for example UTC-6). This would result in wrong dates displayed, one day
   * behind to be exact. Printing the date would give you a previous calendar day with time 18:00:00 (in case of UTC-6).
   *
   * This is because of the way the Date object is displayed to users. In case the date is displayed in the IEMDM
   * format (yyyy-MM-dd), it will always be localized to the timezone of users. Without providing time, date-only
   * forms are interpreted as a UTC time and date-time forms are interpreted as local time.
   * More about it at: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date
   *
   * As a fix for the issue, this.TIME_SUFFIX was created and added to the end of date. After this, the date was correct
   * regardless of the local machine's timezone.
   *
   * @param value provided date (Date | string | number | null | undefined)
   */
  public toDate(value: acceptedDateTypes): Date {
    let stringDate = new Date().toLocaleString();

    if (!value) {
      stringDate = this.INFINITE_DATE;
    } else if (value instanceof Date) {
      if (value.toString() === "Invalid Date") {
        stringDate = this.INFINITE_DATE;
      } else {
        stringDate = format(value, this.DATE_FORMAT);
      }
    } else if (typeof value === "string") {
      stringDate = value;
    }

    return new Date(stringDate.slice(0, 10).concat(this.TIME_SUFFIX));
  }

  public firstDate(first: acceptedDateTypes, second: acceptedDateTypes): Date {
    if (first) {
      return this.toDate(first);
    }
    if (second) {
      return this.toDate(second);
    }
    return new Date();
  }

  public addDays(date: Date, days: number): Date {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
  }

  public isBefore(date1: acceptedDateTypes, date2: acceptedDateTypes): boolean {
    const d1 = this.toDate(date1);
    const d2 = this.toDate(date2);
    return differenceInCalendarDays(d1, d2) < 0;
  }

  public isBeforeToday(date: Date): boolean {
    return this.isBefore(date, new Date());
  }

  public isSameOrBefore(
    date1: acceptedDateTypes,
    date2: acceptedDateTypes
  ): boolean {
    const d1 = this.toDate(date1);
    const d2 = this.toDate(date2);
    return differenceInCalendarDays(d1, d2) <= 0;
  }

  public isSame(date1: acceptedDateTypes, date2: acceptedDateTypes): boolean {
    return isSameDay(this.toDate(date1), this.toDate(date2));
  }

  public isSameOrAfter(
    date1: acceptedDateTypes,
    date2: acceptedDateTypes
  ): boolean {
    const d1 = this.toDate(date1);
    const d2 = this.toDate(date2);
    return differenceInCalendarDays(d1, d2) >= 0;
  }

  public isAfter(date1: acceptedDateTypes, date2: acceptedDateTypes): boolean {
    const d1 = this.toDate(date1);
    const d2 = this.toDate(date2);
    return differenceInCalendarDays(d1, d2) > 0;
  }

  public isAfterToday(date: acceptedDateTypes): boolean {
    return this.isAfter(date, new Date());
  }

  public isInBetween(dates: Date[], mondayOfSelectedWeek: Date): boolean {
    // For Period Events
    if (dates.length > 1) {
      // Checking if Monday of the selected week is after the start date
      let isStartDateValid = this.isSameOrAfter(mondayOfSelectedWeek, dates[0]);
      // If it is not, checking if the start date is within the selected week
      if (
        !isStartDateValid &&
        this.isWithinWeek(dates[0], mondayOfSelectedWeek)
      ) {
        isStartDateValid = true;
      }
      return (
        isStartDateValid && this.isSameOrBefore(mondayOfSelectedWeek, dates[1])
      );
    } else {
      // For Single date Events
      return this.isWithinWeek(dates[0], mondayOfSelectedWeek);
    }
  }

  public isWithinWeek(date: Date, mondayOfSelectedWeek: Date): boolean {
    const nextMonday = this.addDays(mondayOfSelectedWeek, 7);
    return (
      this.isSameOrAfter(date, mondayOfSelectedWeek) &&
      this.isBefore(date, nextMonday)
    );
  }

  public nextMonday(): Date {
    return this.nextMondayFrom(new Date());
  }

  public nextMondayFrom(date: Date): Date {
    return nextMonday(date);
  }

  public mondayTwoWeeksFrom(date: Date): Date {
    return this.nextMondayFrom(this.nextMondayFrom(date));
  }

  // return the index that contains the current effective data. If array is empty or all dates are time-phased, -1  will be returned.
  // note in input array must be sorted!
  public getEffectiveDate(effDates: Array<string>): number {
    let bestDate = effDates[0];
    let index = -1;

    for (let idx = 0; idx < effDates.length; idx++) {
      const date = effDates[idx];

      if (
        this.isSameOrAfter(date, bestDate) &&
        this.isSameOrBefore(date, new Date())
      ) {
        bestDate = date;
        index = idx;
      }
    }
    return bestDate ? index : -1;
  }

  public isMonday(date: Date | null): boolean {
    return !!date && date.getDay() === 1;
  }

  public isSunday(date: Date | null): boolean {
    return !!date && date.getDay() === 0;
  }

  public isClosestToToday(dates: Date[]): Date | undefined {
    const today = new Date();
    return this.isClosestTo(today, dates);
  }

  public isClosestTo(anchor: Date, dates: Date[]): Date | undefined {
    let result = dates.pop();
    if (!result) {
      return undefined;
    }
    let difference = differenceInCalendarDays(anchor, result);
    dates.forEach((date) => {
      const currentDifference = Math.abs(
        differenceInCalendarDays(anchor, date)
      );
      if (currentDifference < difference) {
        difference = currentDifference;
        result = date;
      }
    });
    return result;
  }

  public isInfinite(date: acceptedDateTypes): boolean {
    return this.isSame(date, this.INFINITE_DATE);
  }
}
