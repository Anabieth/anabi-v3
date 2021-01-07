import { async, ComponentFixture, TestBed, tick, fakeAsync } from '@angular/core/testing';

import { WorkspaceListComponent } from './workspace-list.component';
import { WorkspaceService } from '../workspace.service';
import { AuthService } from '../../auth/auth.service';
import { of } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { Workspace } from '../workspaces.domain';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

describe('WorkspaceListComponent', () => {
  const data = [
    new Workspace('First', '111'),
    new Workspace('Second', '222', true),
    new Workspace('Third', '333', true)
  ];
  let component: WorkspaceListComponent;
  let fixture: ComponentFixture<WorkspaceListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [WorkspaceListComponent],
      providers: [
        {
          provide: WorkspaceService,
          useValue: {
            getWorkspaces: () => of(data),
            saveWorkspace: () => of({}),
            deleteWorkspace: () => of({}),
            shareWorkspace: () => of({}),
            unshareWorkspace: () => of({}),
            removeWorkspaceUser: () => of({}),
          }
        },
        {
          provide: AuthService,
          useValue: {
            userProfile: {
              sub: 'johnTheTester'
            }
          }
        },
      ],
      imports: [
        RouterTestingModule,
        FormsModule,
        NgbModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkspaceListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function findModal() {
    return fixture.debugElement.nativeNode.parentNode.querySelector('ngb-modal-window');
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should edit workspace name', fakeAsync(() => {
    component.edit(data[1]);
    expect(component.currentWorkspace.name).toBe('Second');

    component.currentWorkspace.name = 'Second changed';
    fixture.detectChanges();

    const service = TestBed.get(WorkspaceService);
    spyOn(service, 'saveWorkspace').and.callThrough();

    const modal = findModal();
    const button = modal.querySelector('button.btn.btn-outline-success');
    button.click();
    tick();
    expect(service.saveWorkspace).toHaveBeenCalled();
  }));

  it('should delete workspace', () => {
    const service = TestBed.get(WorkspaceService);
    spyOn(service, 'deleteWorkspace').and.callThrough();

    component.delete(data[2]);
    expect(service.deleteWorkspace).toHaveBeenCalled();
  });

  it('should open sharing modal dialog', fakeAsync(() => {
    component.share(data[2]);
    fixture.detectChanges();

    const modal = findModal();
    const header = modal.querySelector('h5');
    expect(header.textContent).toContain('Third');

    // clean up
    modal.querySelector('button.close').click();
    tick();
  }));

  it('should create sharing link', () => {
    const service = TestBed.get(WorkspaceService);
    spyOn(service, 'shareWorkspace').and.callThrough();
    component.currentWorkspace = data[1];
    component.createLink();
    expect(service.shareWorkspace).toHaveBeenCalled();
  });

  it('should delete sharing link', () => {
    const service = TestBed.get(WorkspaceService);
    spyOn(service, 'unshareWorkspace').and.callThrough();
    component.currentWorkspace = data[1];
    component.deleteLink();
    expect(service.unshareWorkspace).toHaveBeenCalled();
  });

  it('should kick user', () => {
    const service = TestBed.get(WorkspaceService);
    spyOn(service, 'removeWorkspaceUser').and.callThrough();
    component.currentWorkspace = { id: '123' } as Workspace;
    component.kickUser('peter');
    expect(service.removeWorkspaceUser).toHaveBeenCalledWith({ id: '123' }, { userName: 'peter' });
  });

  it('should leave workspace', () => {
    const service = TestBed.get(WorkspaceService);
    spyOn(service, 'removeWorkspaceUser').and.callThrough();
    component.leave({ id: '234' } as Workspace);
    expect(service.removeWorkspaceUser).toHaveBeenCalledWith({ id: '234' }, { userName: 'johnTheTester' });
  });

  it('should get sharing link', () => {
    const link = component.getSharingLink({ id: 'id-123', sharingKey: 'key-123' } as Workspace);
    expect(link).toMatch('https?://.+/workspaces/id-123/invite/key-123$');
  });
});
