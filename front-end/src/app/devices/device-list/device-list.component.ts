import { Component, OnInit } from '@angular/core';
import { DeviceService } from '../device.service';
import { SamsDevice } from '../device-domain';

@Component({
  selector: 'app-device-list',
  templateUrl: './device-list.component.html',
  styleUrls: ['./device-list.component.css']
})

export class DeviceListComponent implements OnInit {
  devices: SamsDevice[];

  constructor(private service: DeviceService) { }

  ngOnInit() {
    this.service.getDeviceList()
      .subscribe((data: SamsDevice[]) => {
        this.devices = data;
      });
  }

  toggleIsActive(dev: SamsDevice) {
    this.service.toggleDeviceIsActive(dev.id)
      .subscribe(newValue => dev.isActive = newValue);

  }
}
