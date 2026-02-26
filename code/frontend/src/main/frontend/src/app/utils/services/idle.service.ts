import { EventEmitter, Injectable } from "@angular/core";

@Injectable()
export class IdleService {
  public idleChange: EventEmitter<boolean> = new EventEmitter();

  public emitIdleChangeEvent(bool: boolean): void {
    this.idleChange.emit(bool);
  }

  public getIdleEventEmitter(): EventEmitter<boolean> {
    return this.idleChange;
  }
}
