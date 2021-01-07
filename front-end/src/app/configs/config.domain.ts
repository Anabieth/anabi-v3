import { SamsNode } from '../nodes/nodes.domain';

export interface Config {
  id?: string;
  name: string;
  config?: any;
  devices?: SamsNode[];
}
