import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SamsNode, SamsNodeUtils, Measurements } from '../../nodes/nodes.domain';

import { Chart } from 'chart.js';
import { NodeService } from 'src/app/nodes/node.service';

@Component({
  selector: 'app-last-measurements-modal',
  templateUrl: './last-measurements-modal.component.html',
  styleUrls: ['./last-measurements-modal.component.css']
})
export class LastMeasurementsModalComponent implements OnInit {

  getValueIcon = SamsNodeUtils.getValueIconClass;
  getValueUnit = SamsNodeUtils.getValueUnit;
  selectedType: string;
  showTable = false;

  chart: Chart;
  measurements: Measurements;

  @Input() node: SamsNode;

  constructor(public modal: NgbActiveModal, private service: NodeService) { }

  ngOnInit() {
    this.service.getLatestMeasurements(this.node.id)
      .subscribe(data => {
        this.measurements = data;
        const types = Object.keys(data || {}).sort();
        if (types && types[0]) {
          this.valueTypeChanged(types[0]);
        }
      });
  }

  valueTypeChanged(type: string) {
    this.selectedType = type;

    if (this.chart) {
      this.chart.clear();
      this.chart.destroy();
    }

    this.chart = new Chart('chartCanvas', {
      type: 'line',
      options: {
        legend: {
          display: false
        },
        scales: {
          xAxes: [{
            type: 'time',
            time: {
              isoWeekday: true,
              minUnit: 'minute',
              displayFormats: {
                minute: 'HH:mm',
                hour: 'MMM D HH:mm',
              },
              tooltipFormat: 'YYYY-MM-DD HH:mm'
            },
          }],
          yAxes: [{
            ticks: {
              callback: (v: number) => v.toFixed(1),
              stepSize: 0.1
            }
          }]
        }
      },
      data: {
        datasets: [
          {
            data: this.measurements[type].map(v => ({ t: new Date(v.ts), y: v.value }))
          }
        ]
      }
    });
  }

}
