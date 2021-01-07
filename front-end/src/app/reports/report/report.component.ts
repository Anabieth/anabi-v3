import { Component, OnInit } from '@angular/core';
import { NgbDateAdapter, NgbTypeaheadSelectItemEvent } from '@ng-bootstrap/ng-bootstrap';
import { SimpleDateAdapter, Report, ColorProvider } from '../report-domain';
import { ReportService } from '../report.service';
import { NodeService } from 'src/app/nodes/node.service';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';
import { SamsNode, SamsNodeUtils } from 'src/app/nodes/nodes.domain';
import * as moment from 'moment';
import { combineLatest, Observable, merge, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, map } from 'rxjs/operators';
import * as Chart from 'chart.js';

@Component({
  selector: 'app-report',
  templateUrl: './report.component.html',
  styleUrls: ['./report.component.css'],
  providers: [
    {
      provide: NgbDateAdapter,
      useClass: SimpleDateAdapter
    }
  ]
})
export class ReportComponent implements OnInit {

  constructor(private service: ReportService, private nodeServce: NodeService,
    private router: Router, private route: ActivatedRoute,
    private fb: FormBuilder
  ) { }

  nodes: SamsNode[] = [];
  selectedNodes: SamsNode[] = [];
  reports: Report[] = [];
  selectedReports: Report[] = [];

  manualReportEvents$ = new Subject<string>();

  queryForm = this.fb.group(
    {
      fromDate: [null, Validators.required],
      fromTime: [null],
      toDate: [null, Validators.required],
      toTime: [null],
      nodeCount: [0, Validators.min(1)],
      reportCount: [0, Validators.min(1)]
    }
  );

  chart: Chart;

  ngOnInit(): void {
    combineLatest([
      this.nodeServce.getNodes(),
      this.route.queryParamMap,
      this.service.getReportList()
    ]).subscribe(([nodes, query, reports]) => {
      this.nodes = nodes;
      this.reports = reports.sort((a, b) => a.name.localeCompare(b.name));

      const ids = query.getAll('n');
      this.selectedNodes = this.nodes.filter(n => ids.some(id => id === n.id));
      this.updateNodeCount();

      if (query.get('ra')) {
        this.selectedReports = this.reports;
      } else {
        const codes = query.getAll('r');
        this.selectedReports = this.reports.filter(r => codes.some(c => c === r.code));
      }
      this.updateReportCount();

      this.queryForm.get('fromDate').setValue(query.get('fd') || moment().subtract(7, 'days').format('YYYY-MM-DD'));
      this.queryForm.get('fromTime').setValue(query.get('ft') || null);
      this.queryForm.get('toDate').setValue(query.get('td') || moment().format('YYYY-MM-DD'));
      this.queryForm.get('toTime').setValue(query.get('tt') || null);
    });
  }

  searchReport$ = (text$: Observable<string>) => {
    const stringContains = (value: string, term: string): boolean => {
      return value && value.toLowerCase().indexOf(term) > -1;
    };
    const debouncedText$ = text$.pipe(debounceTime(200), distinctUntilChanged());
    return merge(debouncedText$, this.manualReportEvents$)
      .pipe(
        map(term => term.toLowerCase()),
        map(term => {
          return this.reports.filter(rep => stringContains(rep.name, term) || stringContains(rep.code, term));
        })
      );
  }

  private updateNodeCount() {
    this.queryForm.get('nodeCount').setValue(this.selectedNodes.length);
  }

  private updateReportCount() {
    this.queryForm.get('reportCount').setValue(this.selectedReports.length);
  }

  nodeItemSelected(item: SamsNode) {
    if (this.selectedNodes.some(n => n.id === item.id)) {
      return;
    }
    this.selectedNodes.push(item);
    this.updateNodeCount();
  }

  nodeItemRemoved(idx: number) {
    this.selectedNodes.splice(idx, 1);
    this.updateNodeCount();
  }

  reportItemSelected(event: NgbTypeaheadSelectItemEvent): void {
    event.preventDefault();
    if (this.selectedReports.some(r => r.code === event.item.code)) {
      return;
    }
    this.selectedReports.push(event.item);
    this.updateReportCount();
  }

