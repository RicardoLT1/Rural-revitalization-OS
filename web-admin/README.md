# Xiangyun OS Web Admin

Vue 3 + TypeScript management console for Xiangyun OS.

## Local development

Start Gateway, Auth, Operation, and Analysis with the `demo` Spring profile, then run:

```powershell
npm install
npm run dev
```

The console is available at `http://127.0.0.1:5173`; Vite proxies `/api` to the Gateway on port `8080`.

## Verification

```powershell
npm run build
npm run test:e2e
```

The Playwright release-gate suite is serial because it exercises shared demo accounts and settings. It covers administrator login, settings and map linkage, profile and password security, STAFF/USER access boundaries, persistent notification reads, and audit evidence. Settings, profile fields, and the `admin` password are restored in cleanup hooks.

On Windows the suite uses the installed Microsoft Edge channel by default, so no bundled browser download is required. Override the browser or target URL when needed:

```powershell
$env:E2E_BROWSER_CHANNEL = "chrome"
$env:E2E_BASE_URL = "http://127.0.0.1:5173"
npm run test:e2e
```

Failure screenshots, traces, and HTML reports are written to ignored Playwright output directories.
