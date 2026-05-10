import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { Provider } from "react-redux";
import { Toaster } from "sonner";

import App from "./app/App";
import { store } from "./app/store";
import { logout } from "./features/auth/authSlice";
import { setAuth401Handler } from "./features/auth/session";
import "./index.css";

setAuth401Handler(() => {
  store.dispatch(logout());
});

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <Provider store={store}>
      <App />
      <Toaster richColors position="top-right" theme="dark" closeButton />
    </Provider>
  </StrictMode>
);
