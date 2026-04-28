# Economic Monitoring Frontend (Angular)

Frontend application for economic/project monitoring with role-based views, real-time updates, and SSR support.

## Highlights

- Angular 21 standalone architecture (no NgModules for feature pages)
- Lazy-loaded routes for lower initial bundle size
- Role-based UX (`ADMIN`, `MANAGER`, `EMPLOYEE`)
- Real-time project events via STOMP/WebSocket
- SSR + selective prerender strategy for public routes
- SCSS structure modularized by feature sections
- CI pipeline with typecheck, build, and unit tests

## Tech Stack

- Angular 21
- Angular Material
- RxJS + Angular Signals
- `@ngx-translate` for i18n
- STOMP.js for realtime messaging
- SCSS
- Vitest (Angular unit-test builder)

## Project Structure

```text
src/
  app/
    components/
    models/
    services/
  environments/
    environment.ts
    environment.production.ts
```

## Environment Configuration

Environment files are under `src/environments/`.

- `environment.ts` (development): local backend (`http://localhost:8080`)
- `environment.production.ts` (production): same-origin API/WS by default

Current config keys:

- `apiBaseUrl`
- `wsBaseUrl`

## Available Scripts

- `npm start` -> dev server
- `npm run build` -> production build
- `npm run test` -> unit tests (watch mode)
- `npm run test:ci` -> unit tests (single run)
- `npm run typecheck` -> strict TypeScript check
- `npm run check` -> typecheck + build + test:ci

## Quality & CI

GitHub Actions workflow:

- `.github/workflows/frontend-ci.yml`

Pipeline steps:

1. `npm ci`
2. `npm run typecheck`
3. `npm run build`
4. `npm run test:ci`

## Getting Started

### Prerequisites

- Node.js 22+
- npm 11+

### Install

```bash
npm ci
```

### Run locally

```bash
npm start
```

App default URL:

- `http://localhost:4200`

## Notes

- Build budgets are configured in `angular.json` with realistic warning/error thresholds.
- Styling follows modular SCSS conventions to keep large features maintainable.
