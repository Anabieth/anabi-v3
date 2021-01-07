import { SamsNode } from "../nodes/nodes.domain";

export class SamsDevice extends SamsNode {
  public lastEvent: SamsDeviceEvent;
  public lastError: SamsDeviceEvent;

  constructor() {
    super();
  }
}

export class SamsDeviceEvent {
  public ts: Date;
  public sourceId: string;
  public result: string;
}


export class SamsDeviceEvents {
  public name: string;
  public events: SamsDeviceEvent;
  public errors: SamsDeviceEvent;
}
