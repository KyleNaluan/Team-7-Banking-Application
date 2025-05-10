import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./components/Login";
import Registration from "./components/Registration";
import Dashboard from "./components/Dashboard";
import Accounts from "./components/Accounts";
import IndividualAccount from "./components/IndividualAccount";
import Transfer from "./components/Transfer";
import Payment from "./components/Payment";
import BudgetOverview from "./components/BudgetOverview";
import Profile from "./components/Profile";
import EmailChange from "./components/EmailChange";
import PasswordChange from "./components/PasswordChange";
import PasswordRecovery from "./components/PasswordRecovery";
import ProtectedRoute from "./components/ProtectedRoute";
import { useUser } from "./context/UserContext";
import SessionWatcher from "./components/SessionWatcher";
import { Spinner } from "react-bootstrap";
import { Toast, ToastContainer } from "react-bootstrap";
import { useSessionToast } from "./context/SessionToastContext";

function App() {
  const { user, authLoading } = useUser();
  const { toastMessage, clearToast } = useSessionToast();

  if (authLoading) {
    return (
      <div className="vh-100 d-flex justify-content-center align-items-center">
        <Spinner animation="border" variant="success" role="status" />
      </div>
    );
  }

  return (
    <div className="App">
      <SessionWatcher />
      <ToastContainer position="top-center" className="mt-4">
        <Toast onClose={clearToast} show={!!toastMessage} delay={4000} autohide bg="warning">
          <Toast.Body>{toastMessage}</Toast.Body>
        </Toast>
      </ToastContainer>
      <Routes>
        <Route path="/" element={user ? <Navigate to="/dashboard" /> : <Login />} />
        <Route path="/login" element={user ? <Navigate to="/dashboard" /> : <Login />} />
        <Route path="/register" element={user ? <Navigate to="/dashboard" /> : <Registration />} />
        <Route path="/change-email" element={<EmailChange />} />
        <Route path="/change-password" element={<PasswordChange />} />
        <Route path="/forgot-password" element={<PasswordRecovery />} />


        <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/accounts" element={<ProtectedRoute><Accounts /></ProtectedRoute>} />
        <Route path="/account/:accountId" element={<ProtectedRoute><IndividualAccount /></ProtectedRoute>} />
        <Route path="/transfer" element={<ProtectedRoute><Transfer /></ProtectedRoute>} />
        <Route path="/payment" element={<ProtectedRoute><Payment /></ProtectedRoute>} />
        <Route path="/budgetoverview" element={<ProtectedRoute><BudgetOverview /></ProtectedRoute>} />
        <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
      </Routes>
    </div>
  );
}

export default App;
