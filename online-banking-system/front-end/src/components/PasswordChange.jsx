import React, { useState, useEffect, useRef } from "react";
import { useUser } from "../context/UserContext";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Container, Row, Col, Form, Button, Alert, Modal, Overlay, Popover } from "react-bootstrap";

function PasswordChange() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");
    const navigate = useNavigate();
    const { setUser } = useUser();

    const [firstName, setFirstName] = useState("");
    const [formData, setFormData] = useState({ password: "", confirmPassword: "" });
    const [formErrors, setFormErrors] = useState({});
    const [errorMessage, setErrorMessage] = useState("");
    const [showSuccessModal, setShowSuccessModal] = useState(false);
    const [showPopover, setShowPopover] = useState(false);
    const [tokenValid, setTokenValid] = useState(true);
    const passwordRef = useRef(null);

    const passwordChecks = {
        length: formData.password.length >= 8,
        uppercase: /[A-Z]/.test(formData.password),
        number: /\d/.test(formData.password),
        specialChar: /[@$!%*?&]/.test(formData.password),
    };

    const passwordRegex = /^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;

    const validate = () => {
        const errors = {};

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

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    useEffect(() => {
        fetch("http://localhost:8080/auth/me", { credentials: "include" })
            .then(res => res.ok ? res.json() : null)
            .then(data => {
                if (data?.fName) setFirstName(data.fName);
            })
            .catch(() => setFirstName(""));
    }, []);

    useEffect(() => {
        fetch("http://localhost:8080/auth/validate-token?token=" + token)
            .then(res => {
                if (!res.ok) throw new Error("Invalid or expired token");
            })
            .catch(() => {
                setErrorMessage("This link is invalid or has expired.");
                setTokenValid(false);
            });
    }, [token]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        setFormErrors((prev) => ({ ...prev, [name]: "" }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage("");
        if (!validate()) return;

        try {
            const response = await fetch("http://localhost:8080/auth/reset-password", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({
                    token,
                    newPassword: formData.password,
                }),
            });

            const message = await response.text();
            if (response.ok) {
                localStorage.clear();
                setShowSuccessModal(true);
            } else {
                setErrorMessage(message);
            }
        } catch (err) {
            setErrorMessage("An error occurred. Please try again later.");
        }
    };

    return (
        <div style={{ background: "linear-gradient(to top left, #006649, #2e8b57)", minHeight: "100vh" }}>
            <Container className="d-flex justify-content-center align-items-center vh-100">
                <Row className="shadow-lg rounded p-4 bg-white w-100" style={{ maxWidth: "600px" }}>
                    <Col>
                        <h2 className="fw-bold text-center">Hello {firstName || "User"}!</h2>
                        <p className="text-muted text-center">Enter a new secure password below</p>

                        {errorMessage && <Alert variant="warning">{errorMessage}</Alert>}

                        <Form onSubmit={handleSubmit}>
                            <fieldset disabled={!tokenValid}>
                                <Form.Group className="mb-3 text-start">
                                    <Form.Label>New Password</Form.Label>
                                    <Form.Control
                                        type="password"
                                        name="password"
                                        value={formData.password}
                                        onChange={handleChange}
                                        placeholder="Enter new password"
                                        isInvalid={!!formErrors.password}
                                        ref={passwordRef}
                                        onFocus={() => setShowPopover(true)}
                                        onBlur={() => setShowPopover(false)}
                                    />
                                    {formErrors.password && <div className="text-danger">{formErrors.password}</div>}
                                    <Overlay target={passwordRef.current} show={showPopover} placement="bottom">
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

                                <Form.Group className="mb-3 text-start">
                                    <Form.Label>Confirm New Password</Form.Label>
                                    <Form.Control
                                        type="password"
                                        name="confirmPassword"
                                        value={formData.confirmPassword}
                                        onChange={handleChange}
                                        placeholder="Confirm new password"
                                        isInvalid={!!formErrors.confirmPassword}
                                    />
                                    {formErrors.confirmPassword && <div className="text-danger">{formErrors.confirmPassword}</div>}
                                </Form.Group>

                                <Button type="submit" className="w-100 text-white" style={{ backgroundColor: "#006649" }}>
                                    Reset Password
                                </Button>
                            </fieldset>
                        </Form>
                    </Col>
                </Row>
            </Container>

            <Modal
                show={showSuccessModal}
                backdrop="static"
                keyboard={false}
                centered
            >
                <Modal.Header>
                    <Modal.Title>Password Reset</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p>Your password was successfully updated. Please log in with your new credentials.</p>
                </Modal.Body>
                <Modal.Footer>
                    <Button
                        variant="secondary"
                        onClick={async () => {
                            await fetch("http://localhost:8080/auth/logout", {
                                method: "POST",
                                credentials: "include"
                            });

                            localStorage.clear();
                            setUser(null);
                            navigate("/login");
                        }}
                    >
                        Go to Login
                    </Button>

                </Modal.Footer>
            </Modal>


        </div>
    );
}

export default PasswordChange;
