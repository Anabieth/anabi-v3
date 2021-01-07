import { TestBed, fakeAsync, tick } from '@angular/core/testing';

import { WorkspaceService } from './workspace.service';
import { HttpTestingController, HttpClientTestingModule } from '@angular/common/http/testing';
import { Workspace } from './workspaces.domain';

describe('WorkspaceService', () => {
  let http: HttpTestingController;
  let service: WorkspaceService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [WorkspaceService],
      imports: [HttpClientTestingModule]
    });

    http = TestBed.get(HttpTestingController);
    service = TestBed.get(WorkspaceService);
  });

  afterEach(() => {
    http.verify();
    expect(true).toBeTrue();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should set active workspace', () => {
    spyOn(localStorage, 'setItem');
    service.setActiveWorkspace(new Workspace('testing workspace', '123'));
    expect(localStorage.setItem).toHaveBeenCalledWith('activeWorkspace', '123');
  });

  describe('should update active workspace', () => {
    const list = [
      { isOwner: false, name: 'One', id: '111' },
      { isOwner: true, name: 'Two', id: '222' },
      { isOwner: true, name: 'Three', id: '333' }
    ];

    it('to default', fakeAsync(() => {
      spyOn(localStorage, 'getItem');
      service.updateActive();

      http.expectOne({ method: 'GET', url: '/api/workspaces' }).flush(list);
      tick();
      expect(localStorage.getItem).toHaveBeenCalledWith('activeWorkspace');
      service.activeWorkspace$.subscribe(
        ws => expect(ws.name).toBe('Two')
      );

    }));

    it('to previously selected', fakeAsync(() => {
      spyOn(localStorage, 'getItem').and.returnValue('333');
      service.updateActive();

      http.expectOne({ method: 'GET', url: '/api/workspaces' }).flush(list);
      tick();
      expect(localStorage.getItem).toHaveBeenCalledWith('activeWorkspace');
      service.activeWorkspace$.subscribe(
        ws => expect(ws.name).toBe('Three')
      );
    }));
  });

  it('should get workspaces', () => {
    service.getWorkspaces().subscribe();
    http.expectOne({ method: 'GET', url: '/api/workspaces' });
  });

  it('should save new workspace', () => {
    service.saveWorkspace(new Workspace('New')).subscribe();
    http.expectOne({ method: 'POST', url: '/api/workspaces' });
  });

  it('should save existing workspace', () => {
    service.saveWorkspace(new Workspace('Existing', '123')).subscribe();
    http.expectOne({ method: 'PUT', url: '/api/workspaces/123' });
  });

  it('should delete workspace', () => {
    service.deleteWorkspace(new Workspace('Old', '123')).subscribe();
    http.expectOne({ method: 'DELETE', url: '/api/workspaces/123' });
  });

  it('should share workspace', () => {
    service.shareWorkspace(new Workspace('my', '123')).subscribe();
    http.expectOne({ method: 'POST', url: '/api/workspaces/123/share' });
  });

  it('should unshare workspace', () => {
    service.unshareWorkspace(new Workspace('my', '123')).subscribe();
    http.expectOne({ method: 'DELETE', url: '/api/workspaces/123/share' });
  });

  it('should remove workspace user', () => {
    service.removeWorkspaceUser(new Workspace('my', '123'), 'john').subscribe();
    http.expectOne({ method: 'DELETE', url: '/api/workspaces/123/share/users' });
  });

  it('should accept workspace invite', () => {
    service.acceptWorkspaceInvite('123', 'key-123', true).subscribe();
    const body = http.expectOne({ method: 'POST', url: '/api/workspaces/123/invite' }).request.body;
    expect(body).toEqual({ key: 'key-123', confirm: true });
  });

});
