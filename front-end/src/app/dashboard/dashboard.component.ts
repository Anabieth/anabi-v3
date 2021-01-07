import { Component, OnInit } from '@angular/core';
import { NodeService } from '../nodes/node.service';
import { SamsNode, SamsNodeType, SamsNodeUtils } from '../nodes/nodes.domain';
import { forkJoin } from 'rxjs';
import { tap } from 'rxjs/operators';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Router } from '@angular/router';
import { LastMeasurementsModalComponent } from '../dashboard/last-measurements-modal/last-measurements-modal.component';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  hives: SamsNode[];

  getTypeIcon = SamsNodeUtils.getTypeIconClass;
  getValueIcon = SamsNodeUtils.getValueIconClass;
  getValueUnit = SamsNodeUtils.getValueUnit;

  constructor(
    private service: NodeService,
    private modalService: NgbModal,
    private router: Router
  ) { }

  ngOnInit() {
    this.service.getNodes().subscribe(nodes => {
      this.hives = nodes.filter(node => node.type === SamsNodeType.Hive);

      this.hives.forEach(hive => {
        hive.children = this.getChildren(nodes, hive.id);
        this.fetchLatestValues(hive);
      });
    });
  }

  fetchLatestValues(hive: SamsNode) {
    hive.updateInProgress = true;
    let updateTs: string = new Date(0).toISOString();

    forkJoin(
      [hive, ...hive.children]
        .map(node => this.service.getLatestValues(node.id)
          .pipe(
            tap(data => {
              node.latestMeasurements = data.latestMeasurements || [];
              node.latestModelValues = data.latestModelValues || [];

              node.isActive = false;
              [...node.latestModelValues, ...node.latestMeasurements]
                .forEach(v => {
                  v.isRecent = this.isRecent(v.timestamp);
                  node.isActive = node.isActive || v.isRecent;
                  if (v.timestamp > updateTs) {
                    updateTs = v.timestamp;
                  }
                });
            })
          )
        )
    ).subscribe(() => {
      hive.updateTs = updateTs;
      hive.updatedRecenlty = this.isRecent(hive.updateTs);
      hive.updateInProgress = false;
    });
  }

  private getChildren(allNodes: SamsNode[], parentId: string): SamsNode[] {
    const children = allNodes
      .filter(n => n.parentId === parentId)
      .map(one => {
        const grandchildren = this.getChildren(allNodes, one.id);
        return [one].concat(...grandchildren);
      });

    return [].concat(...children);
  }

  private isRecent(val: Date | number | string): boolean {
    return (new Date().getTime() - new Date(val).getTime()) < 12 * 60 * 60 * 1000;
  }

  openReports(hive: SamsNode) {
    this.router.navigate(['report'],
      {
        queryParams: {
          n: [hive.id, ...hive.children.map(c => c.id)],
          ra: true
        }
      });

  }

  openLastMeasurementsDialog(node: SamsNode) {
    if (node.latestMeasurements && node.latestMeasurements.length > 0) {
      const ref = this.modalService.open(LastMeasurementsModalComponent);
      ref.componentInstance.node = node;
    }
  }
}
