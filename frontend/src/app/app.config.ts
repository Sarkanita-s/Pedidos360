import { ApplicationConfig, importProvidersFrom } from '@angular/core';

import { provideRouter } from '@angular/router';

import {
  provideHttpClient,
  withInterceptorsFromDi,
  HTTP_INTERCEPTORS
} from '@angular/common/http';

import {
  MsalModule,
  MsalService,
  MsalGuard,
  MsalBroadcastService,
  MsalInterceptor
} from '@azure/msal-angular';

import { InteractionType } from '@azure/msal-browser';

import { routes } from './app.routes';

import { msalInstance } from './auth.config';

const apiScope =
  'api://ac3261d7-bac7-48e8-b189-1d5b9c93c107/access_as_user';

export const appConfig: ApplicationConfig = {
  providers: [

    provideRouter(routes),

    provideHttpClient(
      withInterceptorsFromDi()
    ),

    {
      provide: HTTP_INTERCEPTORS,
      useClass: MsalInterceptor,
      multi: true
    },

    importProvidersFrom(
      MsalModule.forRoot(
        msalInstance,
        {
          interactionType: InteractionType.Redirect,

          authRequest: {
            scopes: [apiScope]
          }
        },
        {
          interactionType: InteractionType.Redirect,

          protectedResourceMap: new Map([
            [
              'http://localhost:8080/api/*',
              [apiScope]
            ],
            [
              'http://pedidos360-backend.us-east-1.elasticbeanstalk.com/api/*',
              [apiScope]
            ]
          ])
        }
      )
    ),

    MsalService,
    MsalGuard,
    MsalBroadcastService

  ]
};