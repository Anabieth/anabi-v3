import { Component, OnInit } from '@angular/core';
import { NgbTabChangeEvent } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-meas-system-eval',
  templateUrl: './meas-system-eval.component.html',
  styleUrls: ['./meas-system-eval.component.css']
})
export class MeasSystemEvalComponent implements OnInit {
  activeTab = 'tab-1';

  // IncBasic
  basicIncome = 0;
  numOfColonies = 20;
  prodPerColony = 25;
  honeyPrice = 4.5;

  // EXPinspection
  distToApiary = 50;
  fuelCons = 8;
  fuelPrice = 1.20;
  expFuel = 0;
  persons = [{ wage: 5.36 }];
  persTime = 10;
  travelTime = 120;
  expPerson = 0;
  numOfInspections = 12;


  // ExpDeath
  rateDeath = 20;
  pColony = 140;
  expDeath = 0;

  // ExpSwarm
  rateSwarm = 10;
  pSwarm = 106.25;

  profit = 0;

  // System config rates
  allHivesWithSystemRate = 0.25;
  oneHiveWithSystemRate = 0.15;
  customConfigRate = 0.2;
  deathRedSysRate = 0.2;

  inspectionsWithSystem = 7;
  cheapestMonSys = 289;
  baseMonSysExp = 300;
  installExpPerHive = 10;

  constructor() { }

  ngOnInit() {
  }

  // TODO: Check for negatives
  calcBasicIncome(myColProduction): number {
    const result = this.numOfColonies * myColProduction * this.honeyPrice;
    return result;
  }

  calcFuelExpenses(): number {
    this.expFuel = (this.distToApiary * 2) * this.fuelCons * this.fuelPrice / 100;
    return this.expFuel;
  }

  addPerson() {
    this.persons.push({ wage: null });
  }

  removePerson(p) {
    this.persons.splice(p, 1);
  }

  getTotalPersonHourlyWage(): number {
    let sum = 0;
    for (const p of this.persons) {
      sum += (p.wage);
    }
    return sum;
  }

  calcPersonExpenses(): number {
    this.expPerson = this.getTotalPersonHourlyWage() * (((this.persTime * this.numOfColonies) + this.travelTime) / 60.0);
    return this.expPerson;
  }

  calcInspectionExpenses(): number {
    const expInspection = this.calcFuelExpenses() + this.calcPersonExpenses();
    return expInspection;
  }

  calcInspectionExpYear(myNumOfInspections): number {
    const result = this.calcInspectionExpenses() * myNumOfInspections;
    return result;
  }

  calcDeathExpenses(): number {
    const result = this.numOfColonies * (this.rateDeath / 100) * this.pColony;
    return result;
  }

  calcDeathExpAdvanced(): number {
    const deathExp = this.calcDeathExpenses();
    const numOfDeadColonies = Math.round(this.getNumOfDeadCol(this.deathRedSysRate) * (this.rateDeath / 100));
    const reductionExpenses = numOfDeadColonies * this.pColony - this.calcInspectionExpenses();
    return deathExp - reductionExpenses;
  }

  calcSwarmExpenses(swarmRate): number {
    const result = this.getNumOfSwarmingCol(swarmRate) * this.pSwarm;
    return result;
  }

  calcTotalExpenses(forSystem): number {
    if (forSystem) {
      return this.roundHelper(this.calcInspectionExpYear(this.inspectionsWithSystem) +
        this.calcDeathExpAdvanced());
    }
    return this.calcInspectionExpYear(this.numOfInspections) + this.calcDeathExpenses() + this.calcSwarmExpenses(this.rateSwarm);
  }

  getAllHivesWithSysProd(): number {
    return this.prodPerColony + (this.prodPerColony * this.allHivesWithSystemRate);
  }

  getOneHiveWithSysProd(): number {
    return this.prodPerColony + (this.prodPerColony * this.oneHiveWithSystemRate);
  }

  getCustomSysProd(): number {
    return this.prodPerColony + (this.prodPerColony * this.customConfigRate);
  }

  getNumOfDeadCol(reductionRate): number {
    const result = this.numOfColonies * (this.rateDeath / 100);
    return Math.round(result - (result * reductionRate));
  }

  getNumOfSwarmingCol(swarmRate): number {
    return Math.ceil(this.numOfColonies * (swarmRate / 100));
  }

  calcProfit(forSystem, system): number {
    let colonyProduction = 0;
    switch (system) {
      case 0: colonyProduction = this.prodPerColony; break;
      case 1: colonyProduction = this.getAllHivesWithSysProd(); break;
      case 2: colonyProduction = this.getOneHiveWithSysProd(); break;
      case 3: colonyProduction = this.getCustomSysProd(); break;
    }

    const finalProfit = this.calcBasicIncome(colonyProduction) - this.calcTotalExpenses(forSystem);
    return finalProfit;
  }

  calcInstallCostsForAll(): number {
    return this.numOfColonies * this.cheapestMonSys;
  }

  calcInstallCostsForCustomConfig(): number {
    return this.baseMonSysExp + (this.installExpPerHive * this.numOfColonies);
  }

  roundHelper(value): number {
    return Math.round(value * 100) / 100;
  }

  checkForNegatives(value): number {
    if (value < 0) {
      return 0;
    } else {
      return value;
    }
  }

  onTabChange($event: NgbTabChangeEvent) {
    this.activeTab = $event.nextId;
  }
}
