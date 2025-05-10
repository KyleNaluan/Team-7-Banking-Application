
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Card, Button, Row, Col, Form, Modal, Container, Toast, ToastContainer } from "react-bootstrap";
import NavBar from "./NavBar";
import { useUser } from "../context/UserContext";

function Profile() {
    const [isEditing, setIsEditing] = useState(false);
    const [showPasswordModal, setShowPasswordModal] = useState(false);
    const [showEmailModal, setShowEmailModal] = useState(false);
    const [formErrors, setFormErrors] = useState({});
    const [backendError, setBackendError] = useState("");
    const [backendSuccess, setBackendSuccess] = useState("");
    const [profileData, setProfileData] = useState({});
    const [originalProfileData, setOriginalProfileData] = useState({});
    const [isSending, setIsSending] = useState(false);
    const { user, setUser } = useUser();

    const alphanumericRegex = /^[a-zA-Z0-9]+$/;
    const nameRegex = /^[a-zA-Z]+$/;
    const phoneRegex = /^\d{10}$/;

    const navigate = useNavigate();

    useEffect(() => {
        fetch("http://localhost:8080/auth/me", { credentials: "include" })
            .then(res => {
                if (res.ok) return res.json();
                else throw new Error("Unauthorized");
            })
            .then(data => {
                setProfileData(data);
                setOriginalProfileData(data);
            })
            .catch(() => {
                setBackendError("Session expired. Redirecting to login...");
                setTimeout(() => navigate("/login"), 2000);
            });
    }, [navigate]);

    useEffect(() => {
        if (backendSuccess) {
            const timer = setTimeout(() => setBackendSuccess(""), 4000);
            return () => clearTimeout(timer);
        }
    }, [backendSuccess]);

    useEffect(() => {
        if (backendError) {
            const timer = setTimeout(() => setBackendError(""), 4000);
            return () => clearTimeout(timer);
        }
    }, [backendError]);

    const validateField = (name, value) => {
        let error = "";
        if (name === "username" && (!value.trim() || !alphanumericRegex.test(value))) {
            error = "Username must only contain letters and numbers.";
        }
        if ((name === "fName" || name === "lName") && (!value.trim() || !nameRegex.test(value))) {
            error = `${name === "fName" ? "First" : "Last"} name must only contain letters.`;
        }
        if (name === "phoneNo" && value && !phoneRegex.test(value)) {
            error = "Phone number must be 10 digits (US only).";
        }
        if (name === "dateOfBirth") {
            const dob = new Date(value);
            const today = new Date();
            const age = today.getFullYear() - dob.getFullYear();
            const m = today.getMonth() - dob.getMonth();
            if (age < 16 || (age === 16 && m < 0)) {
                error = "You must be at least 16 years old.";
            }
        }
        setFormErrors(prev => ({ ...prev, [name]: error }));
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProfileData(prev => ({ ...prev, [name]: value }));
        validateField(name, value);
    };

    const handleSave = async () => {
        setBackendError("");
        setBackendSuccess("");

        try {
            const response = await fetch("http://localhost:8080/customers/" + profileData.customerID, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify(profileData),
            });
            const message = await response.text();
            if (!response.ok) {
                setBackendError(message);
            } else {
                const meRes = await fetch("http://localhost:8080/auth/me", { credentials: "include" });
                const updatedData = await meRes.json();
                setProfileData(updatedData);
                setOriginalProfileData(updatedData);
                setUser({
                    ...user,
                    fName: updatedData.fName,
                    lName: updatedData.lName,
                    username: updatedData.username,
                    email: updatedData.email
                });
                setFormErrors({});
                setIsEditing(false);
                setBackendSuccess(message);
            }
        } catch (err) {
            setBackendError("Server error. Please try again later.");
        }
    };

    const formatPhoneNumber = (num) => num ? num.replace(/(\d{3})(\d{3})(\d{4})/, "$1-$2-$3") : "";
    const maxDate = new Date(new Date().setFullYear(new Date().getFullYear() - 16)).toISOString().split("T")[0];

    return (
        <div style={{ backgroundColor: "#f5f5f5", minHeight: "100vh" }}>
            <NavBar />

            <ToastContainer position="bottom-end" className="p-3">
                <Toast bg="success" onClose={() => setBackendSuccess("")} show={!!backendSuccess} delay={4000} autohide>
                    <Toast.Body className="text-white">{backendSuccess}</Toast.Body>
                </Toast>
                <Toast bg="danger" onClose={() => setBackendError("")} show={!!backendError} delay={4000} autohide>
                    <Toast.Body className="text-white">{backendError}</Toast.Body>
                </Toast>
            </ToastContainer>

            <Container className="mt-4 mb-5">
                <Card className="p-4 mb-4">
                    <div className="d-flex justify-content-between align-items-center border-bottom pb-2" style={{ borderColor: "#006649", borderBottom: "3px solid #006649" }}>
                        <Card.Title className="fw-bold mb-0">Account Information</Card.Title>
                        {!isEditing && <Button onClick={() => { setBackendError(""); setBackendSuccess(""); setIsEditing(true); }} style={{ backgroundColor: "#006649" }}>Edit</Button>}
                    </div>
                    <Card.Body>
                        <Form>
                            {isEditing ? (
                                <>
                                    <Form.Group className="mb-3">
                                        <Form.Label>Username</Form.Label>
                                        <Form.Control type="text" name="username" value={profileData.username || ""} onChange={handleChange} isInvalid={!!formErrors.username} />
                                        <Form.Control.Feedback type="invalid">{formErrors.username}</Form.Control.Feedback>
                                        <Form.Text className="text-muted">Must only contain letters and numbers.</Form.Text>
                                    </Form.Group>
                                    <Row className="mb-3 align-items-center">
                                        <Col md={6}><strong>Email:</strong></Col>
                                        <Col md={6} className="d-flex justify-content-between align-items-center">
                                            {profileData.email}
                                            <Button variant="link" onClick={() => { setBackendError(""); setBackendSuccess(""); setShowEmailModal(true); }} style={{ padding: 0 }}>Change</Button>
                                        </Col>
                                    </Row>
                                    <Row className="mb-3">
                                        <Col>
                                            <Form.Group>
                                                <Form.Label>First Name</Form.Label>
                                                <Form.Control type="text" name="fName" value={profileData.fName || ""} onChange={handleChange} isInvalid={!!formErrors.fName} />
                                                <Form.Control.Feedback type="invalid">{formErrors.fName}</Form.Control.Feedback>
                                            </Form.Group>
                                        </Col>
                                        <Col>
                                            <Form.Group>
                                                <Form.Label>Last Name</Form.Label>
                                                <Form.Control type="text" name="lName" value={profileData.lName || ""} onChange={handleChange} isInvalid={!!formErrors.lName} />
                                                <Form.Control.Feedback type="invalid">{formErrors.lName}</Form.Control.Feedback>
                                            </Form.Group>
                                        </Col>
                                    </Row>
                                    <Form.Group className="mb-3">
                                        <Form.Label>Address</Form.Label>
                                        <Form.Control type="text" name="address" value={profileData.address || ""} onChange={handleChange} />
                                    </Form.Group>
                                    <Form.Group className="mb-3">
                                        <Form.Label>Phone Number</Form.Label>
                                        <Form.Control type="tel" name="phoneNo" value={profileData.phoneNo || ""} onChange={handleChange} isInvalid={!!formErrors.phoneNo} />
                                        <Form.Control.Feedback type="invalid">{formErrors.phoneNo}</Form.Control.Feedback>
                                    </Form.Group>
                                    <Form.Group className="mb-3">
                                        <Form.Label>Date of Birth</Form.Label>
                                        <Form.Control type="date" name="dateOfBirth" value={profileData.dateOfBirth || ""} onChange={handleChange} isInvalid={!!formErrors.dateOfBirth} max={maxDate} />
                                        <Form.Control.Feedback type="invalid">{formErrors.dateOfBirth}</Form.Control.Feedback>
                                    </Form.Group>
                                    <Button onClick={handleSave} className="me-2" style={{ backgroundColor: "#006649" }}>Save</Button>
                                    <Button variant="secondary" onClick={() => { setProfileData(originalProfileData); setFormErrors({}); setIsEditing(false); }}>Cancel</Button>
                                </>
                            ) : (
                                <>
                                    <Row className="py-2 border-bottom"><Col md={6}><strong>Username:</strong></Col><Col md={6}>{profileData.username}</Col></Row>
                                    <Row className="py-2 border-bottom"><Col md={6}><strong>Email:</strong></Col><Col md={6}>{profileData.email}</Col></Row>
                                    <Row className="py-2 border-bottom"><Col md={6}><strong>Name:</strong></Col><Col md={6}>{profileData.fName} {profileData.lName}</Col></Row>
                                    <Row className="py-2 border-bottom"><Col md={6}><strong>Address:</strong></Col><Col md={6}>{profileData.address}</Col></Row>
                                    <Row className="py-2 border-bottom"><Col md={6}><strong>Phone Number:</strong></Col><Col md={6}>{formatPhoneNumber(profileData.phoneNo)}</Col></Row>
                                    <Row className="py-2"><Col md={6}><strong>Date of Birth:</strong></Col><Col md={6}>{profileData.dateOfBirth}</Col></Row>
                                </>
                            )}
                        </Form>
                    </Card.Body>
                </Card>

                <Card className="p-4">
                    <Card.Title className="fw-bold border-bottom pb-2" style={{ borderColor: "#006649", borderBottom: "3px solid #006649" }}>Password</Card.Title>
                    <Card.Body>
                        <Row className="py-2 align-items-center">
                            <Col md={6}><strong>Password:</strong></Col>
                            <Col md={6} className="d-flex justify-content-between align-items-center">
                                •••••••
                                <Button variant="link" onClick={() => { setBackendError(""); setBackendSuccess(""); setShowPasswordModal(true); }} style={{ padding: 0 }}>Change</Button>
                            </Col>
                        </Row>
                    </Card.Body>
                </Card>
            </Container>

            <Modal show={showPasswordModal} onHide={() => setShowPasswordModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>Change your password</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <p>
                        An email with a link to change your password will be sent to your address:
                        <br /><strong>{profileData.email}</strong>
                    </p>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowPasswordModal(false)}>Cancel</Button>
                    <Button
                        style={{ backgroundColor: "#006649" }}
                        disabled={isSending}
                        onClick={async () => {
                            try {
                                setIsSending(true);
                                const response = await fetch("http://localhost:8080/auth/request-password-reset", {
                                    method: "POST",
                                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                                    credentials: "include",
                                    body: new URLSearchParams({ email: profileData.email }),
                                });
                                const message = await response.text();
                                setBackendSuccess(message);
                                setShowPasswordModal(false);
                            } catch {
                                setBackendError("Failed to request password change.");
                            } finally {
                                setIsSending(false);
                            }
                        }}
                    >
                        {isSending ? "Sending..." : "Send"}
                    </Button>
                </Modal.Footer>
            </Modal>


            <Modal show={showEmailModal} onHide={() => setShowEmailModal(false)} centered>
                <Modal.Header closeButton><Modal.Title>Change your email address</Modal.Title></Modal.Header>
                <Modal.Body>
                    <p>
                        An email with a link to change your email address will be sent to your current address:
                        <br /><strong>{profileData.email}</strong>
                    </p>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowEmailModal(false)}>Cancel</Button>
                    <Button
                        style={{ backgroundColor: "#006649" }}
                        disabled={isSending}
                        onClick={async () => {
                            try {
                                setIsSending(true);
                                const response = await fetch("http://localhost:8080/auth/request-email-change", {
                                    method: "POST",
                                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                                    credentials: "include",
                                    body: new URLSearchParams({
                                        currentEmail: profileData.email,
                                        newEmail: profileData.email,
                                    }),
                                });
                                const message = await response.text();
                                setBackendSuccess(message);
                                setShowEmailModal(false);
                            } catch {
                                setBackendError("Failed to request email change.");
                            } finally {
                                setIsSending(false);
                            }
                        }}
                    >
                        {isSending ? "Sending..." : "Send"}
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
}

export default Profile;
