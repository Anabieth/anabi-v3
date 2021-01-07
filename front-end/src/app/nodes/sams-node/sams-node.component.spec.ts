import { async, ComponentFixture, TestBed, tick, fakeAsync } from '@angular/core/testing';

import { SamsNodeComponent } from './sams-node.component';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { NodeService } from '../node.service';
import { empty, of, throwError } from 'rxjs';
import { SamsNode } from '../nodes.domain';
import { delay } from 'rxjs/operators';
import { Router } from '@angular/router';

describe('SamsNodeComponent', () => {
  let component: SamsNodeComponent;
  let fixture: ComponentFixture<SamsNodeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SamsNodeComponent],
      providers: [
        {
          provide: NodeService,
          useValue: {
            getNodes: () => empty(),
            isSourceIdUsed: () => empty(),
            saveNode: () => empty(),
            saveNodeMappings: () => empty(),
            deleteNode: () => empty()
          }
        }
      ],
      imports: [
        RouterTestingModule,
        FormsModule,
        ReactiveFormsModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SamsNodeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should parse nodes on init', () => {
    const service = TestBed.get(NodeService);
    spyOn(service, 'getNodes').and.returnValue(of([
      new SamsNode('A', null, 'a', null),
      new SamsNode('B', null, 'b', null),
      new SamsNode('A1', null, 'a1', 'a'),
      new SamsNode('A2', null, 'a2', 'a'),
      new SamsNode('A1x', null, 'a1x', 'a1')
    ]));

    component.ngOnInit();

    expect(component.existingNodes.length).toBe(6);
    expect(component.existingNodes[1].name).toEqual('A');
    expect(component.existingNodes[2].name).toEqual('- A1');
    expect(component.existingNodes[3].name).toEqual('- - A1x');
    expect(component.existingNodes[4].name).toEqual('- A2');
  });

  it('should update preview on form values changed', () => {
    component.samsForm.patchValue({ baseName: 'testing', clientId: '123-789' });

    expect(component.hive.name).toEqual('testing');
    expect(component.hive.params['audio'].sourceId).toEqual('audio-123-789');
    expect(component.device.name).toEqual('testing RaspberryPi');
    expect(component.device.params['clientId'].sourceId).toEqual('123-789@clients');
    expect(component.outside.name).toEqual('testing outside');
    expect(component.outside.params['temperature'].sourceId).toEqual('dht22-temperature-123-789');
  });

  it('should check source IDs on client ID changes', fakeAsync(() => {
    const service = TestBed.get(NodeService);
    spyOn(service, 'isSourceIdUsed').and.returnValues(
      of({ isUsed: false }).pipe(delay(1)),
      of({ isUsed: false }).pipe(delay(1)),
      of({ isUsed: false }).pipe(delay(1)),
      of({ isUsed: true }).pipe(delay(1)),
      of({ isUsed: false }).pipe(delay(5))
    );

    component.samsForm.get('clientId').setValue('new-client-id');

    tick(500);
    expect(component.samsForm.hasError('checkingSources')).toBeTruthy();
    tick(1);
    expect(service.isSourceIdUsed).toHaveBeenCalledWith('none', 'ds18b20-0-new-client-id');
    expect(service.isSourceIdUsed).toHaveBeenCalledWith('none', 'audio-new-client-id');
    expect(service.isSourceIdUsed).toHaveBeenCalledWith('none', 'scale-new-client-id');
    expect(service.isSourceIdUsed).toHaveBeenCalledWith('none', 'dht22-temperature-new-client-id');
    tick(4);
    expect(service.isSourceIdUsed).toHaveBeenCalledWith('none', 'dht22-humidity-new-client-id');
    expect(component.samsForm.hasError('sourcesUsed')).toBeTruthy();
  }));

  it('should register node structure', fakeAsync(() => {
    const service = TestBed.get(NodeService);
    spyOn(service, 'saveNode').and.returnValues(
      of({ id: 'hive-id' }).pipe(delay(10)),
      of({ id: 'device-id' }).pipe(delay(20)),
      of({ id: 'outside-id' }).pipe(delay(40))
    );

    spyOn(service, 'saveNodeMappings').and.returnValues(
      of({}).pipe(delay(5)),
      of({}).pipe(delay(10)),
      of({}).pipe(delay(20))
    );

    const router = TestBed.get(Router);
    spyOn(router, 'navigate');

    component.hive.name = 'hive';
    component.device.name = 'device';
    component.outside.name = 'outside';

    component.registerClicked();
    expect(service.saveNode).toHaveBeenCalledWith(jasmine.objectContaining({ name: 'hive' }));
    tick(10);
    expect(service.saveNode).toHaveBeenCalledWith(jasmine.objectContaining({ name: 'device' }));
    expect(service.saveNode).toHaveBeenCalledWith(jasmine.objectContaining({ name: 'outside' }));
    expect(service.saveNodeMappings).toHaveBeenCalledWith('hive-id', jasmine.anything());
    tick(50);
    expect(service.saveNodeMappings).toHaveBeenCalledWith('outside-id', jasmine.anything());

    expect(router.navigate).toHaveBeenCalledWith(['/nodes', 'hive-id']);
  }));

  it('should handle failed registration', fakeAsync(() => {
    const service = TestBed.get(NodeService);
    spyOn(service, 'saveNode').and.returnValues(
      of({ id: 'hive-id' }),
      throwError({ error: 'Testing error' }),
      of({ id: 'outside-id' })
    );

    spyOn(service, 'saveNodeMappings').and.returnValue(of(null));
    spyOn(service, 'deleteNode').and.returnValue(empty());

    component.registerClicked();
    expect(service.saveNode).toHaveBeenCalledTimes(3);
    expect(service.deleteNode).toHaveBeenCalledWith({ id: 'hive-id' });
    expect(component.resultErorr).toEqual('Testing error');
  }));
});
