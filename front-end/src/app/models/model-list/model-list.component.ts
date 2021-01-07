import { Component, OnInit } from '@angular/core';
import { ModelService } from '../model.service';
import { ModelDefinition } from '../models.domain';
import { forkJoin } from 'rxjs';
import { NodeService } from 'src/app/nodes/node.service';

@Component({
  selector: 'app-model-list',
  templateUrl: './model-list.component.html',
  styleUrls: ['./model-list.component.css']
})
export class ModelListComponent implements OnInit {
  models: ModelDefinition[] = [];

  constructor(private service: ModelService, private nodeService: NodeService) { }

  ngOnInit(): void {
    forkJoin(
      this.service.getModelTemplates(),
      this.nodeService.getNodes(),
      this.service.getModels()
    ).subscribe(([templates, nodes, models]) => {
      this.models = models.map(m => {
        const temp = templates.find(t => t.code === m.modelCode);
        if (temp) {
          m.modelName = temp.name;

          m.paramsLabels = temp.params.map(p => {
            const val = m.params[p.code];
            return {
              label: p.name,
              master:  p.master,
              value: val,
              node: p.type === 'nodeId' && nodes.find(n => n.id === val)
            };
          });
        }
        return m;
      });
    });

  }

}
