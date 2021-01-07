import { Component, OnInit, Input } from '@angular/core';
import { SamsNode, SamsNodeUtils } from '../nodes.domain';

@Component({
  selector: 'app-node-label',
  templateUrl: './node-label.component.html',
  styleUrls: ['./node-label.component.css']
})
export class NodeLabelComponent implements OnInit {

  getIcon = SamsNodeUtils.getTypeIconClass;

  @Input()
  node: SamsNode;

  @Input()
  showLocation = false;

  constructor() { }

  ngOnInit(): void {
  }

}
