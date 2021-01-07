import { SimpleDateAdapter, ColorProvider } from './report-domain';

describe('SimpleDateAdapter', () => {
  let adapter: SimpleDateAdapter;

  beforeEach(() => {
    adapter = new SimpleDateAdapter;
  });

  it('should convert from model value', () => {
    expect(adapter.fromModel('2019-02-11')).toEqual({ year: 2019, month: 2, day: 11 });
  });

  it('should convert from null model', () => {
    expect(adapter.fromModel(null)).toBeNull();
  });

  it('should convert value to model', () => {
    expect(adapter.toModel({ year: 2019, month: 4, day: 1 })).toEqual('2019-04-01');
  });

  it('should convert null to model', () => {
    expect(adapter.toModel(null)).toBeNull();
  });
});

describe('ColorProvider', () => {
  it('should cicle colors infinetely', () => {
    const arr: string[] = [];
    const provider = new ColorProvider();
    Array(9000).fill(null).forEach(() => arr.push(provider.next()));
    expect(arr.length).toBe(9000);
  });
});
