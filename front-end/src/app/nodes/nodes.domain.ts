export class SamsNode {
  location: string;
  clientId: string;
  isActive: boolean;
  hwConfigId: string;
  children: SamsNode[] = [];
  latestMeasurements: LatestMeasurement[];
  latestModelValues: LatestModelValue[];
  updateTs: Date | string;
  updateInProgress = false;
  updatedRecenlty: boolean;
  notCollapsed?: boolean;

  constructor(
    public name?: string,
    public type?: SamsNodeType,
    public id?: string,
    public parentId?: string) { }
}

export interface Measurements {
  [type: string]: {
    ts: Date;
    value: number;
  }[];
}

export interface LatestValues {
  id: string;
  latestMeasurements: LatestMeasurement[];
  latestModelValues: LatestModelValue[];
}

export interface LatestMeasurement {
  type: string;
  timestamp: string;
  value: number;
  isRecent: boolean;
}

export interface LatestModelValue {
  modelCode: string;
  timestamp: string;
  label: string;
  description: string;
  rawValue: any;
  isRecent: boolean;
}

export class SamsNodeDetails extends SamsNode {
  ancestors: SamsNode[];
}

export class SourceMapping {
  sourceId: string;
  valueKey: string;
}

export enum SamsNodeType {
  Group = 'GROUP',
  Apiary = 'APIARY',
  Hive = 'HIVE',
  HiveElement = 'HIVE_ELEMENT',
  Device = 'DEVICE',
  Other = 'OTHER'
}

export class SamsNodeUtils {

  static getValueUnit(valueType): string {
    switch (valueType) {
      case 'temperature':
        return '\xB0C';
      case 'humidity':
        return '%';
      case 'voltage':
        return 'mV';
      case 'weight':
        return 'kg';
      case 'audio':
        return 'db';
      default:
        return '';
    }
  }

  static getValueIconClass(itemKey: string): string {
    switch (itemKey) {
      case 'temperature':
        return 'fa-thermometer-half';
      case 'humidity':
        return 'fa-water';
      case 'weight':
        return 'fas fa-balance-scale';
      case 'voltage':
        return 'fas fa-bolt';
      default:
        return '';
    }
  }

  static getTypeIconClass(type: SamsNodeType): string {
    switch (type) {
      case SamsNodeType.Apiary:
        return 'fa-sitemap';
      case SamsNodeType.Hive:
        return 'fa-archive';
      case SamsNodeType.Group:
        return 'fa-object-group';
      case SamsNodeType.HiveElement:
        return 'fa-vector-square';
      case SamsNodeType.Device:
        return 'fa-microchip';
      default:
        return 'fa-tag';
    }
  }
}
