import { TestBed } from '@angular/core/testing';
import { ReportService } from './report.service';
import { HttpTestingController, HttpClientTestingModule } from '@angular/common/http/testing';
import { SamsNode, SamsNodeType } from '../nodes/nodes.domain';

describe('ReportService', () => {
  let http: HttpTestingController;
  let service: ReportService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ReportService],
      imports: [HttpClientTestingModule]
    });

    http = TestBed.get(HttpTestingController);
    service = TestBed.get(ReportService);
  });

  afterEach(() => {
    http.verify();
    expect(true).toBeTrue();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get report list', () => {
    service.getReportList().subscribe();
    http.expectOne({ method: 'GET', url: '/api/reports' });
  });

  it('should get report details', () => {
    service.getReportDetails('testing-report').subscribe();
    http.expectOne({ method: 'GET', url: '/api/reports/testing-report' });
  });

  it('should get report data', () => {
    service.getReportData('testing-report', 'test-node', '2019-01-01', '2019-02-01').subscribe();
    http.expectOne({ method: 'GET', url: '/api/reports/testing-report/test-node?from=2019-01-01&to=2019-02-01&limit=500' });
  });

  it('should get report data for export', () => {
    service.getReportData('testing-report', 'test-node', '2019-01-01', '2019-02-01', null).subscribe();
    http.expectOne({ method: 'GET', url: '/api/reports/testing-report/test-node?from=2019-01-01&to=2019-02-01' });
  });

  it('should get CSV', () => {
    service.getCSV(
      ['repA', 'repB'],
      ['hive1', 'hive2', 'hive3'].map(id => new SamsNode(id, SamsNodeType.Hive, id)),
      '2020-08-01', '2020-08-07'
    ).subscribe((res: string) => {

      const lines = res.split('\r\n');

      expect(lines[0]).toMatch(/Timestamp.+hive1 rep-simple.+hive2 rep-model state.+hive2 rep-model temp.+hive2 rep-model hum/);
      expect(lines[1]).toMatch('2017,20,,,');
      expect(lines[2]).toMatch('2018,21,a,t1,h1');
      expect(lines[3]).toMatch('2019,22,b,t2,h2');
      expect(lines[4]).toMatch('2020,,c,t3,h3');
    });

    // empty reports
    http.expectOne('/api/reports/repA/hive1?from=2020-08-01&to=2020-08-07').flush({});
    http.expectOne('/api/reports/repA/hive2?from=2020-08-01&to=2020-08-07').flush({ data: [] });
    http.expectOne('/api/reports/repA/hive3?from=2020-08-01&to=2020-08-07').flush({ data: [{}] });
    // simple report
    http.expectOne('/api/reports/repB/hive1?from=2020-08-01&to=2020-08-07')
      .flush({
        code: 'rep-simple',
        data: [
          { type: 'timestamp', values: ['2017', '2018', '2019'] },
          { type: 'temperature', values: ['20', '21', '22'] },
        ]
      });
    // model report
    http.expectOne('/api/reports/repB/hive2?from=2020-08-01&to=2020-08-07')
      .flush({
        code: 'rep-model',
        data: [
          { type: 'timestamp', values: ['2018', '2019', '2020'] },
          { type: 'category', name: 'state', values: ['a', 'b', 'c'] },
          { type: 'temperature', name: 'temp', values: ['t1', 't2', 't3'] },
          { type: 'humidity', name: 'hum', values: ['h1', 'h2', 'h3'] },
        ]
      });
    // report without timestamp
    http.expectOne('/api/reports/repB/hive3?from=2020-08-01&to=2020-08-07')
      .flush({
        data: [
          { type: 'temperature', values: ['20', '21', '22'] },
        ]
      });
  });

  it('should get datasets', () => {
    service.getDatasets(
      ['repA', 'repB'],
      ['hive1', 'hive2', 'hive3'].map(id => new SamsNode(id, SamsNodeType.Hive, id)),
      '2020-08-01', '2020-08-07'
    ).subscribe(datasets => {

      expect(datasets[0]).toEqual(jasmine.objectContaining(
        { axisType: 'temperature', type: 'temperature', label: 'hive1 - rep-simple', hasCategory: false }));
      expect(datasets[1]).toEqual(jasmine.objectContaining(
        { axisType: 'rep-model-state', type: 'category', label: 'hive2 - rep-model (state)', hasCategory: true, categories: ['x', 'y'] }));
      expect(datasets[2]).toEqual(jasmine.objectContaining(
        { axisType: 'temperature', type: 'temperature', label: 'hive2 - rep-model (temp)', hasCategory: true }));
      expect(datasets[3]).toEqual(jasmine.objectContaining(
        { axisType: 'humidity', type: 'humidity', label: 'hive2 - rep-model (hum)', hasCategory: true }));
    });

    // empty reports
    http.expectOne('/api/reports/repA/hive1?from=2020-08-01&to=2020-08-07&limit=500').flush({});
    http.expectOne('/api/reports/repA/hive2?from=2020-08-01&to=2020-08-07&limit=500').flush({ data: [] });
    http.expectOne('/api/reports/repA/hive3?from=2020-08-01&to=2020-08-07&limit=500').flush({ data: [{}] });
    // simple report
    http.expectOne('/api/reports/repB/hive1?from=2020-08-01&to=2020-08-07&limit=500')
      .flush({
        code: 'rep-simple',
        data: [
          { type: 'timestamp', values: ['2017', '2018', '2019'] },
          { type: 'temperature', values: ['20', '21', '22'] },
        ]
      });
    // model report
    http.expectOne('/api/reports/repB/hive2?from=2020-08-01&to=2020-08-07&limit=500')
      .flush({
        code: 'rep-model',
        data: [
          { type: 'timestamp', values: ['2018', '2019', '2020'] },
          { type: 'category', name: 'state', categories: ['x', 'y'], values: ['a', 'b', 'c'] },
          { type: 'temperature', name: 'temp', values: ['t1', 't2', 't3'] },
          { type: 'humidity', name: 'hum', values: ['h1', 'h2', 'h3'] },
        ]
      });
    // report without timestamp
    http.expectOne('/api/reports/repB/hive3?from=2020-08-01&to=2020-08-07&limit=500')
      .flush({
        data: [
          { type: 'temperature', values: ['20', '21', '22'] },
        ]
      });
  });

});
