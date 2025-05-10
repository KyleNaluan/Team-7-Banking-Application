import React, { useState, useEffect } from "react";
import AccountBox from "./AccountBox";
import NavBar from "./NavBar";
import LoadingScreen from "./LoadingScreen";
import {
    Button,
    Container,
    Form,
    Modal,
    Card,
    Toast,
    ToastContainer
} from "react-bootstrap";

function Accounts() {
    const [showModal, setShowModal] = useState(false);
    const [accountType, setAccountType] = useState("Checking");
    const [initialBalance, setInitialBalance] = useState("");
    const [accounts, setAccounts] = useState([]);
    const [success, setSuccess] = useState("");
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        fetch("http://localhost:8080/auth/me", { credentials: "include" })
            .then(res => res.ok ? res.json() : Promise.reject("Unauthorized"))
            .then(user => {
                localStorage.setItem("user", JSON.stringify(user));
                return fetch(`http://localhost:8080/api/accounts/customer/${user.customerID}`, {
                    credentials: "include"
                });
            })
            .then(res => res.json())
            .then(data => setAccounts(data))
            .catch(() => setError("Failed to fetch accounts."))
            .finally(() => setIsLoading(false));
    }, []);

    useEffect(() => {
        if (success) {
            const timer = setTimeout(() => setSuccess(""), 4000);
            return () => clearTimeout(timer);
        }
    }, [success]);

    useEffect(() => {
        if (error) {
            const timer = setTimeout(() => setError(""), 6000);
            return () => clearTimeout(timer);
        }
    }, [error]);

    const handleCreateAccount = async () => {
        setError("");
        const user = JSON.parse(localStorage.getItem("user"));
        const newAccount = {
            accountType,
            accountBalance: parseFloat(initialBalance)
        };

        try {
            const response = await fetch("http://localhost:8080/api/accounts/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(newAccount),
                credentials: "include"
            });

            if (!response.ok) {
                const msg = await response.text();
                throw new Error(msg || "Account creation failed");
            }

            const created = await response.json();
            setAccounts([...accounts, created]);
            setSuccess("Account created successfully!");
            setShowModal(false);
            setInitialBalance("");
        } catch (err) {
            setError(err.message || "Failed to create account.");
        }
    };

    return (
        <div style={{ backgroundColor: "#f5f5f5", minHeight: "100vh" }}>
            <NavBar />

            <ToastContainer position="bottom-end" className="p-3">
                <Toast bg="success" onClose={() => setSuccess("")} show={!!success} delay={4000} autohide>
                    <Toast.Body className="text-white">{success}</Toast.Body>
                </Toast>
                <Toast bg="danger" onClose={() => setError("")} show={!!error} delay={6000} autohide>
                    <Toast.Body className="text-white">{error}</Toast.Body>
                </Toast>
            </ToastContainer>

            {isLoading ? (
                <LoadingScreen />
            ) : (
                <Container className="mt-5 d-flex flex-column align-items-center">
                    <Card className="w-75 shadow-sm d-flex flex-column" style={{ height: "600px" }}>
                        <Card.Header
                            className="text-center border-bottom"
                            style={{ backgroundColor: "#f6f5f5", position: "sticky", top: 0, zIndex: 1 }}
                        >
                            <Card.Title className="fw-bold fs-4 mb-0">Accounts</Card.Title>
                        </Card.Header>
                        <div className="d-flex flex-column flex-grow-1" style={{ overflow: "hidden" }}>
                            <div style={{ overflowY: "auto", padding: "1rem", flex: 1 }}>
                                {accounts.map(account => (
                                    <AccountBox
                                        key={account.accountID}
                                        type={`${account.accountType.toUpperCase()} *${account.accountID.toString().slice(-4)}`}
                                        available={account.accountBalance}
                                        current={account.accountBalance}
                                        accountId={account.accountID}
                                    />
                                ))}
                            </div>
                            <Card.Footer style={{ backgroundColor: "#f8f9fa", position: "sticky", bottom: 0, zIndex: 1 }}>
                                <div className="d-flex justify-content-center">
                                    <Button
                                        size="lg"
                                        onClick={() => setShowModal(true)}
                                        style={{ backgroundColor: '#006649', color: "white" }}
                                    >
                                        Create Account
                                    </Button>
                                </div>
                            </Card.Footer>
                        </div>
                    </Card>

                    <Modal show={showModal} onHide={() => setShowModal(false)} centered>
                        <Modal.Header closeButton>
                            <Modal.Title>Create New Account</Modal.Title>
                        </Modal.Header>
                        <Form onSubmit={(e) => { e.preventDefault(); handleCreateAccount(); }}>
                            <Modal.Body>
                                <Form.Group controlId="accountType" className="mb-3">
                                    <Form.Label>Account Type</Form.Label>
                                    <Form.Select
                                        value={accountType}
                                        onChange={(e) => setAccountType(e.target.value)}
                                    >
                                        <option>Checking</option>
                                        <option>Savings</option>
                                    </Form.Select>
                                </Form.Group>

                                <Form.Group controlId="initialBalance">
                                    <Form.Label>Initial Balance</Form.Label>
                                    <Form.Control
                                        type="number"
                                        placeholder="Enter amount"
                                        value={initialBalance}
                                        onChange={(e) => setInitialBalance(e.target.value)}
                                    />
                                </Form.Group>
                            </Modal.Body>
                            <Modal.Footer>
                                <Button variant="secondary" onClick={() => setShowModal(false)}>Cancel</Button>
                                <Button type="submit" style={{ backgroundColor: '#006649', color: "white" }}>
                                    Confirm
                                </Button>
                            </Modal.Footer>
                        </Form>
                    </Modal>

                </Container>
            )}
        </div>
    );
}

export default Accounts;
