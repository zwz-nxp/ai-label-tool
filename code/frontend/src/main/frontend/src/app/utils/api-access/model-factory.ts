export class ModelFactory {
  public static create<T>(source: T, type: Constructor<T>): T {
    return ModelFactory.assign(source, new type());
  }

  public static createArray<T>(sourceArray: T[], type: Constructor<T>): T[] {
    return sourceArray.map((source) => {
      return ModelFactory.assign(source, new type());
    });
  }

  public static assign<T>(sourceObject: T, targetObject: T): T {
    for (const propertyName in sourceObject) {
      targetObject[propertyName] = sourceObject[propertyName];
    }
    return targetObject;
  }
}

type Constructor<T> = new (...args: any[]) => T;
