import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute, Router, ParamMap } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';

import * as ace from 'ace-builds';
import 'ace-builds/src-noconflict/mode-yaml';
import 'ace-builds/src-noconflict/theme-github';

import * as YAML from 'yaml';
import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, tap, switchMap, map } from 'rxjs/operators';
import { ConfigService } from '../config.service';

@Component({
  selector: 'app-config-details',
  templateUrl: './config-details.component.html',
  styleUrls: ['./config-details.component.css']
})
export class ConfigDetailsComponent implements OnInit {

  @ViewChild('codeEditor', { static: true }) codeEditorElmRef: ElementRef;
  private codeEditor: ace.Ace.Editor;
  private codeChanges: Subject<string> = new Subject();

  configForm = this.fb.group({
    id: [''],
    name: ['', Validators.required],
    config: [null, Validators.required]
  });

  private defaultConfig = {
    numericParam: 123.456,
    group: {
      textParam1: 'Lorem ipsum dolor sit amet',
      textParam2: ' with special : characters'
    },
    otherParam: null
  };

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private router: Router,
    private service: ConfigService
  ) { }

  ngOnInit() {
    const configById$ = this.route.paramMap.pipe(
      map((params: ParamMap) => params.get('configId')),
      switchMap(id => this.service.getConfigDetails(id)),
      tap(data => this.configForm.patchValue(data)),
      map(data => data.config),
    );

    this.route.data.pipe(
      switchMap(data => data.isNew ? of(this.defaultConfig) : configById$),
      tap(code => this.setupEditor(code))
    ).subscribe();

    this.codeChanges.pipe(
      tap(() => this.configForm.get('config').setValue(null)),
      debounceTime(500),
      distinctUntilChanged(),
      tap(text => {
        try {
          const obj = YAML.parse(text, { prettyErrors: true } as any);
          this.configForm.get('config').setValue(obj);
          this.codeEditor.getSession().setAnnotations([]);
        } catch (err) {
          this.codeEditor.getSession().setAnnotations([
            { row: err.linePos.start.line - 1, column: err.linePos.start.col, text: err.message, type: 'error' }
          ]);
        }
      })
    ).subscribe();
  }

  private setupEditor(value) {
    const element = this.codeEditorElmRef.nativeElement;
    const editorOptions: Partial<ace.Ace.EditorOptions> = {
      highlightActiveLine: true,
      minLines: 10,
      maxLines: 25,
    };
    this.codeEditor = ace.edit(element, editorOptions);
    this.codeEditor.setTheme('ace/theme/github');
    this.codeEditor.getSession().setMode('ace/mode/yaml');
    this.codeEditor.setShowFoldWidgets(true);
    this.codeEditor.setValue(YAML.stringify(value), -1);

    this.codeEditor.on('change', () => {
      this.codeChanges.next(this.codeEditor.getValue());
    });
  }

  saveClicked() {
    this.service.saveConfig(this.configForm.value)
      .subscribe(() => this.router.navigate(['/configs']));
  }

  cancelClicked() {
    this.router.navigate(['/configs']);
  }

  deleteClicked() {
    this.service.deleteConfig(this.configForm.value)
      .subscribe(() => this.router.navigate(['/configs']));
  }
}
