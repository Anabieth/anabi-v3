import { Injectable } from '@angular/core';
import { SamsNode, SamsNodeDetails, SourceMapping, LatestValues, Measurements } from './nodes.domain';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class NodeService {

  constructor(private http: HttpClient) { }

  getNodes(): Observable<SamsNode[]> {
    return this.http.get<SamsNode[]>('/api/nodes');
  }

  getNodeDetails(id: string): Observable<SamsNodeDetails> {
    return this.http.get<SamsNodeDetails>(`/api/nodes/${id}`);
  }

  getNodeMappings(id: string): Observable<SourceMapping[]> {
    return this.http.get<SourceMapping[]>(`/api/nodes/${id}/mapping`);
  }

  getMappingValueKeys(id: string): Observable<string[]> {
    return this.http.get<string[]>(`/api/nodes/${id}/mapping/valueKeys`);
  }

  saveNodeMappings(id: string, mappings: SourceMapping[]): Observable<SourceMapping[]> {
    return this.http.put<SourceMapping[]>(`/api/nodes/${id}/mapping`, mappings);
  }

  isSourceIdUsed(nodeId: string, sourceId: string): Observable<any> {
    return this.http.post(`/api/nodes/${nodeId}/mapping/isUsed`, sourceId);
  }

  saveNode(node: SamsNode): Observable<SamsNode> {
    if (!node.id) {
      return this.http.post<SamsNode>('/api/nodes', node);
    } else {
      return this.http.put<SamsNode>(`/api/nodes/${node.id}`, node);
    }
  }

  deleteNode(node: SamsNode): Observable<any> {
    return this.http.delete<any>(`/api/nodes/${node.id}`);
  }

  getLatestValues(id: string): Observable<LatestValues> {
    return this.http.get<LatestValues>(`/api/nodes/${id}/latestValues`);
  }

  getLatestMeasurements(id: string): Observable<Measurements> {
    return this.http.get<Measurements>(`/api/nodes/${id}/latestMeasurements`);
  }
}
