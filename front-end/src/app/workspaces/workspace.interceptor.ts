import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpHandler, HttpRequest, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WorkspaceService } from './workspace.service';
import { filter, take, switchMap } from 'rxjs/operators';

@Injectable()
export class WorkspaceInterceptor implements HttpInterceptor {

  constructor(private service: WorkspaceService) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!req.url.match('^/api/')
      || req.url.match('^/api/workspaces')) {
      return next.handle(req);
    }

    return this.service.activeWorkspace$.pipe(
      filter(ws => ws != null),
      take(1),
      switchMap((ws) => {
        const wsReq = req.clone({ setHeaders: { ws: ws.id } });
        return next.handle(wsReq);
      })
    );
  }
}
