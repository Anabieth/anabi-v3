import { NgbDateAdapter, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { Injectable } from '@angular/core';

export interface Report {
  code: string;
  name: string;
}

export interface ReportData extends Report {
  data: LabeledValues<any>[];
}

export interface LabeledValues<D> {
  name: string;
  type: string;
  categories: string[];
  values: D[];
}

@Injectable()
export class SimpleDateAdapter extends NgbDateAdapter<string> {
  private leftPad(n: number): string {
    return n < 10 ? `0${n}` : `${n}`;
  }

  fromModel(str: string): NgbDateStruct {
    if (str) {
      const elements = str.split('-');
      return {
        year: parseInt(elements[0], 10),
        month: parseInt(elements[1], 10),
        day: parseInt(elements[2], 10)
      };
    } else {
      return null;
    }
  }

  toModel(data) {
    if (data) {
      return `${data.year}-${this.leftPad(data.month)}-${this.leftPad(data.day)}`;
    } else {
      return null;
    }
  }
}

export class ColorProvider {
  private colors = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728',
    '#9467bd', '#8c564b', '#e377c2', '#7f7f7f',
    '#bcbd22', '#17becf'];
  private nextIndex = 0;

  next(): string {
    if (this.nextIndex >= this.colors.length) {
      this.nextIndex = 0;
    }
    return this.colors[this.nextIndex++];
  }
}
