import { Component, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { SamsNode, SamsNodeType, SamsNodeUtils } from '../nodes.domain';
import { enumToMap } from '../../utils';
import { NodeService } from '../node.service';
import { Router, ActivatedRoute } from '@angular/router';
import { switchMap } from 'rxjs/operators';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-node-list',
  templateUrl: './node-list.component.html',
  styleUrls: ['./node-list.component.css']
})
export class NodeListComponent implements OnInit {

  @ViewChild('confirmDelete', { static: true })
  private confirmDelete: TemplateRef<any>;

  private _currentNode: SamsNode;

  nodes: SamsNode[] = [];
  nodeTypes = enumToMap(SamsNodeType);
  getIcon = SamsNodeUtils.getTypeIconClass;

  constructor(
    private service: NodeService,
    private router: Router,
    private route: ActivatedRoute,
    private modalService: NgbModal) { }

  ngOnInit() {
    this.service.getNodes().subscribe(nodes => this.parseNodes(nodes));
  }

  set currentNode(node: SamsNode) {
    this._currentNode = node;
    this.router.navigate([], { queryParams: { n: node ? node.id : null } });
  }

  get currentNode(): SamsNode {
    return this._currentNode;
  }

  private parseNodes(nodes: SamsNode[]) {
    const queryId = this.route.snapshot.queryParamMap.get('n');
    this.currentNode = nodes.find(n => n.id === queryId) || null;
    this.nodes = this.getChildrenNodes(nodes, null);
  }

  private getChildrenNodes(allNodes: SamsNode[], parentId: string): SamsNode[] {
    const nodes = allNodes.filter(n => n.parentId == null && parentId == null || n.parentId === parentId);
    nodes.forEach(n => n.children = this.getChildrenNodes(allNodes, n.id));
    return nodes;
  }

  editClicked(node) {
    this.currentNode = Object.assign(new SamsNode(), node);
  }

  newChildClicked(parent) {
    this.currentNode = new SamsNode();
    this.currentNode.parentId = parent && parent.id || null;
  }

  saveClicked() {
    this.service.saveNode(this.currentNode)
      .pipe(
        switchMap(newNode => {
          this.currentNode = newNode;
          return this.service.getNodes();
        })
      ).subscribe(nodes => this.parseNodes(nodes));
  }

  cancelClicked() {
    this.currentNode = null;
  }

  deleteClicked() {
    this.modalService.open(this.confirmDelete).result
      .then(
        (ok) => {
          this.service.deleteNode(this.currentNode)
            .pipe(
              switchMap(() => {
                this.currentNode = null;
                return this.service.getNodes();
              })
            )
            .subscribe(nodes => this.parseNodes(nodes));
        },
        (cancel) => null
      );
  }

  openReport(nodeId) {
    this.router.navigate(['report'], { queryParams: { n: nodeId, ra: true } });
  }
}
