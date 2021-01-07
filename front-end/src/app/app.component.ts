import { Component, OnInit } from '@angular/core';
import { AuthService } from './auth/auth.service';
import { WorkspaceService } from './workspaces/workspace.service';
import { Router, NavigationStart } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  navbarOpen = false;
  year = new Date().getFullYear();

  constructor(public auth: AuthService, public workspaceService: WorkspaceService, public router: Router) {
    this.auth.handleAuthentication();
  }

  ngOnInit(): void {
    this.router.events.pipe(
      filter(e => e instanceof NavigationStart),
    ).subscribe((e: NavigationStart) => {
      if (e.url !== '/login') {
        this.auth.redirectUrl = e.url;
      }
      this.auth.maybeRenewTokens();
      // close navbar on navigation
      this.navbarOpen = false;
    });

    this.auth.logIn$.subscribe(
      () => this.workspaceService.updateActive()
    );
  }

}
