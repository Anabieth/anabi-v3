import { async, ComponentFixture, TestBed, fakeAsync, tick, flush } from '@angular/core/testing';

import { ConfigDetailsComponent } from './config-details.component';
import { ConfigService } from '../config.service';
import { of, empty, Subject } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, convertToParamMap } from '@angular/router';

describe('ConfigDetailsComponent', () => {
  let component: ConfigDetailsComponent;
  let fixture: ComponentFixture<ConfigDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ConfigDetailsComponent],
      providers: [
        {
          provide: ConfigService,
          useValue: {
            getConfigDetails: () => empty(),
            saveConfig: () => empty(),
            deleteConfig: () => empty()
          }
        },
        {
          provide: ActivatedRoute,
          useValue: {
            data: new Subject(),
            paramMap: new Subject()
          }
        }
      ],
      imports: [
        RouterTestingModule,
        FormsModule,
        ReactiveFormsModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should init new config', () => {
    const route = TestBed.get(ActivatedRoute);
    route.data.next({ isNew: true });

    expect(component['codeEditor'].getValue()).toContain('numericParam: 123.456');
  });

  it('should init existing config', () => {
    const service = TestBed.get(ConfigService);
    spyOn(service, 'getConfigDetails').and.returnValue(of({
      id: '123',
      name: 'test config',
      config: { test: 'value' }
    }));

    const route = TestBed.get(ActivatedRoute);
    route.data.next({});
    route.paramMap.next(convertToParamMap({ configId: '123' }));

    expect(component['codeEditor'].getValue()).toContain('test: value');
    expect(service.getConfigDetails).toHaveBeenCalledWith('123');
  });

  describe('on config changes', () => {
    beforeEach(fakeAsync(() => {
      const route = TestBed.get(ActivatedRoute);
      route.data.next({ isNew: true });
      component.configForm.patchValue({ config: { test: 111 } });

      tick(1000); // wait while editor initialized
    }));

    it('should parse valid config', fakeAsync(() => {
      component['codeEditor'].setValue('aaa: 111\nbbb: 222', -1);

      expect(component.configForm.value.config).toBeNull();
      tick(500);
      expect(component.configForm.value.config).toEqual({ aaa: 111, bbb: 222 });

      flush();
    }));

    it('should report parse errors', fakeAsync(() => {
      component['codeEditor'].setValue('aaa: bbb: ccc', -1);

      expect(component.configForm.value.config).toBeNull();
      tick(500);
      expect(component.configForm.value.config).toBeNull();
      expect(component['codeEditor'].getSession().getAnnotations().length).toBe(1);

      flush();
    }));

  });

  it('should save config', () => {
    const service = TestBed.get(ConfigService);
    spyOn(service, 'saveConfig').and.callThrough();

    component.saveClicked();
    expect(service.saveConfig).toHaveBeenCalled();
  });

  it('should delete config', () => {
    const service = TestBed.get(ConfigService);
    spyOn(service, 'deleteConfig').and.callThrough();

    component.deleteClicked();
    expect(service.deleteConfig).toHaveBeenCalled();
  });
});
