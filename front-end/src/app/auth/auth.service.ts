import { Injectable } from '@angular/core';
import * as auth0 from 'auth0-js';
import { environment } from '../../environments/environment';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { TokenService } from './token.service';

@Injectable()
export class AuthService {
  auth0 = new auth0.WebAuth({
    clientID: '3Zv55BlAANYq7fLkNh1FGWMS5RA4sVEF',
    domain: 'sams-project.eu.auth0.com',
    responseType: 'token id_token',
    audience: 'sams-dwh-web-api',
    redirectUri: environment.origin + '/login',
    scope: 'openid profile email admin hw'
  });

  constructor(private router: Router, private tokens: TokenService) {
    this._userProfile = {};
    this.redirectUrl = '';
  }

  public redirectUrl: string;

  private _userProfile: any;
  get userProfile(): any {
    return this._userProfile;
  }

  private _grantedScopes: string[] = [];

  private _parsingHash = true;
  private _renewingTokens = false;
  get state() {
    if (this._parsingHash) {
      return 'initial';
    } else if (this._renewingTokens) {
      return 'renewing';
    } else if (this.isAuthenticated) {
      return 'loggedIn';
    } else {
      return 'anonymousUser';
    }
  }

  public logIn$: Subject<null> = new Subject();

  public login(): void {
    localStorage.setItem('url', this.redirectUrl);
    this.auth0.authorize();
  }

  get isAuthenticated(): boolean {
    return new Date().getTime() < this.tokens.expiresAt;
  }

  public userHasScopes(scopes: string[]): boolean {
    return scopes.every(scope => this._grantedScopes.includes(scope));
  }

  public handleAuthentication(): void {
    this.auth0.parseHash((err, authResult) => {
      if (authResult && authResult.accessToken && authResult.idToken) {
        history.replaceState(null, document.title, '');
        this.localLogin(authResult);
        let url = localStorage.getItem('url');
        if (url) {
          localStorage.removeItem('url');
        } else {
          url = '/userHome';
        }
        this.router.navigateByUrl(url);
      } else if (err) {
        this.router.navigate(['/']);
        console.error(err);
      }
      this._parsingHash = false;
    });
  }

  protected localLogin(authResult) {
    localStorage.setItem('isLoggedIn', 'true');
    this.tokens.setTokens(authResult);
    this._userProfile = authResult.idTokenPayload;
    this._grantedScopes = (authResult.scope || '').split(' ');
    this.logIn$.next();
  }

  public maybeRenewTokens(): void {
    if (!this.isAuthenticated && localStorage.getItem('isLoggedIn') === 'true') {
      this._renewingTokens = true;
      this.auth0.checkSession({}, (err, authResult) => {
        if (authResult && authResult.accessToken && authResult.idToken) {
          this.localLogin(authResult);
          this.router.navigateByUrl(this.redirectUrl);
        } else if (err) {
          console.error('Could not get a new token', err);
          this.logout();
        }
        this._renewingTokens = false;
      });
    }
  }

  public logout(): void {
    this.tokens.removeTokens();
    localStorage.clear();
    this.auth0.logout({
      returnTo: environment.origin
    });
  }
}
