import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { Workspace } from './workspaces.domain';

@Injectable()
export class WorkspaceService {
  constructor(private http: HttpClient) { }

  private _activeWorkspace$: BehaviorSubject<Workspace> = new BehaviorSubject<Workspace>(null);
  public activeWorkspace$ = this._activeWorkspace$.asObservable();

  setActiveWorkspace(workspace: Workspace) {
    localStorage.setItem('activeWorkspace', workspace.id);
    this._activeWorkspace$.next(workspace);
  }

  updateActive(): any {
    this.getWorkspaces().subscribe(
      list => {
        const activeId = localStorage.getItem('activeWorkspace');
        this.setActiveWorkspace(list.find(ws => ws.id === activeId)
          || list.find(ws => ws.isOwner === true)
        );
      }
    );
  }

  getWorkspaces(): Observable<Workspace[]> {
    return this.http.get<Workspace[]>('/api/workspaces');
  }

  saveWorkspace(ws: Workspace): Observable<Workspace> {
    if (!ws.id) {
      return this.http.post<Workspace>('/api/workspaces', ws);
    } else {
      return this.http.put<Workspace>(`/api/workspaces/${ws.id}`, ws);
    }
  }

  deleteWorkspace(ws: Workspace): Observable<any> {
    return this.http.delete(`/api/workspaces/${ws.id}`);
  }

  shareWorkspace(ws: Workspace): Observable<Workspace> {
    return this.http.post<Workspace>(`/api/workspaces/${ws.id}/share`, {});
  }

  unshareWorkspace(ws: Workspace): Observable<Workspace> {
    return this.http.delete<Workspace>(`/api/workspaces/${ws.id}/share`);
  }

  removeWorkspaceUser(ws: Workspace, payload: any): Observable<Workspace> {
    return this.http.request<any>('delete', `/api/workspaces/${ws.id}/share/users`, { body: payload });
  }

  acceptWorkspaceInvite(id: string, key: string, confirm: boolean = false): Observable<any> {
    return this.http.post<any>(`/api/workspaces/${id}/invite`, { key: key, confirm: confirm });
  }
}
