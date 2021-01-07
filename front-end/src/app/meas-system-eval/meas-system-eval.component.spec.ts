import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MeasSystemEvalComponent } from './meas-system-eval.component';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

describe('MeasSystemEvalComponent', () => {
  let component: MeasSystemEvalComponent;
  let fixture: ComponentFixture<MeasSystemEvalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [MeasSystemEvalComponent],
      imports: [
        NgbModule,
        FormsModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MeasSystemEvalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should calculate basic income', () => {
    component.numOfColonies = 10;
    component.prodPerColony = 10;
    component.honeyPrice = 3.0;
    expect(component.calcBasicIncome(component.prodPerColony)).toBe(300);
  });

  it('should calculate fuel expenses', () => {
    component.distToApiary = 50;
    component.fuelCons = 6;
    component.fuelPrice = 1.5;
    expect(component.calcFuelExpenses()).toBe(9);
  });

  it('should add person', () => {
    const persList = [{ wage: 5 }];
    component.persons = persList;
    component.addPerson();
    expect(component.persons.length).toBe(2);
  });

  it('should remove person', () => {
    const persList = [{ wage: 5 }, { wage: 3 }];
    component.persons = persList;
    component.removePerson({ wage: 5 });
    expect(component.persons.length).toBe(1);
    expect(component.persons[0].wage).toBe(3);
  });

  it('should get total person hourly wage', () => {
    const persList = [{ wage: 5 }, { wage: 3 }, { wage: 2 }];
    component.persons = persList;
    expect(component.getTotalPersonHourlyWage()).toBe(10);
  });

  it('should help rounding decimals to two digits', () => {
    expect(component.roundHelper(1.666666666666)).toBe(1.67);
  });

  it('should calculate person expenses', () => {
    const persList = [{ wage: 5 }, { wage: 3 }];
    component.persons = persList;
    component.persTime = 5;
    component.numOfColonies = 20;
    component.travelTime = 10;
    expect(Math.round(component.calcPersonExpenses() * 100) / 100).toBe(14.67);
  });

  it('should calculate inspection expenses', () => {
    const persList = [{ wage: 5 }, { wage: 3 }];
    component.persons = persList;
    expect(component.calcInspectionExpenses().toFixed(2)).toBe('52.27');
  });

  it('should calculate inspection expenses per year', () => {
    component.numOfInspections = 10;
    expect(component.calcInspectionExpYear(component.numOfInspections).toFixed(2)).toBe('381.87');
  });

  it('should calculate colony death expenses', () => {
    component.numOfColonies = 15;
    expect(component.calcDeathExpenses()).toBe(420);
  });

  it('should calculate colony death expenses with systems', () => {
    component.numOfColonies = 25;
    expect(component.calcDeathExpAdvanced().toFixed(2)).toBe('602.65');
  });

  it('should calculate colony swarm expenses', () => {
    component.numOfColonies = 12;
    component.pSwarm = 80;
    expect(component.calcSwarmExpenses(component.rateSwarm)).toBe(160);
  });

  it('should calculate total expenses', () => {
    component.numOfInspections = 10;
    component.numOfColonies = 10;
    component.inspectionsWithSystem = 10;
    expect(component.calcTotalExpenses(false).toFixed(2)).toBe('678.78');
    expect(component.calcTotalExpenses(true).toFixed(2)).toBe('601.79');
  });

  it('should calculate profit', () => {
    component.numOfColonies = 10;
    expect(component.calcProfit(false, 0).toFixed(2)).toBe('387.71');
    expect(component.calcProfit(true, 1).toFixed(2)).toBe('892.22');
    expect(component.calcProfit(false, 2).toFixed(2)).toBe('556.46');
    expect(component.calcProfit(true, 3).toFixed(2)).toBe('835.97');
  });

  it('should get production, when all hives equipped', () => {
    component.prodPerColony = 15;
    expect(component.getAllHivesWithSysProd().toFixed(2)).toBe('18.75');
  });

  it('should get production, when one hive equipped', () => {
    component.prodPerColony = 15;
    expect(component.getOneHiveWithSysProd().toFixed(2)).toBe('17.25');
  });

  it('should get production, when custom config', () => {
    component.prodPerColony = 15;
    expect(component.getCustomSysProd().toFixed(2)).toBe('18.00');
  });

  it('should get number of dead colonies', () => {
    component.numOfColonies = 10;
    expect(component.getNumOfDeadCol(0.2)).toBe(2);
  });

  it('should calculate install costs for all hives', () => {
    component.numOfColonies = 10;
    expect(component.calcInstallCostsForAll()).toBe(2890);
  });

  it('should calculate install costs for custom config', () => {
    component.numOfColonies = 15;
    expect(component.calcInstallCostsForCustomConfig()).toBe(450);
  });

});
