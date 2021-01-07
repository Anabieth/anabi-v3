import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { empty, of } from 'rxjs';

import { DeviceListComponent } from './device-list.component';
import { DeviceService } from '../device.service';
import { TimeAgoPipe } from '../../pipes/time-ago.pipe';
import { SamsDevice } from '../device-domain';
import { DateTimePipe } from '../../pipes/date-time.pipe';
import { NodeLabelComponent } from '../../nodes/node-label/node-label.component';



describe('DeviceListComponent', () => {
  let component: DeviceListComponent;
  let fixture: ComponentFixture<DeviceListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DeviceListComponent, TimeAgoPipe, DateTimePipe, NodeLabelComponent],
      providers: [
        {
          provide: DeviceService,
          useValue: {
            getDeviceList: () => empty(),
            toggleDeviceIsActive: () => empty()
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
    fixture = TestBed.createComponent(DeviceListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load device list on init', () => {
    const service = TestBed.get(DeviceService);
    spyOn(service, 'getDeviceList').and.returnValue(of([]));

    component.ngOnInit();
    expect(service.getDeviceList).toHaveBeenCalled();
  });

  it('should toggle device activity', () => {
    const service = TestBed.get(DeviceService);
    spyOn(service, 'toggleDeviceIsActive').and.returnValue(of(true));
    const dev = new SamsDevice();
    dev.id = 'dev-123';
    dev.isActive = false;

    component.toggleIsActive(dev);

    expect(service.toggleDeviceIsActive).toHaveBeenCalled();
    expect(dev.isActive).toBeTruthy();
  });
});
