import {
  Component,
  DoCheck,
  EventEmitter,
  Input,
  IterableChangeRecord,
  IterableDiffer,
  IterableDiffers,
  OnChanges,
  Output,
  SimpleChange,
} from "@angular/core";
import { DualListBoxList, DualListItem } from "app/models/dual-list";

export type compareFunction<T> = (a: T, b: T) => number;

let nextId = 0;

@Component({
  selector: "app-dual-list-box",
  styleUrls: ["./dual-list-box.component.scss"],
  templateUrl: "./dual-list-box.component.html",
  standalone: false,
})
export class DualListBoxComponent<T extends DualListItem>
  implements DoCheck, OnChanges
{
  public static readonly AVAILABLE_LIST_NAME = "available";
  public static readonly SELECTED_LIST_NAME = "selected";

  public static readonly LEFT_TO_RIGHT = "left-to-right";
  public static readonly RIGHT_TO_LEFT = "right-to-left";

  public static DEFAULT_FORMAT = {
    add: "Add",
    remove: "Remove",
    all: "All",
    none: "None",
    direction: DualListBoxComponent.LEFT_TO_RIGHT,
    draggable: true,
    locale: undefined,
  };

  @Output() public destinationChange = new EventEmitter();
  @Input() public sourceTitle = "Available Items";
  @Input() public destinationTitle = "Picked Items";
  @Input() public source!: Array<T>;
  @Input() public destination!: Array<T>;
  @Input() public id = `dual-list-box-${nextId++}`;
  @Input() public filter = false;
  @Input() public format = DualListBoxComponent.DEFAULT_FORMAT;
  @Input() public sort = false;
  @Input() public disabled = false;
  @Input() public compare?: compareFunction<T>;

  public availableItems!: DualListBoxList<T>;
  public selectedItems!: DualListBoxList<T>;

  private sourceDiffer!: IterableDiffer<T>;
  private destinationDiffer!: IterableDiffer<T>;

  public constructor(private differs: IterableDiffers) {
    this.availableItems = new DualListBoxList(
      DualListBoxComponent.AVAILABLE_LIST_NAME
    );
    this.selectedItems = new DualListBoxList(
      DualListBoxComponent.SELECTED_LIST_NAME
    );
  }

  public sorter = (a: T, b: T): number => {
    return a.sortOrder < b.sortOrder ? -1 : a.sortOrder > b.sortOrder ? 1 : 0;
  };

  public ngOnChanges(changeRecord: { [key: string]: SimpleChange }): void {
    if (changeRecord["filter"]) {
      if (changeRecord["filter"].currentValue === false) {
        this.clearFilter(this.availableItems);
        this.clearFilter(this.selectedItems);
      }
    }

    if (changeRecord["sort"]) {
      if (
        changeRecord["sort"].currentValue === true &&
        this.compare === undefined
      ) {
        this.compare = this.sorter;
      } else if (changeRecord["sort"].currentValue === false) {
        this.compare = undefined;
      }
    }

    if (changeRecord["format"]) {
      this.format = changeRecord["format"].currentValue;

      if (typeof this.format.direction === "undefined") {
        this.format.direction = DualListBoxComponent.LEFT_TO_RIGHT;
      }

      if (typeof this.format.add === "undefined") {
        this.format.add = DualListBoxComponent.DEFAULT_FORMAT.add;
      }

      if (typeof this.format.remove === "undefined") {
        this.format.remove = DualListBoxComponent.DEFAULT_FORMAT.remove;
      }

      if (typeof this.format.all === "undefined") {
        this.format.all = DualListBoxComponent.DEFAULT_FORMAT.all;
      }

      if (typeof this.format.none === "undefined") {
        this.format.none = DualListBoxComponent.DEFAULT_FORMAT.none;
      }

      if (typeof this.format.draggable === "undefined") {
        this.format.draggable = DualListBoxComponent.DEFAULT_FORMAT.draggable;
      }
    }

    if (changeRecord["source"]) {
      this.availableItems = new DualListBoxList(
        DualListBoxComponent.AVAILABLE_LIST_NAME
      );
      this.setSourceDiffer();
      this.setDestinationDiffer();
    }

    if (changeRecord["destination"]) {
      this.selectedItems = new DualListBoxList(
        DualListBoxComponent.SELECTED_LIST_NAME
      );
      this.setDestinationDiffer();
      this.setSourceDiffer();
    }
  }

  public ngDoCheck(): void {
    if (this.source && this.buildAvailableList(this.source)) {
      this.onFilter(this.availableItems);
    }
    if (this.destination && this.buildChosenList(this.destination)) {
      this.onFilter(this.selectedItems);
    }
  }

  public buildAvailableList(source: Array<T>): boolean {
    const sourceChanges = this.sourceDiffer.diff(source);
    if (sourceChanges) {
      sourceChanges.forEachRemovedItem(
        (changeRecord: IterableChangeRecord<T>) => {
          const idx = this.findItemIndex(
            this.availableItems.sourceList,
            changeRecord.item
          );
          if (idx !== -1) {
            this.availableItems.sourceList.splice(idx, 1);
          }
        }
      );

      sourceChanges.forEachAddedItem(
        (changeRecord: IterableChangeRecord<T>) => {
          // Do not add duplicates even if source has duplicates.
          if (
            this.findItemIndex(
              this.availableItems.sourceList,
              changeRecord.item
            ) === -1
          ) {
            this.availableItems.sourceList.push(changeRecord.item);
          }
        }
      );

      if (this.compare) {
        this.availableItems.sourceList.sort(this.compare);
      }
      this.availableItems.selectedList = this.availableItems.sourceList;

      return true;
    }
    return false;
  }

  public buildChosenList(destination: Array<T>): boolean {
    let moved = false;
    const destChanges = this.destinationDiffer.diff(destination);
    if (destChanges) {
      destChanges.forEachRemovedItem(
        (changeRecord: IterableChangeRecord<T>) => {
          const index = this.findItemIndex(
            this.selectedItems.sourceList,
            changeRecord.item
          );
          if (index !== -1) {
            if (
              !this.isItemSelected(
                this.selectedItems.pickList,
                this.selectedItems.sourceList[index]
              )
            ) {
              this.selectItem(
                this.selectedItems.pickList,
                this.selectedItems.sourceList[index]
              );
            }
            this.moveItem(
              this.selectedItems,
              this.availableItems,
              this.selectedItems.sourceList[index],
              false
            );
            moved = true;
          }
        }
      );

      destChanges.forEachAddedItem((changeRecord: IterableChangeRecord<T>) => {
        const index = this.findItemIndex(
          this.availableItems.sourceList,
          changeRecord.item
        );
        if (index !== -1) {
          if (
            !this.isItemSelected(
              this.availableItems.pickList,
              this.availableItems.sourceList[index]
            )
          ) {
            this.selectItem(
              this.availableItems.pickList,
              this.availableItems.sourceList[index]
            );
          }
          this.moveItem(
            this.availableItems,
            this.selectedItems,
            this.availableItems.sourceList[index],
            false
          );
          moved = true;
        }
      });

      if (this.compare) {
        this.selectedItems.sourceList.sort(this.compare);
      }
      this.selectedItems.selectedList = this.selectedItems.sourceList;

      if (moved) {
        this.trueUp();
      }
      return true;
    }
    return false;
  }

  public setSourceDiffer(): void {
    this.availableItems.sourceList.length = 0;
    this.availableItems.pickList.length = 0;

    if (this.source !== undefined) {
      this.sourceDiffer = this.differs.find(this.source).create();
    }
  }

  public setDestinationDiffer(): void {
    if (this.destination !== undefined) {
      this.destinationDiffer = this.differs.find(this.destination).create();
    }
  }

  public isDirectionLeftToRight(): boolean {
    return this.format.direction === DualListBoxComponent.LEFT_TO_RIGHT;
  }

  public dragEnd(list: DualListBoxList<T> | undefined = undefined): boolean {
    if (list) {
      list.dragStart = false;
    } else {
      this.availableItems.dragStart = false;
      this.selectedItems.dragStart = false;
    }
    return false;
  }

  public drag(event: DragEvent, item: T, list: DualListBoxList<T>): void {
    if (!this.isItemSelected(list.pickList, item)) {
      this.selectItem(list.pickList, item);
    }
    list.dragStart = true;

    // Set a custom type to be this dual-list's id.
    event.dataTransfer?.setData(this.id, item.id);
  }

  public allowDrop(event: DragEvent, list: DualListBoxList<T>): boolean {
    if (
      event.dataTransfer?.types.length &&
      event.dataTransfer.types[0] === this.id
    ) {
      event.preventDefault();
      if (!list.dragStart) {
        list.dragOver = true;
      }
    }
    return false;
  }

  public dragLeave(): void {
    this.availableItems.dragOver = false;
    this.selectedItems.dragOver = false;
  }

  public drop(event: DragEvent, list: DualListBoxList<T>): void {
    if (
      event.dataTransfer?.types.length &&
      event.dataTransfer.types[0] === this.id
    ) {
      event.preventDefault();
      this.dragLeave();
      this.dragEnd();

      if (list === this.availableItems) {
        this.moveItem(this.availableItems, this.selectedItems);
      } else {
        this.moveItem(this.selectedItems, this.availableItems);
      }
    }
  }

  public trueUp(): void {
    let changed = false;

    // Clear removed items.
    let position = this.destination?.length ?? 0;
    while ((position -= 1) >= 0) {
      const move = this.selectedItems.sourceList.filter((selectedItem: T) => {
        if (this.destination) {
          return selectedItem.id === this.destination[position]?.id;
        }
        return false;
      });
      if (move.length === 0) {
        // Not found so remove.
        this.destination?.splice(position, 1);
        changed = true;
      }
    }

    // Push added items.
    for (
      let index = 0, sourceLength = this.selectedItems.sourceList.length;
      index < sourceLength;
      index += 1
    ) {
      let move = this.destination?.filter(
        (item: T) => item.id === this.selectedItems.sourceList[index]?.id
      );

      if (move?.length === 0) {
        // Not found so add.
        move = this.source?.filter(
          (toBeMovedItem: T) =>
            toBeMovedItem.id === this.selectedItems.sourceList[index]?.id
        );

        if (move && move.length > 0) {
          this.destination?.push(move[0]);
          changed = true;
        }
      }
    }

    if (changed) {
      this.destinationChange.emit(this.destination);
    }
  }

  public findItemIndex(list: Array<T>, item: T): number {
    let idx = -1;

    function matchObject(e: T): boolean {
      if (e.id === item.id) {
        idx = list.indexOf(e);
        return true;
      }
      return false;
    }

    // Assumption is that the arrays do not have duplicates.
    list.filter(matchObject);
    return idx;
  }

  public makeUnavailable(source: DualListBoxList<T>, item: T): void {
    const index = source.sourceList.indexOf(item);
    if (index !== -1) {
      source.sourceList.splice(index, 1);
    }
  }

  public moveItem(
    source: DualListBoxList<T>,
    target: DualListBoxList<T>,
    item: T | undefined = undefined,
    trueUp = true
  ): void {
    let index = 0;
    let sourcePickLength = source.pickList.length;

    if (item) {
      index = source.sourceList.indexOf(item);
      sourcePickLength = index + 1;
    }

    while (index < sourcePickLength) {
      // Is the pick still in list?
      let move: Array<T> = [];
      if (item) {
        const itemIndex = this.findItemIndex(source.pickList, item);
        if (itemIndex !== -1) {
          move[0] = source.pickList[itemIndex];
        }
      } else {
        move = source.sourceList.filter((filterItem: T) => {
          return filterItem.id === source.pickList[index].id;
        });
      }

      // Should only ever be 1
      if (move.length === 1) {
        // Add if not already in target.
        if (
          target.sourceList.filter((target: T) => target.id === move[0].id)
            .length === 0
        ) {
          target.sourceList.push(move[0]);
        }

        this.makeUnavailable(source, move[0]);
      }
      index++;
    }

    if (this.compare) {
      target.sourceList.sort(this.compare);
    }

    source.pickList.length = 0;

    // Update destination
    if (trueUp) {
      this.trueUp();
    }

    // Delay ever-so-slightly to prevent race condition.
    setTimeout(() => {
      this.onFilter(source);
      this.onFilter(target);
    }, 10);
  }

  public isItemSelected(list: Array<T>, item: T): boolean {
    return list.filter((e) => Object.is(e, item)).length > 0;
  }

  public shiftClick(
    event: MouseEvent,
    index: number,
    source: DualListBoxList<T>,
    item: T
  ): void {
    if (event.shiftKey && source.last?.id && item.id !== source.last.id) {
      const idx = source.selectedList.indexOf(source.last);
      if (index > idx) {
        for (let i = idx + 1; i < index; i += 1) {
          this.selectItem(source.pickList, source.selectedList[i]);
        }
      } else if (idx !== -1) {
        for (let i = index + 1; i < idx; i += 1) {
          this.selectItem(source.pickList, source.selectedList[i]);
        }
      }
    }
    source.last = item;
  }

  public selectItem(list: Array<T>, selectedItem: T): void {
    const pickedItems = list.filter((item: T) => item.id === selectedItem.id);
    if (pickedItems.length > 0) {
      // Already in list, so deselect.
      for (let i = 0, size = pickedItems.length; i < size; i++) {
        const index = list.indexOf(pickedItems[i]);
        if (index !== -1) {
          list.splice(index, 1);
        }
      }
    } else {
      list.push(selectedItem);
    }
  }

  public selectAll(source: DualListBoxList<T>): void {
    source.pickList.length = 0;
    source.pickList = source.selectedList.slice(0);
  }

  public selectNone(source: DualListBoxList<T>): void {
    source.pickList.length = 0;
  }

  public isAllSelected(source: DualListBoxList<T>): boolean {
    return (
      source.sourceList.length === 0 ||
      source.sourceList.length === source.pickList.length
    );
  }

  public isAnySelected(source: DualListBoxList<T>): boolean {
    return source.pickList.length > 0;
  }

  public clearFilter(source: DualListBoxList<T>): void {
    if (source) {
      source.picker = "";
      this.onFilter(source);
    }
  }

  public onFilter(source: DualListBoxList<T>): void {
    if (source.picker.length > 0) {
      try {
        source.selectedList = source.sourceList.filter((item: T) => {
          if (Object.prototype.toString.call(item) === "[object Object]") {
            if (item.name !== undefined) {
              return (
                item.name
                  .toLocaleLowerCase(this.format.locale)
                  .indexOf(
                    source.picker.toLocaleLowerCase(this.format.locale)
                  ) !== -1
              );
            } else {
              return (
                JSON.stringify(item)
                  .toLocaleLowerCase(this.format.locale)
                  .indexOf(
                    source.picker.toLocaleLowerCase(this.format.locale)
                  ) !== -1
              );
            }
          } else {
            return (
              item.name
                .toLocaleLowerCase(this.format.locale)
                .indexOf(
                  source.picker.toLocaleLowerCase(this.format.locale)
                ) !== -1
            );
          }
        });
        this.unpick(source);
      } catch (e) {
        if (e instanceof RangeError) {
          this.format.locale = undefined;
        }
        source.selectedList = source.sourceList;
      }
    } else {
      source.selectedList = source.sourceList;
    }
  }

  public unpick(source: DualListBoxList<T>): void {
    const startIndex = source.pickList.length - 1;
    for (let i = startIndex; i >= 0; i--) {
      if (source.selectedList.indexOf(source.pickList[i]) === -1) {
        source.pickList.splice(i, 1);
      }
    }
  }
}
