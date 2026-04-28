import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'auth/login',
    renderMode: RenderMode.Prerender
  },
  {
    path: 'auth/signup',
    renderMode: RenderMode.Prerender,
  },
  {
    path: 'auth/forgot-password',
    renderMode: RenderMode.Prerender,
  },
  {
    path: 'auth/reset-password',
    renderMode: RenderMode.Prerender,
  },
  {
    path: 'confirm',
    renderMode: RenderMode.Prerender,
  },
  {
    path: '**',
    renderMode: RenderMode.Client,
  },
];