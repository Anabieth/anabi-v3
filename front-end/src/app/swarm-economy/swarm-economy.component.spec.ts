import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SwarmEconomyComponent } from './swarm-economy.component';
import { FormsModule } from '@angular/forms';

describe('SwarmEconomyComponent', () => {
  let component: SwarmEconomyComponent;
  let fixture: ComponentFixture<SwarmEconomyComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SwarmEconomyComponent],
      imports: [FormsModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SwarmEconomyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should calculate swarm value', () => {
    component.swarmValue = 20;
    expect(component.calcSwarmValue()).toBe(20);
    component.enableSingleBeeCalc = true;

    component.beeCount = 5000;
    component.pricePerBee = 0.005;
    component.queenPrice = 15;
    component.swarmFraction = 0.5;
    expect(component.calcSwarmValue()).toBe(27.5);
  });

  it('should calculate honey costs', () => {
    component.honeyLost = 12.5;
    component.honeyPrice = 2.5;
    expect(component.calcHoneyCosts()).toBe(31.25);
  });

  it('should calculate total swarm costs', () => {
    component.swarmValue = 10;
    component.honeyLost = 10;
    component.honeyPrice = 3.5;
    expect(component.calcTotalSwarmCosts()).toBe(45);
    component.enableSingleBeeCalc = true;

    component.beeCount = 5000;
    component.pricePerBee = 0.005;
    component.queenPrice = 15;
    component.swarmFraction = 0.5;
    expect(component.calcTotalSwarmCosts()).toBe(62.5);
  });

  it('should calculate costs per km', () => {
    component.fuelPrice = 1.2;
    component.fuelCons = 10;
    expect(component.getCostsPerKm()).toBe(0.12);
  });

  it('should calculate total person wage', () => {
    const pList = [{ wage: 8 }, { wage: 6 }, { wage: 5 }];
    component.persons = pList;
    expect(component.getTotalPersonHourlyWage()).toBe(19);

  });

  it('should calculate total travel costs', () => {
    component.persons = [{ wage: 5.36 }];
    component.catchTime = 3;
    expect(component.calcTotalTravelCosts()).toBe(36.4);

  });

  it('should add new person', () => {
    component.persons = [{ wage: 10 }];
    component.addPerson();
    expect(component.persons).toEqual([{ wage: 10 }, { wage: null }]);
  });

  it('should remove person', () => {
    const pList = [{ wage: 8 }, { wage: 6 }, { wage: 5 }];
    component.persons = pList;
    component.removePerson(1);
    expect(component.persons).toEqual([{ wage: 8 }, { wage: 5 }]);
  });

  it('should calculate benefit', () => {
    component.swarmValue = 20;
    component.enableSingleBeeCalc = false;
    component.honeyLost = 10;
    component.honeyPrice = 2;
    component.distanceOneWay = 10;
    component.fuelPrice = 1.2;
    component.fuelCons = 8;
    component.persons = [{ wage: 0 }];
    expect(component.calcBenefitIfSwarmCaught()).toBe(38.08);
  });

  it('should calculate loss when travelling for nothing', () => {
    component.swarmValue = 20;
    component.enableSingleBeeCalc = false;
    component.honeyLost = 10;
    component.honeyPrice = 2;
    component.distanceOneWay = 10;
    component.fuelPrice = 1.2;
    component.fuelCons = 8;
    component.persons = [{ wage: 0 }];
    expect(component.calcLossWhenTravelForNothing()).toBe(41.92);
  });

  it('should calculate travel time', () => {
    expect(component.calcTimeOnRoad()).toBe(2);
  });

  it('should calculate total time', () => {
    expect(component.calcTotalTime()).toBe(4);
  });

  it('should calculate total distance', () => {
    expect(component.calcTotalDistance()).toBe(100);
  });

  it('should calculate benefit by advanced economic model', () => {
    component.distanceOneWay = 140;
    expect(component.calcBenefitWithDistribution()).toBeCloseTo(25.32, 2);
  });

});
