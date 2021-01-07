import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardComponent } from './dashboard.component';
import { TimeAgoPipe } from '../pipes/time-ago.pipe';
import { DateTimePipe } from '../pipes/date-time.pipe';
import { RouterTestingModule } from '@angular/router/testing';
import { NodeService } from '../nodes/node.service';
import { EMPTY, of } from 'rxjs';
import { SamsNode, SamsNodeType, LatestValues, LatestMeasurement, LatestModelValue } from '../nodes/nodes.domain';

import * as moment from 'moment';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Router } from '@angular/router';
import { NodeLabelComponent } from '../nodes/node-label/node-label.component';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        DashboardComponent,
        TimeAgoPipe,
        DateTimePipe,
        NodeLabelComponent
      ],
      providers: [
        {
          provide: NodeService,
          useValue: {
            getNodes: () => EMPTY,
            getLatestValues: () => EMPTY
          }
        }
      ],
      imports: [RouterTestingModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load and parse node list on init', () => {
    const service = TestBed.get(NodeService);

    const nodes: Partial<SamsNode>[] = [
      { id: 'root', type: SamsNodeType.Apiary, },
      { id: 'hiveA', parentId: 'root', type: SamsNodeType.Hive, },
      { id: 'hiveB', parentId: 'root', type: SamsNodeType.Hive, },
      { id: 'childA1', parentId: 'hiveA', type: SamsNodeType.HiveElement, },
      { id: 'grandChildA1', parentId: 'childA1', type: SamsNodeType.Other, },
      { id: 'childA2', parentId: 'hiveA', type: SamsNodeType.HiveElement, },
      { id: 'childB1', parentId: 'hiveB', type: SamsNodeType.HiveElement, },
    ];

    const latestValues: LatestValues[] = [
      {
        id: 'hiveA',
        latestMeasurements: [
          { type: 'a', value: 1, timestamp: moment().subtract(1, 'hour').toISOString() } as LatestMeasurement,
          { type: 'b', value: 2, timestamp: moment().subtract(13, 'hour').toISOString() } as LatestMeasurement
        ]
      } as LatestValues,
      {
        id: 'childB1',
        latestModelValues: [
          { modelCode: 'a', label: '1', timestamp: moment().subtract(2, 'hour').toISOString() } as LatestModelValue,
          { modelCode: 'b', label: '2', timestamp: moment().subtract(30, 'hour').toISOString() } as LatestModelValue
        ]
      } as LatestValues,
    ];

    spyOn(service, 'getNodes').and.returnValue(of(nodes));
    spyOn(service, 'getLatestValues').and.callFake((id) => of(latestValues.find(v => v.id === id) || {}));

    component.ngOnInit();

    expect(component.hives.length).toBe(2);
    const hive1 = component.hives[0];
    const hive2 = component.hives[1];
    expect(hive1.isActive).toBeTruthy();
    expect(hive1.children.length).toBe(3);
    expect(hive1.latestMeasurements[0].isRecent).toBeTruthy();
    expect(hive1.latestMeasurements[1].isRecent).toBeFalsy();

    expect(hive2.isActive).toBeFalsy();
    expect(hive2.children.length).toBe(1);
    expect(hive2.children[0].isActive).toBeTruthy();
    expect(hive2.children[0].latestModelValues[0].isRecent).toBeTruthy();
    expect(hive2.children[0].latestModelValues[1].isRecent).toBeFalsy();
  });

  it('should open reports', () => {
    const router = TestBed.get(Router);
    spyOn(router, 'navigate');

    const hive: SamsNode = {
      id: 'hive',
      children: [{ id: 'child1' }, { id: 'child2' }] as any
    } as SamsNode;
    component.openReports(hive);

    expect(router.navigate).toHaveBeenCalledWith(
      ['report'],
      {
        queryParams: {
          n: ['hive', 'child1', 'child2'],
          ra: true
        }
      }
    );
  });

  it('should open last measurements modal', () => {
    const modalService = TestBed.get(NgbModal);
    spyOn(modalService, 'open').and.returnValue({ componentInstance: {} });
    component.openLastMeasurementsDialog({ id: 'hive', latestMeasurements: [ {} ] } as SamsNode);
    expect(modalService.open).toHaveBeenCalled();
  });

  it('should update values', () => {
    const service = TestBed.get(NodeService);
    spyOn(service, 'getLatestValues').and.returnValue(of({}));
    const hive = {
      id: 'hive',
      children: [{ id: 'child1' }, { id: 'child2' }]
    };

    component.fetchLatestValues(hive as any);
    expect(service.getLatestValues).toHaveBeenCalledWith('hive');
    expect(service.getLatestValues).toHaveBeenCalledWith('child1');
    expect(service.getLatestValues).toHaveBeenCalledWith('child2');
  });
});
