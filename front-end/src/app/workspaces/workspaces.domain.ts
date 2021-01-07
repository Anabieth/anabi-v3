export class Workspace {
  public invitedUsers: any[];
  public sharingKey: string;
  public owner: string;

  constructor(
    public name?: string,
    public id?: string,
    public isOwner?: boolean
  ) { }
}
