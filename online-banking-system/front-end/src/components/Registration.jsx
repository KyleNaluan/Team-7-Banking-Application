import React, { useState, useRef } from "react";
import { useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import { Button, Form, Row, Col, Container, Alert, Overlay, Popover } from "react-bootstrap";

function Registration() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    fName: "",
    lName: "",
    email: "",
    username: "",
    password: "",
    confirmPassword: "",
    dateOfBirth: "",
  });

  const [errorMessage, setErrorMessage] = useState("");
  const [formErrors, setFormErrors] = useState({});
  const [showPasswordPopover, setShowPasswordPopover] = useState(false);
  const passwordRef = useRef(null);

  const alphanumericRegex = /^[a-zA-Z0-9]+$/;
  const nameRegex = /^[a-zA-Z]+$/;
  const passwordRegex = /^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;

  const passwordChecks = {
    length: formData.password.length >= 8,
    uppercase: /[A-Z]/.test(formData.password),
    number: /\d/.test(formData.password),
    specialChar: /[@$!%*?&]/.test(formData.password),
  };

  const validate = () => {
    const errors = {};

    if (!formData.fName.trim()) {
      errors.fName = "First name is required.";
    } else if (!nameRegex.test(formData.fName)) {
      errors.fName = "First name must only contain letters.";
    }

    if (!formData.lName.trim()) {
      errors.lName = "Last name is required.";
    } else if (!nameRegex.test(formData.lName)) {
      errors.lName = "Last name must only contain letters.";
    }

    if (!formData.email.trim()) {
      errors.email = "Email is required.";
    }

    if (!formData.username.trim()) {
      errors.username = "Username is required.";
    } else if (!alphanumericRegex.test(formData.username)) {
      errors.username = "Username must only contain letters and numbers.";
    }

    if (!formData.password.trim()) {
      errors.password = "Password is required.";
    } else if (!passwordRegex.test(formData.password)) {
      errors.password = "Password must be at least 8 characters and include an uppercase letter, a number, and a special character.";
    }

    if (!formData.confirmPassword.trim()) {
      errors.confirmPassword = "Please confirm your password.";
    } else if (formData.password !== formData.confirmPassword) {
      errors.confirmPassword = "Passwords do not match.";
    }

    if (!formData.dateOfBirth) {
      errors.dateOfBirth = "Date of birth is required.";
    } else {
      const dob = new Date(formData.dateOfBirth);
      const today = new Date();
      const age = today.getFullYear() - dob.getFullYear();
      const m = today.getMonth() - dob.getMonth();
      const isTooYoung = age < 16 || (age === 16 && m < 0);
      if (isTooYoung) {
        errors.dateOfBirth = "You must be at least 16 years old.";
      }
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));

    let error = "";

    if (name === "fName" || name === "lName") {
      if (!value.trim()) {
        error = `${name === "fName" ? "First" : "Last"} name is required.`;
      } else if (!nameRegex.test(value)) {
        error = `${name === "fName" ? "First" : "Last"} name must only contain letters.`;
      }
    }

    if (name === "email" && !value.trim()) {
      error = "Email is required.";
    }

    if (name === "username") {
      if (!value.trim()) {
        error = "Username is required.";
      } else if (!alphanumericRegex.test(value)) {
        error = "Username must only contain letters and numbers.";
      }
    }

    if (name === "password") {
      if (!value.trim()) {
        error = "Password is required.";
      }
    }

    if (name === "confirmPassword") {
      if (value !== formData.password) {
        error = "Passwords do not match.";
      }
    }

    if (name === "dateOfBirth") {
      if (!value) {
        error = "Date of birth is required.";
      } else {
        const dob = new Date(value);
        const today = new Date();
        const age = today.getFullYear() - dob.getFullYear();
        const m = today.getMonth() - dob.getMonth();
        const isTooYoung = age < 16 || (age === 16 && m < 0);
        if (isTooYoung) {
          error = "You must be at least 16 years old.";
        }
      }
    }

    setFormErrors((prev) => ({ ...prev, [name]: error }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage("");

    if (!validate()) return;

    try {
      const response = await fetch("http://localhost:8080/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      const result = await response.text();

      if (response.ok && result === "User registered successfully!") {
        navigate("/login");
      } else {
        setErrorMessage(result);
      }
    } catch (err) {
      console.error("Registration failed:", err);
      setErrorMessage("Registration error: " + err.message);
    }
  };

  return (
    <div style={{ background: "linear-gradient(to top left, #006649, #2e8b57)", minHeight: "100vh" }}>
      <Container className="d-flex justify-content-center align-items-center vh-100">
        <Row className="shadow-lg rounded p-4 bg-white w-100" style={{ maxWidth: "850px" }}>
          <Col>
            <h2 className="fw-bold text-center">Create an Account</h2>
            <p className="text-muted text-center">Fill in your details below</p>
            {errorMessage && <Alert key="warning" variant="warning">{errorMessage}</Alert>}
            <Form onSubmit={handleSubmit}>
              <Row>
                <Col>
                  <Form.Group className="mb-3 text-start">
                    <Form.Label>First Name</Form.Label>
                    <Form.Control type="text" name="fName" value={formData.fName} onChange={handleChange} placeholder="Enter first name" isInvalid={!!formErrors.fName} />
                    <Form.Text className="text-muted">Letters only. No numbers or symbols.</Form.Text>
                    {formErrors.fName && <div className="text-danger">{formErrors.fName}</div>}
                  </Form.Group>
                </Col>
                <Col>
                  <Form.Group className="mb-3 text-start">
                    <Form.Label>Last Name</Form.Label>
                    <Form.Control type="text" name="lName" value={formData.lName} onChange={handleChange} placeholder="Enter last name" isInvalid={!!formErrors.lName} />
                    <Form.Text className="text-muted">Letters only. No numbers or symbols.</Form.Text>
                    {formErrors.lName && <div className="text-danger">{formErrors.lName}</div>}
                  </Form.Group>
                </Col>
              </Row>
              <Row>
                <Col>
                  <Form.Group className="mb-3 text-start">
                    <Form.Label>Email</Form.Label>
                    <Form.Control type="email" name="email" value={formData.email} onChange={handleChange} placeholder="Enter email" isInvalid={!!formErrors.email} />
                    {formErrors.email && <div className="text-danger">{formErrors.email}</div>}
                  </Form.Group>
                </Col>
                <Col>
                  <Form.Group className="mb-3 text-start">
                    <Form.Label>Username</Form.Label>
                    <Form.Control type="text" name="username" value={formData.username} onChange={handleChange} placeholder="Enter username" isInvalid={!!formErrors.username} />
                    <Form.Text className="text-muted">Must only contain letters and numbers.</Form.Text>
                    {formErrors.username && <div className="text-danger">{formErrors.username}</div>}
                  </Form.Group>
                </Col>
              </Row>
              <Row>
                <Col>
                  <Form.Group className="mb-3 text-start">
                    <Form.Label>Password</Form.Label>
                    <Form.Control
                      type="password"
                      name="password"
                      value={formData.password}
                      onChange={handleChange}
                      placeholder="Enter password"
                      isInvalid={!!formErrors.password}
                      ref={passwordRef}
                      onFocus={() => setShowPasswordPopover(true)}
                      onBlur={() => setShowPasswordPopover(false)}
                    />
                    {formErrors.password && <div className="text-danger">{formErrors.password}</div>}
                    <Overlay target={passwordRef.current} show={showPasswordPopover} placement="bottom">
                      <Popover id="popover-password-requirements">
                        <Popover.Header as="h3">Password Requirements</Popover.Header>
                        <Popover.Body>
                          <ul className="list-unstyled mb-0">
                            <li style={{ color: passwordChecks.length ? 'green' : 'red' }}>{passwordChecks.length ? '✓' : '✗'} At least 8 characters</li>
                            <li style={{ color: passwordChecks.uppercase ? 'green' : 'red' }}>{passwordChecks.uppercase ? '✓' : '✗'} Uppercase letter</li>
                            <li style={{ color: passwordChecks.number ? 'green' : 'red' }}>{passwordChecks.number ? '✓' : '✗'} Number</li>
                            <li style={{ color: passwordChecks.specialChar ? 'green' : 'red' }}>{passwordChecks.specialChar ? '✓' : '✗'} Special character (@$!%*?&)</li>
                          </ul>
                        </Popover.Body>
                      </Popover>
                    </Overlay>
                  </Form.Group>
                </Col>
                <Col>
                  <Form.Group className="mb-3 text-start">
                    <Form.Label>Confirm Password</Form.Label>
                    <Form.Control type="password" name="confirmPassword" value={formData.confirmPassword} onChange={handleChange} placeholder="Confirm password" isInvalid={!!formErrors.confirmPassword} />
                    {formErrors.confirmPassword && <div className="text-danger">{formErrors.confirmPassword}</div>}
                  </Form.Group>
                </Col>
              </Row>
              <Form.Group className="mb-3 text-start">
                <Form.Label>Date of Birth</Form.Label>
                <Form.Control type="date" name="dateOfBirth" value={formData.dateOfBirth} onChange={handleChange} isInvalid={!!formErrors.dateOfBirth} max={new Date(new Date().setFullYear(new Date().getFullYear() - 16)).toISOString().split("T")[0]} />
                <Form.Text className="text-muted">You must be at least 16 years old.</Form.Text>
                {formErrors.dateOfBirth && <div className="text-danger">{formErrors.dateOfBirth}</div>}
              </Form.Group>
              <Button type="submit" className="w-100 text-white" style={{ backgroundColor: "#006649" }}>Register</Button>
            </Form>
            <hr />
            <p className="text-center text-muted">
              <strong>Already have an account?</strong>
              <Button variant="link" onClick={() => navigate("/login")}>Login</Button>
            </p>
          </Col>
        </Row>
      </Container>
    </div>
  );
}

export default Registration;
