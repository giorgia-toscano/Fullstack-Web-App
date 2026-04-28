import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';

import { provideHttpClient, withFetch, withInterceptors} from '@angular/common/http';
import { provideTranslateService} from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { authInterceptor } from './services/auth/auth-interceptor';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { I18nMatPaginatorIntl } from './services/core/mat-paginator-intl';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes), 
    provideClientHydration(withEventReplay()),

    provideTranslateService({
      loader: provideTranslateHttpLoader({
        prefix: './i18n/', 
        suffix: '.json'
      }),
      fallbackLang: 'it',
      lang: 'it',
    }),

    provideHttpClient(
      withFetch(),
      withInterceptors([authInterceptor])
    ),
    { provide: MatPaginatorIntl, useClass: I18nMatPaginatorIntl }
  ]
};