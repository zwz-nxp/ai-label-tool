// temporary (...) measurement so not the whole frontend application needs to be rewritten
export class SaveLocation {
  public id?: number;
  public acronym = "";
  public isSubContractor = false;
  public tmdbCode = "";
  public city = "";
  public country = "";
  public sapCode = "";
  public planningEngine = "";
  public status = LocationStatus["ACTIVE"];
  public extendedSuffix = "";
  public lastUpdated = new Date();
  public updatedBy = "";
  public menuGrouping = "";
  public vendorCode = "";
  public manufacturerCode = "";
  public manufacturers: Array<Manufacturer> = [];
  public equipmentCount = 0;
  public activeEventsCount = 0;
  public activeEquipmentCount = 0;
}

export class Location extends SaveLocation {
  public override id = 0;
}

export enum LocationStatus {
  ACTIVE = "ACTIVE",
  DELETED = "DELETED",
}

export class Manufacturer {
  public manufacturerCode = "";
  public locationId?: number;
  public lastUpdated = new Date();
  public updatedBy = "";
}
