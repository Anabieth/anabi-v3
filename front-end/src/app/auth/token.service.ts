import { Injectable } from '@angular/core';

@Injectable()
export class TokenService {

  private _idToken: string;
  private _accessToken: string;
  private _expiresAt: number;

  constructor() {
    this.removeTokens();
  }

  public setTokens(authResult) {
    const expiresAt = (authResult.expiresIn * 1000) + new Date().getTime();
    this._accessToken = authResult.accessToken;
    this._idToken = authResult.idToken;
    this._expiresAt = expiresAt;
  }

  public removeTokens() {
    this._idToken = '';
    this._accessToken = '';
    this._expiresAt = 0;
  }

  get accessToken(): string {
    return this._accessToken;
  }

  get idToken(): string {
    return this._idToken;
  }

  get expiresAt() {
    return this._expiresAt;
  }
}
