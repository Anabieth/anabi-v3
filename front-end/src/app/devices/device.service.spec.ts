import { TestBed, inject } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { DeviceService } from './device.service';

describe('DeviceService', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DeviceService],
      imports: [HttpClientTestingModule]
    });

    http = TestBed.get(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    expect(true).toBeTrue();
  });

  it('should be created', inject([DeviceService], (service: DeviceService) => {
    expect(service).toBeTruthy();
  }));

  it('should get list of devices', inject([DeviceService], (service: DeviceService) => {
    service.getDeviceList().subscribe();
    http.expectOne({ method: 'GET', url: '/api/devices' });
  }));

  it('should get devices events', inject([DeviceService], (service: DeviceService) => {
    service.getDeviceEvents('dev-123').subscribe();
    http.expectOne({ method: 'GET', url: '/api/devices/dev-123/events' });
  }));

  it('should toggle active device', inject([DeviceService], (service: DeviceService) => {
    service.toggleDeviceIsActive('dev-123').subscribe();
    http.expectOne({ method: 'PUT', url: '/api/devices/dev-123/active' });
  }));

});
