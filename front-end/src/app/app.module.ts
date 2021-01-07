import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppComponent } from './app.component';
import { RouterModule } from '@angular/router';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ROUTES } from './app.routes';
import { DeviceListComponent } from './devices/device-list/device-list.component';
import { DeviceService } from './devices/device.service';
import { NodeListComponent } from './nodes/node-list/node-list.component';
import { NodeDetailsComponent } from './nodes/node-details/node-details.component';
import { NodeService } from './nodes/node.service';
import { TimeAgoPipe } from './pipes/time-ago.pipe';
import { DeviceEventsComponent } from './devices/device-events/device-events.component';
import { DateTimePipe } from './pipes/date-time.pipe';
import { AuthService } from './auth/auth.service';
import { HomeComponent } from './home/home.component';
import { AuthGuard } from './auth/auth.guard';
import { LoginComponent } from './auth/login/login.component';
import { JwtModule, JWT_OPTIONS } from '@auth0/angular-jwt';
import { TokenService } from './auth/token.service';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ReportService } from './reports/report.service';
import { AllFormErrorsPipe } from './pipes/all-form-errors.pipe';
import { SwarmEconomyComponent } from './swarm-economy/swarm-economy.component';
import { WorkspaceListComponent } from './workspaces/workspace-list/workspace-list.component';
import { WorkspaceService } from './workspaces/workspace.service';
import { WorkspaceInviteComponent } from './workspaces/workspace-invite/workspace-invite.component';
import { WorkspaceInterceptor } from './workspaces/workspace.interceptor';
import { MeasSystemEvalComponent } from './meas-system-eval/meas-system-eval.component';
import { SamsNodeComponent } from './nodes/sams-node/sams-node.component';
import { ConfigListComponent } from './configs/config-list/config-list.component';
import { ConfigDetailsComponent } from './configs/config-details/config-details.component';
import { ConfigService } from './configs/config.service';
import { LastMeasurementsModalComponent } from './dashboard/last-measurements-modal/last-measurements-modal.component';
import { BatteryLifeComponent } from './battery-life/battery-life.component';
import { ModelListComponent } from './models/model-list/model-list.component';
import { ModelDetailsComponent } from './models/model-details/model-details.component';
import { ModelService } from './models/model.service';
import { NodeSelectComponent } from './nodes/node-select/node-select.component';
import { NodeLabelComponent } from './nodes/node-label/node-label.component';
import { ReportComponent } from './reports/report/report.component';

export function jwtOptionsFactory(tokenSevice: TokenService) {
  return {
    tokenGetter: () => tokenSevice.accessToken
  };
}

const httpInterceptorProviders = [
  { provide: HTTP_INTERCEPTORS, useClass: WorkspaceInterceptor, multi: true }
];

@NgModule({
  declarations: [
    AppComponent,
    NodeListComponent,
    NodeDetailsComponent,
    DeviceListComponent,
    DeviceEventsComponent,
    TimeAgoPipe,
    DateTimePipe,
    HomeComponent,
    LoginComponent,
    DashboardComponent,
    AllFormErrorsPipe,
    SwarmEconomyComponent,
    WorkspaceListComponent,
    WorkspaceInviteComponent,
    MeasSystemEvalComponent,
    WorkspaceInviteComponent,
    SamsNodeComponent,
    ConfigListComponent,
    ConfigDetailsComponent,
    DashboardComponent,
    LastMeasurementsModalComponent,
    BatteryLifeComponent,
    ModelListComponent,
    ModelDetailsComponent,
    NodeSelectComponent,
    NodeLabelComponent,
    ReportComponent
  ],
  entryComponents: [
    // no direct routes to these components
    LastMeasurementsModalComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    RouterModule.forRoot(ROUTES),
    NgbModule,
    FormsModule,
    ReactiveFormsModule,
    JwtModule.forRoot({
      jwtOptionsProvider: {
        provide: JWT_OPTIONS,
        useFactory: jwtOptionsFactory,
        deps: [TokenService]
      }
    })
  ],
  providers: [
    AuthService,
    TokenService,
    AuthGuard,
    DeviceService,
    NodeService,
    ReportService,
    WorkspaceService,
    ConfigService,
    ModelService,
    httpInterceptorProviders
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
