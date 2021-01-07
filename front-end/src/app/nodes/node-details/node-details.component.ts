import { Component, OnInit } from '@angular/core';
import { NodeService } from '../node.service';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { SamsNodeDetails, SamsNodeType, SourceMapping, SamsNodeUtils } from '../nodes.domain';
import { enumToMap } from '../../utils';
import {
  FormGroup, FormControl, FormBuilder, FormArray, Validators, ValidatorFn, ValidationErrors,
  AsyncValidatorFn
} from '@angular/forms';
import { Observable, of, timer } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { ConfigService } from '../../configs/config.service';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-node-details',
  templateUrl: './node-details.component.html',
  styleUrls: ['./node-details.component.css']
})
export class NodeDetailsComponent implements OnInit {

  private _node: SamsNodeDetails = new SamsNodeDetails();
  valueKeys: string[] = [];

  hwConfigs: any[];

  get node() {
    return this._node;
  }

  set node(val) {
    this._node = val;
    val.isActive = val.isActive == null ? true : val.isActive;
    this.nodeForm.reset(val);
  }

  private _mappings: SourceMapping[] = [];

  get mappings() {
    return this._mappings;
  }

  set mappings(val) {
    this._mappings = val;

    while (this.mappingItems.length > 0) {
      this.mappingItems.removeAt(0);
    }

    val.forEach(m =>
      this.mappingItems.push(this.createMappingItem(m))
    );
  }

  get mappingItems() {
    return this.mappingForm.get('items') as FormArray;
  }

  nodeTypes = enumToMap(SamsNodeType);
  getIcon = SamsNodeUtils.getTypeIconClass;

  nodeForm = this.fb.group({
    name: ['', Validators.required],
    type: ['', Validators.required],
    location: [''],
    clientId: [''],
    isActive: [true],
    hwConfigId: ['']
  });

  mappingForm = new FormGroup({ // replace with this.fb.group in Angular v7.+
    items: this.fb.array([])
  }, {
      validators: [
        this.getDuplicateValueValidator('sourceId'),
        this.getDuplicateValueValidator('valueKey')
      ]
    });

  isDevice = false;

  constructor(
    private service: NodeService,
    private configService: ConfigService,
    private auth: AuthService,
    private route: ActivatedRoute,
    private fb: FormBuilder
  ) { }

  ngOnInit() {
    const nodeId$ = this.route.paramMap.pipe(
      map((params: ParamMap) => params.get('nodeId'))
    );

    nodeId$.pipe(switchMap(id => this.service.getNodeDetails(id)))
      .subscribe(node => this.node = node);

    nodeId$.pipe(switchMap(id => this.service.getNodeMappings(id)))
      .subscribe(mappings => this.mappings = mappings);

    nodeId$.pipe(switchMap(id => this.service.getMappingValueKeys(id)))
      .subscribe(keys => {
        this.valueKeys = keys;
        this.mappingItems.controls.forEach(c => c.get('valueKey').updateValueAndValidity());
      });

    this.nodeForm.get('type').valueChanges
      .subscribe(newValue => {
        if (newValue === SamsNodeType.Device) {
          this.isDevice = true;
          this.nodeForm.get('clientId').enable();
          this.nodeForm.get('isActive').enable();
          this.nodeForm.get('hwConfigId').enable();
        } else {
          this.isDevice = false;
          this.nodeForm.get('clientId').disable();
          this.nodeForm.get('isActive').disable();
          this.nodeForm.get('hwConfigId').disable();
        }
      });

    this.nodeForm.get('isActive').valueChanges
      .subscribe(active => {
        const control = this.nodeForm.get('clientId');
        if (active) {
          control.setValidators([Validators.required]);
        } else {
          control.clearValidators();
        }
        control.updateValueAndValidity();
      });

    if (this.hwEngineer) {
      this.configService.getConfigs().subscribe(list => this.hwConfigs = list);
    }
  }

  get hwEngineer(): boolean {
    return this.auth.userHasScopes(['hw']);
  }

  private getDuplicateValueValidator(key: string): ValidatorFn {
    return (control: FormGroup): ValidationErrors | null => {
      const duplicates: string[] = [];
      const values: string[] = control.get('items').value.map(v => v[key]).filter(v => v !== null);

      values.forEach((v, i) => {
        if (values.indexOf(v, i + 1) > -1) {
          if (duplicates.indexOf(v) === -1) {
            duplicates.push(v);
          }
        }
      });

      if (duplicates.length > 0) {
        const result = {};
        result[key + 'Duplicates'] = duplicates.sort();
        return result;
      }

      return null;
    };
  }

  private sourceIdValidator: AsyncValidatorFn = (control: FormControl): Observable<any> | null => {
    if (control.dirty) {
      return timer(500)
        .pipe(
          switchMap(() => this.service.isSourceIdUsed(this.node.id, control.value)),
          map(res => res.isUsed ? res : null)
        );
    } else {
      return of(null);
    }
  }

  private valueKeyWarning: ValidatorFn = (control: FormControl): ValidationErrors | null => {
    if (this.valueKeys.length > 0 && this.valueKeys.indexOf(control.value) < 0) {
      control['valueKeyNotSupported'] = true;
    } else {
      control['valueKeyNotSupported'] = null;
    }

    return null;
  }

  createMappingItem(sourceMap: SourceMapping): FormGroup {
    return this.fb.group({
      sourceId: [sourceMap.sourceId, Validators.required, this.sourceIdValidator],
      valueKey: [sourceMap.valueKey, [Validators.required, this.valueKeyWarning]]
    });
  }

  addItemClicked() {
    this.mappingItems.push(this.createMappingItem(new SourceMapping()));
    this.mappingForm.markAsDirty();
  }

  deleteItemClicked(idx: number) {
    this.mappingItems.removeAt(idx);
    this.mappingForm.markAsDirty();
  }

  saveClicked() {
    if (this.nodeForm.dirty) {
      const newValues = Object.assign({}, this.nodeForm.value);
      newValues.id = this.node.id;
      this.service.saveNode(newValues)
        .pipe(switchMap(node => this.service.getNodeDetails(node.id)))
        .subscribe(node => this.node = node);
    }

    if (this.mappingForm.dirty) {
      this.service.saveNodeMappings(this.node.id, this.mappingItems.value)
        .subscribe(mappings => this.mappings = mappings);
    }
  }

  resetClicked() {
    this.ngOnInit();
  }
}