  reportItemRemoved(idx: number): void {
    this.selectedReports.splice(idx, 1);
    this.updateReportCount();
  }

  private getTimestampRange() {
    const fromDate = this.queryForm.get('fromDate').value;
    const fromTime = this.queryForm.get('fromTime').value || '00:00';
    const toDate = this.queryForm.get('toDate').value;
    const toTime = this.queryForm.get('toTime').value || '23:59';

    const from = moment(`${fromDate}T${fromTime}`).utc().format();
    const to = moment(`${toDate}T${toTime}`).utc().format();
    return { from, to };
  }

  private updateUrlQueryParams(): any {
    const params = {
      n: this.selectedNodes.map(n => n.id),
      fd: this.queryForm.get('fromDate').value,
      ft: this.queryForm.get('fromTime').value,
      td: this.queryForm.get('toDate').value,
      tt: this.queryForm.get('toTime').value,
      r: this.selectedReports.map(r => r.code)
    };
    this.router.navigate([], { queryParams: params });
    this.queryForm.markAsPristine();
  }

  resetClicked() {
    this.selectedNodes = [];
    this.selectedReports = [];
    this.queryForm.reset();
  }

  requestDataClicked() {
    this.updateUrlQueryParams();

    if (this.chart) {
      this.chart.clear();
      this.chart.destroy();
    }

    const colors = new ColorProvider();
    const { from, to } = this.getTimestampRange();

    this.service.getDatasets(this.selectedReports.map(r => r.code), this.selectedNodes, from, to)
      .subscribe(data => {
        const datasets = [];
        const axes = [];

        data.forEach(ds => {
          const color = colors.next();
          const isCategory = ds.type === 'category';

          datasets.push({
            yAxisID: ds.axisType,
            label: ds.label,
            data: ds.data,
            hidden: ds.hasCategory && !isCategory,
            steppedLine: isCategory,
            fill: false,
            pointStyle: isCategory ? 'rect' : 'circle',
            borderColor: color + (!isCategory && ds.hasCategory ? '33' : 'a0'),
            backgroundColor: color + (ds.hasCategory ? '33' : '70')
          });

          if (axes.some(ax => ax.id === ds.type)) {
            return;
          }

          let axisLabel = ds.axisLabel.charAt(0).toUpperCase() + ds.axisLabel.slice(1);
          const units = SamsNodeUtils.getValueUnit(ds.axisType);
          if (units) {
            axisLabel = axisLabel + ', ' + units;
          }

          axes.push({
            id: ds.axisType,
            position: isCategory ? 'right' : 'left',
            type: isCategory ? 'category' : undefined,
            labels: ds.categories,
            scaleLabel: {
              display: true,
              labelString: axisLabel
            },
            gridLines: {
              display: !isCategory
            },
            offset: isCategory
          });
        });

        const options: Chart.ChartConfiguration = {
          type: 'line',
          options: {
            aspectRatio: 2.2, // fit FullHD display
            elements: {
              line: {
                tension: 0
              }
            },
            tooltips: {
              mode: 'index',
              intersect: true
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
                scaleLabel: {
                  display: true,
                  labelString: 'Timestamp'
                }
              }],
              yAxes: axes
            },
          },
          data: {
            datasets
          }
        };

        this.chart = new Chart('chartCanvas', options);
      });
  }

  downloadClicked() {
    const { from, to } = this.getTimestampRange();

    this.updateUrlQueryParams();
    this.service.getCSV(this.selectedReports.map(r => r.code), this.selectedNodes, from, to)
      .subscribe(csv => {
        const blob = new Blob([csv], { type: 'text/csv' });
        const lnk = document.createElement('a');
        const url = URL.createObjectURL(blob);
        lnk.setAttribute('href', url);
        lnk.setAttribute('download', 'export_' + moment().format('YYYY-MM-DDTHH:mm') + '.csv');
        lnk.style.visibility = 'hidden';
        document.body.appendChild(lnk);
        lnk.click();
        document.body.removeChild(lnk);
      });
  }

}
