export interface DualListItem {
  id: string;
  name: string;
  sortOrder: number;
}

export interface DualListResult<T extends DualListItem> {
  shown: T[];
  hidden: T[];
  saved: boolean;
}

export class DualListBoxList<T extends DualListItem> {
  /** Last element touched */
  public last: T | undefined;
  /** Name of the list */
  public readonly name: string;
  /** text filter */
  public picker: string;

  public dragStart: boolean;
  public dragOver: boolean;

  public pickList: Array<T>;
  public sourceList: Array<T>;
  public selectedList: Array<T>;

  public constructor(name: string) {
    this.name = name;
    this.last = undefined;
    this.picker = "";
    this.dragStart = false;
    this.dragOver = false;

    // Arrays will contain objects of { _id, _name }.
    this.pickList = [];
    this.sourceList = [];
    this.selectedList = [];
  }
}
