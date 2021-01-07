import { async, ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';

import { NodeListComponent } from './node-list.component';
import { NodeService } from '../node.service';
import { of } from 'rxjs';
import { SamsNode } from '../nodes.domain';
import { By } from '@angular/platform-browser';
import { RouterTestingModule } from '@angular/router/testing';
import { Router, ActivatedRoute } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { NodeLabelComponent } from '../node-label/node-label.component';

describe('NodeListComponent', () => {
  let component: NodeListComponent;
  let fixture: ComponentFixture<NodeListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [NodeListComponent, NodeLabelComponent],
      providers: [
        {
          provide: NodeService,
          useValue: {
            getNodes: () => of([]),
            deleteNode: (node) => of('ok'),
            saveNode: (node) => of(Object.assign(new SamsNode(), node))
          }
        }
      ],
      imports: [
        FormsModule,
        RouterTestingModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NodeListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should parse nodes on startup', () => {
    const nodes = [
      new SamsNode('A', null, '1', null),
      new SamsNode('Aa', null, '11', '1'),
      new SamsNode('Ab', null, '12', '1'),
      new SamsNode('Ab1', null, '121', '12'),
      new SamsNode('B', null, '2', null),
      new SamsNode('Ba', null, '21', '2'),
    ];
    const service = TestBed.get(NodeService);
    spyOn(service, 'getNodes').and.returnValue(of(nodes));

    component.ngOnInit();

    expect(service.getNodes).toHaveBeenCalled();
    expect(component.nodes).toEqual([nodes[0], nodes[4]]);
    expect(nodes[0].children).toEqual([nodes[1], nodes[2]]);
    expect(nodes[1].children).toEqual([]);
    expect(nodes[2].children).toEqual([nodes[3]]);
    expect(nodes[3].children).toEqual([]);
    expect(nodes[4].children).toEqual([nodes[5]]);
    expect(nodes[5].children).toEqual([]);
  });

  it('should edit node', () => {
    expect(fixture.debugElement.query(By.css('.sidebar > .card'))).toBeFalsy();

    const node = new SamsNode('Test node', null, '123');
    component.editClicked(node);

    expect(component.currentNode).not.toBe(node);
    expect(component.currentNode.name).toEqual(node.name);
    expect(component.currentNode.id).toEqual(node.id);

    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('.sidebar > .card'))).toBeTruthy();
  });

  it('should save node', () => {
    const node = new SamsNode('Node to save', null, '555');
    component.currentNode = node;
    const service = TestBed.get(NodeService);
    spyOn(service, 'saveNode').and.callThrough();
    spyOn(service, 'getNodes').and.returnValue(of([Object.assign(new SamsNode(), node)]));
    const route = TestBed.get(ActivatedRoute);
    spyOn(route.snapshot.queryParamMap, 'get').and.returnValue('555');

    component.saveClicked();

    expect(service.saveNode).toHaveBeenCalled();
    expect(service.getNodes).toHaveBeenCalled();
    expect(route.snapshot.queryParamMap.get).toHaveBeenCalledWith('n');
    expect(component.currentNode).not.toBe(node);
    expect(component.currentNode.name).toEqual(node.name);
    expect(component.currentNode.id).toEqual(node.id);
  });

  it('should delete node', fakeAsync(() => {
    component.currentNode = new SamsNode('Node to delete', null, '987');
    const service = TestBed.get(NodeService);
    spyOn(service, 'deleteNode').and.callThrough();
    spyOn(service, 'getNodes').and.callThrough();
    const route = TestBed.get(ActivatedRoute);
    spyOn(route.snapshot.queryParamMap, 'get').and.returnValue(null);

    const modal = TestBed.get(NgbModal);
    spyOn(modal, 'open').and.returnValue({
      result: new Promise((resolve, reject) => {
        resolve(1);
      })
    });

    component.deleteClicked();
    tick();

    expect(service.deleteNode).toHaveBeenCalled();
    expect(service.getNodes).toHaveBeenCalled();
    expect(component.currentNode).toBeNull();
  }));

  it('should update query param when current node is changed', () => {
    const router = TestBed.get(Router);
    spyOn(router, 'navigate').and.callThrough();

    component.currentNode = new SamsNode('A node', null, 'a-node-id');
    expect(router.navigate).toHaveBeenCalledWith([], { queryParams: { n: 'a-node-id' } });

    component.currentNode = null;
    expect(router.navigate).toHaveBeenCalledWith([], { queryParams: { n: null } });
  });

  it('should open report', () => {
    const router = TestBed.get(Router);
    spyOn(router, 'navigate');

    component.openReport('node-123');

    expect(router.navigate).toHaveBeenCalledWith(['report'], { queryParams: { n: 'node-123', ra: true } });
  });
});
