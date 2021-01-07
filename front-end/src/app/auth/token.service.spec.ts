import { TestBed, inject } from '@angular/core/testing';

import { TokenService } from './token.service';

describe('TokenService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TokenService]
    });
  });

  it('should be created', inject([TokenService], (service: TokenService) => {
    expect(service).toBeTruthy();
    expect(service.accessToken).toEqual('');
    expect(service.idToken).toEqual('');
    expect(service.expiresAt).toEqual(0);
  }));

  it('should set tokens', inject([TokenService], (service: TokenService) => {
    jasmine.clock().mockDate(new Date('2019-01-24T10:20:30Z'));

    service.setTokens({ accessToken: 'asd', idToken: 'qwe', expiresIn: 123 });

    expect(service.accessToken).toEqual('asd');
    expect(service.idToken).toEqual('qwe');
    expect(service.expiresAt).toEqual(new Date('2019-01-24T10:22:33Z').getTime());
  }));
});
