import { Component, OnInit } from '@angular/core';
import { Validators, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { SamsNodeUtils, SamsNodeType, SamsNode, SourceMapping } from '../nodes.domain';
import { NodeService } from '../node.service';
import { forkJoin } from 'rxjs';
import { debounceTime, map, switchMap, distinctUntilChanged, tap } from 'rxjs/operators';


interface ParamPreview {
  sourceId?: string;
  status?: 'ok' | 'nok' | 'pending';
}

interface NodePreview {
  name?: string;
  type: SamsNodeType;
  params?: { [key: string]: ParamPreview };
}

@Component({
  selector: 'app-sams-node',
  templateUrl: './sams-node.component.html',
  styleUrls: ['./sams-node.component.css']
})
export class SamsNodeComponent implements OnInit {

  samsForm = this.fb.group({
    baseName: ['', Validators.required],
    clientId: ['', Validators.required],
    location: [''],
    root: ['root']
  });

  root: NodePreview = { type: SamsNodeType.Other };
  hive: NodePreview = { type: SamsNodeType.Hive, params: { temperature: {}, audio: {}, weight: {} } };
  device: NodePreview = { type: SamsNodeType.Device, params: { clientId: {} } };
  outside: NodePreview = { type: SamsNodeType.HiveElement, params: { temperature: {}, humidity: {} } };

  getIcon = SamsNodeUtils.getTypeIconClass;

  existingNodes: SamsNode[] = [new SamsNode('(root)', SamsNodeType.Other, 'root')];

  resultErorr = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private service: NodeService
  ) { }

  ngOnInit() {
    this.service.getNodes().subscribe(nodes => {
      this.existingNodes.push(...this.getChildrenNodes(nodes, null));
    });

    this.samsForm.valueChanges.subscribe(val => this.updatePreview(val));

    this.samsForm.get('clientId').valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      tap(() => this.samsForm.setErrors({ checkingSources: true })),
      switchMap(() => forkJoin(
        [this.hive.params, this.outside.params]
          .map(p => Object.values(p))
          .reduce((acc, val) => acc.concat(val), [])
          .map(v => {
            v.status = 'pending';
            return this.service.isSourceIdUsed('none', v.sourceId)
              .pipe(
                map(res => res.isUsed),
                tap(isUsed => v.status = isUsed ? 'nok' : 'ok'),
            );
          })
      )),
      map(sources => sources.indexOf(true) > -1),
    ).subscribe(hasErrors => {
      this.samsForm.setErrors(hasErrors ? { sourcesUsed: true } : null);
    });

    this.updatePreview(this.samsForm.value);
  }

  private getChildrenNodes(allNodes: SamsNode[], parentId: string): SamsNode[] {
    const result = [];
    const nodes = allNodes.filter(n => n.parentId == null && parentId == null || n.parentId === parentId);
    nodes.forEach(n => {
      result.push(n);
      const children = this.getChildrenNodes(allNodes, n.id);
      children.forEach(child => child.name = '- ' + child.name);
      result.push(...children);
    });
    return result;
  }

  private updatePreview(newValue) {
    const baseName = newValue.baseName || '[base name]';
    const clientId = newValue.clientId || '[client ID]';
    this.hive.name = baseName;
    this.hive.params['temperature'].sourceId = 'ds18b20-0-' + clientId;
    this.hive.params['audio'].sourceId = 'audio-' + clientId;
    this.hive.params['weight'].sourceId = 'scale-' + clientId;

    this.device.name = baseName + ' RaspberryPi';
    this.device.params['clientId'].sourceId = clientId + '@clients';

    this.outside.name = baseName + ' outside';
    this.outside.params['temperature'].sourceId = 'dht22-temperature-' + clientId;
    this.outside.params['humidity'].sourceId = 'dht22-humidity-' + clientId;

    const root = this.existingNodes.find(n => newValue.root === 'root' && n.id == null || n.id === newValue.root);
    this.root.name = root.name;
    this.root.type = root.type;
  }

  getStatusIcon(status) {
    switch (status) {
      case 'ok':
        return 'fa-check-circle text-success';
      case 'nok':
        return 'fa-times-circle text-danger';
      case 'pending':
        return 'fa-spinner fa-spin text-warning';
    }
  }

  registerClicked() {
    this.resultErorr = null;

    const getSamsNode = (preview: NodePreview, parent: string) => {
      const node = new SamsNode(preview.name, preview.type, null);
      node.location = this.samsForm.get('location').value;
      if (parent !== 'root') {
        node.parentId = parent;
      }

      if (preview.type === SamsNodeType.Device) {
        node.clientId = preview.params['clientId'].sourceId;
      }

      return node;
    };

    const getMappings = (preview: NodePreview): SourceMapping[] => {
      return Object.entries(preview.params)
        .map(param => ({ sourceId: param[1].sourceId, valueKey: param[0] } as SourceMapping));
    };

    let hiveId;

    this.service.saveNode(getSamsNode(this.hive, this.samsForm.get('root').value))
      .pipe(
        tap(saved => hiveId = saved.id),
        switchMap(hive => forkJoin(
          this.service.saveNodeMappings(hive.id, getMappings(this.hive)),
          this.service.saveNode(getSamsNode(this.device, hive.id)),
          this.service.saveNode(getSamsNode(this.outside, hive.id))
            .pipe(
              switchMap(saved => this.service.saveNodeMappings(saved.id, getMappings(this.outside)))
            )
        ))
      )
      .subscribe(
        () => this.router.navigate(['/nodes', hiveId]),
        err => {
          console.log(err);
          this.resultErorr = err.error;
          this.service.deleteNode({ id: hiveId } as SamsNode).subscribe();
        });
  }

  cancelClicked() {
    this.router.navigate(['/nodes']);
  }

}
