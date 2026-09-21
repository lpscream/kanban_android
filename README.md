# Kanban Android app

Client for the backend in `/backend`. The local Room database has been removed — all business
data (subdivisions, nomenclature, order lines, printer settings, label templates) is now fetched
over REST from the server, and the app requires logging in before it can be used.

## What changed

- **Networking**: `data/network/NetworkModule.kt` builds two Retrofit clients — one for
  `data/auth/AuthApi.kt` (`/oauth/token`), one for `data/api/KanbanApiService.kt` (everything
  else). The business-data client attaches a bearer token via `AuthInterceptor` and transparently
  refreshes it on a 401 via `AuthAuthenticator`.
- **Auth**: `data/auth/TokenStore.kt` keeps the access/refresh tokens in `EncryptedSharedPreferences`
  (separate from the app's regular, unencrypted settings). `UI/auth/LoginActivity.kt` is shown
  whenever there's no valid session; `MainActivity` checks this on every launch.
- **Repository**: `repository/ImportRepository.kt` was rewritten to call `KanbanApiService`
  instead of the old Room DAOs. Its public method signatures were kept the same on purpose, so the
  ViewModels and adapters that consume it needed no changes.
- **Server URL**: configurable from the Settings screen (`Settings.serverBaseUrl()`), defaulting
  to `http://10.0.2.2:8080/` (the Android emulator's alias for the host machine, so a locally run
  backend just works). Changing it requires restarting the app, since the Retrofit clients are
  built once at process start.
- **Removed**: `db/AppDatabase.kt`, `db/Dao.kt`, the Room dependency and `kotlin-kapt` plugin.
  `db/Entities.kt` keeps the same data classes (same field names/types) but as plain
  Gson-serializable DTOs instead of Room `@Entity`/`@Dao` types.

Two pre-existing issues noticed while doing this were fixed along the way since they were directly
relevant to "move the database off the phone":
- The Telegram bot token was hardcoded as a SharedPreferences default in `data/Settings.kt` —
  removed (must be entered in Settings; **rotate that token, it was committed to source**).
  Retrofit's HTTP body logging (Telegram client and the new backend client) is now gated behind
  `BuildConfig.DEBUG` — it was unconditionally on before, which would have logged bearer tokens too.
- The old import flow deleted **every** previously imported order line before every import
  (`ImportViewModel.confirmImport` → `repository.deleteAllOrders()`); the backend's
  `/api/order-lines/import` now scopes deletion to just the dates being replaced (see
  `backend/README.md`), so that call was removed entirely from the client.

## Project structure

This module didn't ship with its root Gradle scaffolding (only the `app` module's own
`build.gradle.kts` and `src/`), so `settings.gradle.kts`, the root `build.gradle.kts`,
`gradle/libs.versions.toml` and the Gradle wrapper were added here to make it a buildable project
again.

## Building

This sandbox has no Android SDK and no network access to `dl.google.com`, so this module could
**not** actually be compiled/verified here (unlike `/backend`, which was built and smoke-tested
against a live Postgres). Open it in Android Studio as usual; if Gradle sync complains about the
AGP version in `gradle/libs.versions.toml` (currently `8.9.1`), bump it to whatever your installed
Studio bundles.

## Running against the backend

1. `cd ../backend && docker compose up --build` (see backend/README.md).
2. Launch the app in an emulator — the default server URL (`http://10.0.2.2:8080/`) reaches the
   host machine automatically. On a physical device, set Settings → "Адрес сервера" to your
   machine's LAN IP instead.
3. Log in with the bootstrap admin account (`ADMIN_USERNAME`/`ADMIN_PASSWORD` from
   `docker-compose.yml`, defaults `admin`/`change-me-admin-password`).
