import { AllFormErrorsPipe } from './all-form-errors.pipe';
import { FormGroup, AbstractControl } from '@angular/forms';


describe('AllFormErrorsPipe', () => {
  let pipe: AllFormErrorsPipe;

  beforeEach(() => {
    pipe = new AllFormErrorsPipe();
  });

  it('create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should transform', () => {
    const controls = {
      'c1': { errors: { one: 1, two: 2 } },
      'c2': {}
    };
    const form: FormGroup = {
      controls: controls,
      get: (key) => controls[key],
    } as any;
    const result = pipe.transform(form);
    expect(result).toEqual({ c1: { one: 1, two: 2 } });
  });
});
