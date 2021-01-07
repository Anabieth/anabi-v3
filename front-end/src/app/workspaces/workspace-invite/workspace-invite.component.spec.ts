import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkspaceInviteComponent } from './workspace-invite.component';
import { WorkspaceService } from '../workspace.service';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { empty, of, throwError } from 'rxjs';
import { Workspace } from '../workspaces.domain';

describe('WorkspaceInviteComponent', () => {
  let component: WorkspaceInviteComponent;
  let fixture: ComponentFixture<WorkspaceInviteComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [WorkspaceInviteComponent],
      providers: [
        {
          provide: WorkspaceService,
          useValue: {
            acceptWorkspaceInvite: () => empty()
          }
        }
      ],
      imports: [
        RouterTestingModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkspaceInviteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('on init', () => {
    it('should fetch valid invite details', () => {
      const service = TestBed.get(WorkspaceService);
      service.acceptWorkspaceInvite = () => of({ status: 'Valid', workspace: new Workspace('my', '123') });
      component.ngOnInit();

      expect(component.status).toBe('Valid');
      expect(component.workspace.id).toBe('123');
    });

    it('should handle invalid invite', () => {
      const service = TestBed.get(WorkspaceService);
      service.acceptWorkspaceInvite = () => throwError({ error: { status: 'NotValid' } });
      component.ngOnInit();

      expect(component.status).toBe('NotValid');
      expect(component.workspace).toBeUndefined();
    });
  });

  it('should confirm invite', () => {
    const service = TestBed.get(WorkspaceService);
    spyOn(service, 'acceptWorkspaceInvite').and.returnValue(of({ status: 'Joined' }));

    const router = TestBed.get(Router);
    spyOn(router, 'navigate');

    component.workspace = new Workspace('my', '123');
    component.key = 'key-123';

    component.confirm();

    expect(component.status).toBe('Joined');
    expect(service.acceptWorkspaceInvite).toHaveBeenCalledWith('123', 'key-123', true);
    expect(router.navigate).toHaveBeenCalledWith(['/workspaces']);
  });

  describe('should render message for ', () => {
    let native;
    beforeEach(() => {
      native = fixture.nativeElement;
    });

    [
      { status: 'Valid', message: 'about to join' },
      { status: 'Joined', message: 'joined' },
      { status: 'AlreadyJoined', message: 'already joined' },
      { status: 'NotValid', message: 'not valid' },
      { status: 'OwnWorkspace', message: 'own workspace' },
    ].forEach(tc =>
      it(`${tc.status} status`, () => {
        component.status = tc.status;
        fixture.detectChanges();

        const message = native.querySelector('p').textContent;
        expect(message).toContain(tc.message);
      })
    );
  });
});
