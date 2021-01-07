import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ModelListComponent } from './model-list.component';
import { ModelService } from '../model.service';
import { EMPTY, of } from 'rxjs';
import { NodeService } from '../../nodes/node.service';
import { NodeLabelComponent } from '../../nodes/node-label/node-label.component';

describe('ModelListComponent', () => {
  let component: ModelListComponent;
  let fixture: ComponentFixture<ModelListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ModelListComponent, NodeLabelComponent],
      providers: [
        {
          provide: ModelService,
          useValue: {
            getModels: () => EMPTY,
            getModelTemplates: () => EMPTY
          }
        },
        {
          provide: NodeService,
          useValue: {
            getNodes: () => EMPTY,
          }
        },
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModelListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load model list on init', () => {
    const modelService = TestBed.get(ModelService);
    spyOn(modelService, 'getModels').and.returnValue(
      of([
        { id: '123', modelCode: 'a', params: { one: 'node1', two: 123 } },
        { id: '234', modelCode: 'b' }
      ]));

    spyOn(modelService, 'getModelTemplates').and.returnValue(
      of([
        { code: 'a', name: 'model A', params: [{ code: 'one', type: 'nodeId' }, { code: 'two', type: 'numeric' }] },
        { code: 'b', name: 'model B', params: [] }
      ])
    );

    const nodeService = TestBed.get(NodeService);
    spyOn(nodeService, 'getNodes').and.returnValue(of([{ id: 'node1' }, { id: 'node2' }, { id: 'node3' }]));

    component.ngOnInit();
    expect(modelService.getModels).toHaveBeenCalled();
    expect(modelService.getModelTemplates).toHaveBeenCalled();
    expect(nodeService.getNodes).toHaveBeenCalled();
    expect(component.models.length).toBe(2);
    expect(component.models[0].paramsLabels.length).toBe(2);
    expect(component.models[0].paramsLabels[0].node).toEqual({ id: 'node1' } as any);
  });

});
