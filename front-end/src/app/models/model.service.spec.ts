import { TestBed } from '@angular/core/testing';

import { ModelService } from './model.service';
import { HttpTestingController, HttpClientTestingModule } from '@angular/common/http/testing';
import { ModelDefinition } from './models.domain';

describe('ModelService', () => {
  let service: ModelService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ModelService],
      imports: [HttpClientTestingModule]
    });

    service = TestBed.inject(ModelService);
    http = TestBed.get(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    expect(true).toBeTrue();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get model templates', () => {
    service.getModelTemplates().subscribe();
    http.expectOne({ method: 'GET', url: '/api/models/templates' });
  });

  it('should save new model definition', () => {
    service.saveModel({} as ModelDefinition).subscribe();
    http.expectOne({ method: 'POST', url: '/api/models' });
  });

  it('should save existing model definition', () => {
    service.saveModel({ id: '123' } as ModelDefinition).subscribe();
    http.expectOne({ method: 'PUT', url: '/api/models/123' });
  });

  it('should get model list', () => {
    service.getModels().subscribe();
    http.expectOne({ method: 'GET', url: '/api/models' });
  });

  it('should get model details', () => {
    service.getModel('123').subscribe();
    http.expectOne({ method: 'GET', url: '/api/models/123' });
  });

  it('should delete model definition', () => {
    service.deleteModel('123').subscribe();
    http.expectOne({ method: 'DELETE', url: '/api/models/123' });
  });

});
