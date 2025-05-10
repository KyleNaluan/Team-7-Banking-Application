import React, { useState } from "react";
import { useUser } from "../context/UserContext";
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import { Button, Form, Alert } from "react-bootstrap";


function Login() {
  const navigate = useNavigate();
  const { setUser } = useUser();

  const [loginData, setLoginData] = useState({
    username: "",
    password: "",
    rememberMe: false,
  });

  const [errorMessage, setErrorMessage] = useState("");
  const [formErrors, setFormErrors] = useState({});

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setLoginData((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));

    const errors = { ...formErrors };
    if (type !== "checkbox" && !value.trim()) {
      errors[name] = `${name === "username" ? "Email or Username" : "Password"} is required.`;
    } else {
      delete errors[name];
    }
    setFormErrors(errors);
  };

  const validate = () => {
    const errors = {};
    if (!loginData.username.trim()) errors.username = "Email or Username is required.";
    if (!loginData.password.trim()) errors.password = "Password is required.";
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setErrorMessage("");

    if (!validate()) return;

    try {
      const response = await fetch("http://localhost:8080/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(loginData),
        credentials: "include",
      });

      const result = await response.json();

      if (response.ok) {
        const meResponse = await fetch("http://localhost:8080/auth/me", {
          credentials: "include",
        });

        if (meResponse.ok) {
          const user = await meResponse.json();
          localStorage.setItem("user", JSON.stringify(user));
          setUser(user);
          navigate("/dashboard");
        } else {
          setErrorMessage("Login succeeded, but session check failed.");
        }
      } else {
        setErrorMessage(result.message || "Login failed.");
      }
    } catch (err) {
      console.error("Login error:", err);
      setErrorMessage("Login request failed.");
    }
  };

  return (
    <div className="d-flex vh-100">
      <div className="w-50 position-relative d-flex align-items-center justify-content-center">
        <div className="position-absolute top-0 start-0 p-4">
          <h4 className="fw-bold mb-0">Team7Banking</h4>
        </div>

        <div className="p-4" style={{ width: "100%", maxWidth: "500px" }}>
          <h2 className="fw-bold">Welcome back</h2>
          <p className="text-muted">Please enter your details</p>
          {errorMessage && <Alert key="warning" variant="warning">{errorMessage}</Alert>}
          <Form onSubmit={handleLogin}>
            <Form.Group className="mb-3 text-start">
              <Form.Label>Email or Username</Form.Label>
              <Form.Control
                type="text"
                name="username"
                value={loginData.username}
                onChange={handleChange}
                placeholder="Enter email or username"
                isInvalid={!!formErrors.username}
              />
              {formErrors.username && <div className="text-danger">{formErrors.username}</div>}
            </Form.Group>
            <Form.Group className="mb-3 text-start">
              <Form.Label>Password</Form.Label>
              <Form.Control
                type="password"
                name="password"
                value={loginData.password}
                onChange={handleChange}
                placeholder="Enter password"
                isInvalid={!!formErrors.password}
              />
              {formErrors.password && <div className="text-danger">{formErrors.password}</div>}
            </Form.Group>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <Form.Check
                type="checkbox"
                label="Remember me"
                name="rememberMe"
                checked={loginData.rememberMe}
                onChange={handleChange}
              />
              <span
                role="button"
                className="text-primary"
                style={{ fontSize: "small", cursor: "pointer" }}
                onClick={() => navigate("/forgot-password")}
              >
                Forgot password?
              </span>
            </div>
            <Button type="submit" className="w-100 text-white" style={{ backgroundColor: "#006649" }}>
              Login
            </Button>
          </Form>
          <hr />
          <div className="d-flex justify-content-center align-items-center gap-1 text-muted mt-2">
            <span><strong>Don’t have an account?</strong></span>
            <Button variant="link" className="p-0" onClick={() => navigate("/register")}>Sign up</Button>
          </div>
        </div>
      </div>

      <div
        className="w-50 d-none d-md-block"
        style={{
          background: "linear-gradient(to top left, #006649, #2e8b57)",
        }}
      />
    </div>
  );
}

export default Login;
