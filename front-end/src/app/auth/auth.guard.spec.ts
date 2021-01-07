import { TestBed } from '@angular/core/testing';

import { AuthGuard } from './auth.guard';
import { AuthService } from './auth.service';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';

describe('AuthGuard', () => {
  let guard: AuthGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthGuard,
        {
          provide: AuthService,
          useValue: {
            userHasScopes: () => false
          }
        }
      ],
      imports: [
        RouterTestingModule
      ]
    });

    guard = TestBed.get(AuthGuard);
  });

  it('should initialize', () => {
    expect(guard).toBeTruthy();
  });

  describe('for authenticated user', () => {
    beforeEach(() => {
      const auth = TestBed.get(AuthService);
      auth.isAuthenticated = true;
    });

    it('should allow navigation if no scopes required', () => {
      expect(guard.canActivate({} as any, {} as any)).toBeTruthy();
    });

    it('should reject navigation if user does not have required scopes', () => {
      expect(guard.canActivate({ data: { requiredScopes: ['roleA', 'roleB'] } } as any, {} as any)).toBeFalsy();
    });

    it('should allow navigation if user has required scopes', () => {
      const auth = TestBed.get(AuthService);
      spyOn(auth, 'userHasScopes').and.returnValue(true);
      expect(guard.canActivate({ data: { requiredScopes: ['roleA', 'roleB'] } } as any, {} as any)).toBeTruthy();
      expect(auth.userHasScopes).toHaveBeenCalledWith(['roleA', 'roleB']);
    });
  });

  it('should redirect anonymous user', () => {
    const target = '/target/recource?with=params#andFragment';
    const auth = TestBed.get(AuthService);
    auth.isAuthenticated = false;
    const router = TestBed.get(Router);
    spyOn(router, 'navigate');

    expect(guard.canActivate({} as any, { url: target } as any)).toBeFalsy();

    expect(auth.redirectUrl).toEqual(target);
    expect(router.navigate).toHaveBeenCalledWith(['/login'], { replaceUrl: true });
  });
});
