import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Container, Form, Row, Col, Button, Card, InputGroup, Toast, ToastContainer } from "react-bootstrap";
import NavBar from "./NavBar";

function Transfer() {
    const [accounts, setAccounts] = useState([]);
    const [formData, setFormData] = useState({
        sourceAccount: "",
        transferNote: "",
        receivingAccount: "",
        amount: "",
    });

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const navigate = useNavigate();

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

    const fetchAccounts = async () => {
        try {
            const userRes = await fetch('http://localhost:8080/auth/me', { credentials: 'include' });
            const user = await userRes.json();
            const accountsRes = await fetch(`http://localhost:8080/api/accounts/customer/${user.customerID}`, { credentials: 'include' });
            const data = await accountsRes.json();
            setAccounts(data);
        } catch (err) {
            setError("Failed to load accounts.");
        }
    };

    useEffect(() => {
        fetchAccounts();
    }, []);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSuccess("");

        const { sourceAccount, receivingAccount, amount } = formData;

        if (!sourceAccount || !receivingAccount) {
            setError("Please select both a source and recipient account.");
            return;
        }

        const numericAmount = parseFloat(amount);
        if (isNaN(numericAmount) || numericAmount <= 0 || !/^\d+(\.\d{1,2})?$/.test(amount)) {
            setError("Please enter a valid amount greater than 0 with up to 2 decimal places.");
            return;
        }

        try {
            const res = await fetch("http://localhost:8080/api/transfer", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({
                    sourceAccount,
                    receivingAccount,
                    amount: numericAmount,
                    comment: formData.transferNote,
                })
            });

            const result = await res.json();

            if (!res.ok) {
                throw new Error(result.error || "Transfer failed.");
            }

            navigate("/dashboard", { state: { showToast: "Transfer successful!" } });

        } catch (err) {
            setError(err.message);
        }
    };

    const renderAccountOption = (acc) =>
        `${acc.accountType} - ****${acc.accountID.toString().slice(-4)} ($${acc.accountBalance.toFixed(2)})`;

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

            <Container className="mt-4">
                <h3 className="fw-bold mb-3">Payment Transfer</h3>
                <p className="text-muted">Please provide any specific details or notes related to the payment transfer</p>

                <Card className="p-4 shadow-sm">
                    <Form onSubmit={handleSubmit}>
                        <h5 className="mb-3 fw-semibold">Transfer Details</h5>

                        <Form.Group className="mb-3" controlId="sourceAccount">
                            <Form.Label>Source Bank Account</Form.Label>
                            <Form.Select name="sourceAccount" onChange={handleChange} value={formData.sourceAccount}>
                                <option value="">Select Account</option>
                                {accounts.map((acc) => (
                                    <option key={acc.accountID} value={acc.accountID}>
                                        {renderAccountOption(acc)}
                                    </option>
                                ))}
                            </Form.Select>
                        </Form.Group>

                        <Form.Group className="mb-3" controlId="transferNote">
                            <Form.Label>Transfer Note (Optional)</Form.Label>
                            <Form.Control
                                as="textarea"
                                rows={3}
                                placeholder="Write a message..."
                                name="transferNote"
                                onChange={handleChange}
                                value={formData.transferNote}
                            />
                        </Form.Group>

                        <Form.Group className="mb-3" controlId="receivingAccount">
                            <Form.Label>Recipient Bank Account</Form.Label>
                            <Form.Select name="receivingAccount" onChange={handleChange} value={formData.receivingAccount}>
                                <option value="">Select Account</option>
                                {accounts.map((acc) => (
                                    <option key={acc.accountID} value={acc.accountID}>
                                        {renderAccountOption(acc)}
                                    </option>
                                ))}
                            </Form.Select>
                        </Form.Group>

                        <Form.Group className="mb-4" controlId="amount">
                            <Form.Label>Amount</Form.Label>
                            <InputGroup>
                                <InputGroup.Text>$</InputGroup.Text>
                                <Form.Control
                                    type="number"
                                    step="0.01"
                                    min="0"
                                    placeholder="0.00"
                                    name="amount"
                                    onChange={handleChange}
                                    value={formData.amount}
                                />
                            </InputGroup>
                        </Form.Group>

                        <Button type="submit" className="w-100" style={{ backgroundColor: '#006649', color: "white" }}>
                            Transfer Funds
                        </Button>
                    </Form>
                </Card>
            </Container>
        </div>
    );
}

export default Transfer;
