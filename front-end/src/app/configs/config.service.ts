import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Config } from './config.domain';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ConfigService {

  constructor(private http: HttpClient) { }

  getConfigs(): Observable<Config[]> {
    return this.http.get<Config[]>('/api/configs');
  }

  getConfigDetails(id: string): Observable<Config> {
    return this.http.get<Config>(`/api/configs/${id}`);
  }

  saveConfig(config: Config): Observable<Config> {
    if (!config.id) {
      return this.http.post<Config>('/api/configs', config);
    } else {
      return this.http.put<Config>(`/api/configs/${config.id}`, config);
    }
  }

  deleteConfig(config: Config): Observable<any> {
    return this.http.delete<any>(`/api/configs/${config.id}`);
  }

  setDefaultConfig(config: Config): Observable<Config> {
    return this.http.put<Config>(`/api/configs/${config.id}/default`, null);
  }
}
