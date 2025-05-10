import React, { useState, useRef } from "react";
import { Container, Row, Col, Form, Button, Alert } from "react-bootstrap";

function PasswordRecovery() {
    const [email, setEmail] = useState("");
    const [formError, setFormError] = useState("");
    const [backendMessage, setBackendMessage] = useState(null);
    const [alertVariant, setAlertVariant] = useState("success");
    const emailRef = useRef(null);

    const validate = () => {
        if (!email.trim()) {
            setFormError("Email is required.");
            return false;
        }
        setFormError("");
        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setBackendMessage(null);

        if (!validate()) return;

        try {
            const response = await fetch("http://localhost:8080/auth/request-password-reset", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ email }),
            });

            const message = await response.text();
            if (response.ok) {
                setAlertVariant("success");
            } else {
                setAlertVariant("warning");
            }
            setBackendMessage(message);
        } catch {
            setAlertVariant("danger");
            setBackendMessage("An unexpected error occurred. Please try again later.");
        }
    };

    return (
        <div style={{ background: "linear-gradient(to top left, #006649, #2e8b57)", minHeight: "100vh" }}>
            <Container className="d-flex justify-content-center align-items-center vh-100">
                <Row className="shadow-lg rounded p-4 bg-white w-100" style={{ maxWidth: "600px" }}>
                    <Col>
                        <h2 className="fw-bold text-center">Forgot Your Password?</h2>
                        <p className="text-muted text-center">Enter your email address below and we’ll send you a link to reset your password.</p>

                        {backendMessage && (
                            <Alert variant={alertVariant}>{backendMessage}</Alert>
                        )}

                        <Form onSubmit={handleSubmit}>
                            <Form.Group className="mb-3 text-start">
                                <Form.Label>Email Address</Form.Label>
                                <Form.Control
                                    type="email"
                                    name="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    placeholder="Enter your email"
                                    isInvalid={!!formError}
                                    ref={emailRef}
                                />
                                {formError && <div className="text-danger">{formError}</div>}
                            </Form.Group>

                            <Button type="submit" className="w-100 text-white" style={{ backgroundColor: "#006649" }}>
                                Send Reset Email
                            </Button>
                        </Form>
                    </Col>
                </Row>
            </Container>
        </div>
    );
}

export default PasswordRecovery;
