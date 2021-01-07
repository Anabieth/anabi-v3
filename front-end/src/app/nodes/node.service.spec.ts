import { TestBed, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { NodeService } from './node.service';
import { SamsNode } from './nodes.domain';

describe('NodeService', () => {

  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [NodeService],
      imports: [HttpClientTestingModule]
    });

    http = TestBed.get(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    expect(true).toBeTrue();
  });

  it('should be created', inject([NodeService], (service: NodeService) => {
    expect(service).toBeTruthy();
  }));

  it('should get list of nodes', inject([NodeService], (service: NodeService) => {
    service.getNodes().subscribe();
    http.expectOne({ method: 'GET', url: '/api/nodes' });
  }));

  it('should save new node', inject([NodeService], (service: NodeService) => {
    const node = new SamsNode('new node', null, null, 'parent123');
    service.saveNode(node).subscribe();
    const req = http.expectOne({ method: 'POST', url: '/api/nodes' });
    expect(req.request.body).toEqual(node);
  }));

  it('should save existing node', inject([NodeService], (service: NodeService) => {
    const node = new SamsNode('existing node', null, 'id987', 'parent123');
    service.saveNode(node).subscribe();
    const req = http.expectOne({ method: 'PUT', url: '/api/nodes/id987' });
    expect(req.request.body).toEqual(node);
  }));

  it('should delete node', inject([NodeService], (service: NodeService) => {
    const node = new SamsNode('old node', null, 'id123', null);
    service.deleteNode(node).subscribe();
    http.expectOne({ method: 'DELETE', url: '/api/nodes/id123' });
  }));

  it('should get node details', inject([NodeService], (service: NodeService) => {
    service.getNodeDetails('node-123').subscribe();
    http.expectOne({ method: 'GET', url: '/api/nodes/node-123' });
  }));

  it('should get node mappings', inject([NodeService], (service: NodeService) => {
    service.getNodeMappings('node-123').subscribe();
    http.expectOne({ method: 'GET', url: '/api/nodes/node-123/mapping' });
  }));

  it('should get node mappings', inject([NodeService], (service: NodeService) => {
    service.saveNodeMappings('node-123', []).subscribe();
    http.expectOne({ method: 'PUT', url: '/api/nodes/node-123/mapping' });
  }));

  it('should check source id usage', inject([NodeService], (service: NodeService) => {
    service.isSourceIdUsed('node-123', 'src-111').subscribe();
    http.expectOne({ method: 'POST', url: '/api/nodes/node-123/mapping/isUsed' });
  }));

  it('should get mapping value keys', inject([NodeService], (service: NodeService) => {
    service.getMappingValueKeys('node-123').subscribe();
    http.expectOne({ method: 'GET', url: '/api/nodes/node-123/mapping/valueKeys' });
  }));

  it('should get latest values', inject([NodeService], (service: NodeService) => {
    service.getLatestValues('node-123').subscribe();
    http.expectOne({ method: 'GET', url: '/api/nodes/node-123/latestValues' });
  }));

  it('should get latest measurements', inject([NodeService], (service: NodeService) => {
    service.getLatestMeasurements('node-123').subscribe();
    http.expectOne({ method: 'GET', url: '/api/nodes/node-123/latestMeasurements' });
  }));
});
