import { Component, OnInit, Input, EventEmitter, Output, OnChanges, SimpleChanges } from '@angular/core';
import { SamsNode, SamsNodeUtils } from '../nodes.domain';
import { Observable, merge, Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { NgbTypeaheadSelectItemEvent } from '@ng-bootstrap/ng-bootstrap';


function stringContains(value: string, term: string): boolean {
  return value && value.toLowerCase().indexOf(term) > -1;
}

@Component({
  selector: 'app-node-select',
  templateUrl: './node-select.component.html',
  styleUrls: ['./node-select.component.css']
})
export class NodeSelectComponent implements OnInit, OnChanges {
  getIcon = SamsNodeUtils.getTypeIconClass;
  private _nodes: SamsNode[];
  private _nodeHierarchy: SamsNode[];

  @Input()
  set nodes(nodes: SamsNode[]) {
    this._nodes = nodes.sort((a, b) => a.name.localeCompare(b.name));
    this._nodeHierarchy = this.getChildrenNodes(this._nodes, null, 0);
  }

  @Input()
  excludedNodes: SamsNode[] = [];

  @Input()
  maxResults = Number.MAX_VALUE;

  @Input()
  useInput = true;

  @Input()
  value: SamsNode;

  @Output()
  nodeSelected = new EventEmitter<SamsNode>();

  manualEvents$ = new Subject<string>();
  inputValue = '';

  @Input()
  nodeInputFormatter = (item: SamsNode) => item.name
  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.value && !!changes.value.currentValue) {
      this.inputValue = this.nodeInputFormatter(changes.value.currentValue);
    } else {
      this.inputValue = '';
    }
  }

  private getChildrenNodes(allNodes: SamsNode[], parentId: string, level: number): SamsNode[] {
    const result = [];
    const nodes = allNodes.filter(n => n.parentId == null && parentId == null || n.parentId === parentId);
    nodes.forEach(n => {
      result.push(Object.assign({ level }, n)); // deep clone hack
      const children = this.getChildrenNodes(allNodes, n.id, level + 1);
      result.push(...children);
    });
    return result;
  }

  searchNode$ = (text$: Observable<string>) => {
    const filterFunc = (term: string, mapper: (n: SamsNode) => string, result: SamsNode[] = []) => {
      return this._nodes.filter(node =>
        !this.excludedNodes.includes(node) && !result.includes(node) && stringContains(mapper(node), term)
      );
    };

    const populateResult = (term: string) => {
      let result = filterFunc(term, (n) => n.name);
      if (result.length < this.maxResults) {
        result = result.concat(filterFunc(term, (n) => n.location, result));
      }
      if (result.length < this.maxResults) {
        result = result.concat(filterFunc(term, (n) => n.type, result));
      }
      return result.slice(0, this.maxResults);
    };

    return merge(text$.pipe(debounceTime(200)), this.manualEvents$)
      .pipe(
        distinctUntilChanged(),
        switchMap(term => {
          if (!!term) {
            return of(populateResult(term.toLowerCase()));
          } else {
            return of(this._nodeHierarchy);
          }
        })
      );
  }

  nodeItemSelected(event: NgbTypeaheadSelectItemEvent) {
    this.nodeSelected.emit(event.item);
    if (!this.useInput) {
      event.preventDefault();
    }
  }
}
