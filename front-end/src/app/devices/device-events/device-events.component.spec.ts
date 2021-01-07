import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeviceEventsComponent } from './device-events.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { DateTimePipe } from '../../pipes/date-time.pipe';
import { TimeAgoPipe } from '../../pipes/time-ago.pipe';
import { RouterTestingModule } from '@angular/router/testing';
import { DeviceService } from '../device.service';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { of, empty } from 'rxjs';

describe('DeviceEventsComponent', () => {
  let component: DeviceEventsComponent;
  let fixture: ComponentFixture<DeviceEventsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        DeviceEventsComponent,
        DateTimePipe,
        TimeAgoPipe
      ],
      providers: [
        {
          provide: DeviceService,
          useValue: {
            getDeviceEvents: () => of({})
          }
        },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: empty(),
            fragment: empty()
          }
        }
      ],
      imports: [
        NgbModule,
        RouterTestingModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeviceEventsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should parse params on init', () => {
    const route = TestBed.get(ActivatedRoute);
    const service = TestBed.get(DeviceService);
    route.paramMap = of(convertToParamMap({ deviceId: 'dev-123' }));
    spyOn(service, 'getDeviceEvents').and.returnValue(of({}));

    component.ngOnInit();

    expect(service.getDeviceEvents).toHaveBeenCalledWith('dev-123');
  });

  it('should parse fragment on init ', () => {
    const route = TestBed.get(ActivatedRoute);

    route.fragment = of(null);
    component.ngOnInit();
    expect(component.activeTab).toBe('all');

    route.fragment = of('testing-fragment');
    component.ngOnInit();
    expect(component.activeTab).toBe('testing-fragment');
  });

  it('should handle tab change event', () => {
    const router = TestBed.get(Router);
    spyOn(router, 'navigate').and.callThrough();
    component.onTabChange({ nextId: 'test-tab' } as any);
    expect(router.navigate).toHaveBeenCalledWith([], { fragment: 'test-tab' });
  });

});
