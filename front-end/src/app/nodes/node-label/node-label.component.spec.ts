import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NodeLabelComponent } from './node-label.component';

describe('NodeLabelComponent', () => {
  let component: NodeLabelComponent;
  let fixture: ComponentFixture<NodeLabelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NodeLabelComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NodeLabelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
