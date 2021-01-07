import { Routes } from '@angular/router';
import { NodeListComponent } from './nodes/node-list/node-list.component';
import { NodeDetailsComponent } from './nodes/node-details/node-details.component';
import { DeviceListComponent } from './devices/device-list/device-list.component';
import { DeviceEventsComponent } from './devices/device-events/device-events.component';
import { HomeComponent } from './home/home.component';
import { AuthGuard } from './auth/auth.guard';
import { LoginComponent } from './auth/login/login.component';
import { SwarmEconomyComponent } from './swarm-economy/swarm-economy.component';
import { WorkspaceListComponent } from './workspaces/workspace-list/workspace-list.component';
import { WorkspaceInviteComponent } from './workspaces/workspace-invite/workspace-invite.component';
import { MeasSystemEvalComponent } from './meas-system-eval/meas-system-eval.component';
import { SamsNodeComponent } from './nodes/sams-node/sams-node.component';
import { ConfigListComponent } from './configs/config-list/config-list.component';
import { ConfigDetailsComponent } from './configs/config-details/config-details.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { BatteryLifeComponent } from './battery-life/battery-life.component';
import { ModelListComponent } from './models/model-list/model-list.component';
import { ModelDetailsComponent } from './models/model-details/model-details.component';
import { ReportComponent } from './reports/report/report.component';

export const ROUTES: Routes = [
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'battery-life',
    component: BatteryLifeComponent
  },
  {
    path: 'swarm-economy',
    component: SwarmEconomyComponent
  },
  {
    path: 'system-eval',
    component: MeasSystemEvalComponent
  },
  {
    path: 'devices',
    component: DeviceListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'devices/:deviceId/events',
    component: DeviceEventsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'nodes',
    component: NodeListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'nodes/sams',
    component: SamsNodeComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'nodes/:nodeId',
    component: NodeDetailsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'report',
    component: ReportComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'workspaces',
    component: WorkspaceListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'workspaces/:id/invite/:key',
    component: WorkspaceInviteComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'models',
    component: ModelListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'models/new',
    component: ModelDetailsComponent,
    canActivate: [AuthGuard],
    data: {
      isNew: true
    }
  },
  {
    path: 'models/:modelId',
    component: ModelDetailsComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'configs',
    component: ConfigListComponent,
    canActivate: [AuthGuard],
    data: {
      requiredScopes: ['hw']
    }
  },
  {
    path: 'configs/new',
    component: ConfigDetailsComponent,
    canActivate: [AuthGuard],
    data: {
      isNew: true,
      requiredScopes: ['hw']
    }
  },
  {
    path: 'configs/:configId',
    component: ConfigDetailsComponent,
    canActivate: [AuthGuard],
    data: {
      requiredScopes: ['hw']
    }
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'userHome',
    redirectTo: 'dashboard'
  },
  {
    path: '',
    component: HomeComponent
  },
  {
    path: '**',
    redirectTo: ''
  }
];
