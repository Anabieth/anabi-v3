import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportComponent } from './report.component';
import { ReportService } from '../report.service';
import { EMPTY, of } from 'rxjs';
import { NodeService } from 'src/app/nodes/node.service';
import { RouterTestingModule } from '@angular/router/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import * as moment from 'moment-timezone';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { SamsNode } from 'src/app/nodes/nodes.domain';
import { NodeSelectComponent } from 'src/app/nodes/node-select/node-select.component';

describe('ReportComponent', () => {
  let component: ReportComponent;
  let fixture: ComponentFixture<ReportComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ReportComponent, NodeSelectComponent],
      providers: [
        {
          provide: ReportService,
          useValue: {
            getCSV: () => EMPTY,
            getDatasets: () => EMPTY,
            getReportList: () => EMPTY
          }
        },
        {
          provide: NodeService,
          useValue: {
            getNodes: () => EMPTY
          }
        },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: EMPTY
          }
        }
      ],
      imports: [RouterTestingModule, ReactiveFormsModule, NgbModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    moment.tz.setDefault('UTC');
    fixture = TestBed.createComponent(ReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should init default values', () => {
    jasmine.clock().mockDate(new Date('2020-08-07T13:40:00Z'));
    const service = TestBed.get(NodeService);
    spyOn(service, 'getNodes').and.returnValue(of([]));

    const reportService = TestBed.get(ReportService);
    spyOn(reportService, 'getReportList').and.returnValue(of([]));

    const route = TestBed.get(ActivatedRoute);
    route.queryParamMap = of(convertToParamMap({}));

    component.ngOnInit();
    expect(component.selectedNodes).toEqual([]);
    expect(component.selectedReports).toEqual([]);
    const form = component.queryForm;
    expect(form.get('fromDate').value).toEqual('2020-07-31');
    expect(form.get('fromTime').value).toBeNull();
    expect(form.get('toDate').value).toEqual('2020-08-07');
    expect(form.get('toTime').value).toBeNull();
    expect(form.get('nodeCount').value).toEqual(0);
    expect(form.get('reportCount').value).toEqual(0);
  });

  it('should init from query params', () => {
    const node1: SamsNode = { id: '1', name: 'Z' } as any;
    const node2: SamsNode = { id: '2', name: 'Y' } as any;
    const node3: SamsNode = { id: '3', name: 'X' } as any;
    const service = TestBed.get(NodeService);
    spyOn(service, 'getNodes').and.returnValue(of([node1, node2, node3]));

    const rep1 = { code: 'rep1', name: '' };
    const rep2 = { code: 'rep2', name: '' };
    const rep3 = { code: 'rep3', name: '' };
    const reportService = TestBed.get(ReportService);
    spyOn(reportService, 'getReportList').and.returnValue(of([rep1, rep2, rep3]));

    const route = TestBed.get(ActivatedRoute);
    route.queryParamMap = of(convertToParamMap({
      n: ['2', '3', '99'],
      r: ['rep1', 'rep2', 'repX'],
      fd: 'from-date',
      ft: 'from-time',
      td: 'to-date',
      tt: 'to-time'
    }));

    component.ngOnInit();
    expect(component.selectedNodes).toEqual([node2, node3]);
    expect(component.selectedReports).toEqual([rep1, rep2]);
    const form = component.queryForm;
    expect(form.get('fromDate').value).toEqual('from-date');
    expect(form.get('fromTime').value).toEqual('from-time');
    expect(form.get('toDate').value).toEqual('to-date');
    expect(form.get('toTime').value).toEqual('to-time');
    expect(form.get('nodeCount').value).toEqual(2);
    expect(form.get('reportCount').value).toEqual(2);
  });

  describe('should search in report', () => {
    beforeEach(() => {
      component['reports'] = [1, 2, 3, 4, 5].map((v) => ({ code: 'rep' + v, name: 'Report-' + v }) as any);
    });

    it('by name', () => {
      const term = of('report-2');
      component.searchReport$(term).subscribe(reps => {
        expect(reps.length).toBe(1);
      });
    });

    it('by code', () => {
      const term = of('rep3');
      component.searchReport$(term).subscribe(reps => {
        expect(reps.length).toBe(1);
      });
    });

    it('by manual event', () => {
      component.searchReport$(EMPTY).subscribe(reps => {
        expect(reps.length).toBe(5);
      });

      component.manualReportEvents$.next('');
    });
  });

  it('should select node', () => {
    const node = new SamsNode();
    component.nodeItemSelected(node);
    expect(component.selectedNodes[0]).toBe(node);
    expect(component.queryForm.get('nodeCount').value).toBe(1);
  });

  it('should unselect node', () => {
    component.selectedNodes = [new SamsNode('first'), new SamsNode('second'), new SamsNode('third')];
    component.nodeItemRemoved(1);
    expect(component.selectedNodes[0].name).toEqual('first');
    expect(component.selectedNodes[1].name).toEqual('third');
    expect(component.queryForm.get('nodeCount').value).toBe(2);
  });

  it('should select report', () => {
    const rep = { code: 'repA', name: '' };
    component.reportItemSelected({ item: rep, preventDefault: () => null });
    expect(component.selectedReports[0]).toBe(rep);
    expect(component.queryForm.get('reportCount').value).toBe(1);
  });

  it('should unselect report', () => {
    component.selectedReports = [{ code: 'repA', name: '' }, { code: 'repB', name: '' }, { code: 'repC', name: '' }];
    component.reportItemRemoved(1);
    expect(component.selectedReports[0].code).toEqual('repA');
    expect(component.selectedReports[1].code).toEqual('repC');
    expect(component.queryForm.get('reportCount').value).toBe(2);
  });

  describe('should fetch data', () => {
    let router: Router;
    let service: ReportService;

    beforeEach(() => {
      router = TestBed.get(Router);
      spyOn(router, 'navigate');

      service = TestBed.get(ReportService);

      component.queryForm.get('fromDate').setValue('2019-01-25');
      component.queryForm.get('fromTime').setValue('12:45');
      component.queryForm.get('toDate').setValue('2019-02-10');
      component.queryForm.get('toTime').setValue(null);
      component.selectedNodes = [{ id: 'n1', }, { id: 'n2', }] as any[];
      component.selectedReports = [{ code: 'repA' }, { code: 'repB' }] as any[];

    });

    afterEach(() => {
      expect(router.navigate).toHaveBeenCalledWith([],
        {
          queryParams: {
            n: ['n1', 'n2'],
            fd: '2019-01-25',
            ft: '12:45',
            td: '2019-02-10',
            tt: null,
            r: ['repA', 'repB']
          }
        });
    });

    it('as CSV', () => {
      spyOn(service, 'getCSV').and.returnValues(
        of(`Timestamp,A,B,C
        2019,11,12,13
        2020,21,22,23`)
      );

      const link = jasmine.createSpyObj<HTMLAnchorElement>(['setAttribute', 'click', 'style']);
      spyOn(document, 'createElement').and.returnValue(link);
      spyOn(document.body, 'appendChild');
      spyOn(document.body, 'removeChild');

      component.downloadClicked();

      expect(service.getCSV).toHaveBeenCalledWith(['repA', 'repB'], component.selectedNodes,
        '2019-01-25T12:45:00Z', '2019-02-10T23:59:00Z');
      expect(document.body.appendChild).toHaveBeenCalled();
      expect(document.body.removeChild).toHaveBeenCalled();
      expect(link.setAttribute).toHaveBeenCalledWith('href', jasmine.stringMatching('blob'));
      expect(link.setAttribute).toHaveBeenCalledWith('download', jasmine.stringMatching('export'));
      expect(link.click).toHaveBeenCalled();
    });

    it('as chart', () => {
      spyOn(service, 'getDatasets').and.returnValues(
        of([
          { axisType: 'typeA', axisLabel: 'Type A', label: 'A', type: 'test', data: [1, 2, 3] },
          { axisType: 'typeB', axisLabel: 'Type B', label: 'B', type: 'test', data: [4, 5, 6] }
        ])
      );
      component.requestDataClicked();
      expect(service.getDatasets).toHaveBeenCalledWith(['repA', 'repB'], component.selectedNodes,
        '2019-01-25T12:45:00Z', '2019-02-10T23:59:00Z');
      expect(component.chart).toBeTruthy();

      // hack to prevent Maximum call stack size exceeded
      component.chart.clear();
      component.chart.destroy();
    });
  });
});
