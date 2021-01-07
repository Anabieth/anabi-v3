import { Pipe, PipeTransform } from '@angular/core';
import { DatePipe } from '@angular/common';

@Pipe({
  name: 'dateTime'
})
export class DateTimePipe extends DatePipe implements PipeTransform {

  transform(value: any, format?: string, timezone?: string, locale?: string): any {
    return super.transform(value, format || 'yyyy-MM-dd HH:mm:ss', timezone, locale);
  }

}
