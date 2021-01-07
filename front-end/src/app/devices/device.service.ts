import { Injectable } from '@angular/core';
import { SamsDevice, SamsDeviceEvents } from './device-domain';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { SamsNode } from '../nodes/nodes.domain';

@Injectable()
export class DeviceService {

  constructor(private http: HttpClient) { }

  getDeviceList(): Observable<SamsNode[]> {
    return this.http.get<SamsNode[]>('/api/devices');
  }

  getDeviceEvents(deviceId: String): Observable<SamsDeviceEvents> {
    return this.http.get<SamsDeviceEvents>(`/api/devices/${deviceId}/events`);
  }

  toggleDeviceIsActive(deviceId: string): Observable<boolean> {
    return this.http.put<boolean>(`/api/devices/${deviceId}/active`, null);
  }
}
