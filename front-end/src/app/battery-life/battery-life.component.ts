import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';

@Component({
  selector: 'app-battery-life',
  templateUrl: './battery-life.component.html',
  styleUrls: ['./battery-life.component.css']
})
export class BatteryLifeComponent implements OnInit {

  myForm: FormGroup;
  batLifeInHours = 0;
  powerUnits: any = ['mA', '\xb5A'];
  timeUnits: any = ['s', 'min'];

  constructor(private fb: FormBuilder) { }

  ngOnInit() {
    this.initDefault();
  }

  initDefault() {
    this.myForm = this.fb.group({
      powerStates: this.fb.array([]),
      batCapacity: [1900, [
        Validators.required,
        Validators.min(0)]],
      dischrgSafety: [80, [
        Validators.required,
        Validators.min(0),
        Validators.max(100)
      ]]
    });
    this.powerStateForms.push(this.getInputGroup("Measure", 1.2, "s", 25, "mA"));
    this.powerStateForms.push(this.getInputGroup("WiFi power-up", 1.4, "s", 47, "mA"));
    this.powerStateForms.push(this.getInputGroup("WiFi connection", 2.3, "s", 69, "mA"));
    this.powerStateForms.push(this.getInputGroup("Data sending", 1.8, "s", 79, "mA"));
    this.powerStateForms.push(this.getInputGroup("Going into sleep", 1.4, "s", 36, "mA"));
    this.powerStateForms.push(this.getInputGroup("Deep sleep", 30, "s", 30, "\xb5A"));
    this.calcBatteryLife();
    this.myForm.valueChanges.subscribe(() => {
      if (this.myForm.valid) {
        this.calcBatteryLife();
      }
    });
  }

  get powerStateForms() {
    return this.myForm.get('powerStates') as FormArray;
  }

  get batCapacity() {
    return this.myForm.get('batCapacity');
  }

  get dischrgSafety() {
    return this.myForm.get('dischrgSafety');
  }

  getInputGroup(labelField, durationField, timeUnitField, powerField, powerUnitField) {
    return this.fb.group({ label: labelField, duration: [durationField, [Validators.required, Validators.min(0)]], timeUnit: [timeUnitField], power: [powerField, [Validators.required, Validators.min(0)]], powerUnit: [powerUnitField] });
  }

  addPowerState() {
    const powerState = this.fb.group({
      label: [''],
      duration: [null, [Validators.required, Validators.min(0)]],
      timeUnit: [this.timeUnits[0]],
      power: [null, [Validators.required, Validators.min(0)]],
      powerUnit: [this.powerUnits[0]]
    });

    this.powerStateForms.push(powerState);
  }

  deletePowerState(i) {
    this.powerStateForms.removeAt(i);
  }

  calcBatteryLife() {
    const pStates = this.powerStateForms;
    let powerSum = 0;
    let cycleCountPerHour = 0;
    let totalTime = 0;
    for (const p of pStates.value) {
      powerSum += (p.timeUnit === "min" ? p.duration * 60 : p.duration) * (p.powerUnit === "\xb5A" ? p.power / 1000 : p.power);
      totalTime += (p.timeUnit === "min" ? p.duration * 60 : p.duration);
    }

    //Convert to mAh
    powerSum /= 3600;
    cycleCountPerHour = 3600 / totalTime;

    this.batLifeInHours = (this.myForm.value.batCapacity * (this.myForm.value.dischrgSafety / 100)) / (powerSum * cycleCountPerHour);
  }
}
