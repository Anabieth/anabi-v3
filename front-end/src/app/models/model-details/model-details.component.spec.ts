import { async, ComponentFixture, TestBed, tick, fakeAsync } from '@angular/core/testing';

import { ModelDetailsComponent } from './model-details.component';
import { ModelService } from '../model.service';
import { EMPTY, of } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NodeService } from 'src/app/nodes/node.service';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { SamsNode } from 'src/app/nodes/nodes.domain';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

describe('ModelDetailsComponent', () => {
  let component: ModelDetailsComponent;
  let fixture: ComponentFixture<ModelDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        FormsModule,
        ReactiveFormsModule
      ],
      declarations: [ModelDetailsComponent],
      providers: [
        {
          provide: ModelService,
          useValue: {
            getModelTemplates: () => EMPTY,
            getModel: () => EMPTY,
            saveModel: () => EMPTY,
            deleteModel: () => EMPTY
          }
        },
        {
          provide: NgbModal,
          useValue: {
            open: () => null
          }
        },
        {
          provide: NodeService,
          useValue: {
            getNodes: () => EMPTY
          }
        },
        {
          provide: ActivatedRoute,
          useValue: {
            data: EMPTY,
            paramMap: EMPTY
          }
        }
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ModelDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should init for new model', () => {
    const route = TestBed.get(ActivatedRoute);
    const nodeService = TestBed.get(NodeService);
    spyOn(nodeService, 'getNodes').and.returnValue(of([{} as SamsNode]));

    const modelService = TestBed.get(ModelService);
    spyOn(modelService, 'getModelTemplates').and.returnValue(
      of([{ code: 'A', params: [] }, { code: 'B', params: [] }])
    );

    route.data = of({ isNew: true });
    route.queryParamMap = of(convertToParamMap({ m: 'A' }));
    component.ngOnInit();
    expect(component.selectedModel.code).toBe('A');
  });

  it('should init for existing model', () => {
    const route = TestBed.get(ActivatedRoute);
    const nodeService = TestBed.get(NodeService);
    spyOn(nodeService, 'getNodes').and.returnValue(of([{} as SamsNode]));

    const modelService = TestBed.get(ModelService);
    spyOn(modelService, 'getModelTemplates').and.returnValue(
      of([{ code: 'A', params: [] }, { code: 'B', params: [] }])
    );

    spyOn(modelService, 'getModel').and.returnValue(
      of({ id: '123', modelCode: 'A', params: [] })
    );

    route.data = of({ isNew: false });
    route.paramMap = of(convertToParamMap({ modelId: '123' }));
    component.ngOnInit();
    expect(component.modelForm.get('modelCode').value).toBe('A');
    expect(modelService.getModel).toHaveBeenCalledWith('123');
  });

  it('should save model', () => {
    const router = TestBed.get(Router);
    spyOn(router, 'navigate');

    const modelService = TestBed.get(ModelService);
    spyOn(modelService, 'saveModel').and.returnValue(of({}));

    const value = { id: 123, modelCode: 'A', params: [] };
    component.modelForm.patchValue(value);

    component.saveClicked();

    expect(modelService.saveModel).toHaveBeenCalledWith(value);
    expect(router.navigate).toHaveBeenCalled();
  });

  it('should delete model', fakeAsync(() => {
    const router = TestBed.get(Router);
    spyOn(router, 'navigate');

    const modelService = TestBed.get(ModelService);
    spyOn(modelService, 'deleteModel').and.returnValue(of({}));

    const modal = TestBed.get(NgbModal);
    spyOn(modal, 'open').and.returnValue({ result: Promise.resolve(1)});

    component.deleteClicked({});
    tick();

    expect(modal.open).toHaveBeenCalled();
    expect(modelService.deleteModel).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalled();
  }));
});
