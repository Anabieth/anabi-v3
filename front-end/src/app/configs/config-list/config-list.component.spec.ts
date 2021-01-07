import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigListComponent } from './config-list.component';
import { RouterTestingModule } from '@angular/router/testing';
import { ConfigService } from '../config.service';
import { of } from 'rxjs';
import { TimeAgoPipe } from '../../pipes/time-ago.pipe';

describe('ConfigListComponent', () => {
  let component: ConfigListComponent;
  let fixture: ComponentFixture<ConfigListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ConfigListComponent, TimeAgoPipe],
      providers: [
        {
          provide: ConfigService,
          useValue: {
            getConfigs: () => of([]),
            setDefaultConfig: () => of({})
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
    fixture = TestBed.createComponent(ConfigListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load config list on init', () => {
    const service = TestBed.get(ConfigService);
    spyOn(service, 'getConfigs').and.callThrough();

    component.ngOnInit();
    expect(service.getConfigs).toHaveBeenCalled();
  });

  it('should set default config', () => {
    const service = TestBed.get(ConfigService);
    spyOn(service, 'setDefaultConfig').and.callThrough();
    const config = { id: 123, name: 'test' };

    component.defaultClicked(config);
    expect(service.setDefaultConfig).toHaveBeenCalledWith(config);
  });
});
