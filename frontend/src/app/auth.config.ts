import {
  PublicClientApplication,
  BrowserCacheLocation,
  LogLevel,
  type Configuration,
  type BrowserSystemOptions
} from '@azure/msal-browser';

export const msalClientId = 'ac3261d7-bac7-48e8-b189-1d5b9c93c107';

export const msalTenantId = '501361f2-a641-4fba-b6a9-954644204a59';

export const msalAuthority =
  `https://login.microsoftonline.com/${msalTenantId}`;

export const msalRedirectUri = 'http://localhost:4200/';

export const msalConfig: Configuration = {
  auth: {
    clientId: msalClientId,
    authority: msalAuthority,
    redirectUri: msalRedirectUri
  },

  cache: {
    cacheLocation: BrowserCacheLocation.LocalStorage
  },

  system: {
    loggerOptions: {
      logLevel: LogLevel.Warning,
      piiLoggingEnabled: false,
      loggerCallback: (level, message, containsPii) => {
        if (containsPii) {
          return;
        }

        switch (level) {
          case LogLevel.Error:
            console.error('[MSAL]', message);
            break;

          case LogLevel.Warning:
            console.warn('[MSAL]', message);
            break;
        }
      }
    }
  } as BrowserSystemOptions
};

export const msalInstance =
  new PublicClientApplication(msalConfig);