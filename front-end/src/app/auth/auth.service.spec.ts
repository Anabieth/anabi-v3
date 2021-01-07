import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';
import { RouterTestingModule } from '@angular/router/testing';
import { JwtHelperService } from '@auth0/angular-jwt';
import { TokenService } from './token.service';
import { Router } from '@angular/router';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        {
          provide: JwtHelperService,
          useValue: {
            decodeToken: () => null
          }
        },
        {
          provide: TokenService,
          useValue: {
            setTokens: () => null,
            removeTokens: () => null
          }
        }
      ],
      imports: [
        RouterTestingModule
      ]
    });

    service = TestBed.get(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should forward to Auth0 on login', () => {
    spyOn(service.auth0, 'authorize');
    spyOn(localStorage, 'setItem');
    service.redirectUrl = '/test/url';
    service.login();
    expect(service.auth0.authorize).toHaveBeenCalled();
    expect(localStorage.setItem).toHaveBeenCalledWith('url', '/test/url');
  });

  it('should do internal login', () => {
    spyOn(localStorage, 'setItem');

    const tokens = TestBed.get(TokenService);
    tokens.idToken = 'qwe-123';
    spyOn(tokens, 'setTokens');
    spyOn(service.logIn$, 'next');

    const authResult = { accessToken: 'abc-123', idToken: 'qwe-123', idTokenPayload: { name: 'John' } };

    service['localLogin'](authResult); // calling private method :P

    expect(localStorage.setItem).toHaveBeenCalledWith('isLoggedIn', 'true');
    expect(tokens.setTokens).toHaveBeenCalled();
    expect(service.logIn$.next).toHaveBeenCalled();
    expect(service.userProfile.name).toEqual('John');
  });


  describe('should handle authentication', () => {
    let logged = false;
    let router = null;

    beforeEach(() => {
      router = TestBed.get(Router);
      spyOn(router, 'navigateByUrl');

      service.auth0.parseHash = (cb) => cb(null,
        { accessToken: 'abc-123', idToken: 'qwe-123' });
      service['localLogin'] = () => logged = true;

      localStorage.clear();
    });

    it('without redirect url', () => {
      service.handleAuthentication();
      expect(router.navigateByUrl).toHaveBeenCalledWith('/userHome');
      expect(logged).toBeTruthy();
    });

    it('with redirect url', () => {
      localStorage.setItem('url', '/test/url');
      spyOn(localStorage, 'removeItem');

      service.handleAuthentication();

      expect(router.navigateByUrl).toHaveBeenCalledWith('/test/url');
      expect(localStorage.removeItem).toHaveBeenCalledWith('url');
      expect(logged).toBeTruthy();
    });
  });

  it('should handle authentication error', () => {
    const router = TestBed.get(Router);
    spyOn(router, 'navigate');
    service.auth0.parseHash = (cb) => cb({ error: 'Testing error' }, null);

    service.handleAuthentication();

    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should renew tokens', () => {
    spyOn(localStorage, 'getItem').and.returnValue('true');

    const router = TestBed.get(Router);
    spyOn(router, 'navigateByUrl');

    let logged = false;
    service['localLogin'] = () => logged = true;
    service.auth0.checkSession = (_, cb) => cb(null,
      { accessToken: 'abc-123', idToken: 'qwe-123' });
    service.redirectUrl = '/redirectUrl?with=params#andFragment';

    service.maybeRenewTokens();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/redirectUrl?with=params#andFragment');
    expect(logged).toBeTruthy();
  });

  it('should handle renew tokens error', () => {
    spyOn(localStorage, 'getItem').and.returnValue('true');

    const router = TestBed.get(Router);
    spyOn(router, 'navigateByUrl');

    spyOn(service.auth0, 'logout');

    service.auth0.checkSession = (_, cb) => cb({ error: 'Testing' }, null);
    service.redirectUrl = '/redirectUrl?with=params#andFragment';

    service.maybeRenewTokens();

    expect(service.auth0.logout).toHaveBeenCalled();
  });

  describe('should not renew tokens when', () => {
    let tokens;
    beforeEach(() => {
      jasmine.clock().mockDate(new Date('2019-01-24T10:20:30Z'));
      spyOn(service.auth0, 'checkSession');
      tokens = TestBed.get(TokenService);
    });

    it('user is already authenticated', () => {
      spyOn(localStorage, 'getItem').and.returnValue('true');
      tokens.expiresAt = new Date('2019-01-24T10:20:40Z').getTime();

      service.maybeRenewTokens();
      expect(service.auth0.checkSession).toHaveBeenCalledTimes(0);
    });

    it('user was logged off', () => {
      spyOn(localStorage, 'getItem').and.returnValue(null);
      tokens.expiresAt = 0;

      service.maybeRenewTokens();
      expect(service.auth0.checkSession).toHaveBeenCalledTimes(0);
    });
  });

  it('should log out', () => {
    const tokens = TestBed.get(TokenService);
    spyOn(tokens, 'removeTokens');
    spyOn(service.auth0, 'logout');
    spyOn(localStorage, 'clear');

    service.logout();

    expect(tokens.removeTokens).toHaveBeenCalled();
    expect(service.auth0.logout).toHaveBeenCalled();
    expect(localStorage.clear).toHaveBeenCalled();
  });

  it('should return is user authenticated', () => {
    jasmine.clock().mockDate(new Date('2019-01-24T10:20:30Z'));
    const tokens = TestBed.get(TokenService);
    tokens.expiresAt = new Date('2019-01-24T10:20:30Z').getTime();
    expect(service.isAuthenticated).toBeFalsy();
    tokens.expiresAt = new Date('2019-01-24T10:20:31Z').getTime();
    expect(service.isAuthenticated).toBeTruthy();
  });

  it('should check if user has all requested scopes', () => {
    service['localLogin']({ scope: 'roleA roleB' });

    expect(service.userHasScopes(['roleA'])).toBeTruthy();
    expect(service.userHasScopes(['roleB'])).toBeTruthy();
    expect(service.userHasScopes(['roleA', 'roleB'])).toBeTruthy();
    expect(service.userHasScopes(['roleC'])).toBeFalsy();
    expect(service.userHasScopes(['roleA', 'roleC'])).toBeFalsy();

  });
});
