import { Pipe, PipeTransform, ChangeDetectorRef, NgZone, OnDestroy } from '@angular/core';
import { formatDate, AsyncPipe } from '@angular/common';
import { Observable, timer, merge, BehaviorSubject, of } from 'rxjs';
import { map, takeUntil, switchMap } from 'rxjs/operators';

function secs(num: number) {
  return num * 1000;
}

function mins(num: number) {
  return secs(num * 60);
}

function hrs(num: number) {
  return mins(num * 60);
}

@Pipe({
  name: 'timeAgo',
  pure: false
})
export class TimeAgoPipe extends AsyncPipe implements PipeTransform {

  private label$: Observable<string>;
  private subject = new BehaviorSubject(undefined);
  value: Date;
  format: string;
  oldValue: any;

  constructor(changeDetectorRef: ChangeDetectorRef) {
    super(changeDetectorRef);
  }

  transform(value: any, format?: any): any {
    if (typeof value === 'undefined') {
      return null;
    }

    this.value = new Date(value);
    if (isNaN(this.value.getTime())) {
      return null;
    }

    this.format = format === 'withTime' ? 'yyyy-MM-dd HH:mm' : 'yyyy-MM-dd';

    if (!this.label$) {
      this.label$ = this.getObservable();
    }

    if (value !== this.subject.getValue()) {
      this.subject.next(value);
    }

    return super.transform(this.label$);
  }

  private createTimer(start: number, period: number, stop: number) {
    return timer(start, period).pipe(takeUntil(timer(stop)));
  }

  private getSeconds(): number {
    const now = new Date();
    return Math.round((now.getTime() - this.value.getTime()) / 1000);
  }

  private getTimers(): Observable<number>[] {
    const timers = [];
    const seconds = this.getSeconds();

    if (seconds < 60) {
      timers.push(
        this.createTimer(secs(10), secs(3), mins(1))
      );
    }

    if (seconds < 60 * 60) {
      timers.push(
        this.createTimer(mins(1), mins(1), hrs(1))
      );
    }

    if (seconds < 60 * 60 * 24) {
      timers.push(
        this.createTimer(hrs(1), hrs(1), hrs(24))
      );
    }
    return timers;
  }

  private getObservable(): Observable<string> {
    return this.subject.pipe(
      switchMap((v) => merge(of(v), ...this.getTimers())),
      map(() => {
        const seconds = this.getSeconds();
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);

        if (seconds < 0) {
          return formatDate(this.value, this.format, 'en-US');
        } else if (seconds <= 10) {
          return 'a few seconds ago';
        } else if (seconds <= 59) {
          return seconds + ' seconds ago';
        } else if (minutes <= 1) {
          return 'a minute ago';
        } else if (minutes <= 59) {
          return minutes + ' minutes ago';
        } else if (hours <= 1) {
          return 'an hour ago';
        } else if (hours < 24) {
          return hours + ' hours ago';
        } else {
          return formatDate(this.value, this.format, 'en-US');
        }
      }),
    );
  }
}
