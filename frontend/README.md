# DevSync AI — Frontend

Vite + React 18 + TypeScript. Styling with **Tailwind CSS**; client state with **Redux Toolkit**; routing with **React Router**; HTTP with **Axios**.

## Scripts

```bash
cp env.example .env
npm install
npm run dev
```

Development calls the API through the Vite proxy (`/api` → `http://localhost:8080`). Override the target with shell env `VITE_PROXY_TARGET` if needed.

## Structure

- `src/app/` — store, router shell, layout
- `src/features/` — Redux slices and feature modules
- `src/pages/` — route-level pages
- `src/shared/` — Axios client and API helpers
