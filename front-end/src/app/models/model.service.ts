import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ModelTemplate, ModelDefinition } from './models.domain';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ModelService {

  constructor(private http: HttpClient) { }

  getModelTemplates(): Observable<ModelTemplate[]> {
    return this.http.get<ModelTemplate[]>('/api/models/templates');
  }

  saveModel(data: ModelDefinition): Observable<ModelDefinition> {
    if (!!data.id) {
      return this.http.put<ModelDefinition>(`/api/models/${data.id}`, data);
    } else {
      return this.http.post<ModelDefinition>('/api/models', data);
    }
  }

  getModels(): Observable<ModelDefinition[]> {
    return this.http.get<ModelDefinition[]>('/api/models');
  }

  getModel(id: string): Observable<ModelDefinition> {
    return this.http.get<ModelDefinition>(`/api/models/${id}`);
  }

  deleteModel(id: string): Observable<any> {
    return this.http.delete<any>(`/api/models/${id}`);
  }
}
