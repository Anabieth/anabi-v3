import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Report, ReportData } from './report-domain';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import { SamsNode } from '../nodes/nodes.domain';

@Injectable()
export class ReportService {

  constructor(private http: HttpClient) { }

  getReportList(): Observable<Report[]> {
    return this.http.get<Report[]>('/api/reports');
  }

  getReportDetails(code: string): Observable<Report> {
    return this.http.get<Report>(`/api/reports/${code}`);
  }

  getReportData(code: string, nodeId: string, from: string, to: string, limit: number = 500): Observable<ReportData> {
    let params = new HttpParams()
      .set('from', from)
      .set('to', to);
    if (limit) {
      params = params.set('limit', limit.toString());
    }
    return this.http.get<ReportData>(`/api/reports/${code}/${nodeId}`, { params });
  }

  getCSV(codes: string[], nodes: SamsNode[], from: string, to: string): Observable<any> {
    const pairs = codes.flatMap(code => nodes.map(node => ({ code, node })));

    return forkJoin(
      pairs.map(pair => this.getReportData(pair.code, pair.node.id, from, to, null))
    ).pipe(
      map(reports => {
        const data = new Map<string, any>();
        const headers = [];
        reports.forEach((report, reportIdx) => {

          const hasResults = report.data && report.data[0] && report.data[0].values && report.data[0].values.length > 0;
          if (!hasResults) {
            return; // empty report
          }

          const ts = report.data.find(d => d.type === 'timestamp');
          if (!ts) {
            return;
          }

          const multipleValues = report.data.length > 2;

          report.data.filter(d => d.type !== 'timestamp')
            .forEach(val => {
              let key = `${pairs[reportIdx].node.name} ${report.code}`;
              if (multipleValues) {
                key = key + ` ${val.name}`;
              }
              headers.push(key);

              ts.values.forEach((t, i) => {
                if (!data.has(t)) {
                  data.set(t, {});
                }
                data.get(t)[key] = val.values[i];
              });

            });
        });

        return { data, headers };
      }),
      map(({ data, headers }) => {
        const table = Array.from(data.entries())
          .sort()
          .map(v => ([v[0], ...headers.map(k => v[1][k])]));

        const csv = [
          ['Timestamp', ...headers.map(h => `"${h}"`)],
          ...table
        ].map(row => row.join(','))
          .join('\r\n');
        return csv;
      })
    );
  }

  getDatasets(codes: string[], nodes: SamsNode[], from: string, to: string): Observable<any> {
    const pairs = codes.flatMap(code => nodes.map(node => ({ code, node })));

    return forkJoin(
      pairs.map(pair => this.getReportData(pair.code, pair.node.id, from, to))
    ).pipe(
      map(reports => {
        const datasets = [];

        reports.forEach((report, reportIdx) => {
          const hasResults = report.data && report.data[0] && report.data[0].values && report.data[0].values.length > 0;
          if (!hasResults) {
            return; // empty report
          }

          const ts = report.data.find(d => d.type === 'timestamp');
          if (!ts) {
            return;
          }

          const multipleValues = report.data.length > 2;
          const hasCategory = report.data.some(d => d.type === 'category');

          report.data.filter(d => d.type !== 'timestamp')
            .forEach(d => {
              let label = `${pairs[reportIdx].node.name} - ${report.code}`;
              if (multipleValues) {
                label = `${label} (${d.name})`;
              }

              datasets.push({
                label,
                data: d.values.map((v, i) => ({ t: new Date(ts.values[i]), y: v })),
                type: d.type,
                axisType: d.type === 'category' ? report.code+'-'+d.name : d.type,
                axisLabel: d.type === 'category' ? report.name : d.type,
                hasCategory,
                categories: d.categories
              });
            });

        });

        return datasets;
      })
    );
  }
}
