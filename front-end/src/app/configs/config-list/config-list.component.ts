import { Component, OnInit } from '@angular/core';
import { ConfigService } from '../config.service';
import { Config } from '../config.domain';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-config-list',
  templateUrl: './config-list.component.html',
  styleUrls: ['./config-list.component.css']
})
export class ConfigListComponent implements OnInit {

  configs: Config[] = [];

  constructor(
    private service: ConfigService
  ) { }

  ngOnInit() {
    this.service.getConfigs().subscribe(list => this.configs = list);
  }

  defaultClicked(config) {
    this.service.setDefaultConfig(config).pipe(
      switchMap(() => this.service.getConfigs())
    ).subscribe(list => this.configs = list);
  }

}
