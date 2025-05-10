import React, { useState, useEffect, useRef } from "react";
import { useUser } from "../context/UserContext";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Container, Row, Col, Form, Button, Alert, Modal } from "react-bootstrap";

function EmailChange() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");
    const navigate = useNavigate();
    const { setUser } = useUser();

    const [firstName, setFirstName] = useState("");
    const [formData, setFormData] = useState({ email: "", confirmEmail: "" });
    const [formErrors, setFormErrors] = useState({});
    const [errorMessage, setErrorMessage] = useState("");
    const [showSuccessModal, setShowSuccessModal] = useState(false);
    const [tokenValid, setTokenValid] = useState(true);

    const emailRef = useRef(null);
    const confirmRef = useRef(null);

    const validate = () => {
        const errors = {};
        if (!formData.email.trim()) {
            errors.email = "This field is required.";
        }
        if (!formData.confirmEmail.trim()) {
            errors.confirmEmail = "This field is required.";
        } else if (formData.email !== formData.confirmEmail) {
            errors.confirmEmail = "Emails do not match.";
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
        fetch("http://localhost:8080/auth/validate-email-token?token=" + token)
            .then(res => {
                if (!res.ok) throw new Error("Token invalid");
            })
            .catch(() => {
                setErrorMessage("This email change link is invalid or has expired.");
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
            const response = await fetch("http://localhost:8080/auth/confirm-email-change", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({
                    token,
                    newEmail: formData.email,
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
                        <p className="text-muted text-center">Enter your new email below</p>

                        {errorMessage && <Alert variant="warning">{errorMessage}</Alert>}

                        <Form onSubmit={handleSubmit}>
                            <fieldset disabled={!tokenValid}>
                                <Form.Group className="mb-3 text-start">
                                    <Form.Label>New Email</Form.Label>
                                    <Form.Control
                                        type="email"
                                        name="email"
                                        value={formData.email}
                                        onChange={handleChange}
                                        placeholder="Enter new email"
                                        isInvalid={!!formErrors.email}
                                        ref={emailRef}
                                    />
                                    {formErrors.email && <div className="text-danger">{formErrors.email}</div>}
                                </Form.Group>

                                <Form.Group className="mb-3 text-start">
                                    <Form.Label>Confirm New Email</Form.Label>
                                    <Form.Control
                                        type="email"
                                        name="confirmEmail"
                                        value={formData.confirmEmail}
                                        onChange={handleChange}
                                        placeholder="Confirm new email"
                                        isInvalid={!!formErrors.confirmEmail}
                                        ref={confirmRef}
                                    />
                                    {formErrors.confirmEmail && <div className="text-danger">{formErrors.confirmEmail}</div>}
                                </Form.Group>

                                <Button type="submit" className="w-100 text-white" style={{ backgroundColor: "#006649" }}>
                                    Confirm Email Change
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
                    <Modal.Title>Email Updated</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p>Your email was successfully updated. Please log in with your new email address.</p>
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

export default EmailChange;
