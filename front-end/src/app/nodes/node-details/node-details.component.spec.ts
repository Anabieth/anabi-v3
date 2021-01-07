import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';

import { NodeDetailsComponent } from './node-details.component';
import { RouterTestingModule } from '@angular/router/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { NodeService } from '../node.service';
import { SamsNodeDetails, SamsNodeType, SamsNode } from '../nodes.domain';
import { of, Observable } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { ConfigService } from '../../configs/config.service';
import { AuthService } from '../../auth/auth.service';
import { NodeLabelComponent } from '../node-label/node-label.component';

const createNode = (id, name?, parentId?): SamsNode => ({
  'id': id, 'name': name, parentId: parentId,
  type: SamsNodeType.Other, children: [], hwConfigId: null,
  location: null, clientId: null, isActive: null, lastValues: null
} as any);

describe('NodeDetailsComponent', () => {
  let component: NodeDetailsComponent;
  let fixture: ComponentFixture<NodeDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [NodeDetailsComponent, NodeLabelComponent],
      providers: [
        {
          provide: NodeService,
          useValue: {
            getNodeDetails: (nodeId): Observable<SamsNodeDetails> => {
              const node: SamsNodeDetails = Object.assign(new SamsNodeDetails(), createNode(nodeId, 'Testing node'));
              node.children = [
                createNode('child-111', 'Child 1'),
                createNode('child-222', 'Child 2'),
                createNode('child-333', 'Child 3')
              ];
              node.ancestors = [
                createNode('parent-000', 'Grand-parent node'),
                createNode('parent-111', 'Parent node')
              ];
              return of(node);
            },
            getNodeMappings: () => of([
              { sourceId: 'src-111', valueKey: 'aaa' },
              { sourceId: 'src-222', valueKey: 'bbb' }
            ]),
            saveNode: (node) => of(node),
            saveNodeMappings: (_, maps) => of(maps),
            isSourceIdUsed: () => of({ isUsed: false }),
            getMappingValueKeys: () => of(['key-A', 'key-B'])
          }
        },
        {
          provide: ConfigService,
          useValue: {
            getConfigs: () => of([{ name: 'One' }, { name: 'Two' }])
          }
        },
        {
          provide: AuthService,
          useValue: {
            userHasScopes: () => false
          }
        },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of({
              get: () => 'node-123'
            })
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
    fixture = TestBed.createComponent(NodeDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load node details and mapping on startup', () => {
    const service = TestBed.get(NodeService);
    spyOn(service, 'getNodeDetails').and.callThrough();
    spyOn(service, 'getNodeMappings').and.callThrough();
    spyOn(component.nodeForm, 'reset').and.callThrough();
    spyOn(component.mappingItems, 'removeAt').and.callThrough();
    spyOn(component.mappingItems, 'push').and.callThrough();

    component.ngOnInit();

    expect(service.getNodeDetails).toHaveBeenCalledWith('node-123');
    expect(component.nodeForm.reset).toHaveBeenCalled();
    expect(service.getNodeMappings).toHaveBeenCalledWith('node-123');
    expect(component.mappingItems.removeAt).toHaveBeenCalledTimes(2);
    expect(component.mappingItems.push).toHaveBeenCalledTimes(2);
  });

  it('should load HW configs if user has scopes', () => {
    const service = TestBed.get(ConfigService);
    spyOn(service, 'getConfigs').and.callThrough();
    const auth = TestBed.get(AuthService);
    spyOn(auth, 'userHasScopes').and.returnValue(true);

    component.ngOnInit();

    expect(service.getConfigs).toHaveBeenCalled();
    expect(auth.userHasScopes).toHaveBeenCalledWith(['hw']);
    expect(component.hwConfigs.length).toBe(2);
  });


  it('should add new mapping item', () => {
    component.addItemClicked();
    expect(component.mappingItems.length).toEqual(3);
    expect(component.mappingItems.at(2).get('sourceId').value).toBeFalsy();
  });

  it('should delete mapping item', () => {
    component.deleteItemClicked(1);
    expect(component.mappingItems.length).toEqual(1);
    expect(component.mappingItems.at(0).get('sourceId').value).toEqual('src-111');
  });

  it('should toggle controls on node type change', () => {
    const clientId = component.nodeForm.get('clientId');
    const isActive = component.nodeForm.get('isActive');

    component.nodeForm.get('type').setValue('OTHER');
    expect(component.isDevice).toBeFalsy();
    expect(clientId.disabled).toBeTruthy();
    expect(isActive.disabled).toBeTruthy();

    component.nodeForm.get('type').setValue('DEVICE');
    expect(component.isDevice).toBeTruthy();
    expect(clientId.enabled).toBeTruthy();
    expect(isActive.enabled).toBeTruthy();
  });

  it('should manage validators when isActive changed', () => {
    const clientId = component.nodeForm.get('clientId');
    const isActive = component.nodeForm.get('isActive');

    isActive.setValue(false);
    expect(clientId.validator).toBeNull();

    isActive.setValue(true);
    expect(clientId.errors).toEqual({ required: true });
  });

  describe('should save', () => {
    let service: NodeService;

    beforeEach(() => {
      service = TestBed.get(NodeService);
      spyOn(service, 'saveNode').and.callThrough();
      spyOn(service, 'getNodeDetails').and.callThrough();
      spyOn(service, 'saveNodeMappings').and.callThrough();
    });

    it('nothing', () => {
      component.saveClicked();
      expect(service.saveNode).toHaveBeenCalledTimes(0);
      expect(service.getNodeDetails).toHaveBeenCalledTimes(0);
      expect(service.saveNodeMappings).toHaveBeenCalledTimes(0);
    });

    it('only node details', () => {
      component.nodeForm.markAsDirty();
      component.saveClicked();
      expect(service.saveNode).toHaveBeenCalledTimes(1);
      expect(service.getNodeDetails).toHaveBeenCalledTimes(1);
      expect(service.saveNodeMappings).toHaveBeenCalledTimes(0);
    });

    it('only node mapping', () => {
      component.mappingForm.markAsDirty();
      component.saveClicked();
      expect(service.saveNode).toHaveBeenCalledTimes(0);
      expect(service.getNodeDetails).toHaveBeenCalledTimes(0);
      expect(service.saveNodeMappings).toHaveBeenCalledTimes(1);
    });

    it('both node details and mapping', () => {
      component.nodeForm.markAsDirty();
      component.mappingForm.markAsDirty();
      component.saveClicked();
      expect(service.saveNode).toHaveBeenCalledTimes(1);
      expect(service.getNodeDetails).toHaveBeenCalledTimes(1);
      expect(service.saveNodeMappings).toHaveBeenCalledTimes(1);
    });
  });

  describe('should validate', () => {

    function addMapping(sourceId, valueKey) {
      component.mappingItems.push(component.createMappingItem({ sourceId: sourceId, valueKey: valueKey }));
    }

    it('source ID duplicattes', () => {
      addMapping('src-x', 'one');
      addMapping('src-x', 'two');
      addMapping('src-k', 'three');
      addMapping('src-k', 'four');
      fixture.detectChanges();
      expect(component.mappingForm.errors.sourceIdDuplicates).toEqual(['src-k', 'src-x']);
      expect(fixture.nativeElement.querySelector('label > small.text-danger').textContent).toContain('src-k,src-x');
    });

    it('value key duplicattes', () => {
      addMapping('src-1', 'one');
      addMapping('src-2', 'two');
      addMapping('src-3', 'one');
      addMapping('src-4', 'two');
      fixture.detectChanges();
      expect(component.mappingForm.errors.valueKeyDuplicates).toEqual(['one', 'two']);
      expect(fixture.nativeElement.querySelector('label > small.text-danger').textContent).toContain('one,two');
    });

    it('value key not supported', () => {
      component.valueKeys = ['one', 'other'];
      component.mappings = [];
      addMapping('src-1', 'one');
      addMapping('src-2', 'two');
      fixture.detectChanges();
      expect(component.mappingItems.at(0).get('valueKey')['valueKeyNotSupported']).toBeNull();
      expect(component.mappingItems.at(1).get('valueKey')['valueKeyNotSupported']).toBeTruthy();
      expect(fixture.nativeElement.querySelector('small.text-warning').textContent).toContain('not supported');
    });

    it('empty value keys', () => {
      component.valueKeys = [];
      component.mappings = [];
      addMapping('src-1', 'one');
      addMapping('src-2', 'two');
      fixture.detectChanges();
      expect(component.mappingItems.at(0).get('valueKey')['valueKeyNotSupported']).toBeNull();
      expect(component.mappingItems.at(1).get('valueKey')['valueKeyNotSupported']).toBeNull();
    });

    it('source ID already used', fakeAsync(() => {
      const service = TestBed.get(NodeService);
      spyOn(service, 'isSourceIdUsed').and.returnValue(of({ isUsed: true, nodeId: 'node-987', nodeName: 'Other node' }));
      const ctrl = component.mappingItems.at(0).get('sourceId');
      ctrl.markAsDirty();
      ctrl.setValue('changed-src-id');
      tick(501);

      expect(service.isSourceIdUsed).toHaveBeenCalled();
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('small.text-danger').textContent).toContain('already used');
      const link = fixture.nativeElement.querySelector('small.text-danger > span > a');
      expect(link.textContent).toEqual('Other node');
      expect(link.href).toContain('/nodes/node-987');
    }));

    it('source ID already used by other user', fakeAsync(() => {
      const service = TestBed.get(NodeService);
      spyOn(service, 'isSourceIdUsed').and.returnValue(of({ isUsed: true }));
      const ctrl = component.mappingItems.at(0).get('sourceId');
      ctrl.markAsDirty();
      ctrl.setValue('changed-src-id');
      tick(501);

      expect(service.isSourceIdUsed).toHaveBeenCalled();
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('small.text-danger').textContent).toContain('already used');
      expect(fixture.nativeElement.querySelector('small.text-danger > span > a')).toBeFalsy();
    }));
  });

});
