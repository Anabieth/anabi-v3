import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReactiveFormsModule, FormsModule, FormArray, FormBuilder } from '@angular/forms';
import { BatteryLifeComponent } from './battery-life.component';


describe('BatteryLifeComponent', () => {
  let component: BatteryLifeComponent;
  let fixture: ComponentFixture<BatteryLifeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BatteryLifeComponent],
      imports: [
        FormsModule,
        ReactiveFormsModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BatteryLifeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be a valid form on init', () => {
    expect(component.myForm.valid).toBeTruthy();
  });

  it('should add power state', () => {
    component.myForm = new FormBuilder().group({
      batCapacity: 1000,
      dischrgSafety: 20,
      powerStates: new FormBuilder().array([new FormBuilder().group({ label: "t", duration: 1, timeUnit: "s", power: 25, powerUnit: "mA" }),
      new FormBuilder().group({ label: "t1", duration: 2, timeUnit: "s", power: 25, powerUnit: "mA" })])
    });
    component.addPowerState();

    expect((component.myForm.get('powerStates') as FormArray).length).toBe(3);
  })

  it('should be an invalid form after adding new power state', () => {
    component.addPowerState();
    expect(component.myForm.valid).toBeFalsy();
  });

  it('should be an invalid battery capacity', () => {
    let bCap = component.myForm.controls['batCapacity'];
    bCap.setValue(-1);
    expect(bCap.valid).toBeFalsy();
    expect(component.myForm.valid).toBeFalsy();
  });

  it('should be an invalid lower discharge safety value', () => {
    let dSafe = component.myForm.controls['dischrgSafety'];
    dSafe.setValue(-1);
    expect(dSafe.valid).toBeFalsy();
    expect(component.myForm.valid).toBeFalsy();
  });

  it('should be an invalid upper discharge safety value', () => {
    let dSafe = component.myForm.controls['dischrgSafety'];
    dSafe.setValue(101);
    expect(dSafe.valid).toBeFalsy();
    expect(component.myForm.valid).toBeFalsy();
  })

  it('should delete power state', () => {
    component.myForm = new FormBuilder().group({
      batCapacity: 1000,
      dischrgSafety: 20,
      powerStates: new FormBuilder().array([new FormBuilder().group({ label: "t", duration: 1, timeUnit: "s", power: 25, powerUnit: "mA" }),
      new FormBuilder().group({ label: "t1", duration: 2, timeUnit: "s", power: 25, powerUnit: "mA" })])
    });
    component.deletePowerState(0);
    let powerStates = (component.myForm.get('powerStates') as FormArray).value;
    expect(powerStates[0].label).toEqual("t1");
  });

  it('should calculate battery life', () => {
    component.myForm = new FormBuilder().group({
      batCapacity: 1900,
      dischrgSafety: 80,
      powerStates: new FormBuilder().array([new FormBuilder().group({ label: "Code execution", duration: 8.1, timeUnit: "s", power: 25, powerUnit: "mA" }),
      new FormBuilder().group({ label: "Sleep", duration: 30, timeUnit: "s", power: 30, powerUnit: "\xb5A" })])
    });
    component.calcBatteryLife();
    expect((component.myForm.get('powerStates') as FormArray).length).toBe(2);
    expect(component.batLifeInHours.toFixed(0)).toBe('285');
  });

});
