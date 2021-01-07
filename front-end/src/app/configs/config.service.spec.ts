import { TestBed } from '@angular/core/testing';

import { ConfigService } from './config.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('ConfigService', () => {
  let http: HttpTestingController;
  let service: ConfigService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ConfigService],
      imports: [HttpClientTestingModule]
    });

    http = TestBed.get(HttpTestingController);
    service = TestBed.get(ConfigService);
  });

  afterEach(() => {
    http.verify();
    expect(true).toBeTrue();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get configs', () => {
    service.getConfigs().subscribe();
    http.expectOne({ method: 'GET', url: '/api/configs' });
  });

  it('should get config details', () => {
    service.getConfigDetails('123').subscribe();
    http.expectOne({ method: 'GET', url: '/api/configs/123' });
  });

  it('should save new config', () => {
    service.saveConfig({ name: 'testing' }).subscribe();
    http.expectOne({ method: 'POST', url: '/api/configs' });
  });

  it('should save existing config', () => {
    service.saveConfig({ id: '123', name: 'testing' }).subscribe();
    http.expectOne({ method: 'PUT', url: '/api/configs/123' });
  });

  it('should delete config', () => {
    service.deleteConfig({ id: '123', name: 'test' }).subscribe();
    http.expectOne({ method: 'DELETE', url: '/api/configs/123' });
  });

  it('should set default config', () => {
    service.setDefaultConfig({ id: '123', name: 'test' }).subscribe();
    http.expectOne({ method: 'PUT', url: '/api/configs/123/default' });
  });

});
