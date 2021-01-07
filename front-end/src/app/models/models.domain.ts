import { SamsNode } from '../nodes/nodes.domain';

export interface ModelTemplate {
  code: string;
  name: string;
  description: string;
  params: ModelTemplateParam[];
}

interface ModelTemplateParam {
  code: string;
  name: string;
  description: string;
  type: string;
  master: boolean;
  options: string[];
}

export interface ModelDefinition {
  id: string;
  modelCode: string;
  modelName?: string;
  params: {
    [code: string]: string;
  };
  paramsLabels: {
    label: string;
    master: boolean;
    node: SamsNode;
    value: string;
  }[];
}
