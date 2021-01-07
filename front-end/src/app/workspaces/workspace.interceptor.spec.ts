import { TestBed, tick, fakeAsync } from '@angular/core/testing';
import { WorkspaceInterceptor } from './workspace.interceptor';
import { WorkspaceService } from './workspace.service';
import { HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { BehaviorSubject, of } from 'rxjs';
import { Workspace } from './workspaces.domain';

describe('WorkspaceInterceptor', () => {
  let interceptor: WorkspaceInterceptor;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [WorkspaceInterceptor,
        {
          provide: WorkspaceService,
          useValue: {
            activeWorkspace$: new BehaviorSubject(null)
          }
        }
      ],
    });
    interceptor = TestBed.get(WorkspaceInterceptor);
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  it('should bypass non-api and workspace requests', () => {
    const next = { handle: () => null } as HttpHandler;
    spyOn(next, 'handle');

    ['/other', '/api/workspaces'].forEach(url => {
      const req = new HttpRequest<any>('GET', url);
      interceptor.intercept(req, next);
      expect(next.handle).toHaveBeenCalledWith(req);
    });
  });

  it('should handle api requests', fakeAsync(() => {
    const service = TestBed.get(WorkspaceService);
    const next = { handle: () => null } as HttpHandler;
    const req = new HttpRequest<any>('GET', '/api/any/url');

    spyOn(next, 'handle').and.callFake(r => {
      expect(r.url).toBe('/api/any/url');
      expect(r.headers.get('ws')).toBe('123');
      return of({} as HttpEvent<any>);
    });

    interceptor.intercept(req, next).subscribe();

    expect(next.handle).toHaveBeenCalledTimes(0);
    service.activeWorkspace$.next(new Workspace('Active WS', '123'));

    tick();
    expect(next.handle).toHaveBeenCalledTimes(1);
  }));
});
