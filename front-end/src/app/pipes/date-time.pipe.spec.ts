import { DateTimePipe } from './date-time.pipe';

describe('DateTimePipe', () => {
  let pipe;

  beforeEach(() => {
    pipe = new DateTimePipe('en-US');
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should use default format', () => {
    expect(pipe.transform('2019-02-03T13:14:15Z', null, '+0000')).toMatch('2019-02-03 13:14:15');
  });

  it('should use provided format', () => {
    expect(pipe.transform('2019-02-03T13:14:15Z', 'dd.MM.yyyy HH:mm', '+0000')).toMatch('03.02.2019 13:14');
  });
});
