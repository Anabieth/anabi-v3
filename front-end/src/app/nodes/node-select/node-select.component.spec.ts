import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NodeSelectComponent } from './node-select.component';
import { SamsNode, SamsNodeType } from '../nodes.domain';
import { NgbTypeaheadModule, NgbTypeaheadSelectItemEvent } from '@ng-bootstrap/ng-bootstrap';
import { EMPTY } from 'rxjs';
import { SimpleChange } from '@angular/core';

describe('NodeSelectComponent', () => {
  let component: NodeSelectComponent;
  let fixture: ComponentFixture<NodeSelectComponent>;
  let testNodes: SamsNode[];

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [NodeSelectComponent],
      imports: [NgbTypeaheadModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NodeSelectComponent);
    component = fixture.componentInstance;

    testNodes = [
      new SamsNode('One', SamsNodeType.Hive, '1', null),
      new SamsNode('One child 1', SamsNodeType.HiveElement, '11', '1'),
      new SamsNode('One child 2', SamsNodeType.HiveElement, '12', '1'),
      new SamsNode('Two', SamsNodeType.Hive, '2', null),
      new SamsNode('Two child 1', SamsNodeType.HiveElement, '21', '2'),
      new SamsNode('Two child 2', SamsNodeType.HiveElement, '22', '2'),
      new SamsNode('Three', SamsNodeType.Hive, '3', null),
      new SamsNode('Three child 1', SamsNodeType.HiveElement, '31', '3'),
      new SamsNode('Three child 2', SamsNodeType.HiveElement, '32', '3')
    ];

    testNodes[4].location = 'Room 307';
    testNodes[5].location = 'Room 307';

    component.nodes = testNodes;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should parse node list', () => {
    expect(component['_nodes'].length).toBe(9);
    expect(component['_nodeHierarchy'].length).toBe(9);
    expect(component['_nodeHierarchy'][0]['level']).toBe(0);
    expect(component['_nodeHierarchy'][1]['level']).toBe(1);
    expect(component['_nodeHierarchy'][3]['level']).toBe(0);
    expect(component['_nodeHierarchy'][4]['level']).toBe(1);
  });

  it('should show hierarchy when term is empty', () => {
    component.searchNode$(EMPTY).subscribe(res => {
      expect(res.length).toBe(9);
      expect(res[1]['level']).toBe(1);
    });

    component.manualEvents$.next('');
  });

  describe('should search for nodes', () => {
    function setupCheck(count: number) {
      component.searchNode$(EMPTY).subscribe(res => {
        expect(res.length).toBe(count);
      });
    }

    it('by name', () => {
      setupCheck(3);
      component.manualEvents$.next('CHILD 1');
    });

    it('by type', () => {
      setupCheck(6);
      component.manualEvents$.next('ELemeNT');
    });

    it('by location', () => {
      setupCheck(2);
      component.manualEvents$.next('room');
    });

    it('with max results', () => {
      component.maxResults = 2;
      setupCheck(2);
      component.manualEvents$.next('child');
    });

    it('without duplicates', () => {
      setupCheck(9);
      component.manualEvents$.next('i');
    });

    it('with excluded nodes', () => {
      component.excludedNodes = [testNodes[0]];
      setupCheck(2);
      component.manualEvents$.next('one');
    });
  });

  describe('should format input on changes', () => {
    function change() {
      component.ngOnChanges({
        value: new SimpleChange(null, new SamsNode('new value', SamsNodeType.Other, '123'), false)
      });
    }

    it('with default formatter', () => {
      change();
      expect(component.inputValue).toEqual('new value');

    });

    it('with custom formatter', () => {
      component.nodeInputFormatter = (node) => node.id + ' custom format';
      change();
      expect(component.inputValue).toEqual('123 custom format');
    });

    it('with empty value', () => {
      component.ngOnChanges({
        value: null
      });
      expect(component.inputValue).toEqual('');
    });
  });

  it('should emit event when node item is selected', () => {
    component.nodeSelected.subscribe((next: SamsNode) => {
      expect(next.name).toEqual('Two child 2');
    });

    component.nodeItemSelected({ item: testNodes[8] } as NgbTypeaheadSelectItemEvent);
  });

});
