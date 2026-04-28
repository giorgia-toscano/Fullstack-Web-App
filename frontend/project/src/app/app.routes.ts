import { Routes } from '@angular/router';
import { authGuard } from './services/auth/auth-guard';

export const routes: Routes = [
  {
    path: 'auth/signup',
    title: 'Signup',
    loadComponent: () =>
      import('./components/signup/signup.component').then((m) => m.SignupComponent),
  },
  {
    path: 'confirm',
    title: 'Confirm Account',
    loadComponent: () =>
      import('./components/confirm-account/confirm-account.component').then(
        (m) => m.ConfirmAccountComponent,
      ),
  },
  {
    path: 'auth/login',
    title: 'Login',
    loadComponent: () => import('./components/login/login.component').then((m) => m.Login),
  },
  {
    path: 'dashboard',
    title: 'Dashboard',
    loadComponent: () => import('./components/dashboard/dashboard').then((m) => m.Dashboard),
    canActivate: [authGuard],
  },
  { path: 'home', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'auth/forgot-password',
    title: 'Forgot Password',
    loadComponent: () =>
      import('./components/forgot-password/forgot-password').then(
        (m) => m.ForgotPasswordComponent,
      ),
  },
  {
    path: 'auth/reset-password',
    title: 'Reset Password',
    loadComponent: () =>
      import('./components/reset-password/reset-password').then((m) => m.ResetPasswordComponent),
  },
  {
    path: 'user/profile',
    title: 'User',
    loadComponent: () => import('./components/user/user').then((m) => m.UserComponent),
    canActivate: [authGuard],
  },
  {
    path: 'projects',
    title: 'Projects',
    loadComponent: () => import('./components/projects/projects').then((m) => m.ProjectsComponent),
    canActivate: [authGuard],
  },
  {
    path: 'projects/create',
    title: 'Create Project',
    loadComponent: () =>
      import('./components/projects/project-create/project-create').then((m) => m.ProjectCreate),
    canActivate: [authGuard],
  },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'dashboard' },
];