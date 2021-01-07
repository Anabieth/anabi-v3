import { Component, OnInit } from '@angular/core';
import * as mathjs from 'mathjs';


@Component({
  selector: 'app-swarm-economy',
  templateUrl: './swarm-economy.component.html',
  styleUrls: ['./swarm-economy.component.css']
})
export class SwarmEconomyComponent implements OnInit {

  swarmValue = 50;
  honeyLost = 12.5;
  honeyPrice = 4.5;
  swarmResult = 0;

  distanceOneWay = 50;

  fuelPrice = 1.2;
  fuelCons = 8;
  kmAllowance = 0.42;
  avgSpeed = 50;
  timeOnRoad = this.calcTimeOnRoad();
  catchTime = 2;
  persons = [{ wage: 5.36 }];

  beeCount = 15000;
  pricePerBee = 0.006;
  queenPrice = 20;
  swarmFraction = 0.75;

  enableSingleBeeCalc = false;
  enablePerKmCalc = false;
  distributionMean = 300;
  distributionStd = 50;

  constructor() { }

  ngOnInit() {
  }

  calcTotalSwarmCosts(): number {
    if (this.swarmValue < 0) {
      this.swarmValue = 0;
    }
    return this.calcSwarmValue() + this.calcHoneyCosts();
  }

  calcSwarmValue(): number {
    if (this.enableSingleBeeCalc) {
      return (this.beeCount * this.swarmFraction * this.pricePerBee) + this.queenPrice;
    } else {
      return this.swarmValue;
    }
  }

  calcHoneyCosts(): number {
    return this.honeyLost * this.honeyPrice;
  }

  calcTotalTravelCosts(): number {
    if (!this.enablePerKmCalc) {
      return (this.calcTotalDistance() * this.getCostsPerKm()) + (this.calcTotalTime() * this.getTotalPersonHourlyWage());
    } else {
      return (this.kmAllowance * this.calcTotalDistance()) + (this.calcTotalTime() * this.getTotalPersonHourlyWage());
    }
  }

  getTotalPersonHourlyWage(): number {
    let sum = 0;
    for (const p of this.persons) {
      sum += (p.wage);
    }
    return sum;
  }

  getCostsPerKm(): number {
    return (this.fuelPrice * this.fuelCons) / 100;
  }

  addPerson() {
    this.persons.push({ wage: null });
  }

  removePerson(p) {
    this.persons.splice(p, 1);
  }

  calcBenefitIfSwarmCaught(): number {
    return (this.calcTotalSwarmCosts() - this.calcTotalTravelCosts());
  }

  calcLossWhenTravelForNothing(): number {
    return this.calcTotalTravelCosts() + this.calcTotalSwarmCosts();
  }

  calcTimeOnRoad(): number {
    return this.calcTotalDistance() / this.avgSpeed;
  }

  calcTotalTime(): number {
    return this.calcTimeOnRoad() + this.catchTime;
  }

  calcTotalDistance(): number {
    return this.distanceOneWay * 2;
  }

  cdfNormal(mean, std, distance): number {
    const erf: number = mathjs.erf((mean - distance) / (Math.sqrt(2) * std)) as number;
    return (1 - erf) / 2;
  }

  calcBenefitWithDistribution(): number {
    const cDistribution = this.cdfNormal(this.distributionMean, this.distributionStd, this.calcTotalDistance());
    return (this.calcTotalSwarmCosts() - this.calcTotalTravelCosts()) * (1 - cDistribution);
  }

}
