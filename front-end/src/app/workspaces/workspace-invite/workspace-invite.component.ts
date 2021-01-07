import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Workspace } from '../workspaces.domain';
import { switchMap } from 'rxjs/operators';
import { WorkspaceService } from '../workspace.service';

@Component({
  selector: 'app-workspace-invite',
  templateUrl: './workspace-invite.component.html',
  styleUrls: ['./workspace-invite.component.css']
})
export class WorkspaceInviteComponent implements OnInit {
  workspace: Workspace;
  status: string;
  key: string;

  constructor(private route: ActivatedRoute, private service: WorkspaceService, private router: Router) { }

  ngOnInit() {
    this.route.paramMap
      .pipe(
        switchMap(
          (map: ParamMap) => {
            this.key = map.get('key');
            return this.service.acceptWorkspaceInvite(map.get('id'), map.get('key'));
          }
        )
      )
      .subscribe(
        result => {
          this.workspace = result.workspace;
          this.status = result.status;
        },
        err => this.status = err.error.status
      );
  }

  confirm() {
    this.service.acceptWorkspaceInvite(this.workspace.id, this.key, true).subscribe(
      (result) => {
        this.status = result.status;
        this.cancel();
      },
      err => this.status = err.error.status
    );
  }

  cancel() {
    this.router.navigate(['/workspaces']);
  }

}
