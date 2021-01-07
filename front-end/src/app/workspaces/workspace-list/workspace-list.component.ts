import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { WorkspaceService } from '../workspace.service';
import { Workspace } from '../workspaces.domain';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../../auth/auth.service';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-workspace-list',
  templateUrl: './workspace-list.component.html',
  styleUrls: ['./workspace-list.component.css']
})
export class WorkspaceListComponent implements OnInit {
  @ViewChild('renameDialog', { static: true })
  private renameDialog: TemplateRef<any>;

  @ViewChild('shareDialog', { static: true })
  private shareDialog: TemplateRef<any>;

  workspaces: Workspace[] = [];
  currentWorkspace: Workspace;

  get ownWorkspaces(): Workspace[] {
    return this.workspaces.filter(w => w.isOwner);
  }

  get sharedWorkspaces(): Workspace[] {
    return this.workspaces.filter(w => !w.isOwner);
  }

  constructor(public service: WorkspaceService,
    private modal: NgbModal,
    private auth: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    this.service.getWorkspaces()
      .subscribe(list => this.workspaces = list);
  }

  edit(ws: Workspace) {
    this.currentWorkspace = Object.assign({}, ws);
    this.modal.open(this.renameDialog, { backdrop: 'static' }).result.then(
      () => {
        this.service.saveWorkspace(this.currentWorkspace)
          .subscribe(() => {
            this.ngOnInit();
            this.currentWorkspace = null;
          });
      },
      () => null
    );
  }

  delete(ws: Workspace) {
    this.service.deleteWorkspace(ws)
      .subscribe(() => this.ngOnInit());
  }

  share(ws: Workspace) {
    this.currentWorkspace = Object.assign({}, ws);
    this.modal.open(this.shareDialog, { backdrop: 'static', size: 'lg' }).result.then(
      () => {
        this.ngOnInit();
        this.currentWorkspace = null;
      },
      () => null
    );
  }

  createLink() {
    this.service.shareWorkspace(this.currentWorkspace)
      .subscribe(ws => this.currentWorkspace = ws);
  }

  deleteLink() {
    this.service.unshareWorkspace(this.currentWorkspace)
      .subscribe(ws => this.currentWorkspace = ws);
  }

  kickUser(userName: string) {
    this.service.removeWorkspaceUser(this.currentWorkspace, { userName })
      .subscribe(ws => this.currentWorkspace = ws);
  }

  leave(ws: Workspace) {
    this.service.removeWorkspaceUser(ws, { userName: this.auth.userProfile.sub })
      .subscribe(() => this.ngOnInit());
  }

  getSharingLink(ws: Workspace): string {
    return environment.origin
      + this.router.createUrlTree(['/workspaces', ws.id, 'invite', ws.sharingKey], { replaceUrl: true }).toString();
  }
}
