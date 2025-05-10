import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { BrowserRouter } from "react-router-dom";
import { UserProvider } from "./context/UserContext";
import { SessionToastProvider } from "./context/SessionToastContext";
import "bootstrap/dist/css/bootstrap.min.css";

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <BrowserRouter>
      <UserProvider>
        <SessionToastProvider>
          <App />
        </SessionToastProvider>
      </UserProvider>
    </BrowserRouter>
  </React.StrictMode>
);
