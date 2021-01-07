import { Pipe, PipeTransform } from '@angular/core';
import { FormGroup } from '@angular/forms';

@Pipe({
  name: 'allFormErrors',
  pure: false
})
export class AllFormErrorsPipe implements PipeTransform {

  transform(form: FormGroup, args?: any): any {
    const res = Object.assign({}, form.errors);

    for (const field in form.controls) {
      if (form.controls.hasOwnProperty(field)) {
        const ctr = form.get(field);
        if (ctr.errors) {
          res[field] = ctr.errors;
        }
      }
    }

    return res;
  }
}
