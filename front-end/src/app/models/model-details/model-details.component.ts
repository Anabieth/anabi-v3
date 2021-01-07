import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ModelService } from '../model.service';
import { ModelTemplate } from '../models.domain';
import { ActivatedRoute, Router } from '@angular/router';
import { map, filter, tap, switchMap } from 'rxjs/operators';
import { NodeService } from 'src/app/nodes/node.service';
import { SamsNode } from 'src/app/nodes/nodes.domain';
import { forkJoin } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-model-details',
  templateUrl: './model-details.component.html',
  styleUrls: ['./model-details.component.css']
})
export class ModelDetailsComponent implements OnInit {

  nodes: SamsNode[] = [];
  models: ModelTemplate[] = [];
  selectedModel: ModelTemplate = null;

  saveError: any;

  modelForm = this.fb.group({
    id: null,
    modelCode: [null, Validators.required],
    params: null
  });

  get isNew(): boolean {
    return !this.modelForm.get('id').value;
  }

  constructor(private fb: FormBuilder,
    private service: ModelService,
    private nodeService: NodeService,
    private route: ActivatedRoute,
    private router: Router,
    private modalService: NgbModal
  ) {
    this.modelForm.get('modelCode').valueChanges
      .subscribe(code => {
        this.selectedModel = this.models.find(m => m.code === code);
        if (!this.selectedModel) {
          return;
        }
        this.modelForm.removeControl('params');
        const params = {};
        this.selectedModel.params.forEach(p => params[p.code] = this.fb.control('', Validators.required));
        this.modelForm.addControl('params', this.fb.group(params));
        this.router.navigate([], { queryParams: { m: code }, skipLocationChange: true });
      });
  }

  ngOnInit(): void {
    forkJoin({
      nodes: this.nodeService.getNodes(),
      models: this.service.getModelTemplates()
    }).subscribe(({ nodes, models }) => {
      this.nodes = nodes;
      this.models = models;

      this.route.data.pipe(
        filter(data => data.isNew),
        switchMap(_ => this.route.queryParamMap),
        map(paramMap => paramMap.get('m')),
        filter(code => !!code),
        filter(code => this.models.some(m => m.code === code)),
        tap(code => this.modelForm.get('modelCode').setValue(code)),
      ).subscribe();

      this.route.data.pipe(
        filter(data => !data.isNew),
        switchMap(_ => this.route.paramMap),
        map(paramMap => paramMap.get('modelId')),
        switchMap(id => this.service.getModel(id)),
        tap(def => {
          this.modelForm.setValue(def);
          // this.modelForm.get('modelCode').disable();
        })
      ).subscribe();

    });
  }

  getParamNode(code: string) {
    const id = this.modelForm.get('params').get(code).value;
    return this.nodes.find(n => n.id === id);
  }

  paramNodeSelected(code: string, item: SamsNode) {
    this.modelForm.get('params').get(code).setValue(item.id);
  }

  saveClicked(): void {
    this.service.saveModel(this.modelForm.value)
      .subscribe(_ => {
        this.router.navigate(['/models']);
      },
      err => {
        this.saveError = err.error;
      }
    );
  }

  cancelClicked(): void {
    this.router.navigate(['/models']);
  }

  deleteClicked(content: any): void {
    this.modalService.open(content).result
      .then(
        (ok) => {
          this.service.deleteModel(this.modelForm.get('id').value)
            .subscribe(_ => this.router.navigate(['/models']));
        },
        (cancel) => null
      );
  }

}
