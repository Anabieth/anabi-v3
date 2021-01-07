import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable()
export class AuthGuard implements CanActivate {


  constructor(private auth: AuthService, private router: Router) { }

  private reject(): boolean {
    this.router.navigate(['/login'], { replaceUrl: true });
    return false;
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | boolean {
    if (this.auth.isAuthenticated) {
      const scopes = route.data && route.data['requiredScopes'];
      if (scopes) {
        return this.auth.userHasScopes(scopes);
      } else {
        return true;
      }
    } else {
      this.auth.redirectUrl = state.url;
      return this.reject();
    }
  }
}
