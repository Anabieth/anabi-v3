import { TestBed, async, ComponentFixture } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { AppComponent } from './app.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from './auth/auth.service';
import { WorkspaceService } from './workspaces/workspace.service';
import { of } from 'rxjs';

describe('AppComponent', () => {

  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let native: any;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AppComponent
      ],
      providers: [
        {
          provide: AuthService,
          useValue: {
            handleAuthentication: () => null,
            maybeRenewTokens: () => null,
            logIn$: of(1),
            userHasScopes: () => false
          }
        },
        {
          provide: WorkspaceService,
          useValue: {
            updateActive: () => null,
            activeWorkspace$: of({ name: 'Testing workspace' })
          }
        }
      ],
      imports: [
        NgbModule,
        RouterTestingModule
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    native = fixture.nativeElement;
    fixture.detectChanges();
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  it('should render title', () => {
    expect(native.querySelector('a.navbar-brand').textContent).toContain('SAMS DW');
  });

  describe('for authenticated user', () => {
    beforeEach(() => {
      const auth = TestBed.get(AuthService);
      auth.isAuthenticated = true;
      auth.userProfile = { name: 'John The Tester' };
      fixture.detectChanges();
    });

    it('should render user name', () => {
      const span = native.querySelector('#userDropdown > span');
      expect(span.innerText).toContain('John The Tester');
    });

    it('should render Log Out link', () => {
      const link = native.querySelector('header > ul.navbar-nav.ml-auto > li > div > a');
      expect(link.text).toContain('Log out');
    });

    it('should render active workspace', () => {
      const span = native.querySelector('.sidebar > div:nth-child(1) > p');
      expect(span.innerText).toMatch('Testing workspace');
    });

    ['Dashboard', 'Nodes', 'Devices', 'Models', 'Reports', 'Workspaces']
      .forEach((val, idx) =>
        it(`should render ${val} link`, () => {
          const link = native.querySelector(`.sidebar > div:nth-child(2) > nav > a:nth-child(${idx + 1})`);
          expect(link.text).toContain(val);
        })
      );

    it('should render HW Configs if user has scope', () => {
      const auth = TestBed.get(AuthService);
      spyOn(auth, 'userHasScopes').and.returnValue(true);
      fixture.detectChanges();
      const link = native.querySelector(`.sidebar > div:nth-child(2) > nav > a:nth-child(7)`);
      expect(link.text).toContain('Configs');
    });
  });

  describe('for anonymous user', () => {
    it('should render Log In link', () => {
      const link = native.querySelector('header > ul > li.nav-item > a.nav-link');
      expect(link.text).toContain('Log in');
    });

    ['Battery life', 'Swarm economy', 'Monitoring system evaluation']
      .forEach((val, idx) => {
        it(`should render ${val} link`, () => {
          const link = native.querySelector(`.sidebar > div:nth-child(2) > nav > a:nth-child(${idx + 1})`);
          expect(link.text).toContain(val);
        });
      });
  });
});
