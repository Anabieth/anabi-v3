import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LastMeasurementsModalComponent } from './last-measurements-modal.component';
import { NgbModule, NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TimeAgoPipe } from '../../pipes/time-ago.pipe';
import { DateTimePipe } from '../../pipes/date-time.pipe';
import { SamsNode } from '../../nodes/nodes.domain';
import { NodeService } from 'src/app/nodes/node.service';
import { EMPTY, of } from 'rxjs';

describe('LastMeasurementsModalComponent', () => {
  let component: LastMeasurementsModalComponent;
  let fixture: ComponentFixture<LastMeasurementsModalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        LastMeasurementsModalComponent,
        TimeAgoPipe,
        DateTimePipe
      ],
      providers: [
        {
          provide: NgbActiveModal,
          useValue: {}
        },
        {
          provide: NodeService,
          useValue: {
            getLatestMeasurements: () => EMPTY
          }
        }
      ],
      imports: [
        NgbModule,
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LastMeasurementsModalComponent);
    component = fixture.componentInstance;
    component.node = {} as SamsNode;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create chart on type change', () => {
    const service = TestBed.get(NodeService);
    spyOn(service, 'getLatestMeasurements').and.returnValue(
      of({
        typeA: [
          { ts: 1, value: 10 },
          { ts: 2, value: 20 },
          { ts: 3, value: 30 },
          { ts: 4, value: 40 },
        ],
        typeB: [
          { ts: 100, value: 110 },
          { ts: 200, value: 220 },
        ]
      })
    );
    component.node = { id: 'node-id'} as any;
    component.ngOnInit();

    expect(service.getLatestMeasurements).toHaveBeenCalledWith('node-id');

    component.valueTypeChanged('typeA');
    let data = component.chart.data.datasets[0].data;
    expect(data.length).toBe(4);
    expect(data[2]).toEqual({ t: new Date(3), y: 30 });

    component.valueTypeChanged('typeB');
    data = component.chart.data.datasets[0].data;
    expect(data.length).toBe(2);
    expect(data[1]).toEqual({ t: new Date(200), y: 220 });
  });
});
