import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DeviceService } from '../device.service';
import { switchMap, tap } from 'rxjs/operators';
import { SamsDeviceEvents } from '../device-domain';
import { NgbTabChangeEvent } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-device-events',
  templateUrl: './device-events.component.html',
  styleUrls: ['./device-events.component.css']
})
export class DeviceEventsComponent implements OnInit {

  activeTab: string;
  device: SamsDeviceEvents;

  constructor(private route: ActivatedRoute,
    private service: DeviceService,
    private router: Router
  ) { }

  ngOnInit() {
    this.route.paramMap
      .pipe(switchMap(params => this.service.getDeviceEvents(params.get('deviceId'))))
      .subscribe(data => this.device = data);

    this.route.fragment
      .subscribe(frag => this.activeTab = frag || 'all');
  }

  public onTabChange(event: NgbTabChangeEvent) {
    this.router.navigate([], { fragment: event.nextId });
  }

  public getLogRowClass(level: string): string {
    switch (level) {
      case 'error':
        return 'text-danger';
      case 'warning':
        return 'text-warning';
      case 'info':
        return 'text-success';
      case 'debug':
        return 'text-secondary';
      default:
        return null;
    }
  }

  public getResultIconClass(result: string): string {
    switch (result) {
      case 'NotFound':
      case 'AccessDenied':
      case 'CoreTemporarilyUnavailable':
        return 'fa-exclamation-triangle';
      case 'Ok':
        return 'fa-check';
      default:
        return null;
    }
  }

  public getResultTextClass(result: string): string {
    switch (result) {
      case 'NotFound':
      case 'AccessDenied':
        return 'text-danger';
      case 'Ok':
        return 'text-success';
      default:
        return 'text-warning';
    }
  }

}
