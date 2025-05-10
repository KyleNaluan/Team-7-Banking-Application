import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Container, Form, Row, Col, Button, Card, InputGroup, Toast, ToastContainer } from "react-bootstrap";
import NavBar from "./NavBar";

function Transactions() {
    const [accounts, setAccounts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [formData, setFormData] = useState({
        sourceAccount: "",
        transactionNote: "",
        transactionCategory: "",
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
            const accRes = await fetch(`http://localhost:8080/api/accounts/customer/${user.customerID}`, { credentials: 'include' });
            const accountsData = await accRes.json();
            setAccounts(accountsData);
        } catch (err) {
            setError("Failed to load accounts.");
        }
    };

    useEffect(() => {
        async function loadCategories() {
            try {
                const catRes = await fetch("http://localhost:8080/api/categories", { credentials: "include" });
                const categoriesData = await catRes.json();

                setCategories(categoriesData);
            } catch (err) {
                setError("Failed to load accounts or categories.");
            }
        }

        loadCategories();
        fetchAccounts();
    }, []);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSuccess(false);

        const { sourceAccount, transactionCategory, amount } = formData;

        if (!sourceAccount || !transactionCategory) {
            setError("Please select both an account and a category.");
            return;
        }

        const numericAmount = parseFloat(amount);
        if (isNaN(numericAmount) || numericAmount <= 0 || !/^\d+(\.\d{1,2})?$/.test(amount)) {
            setError("Please enter a valid amount greater than 0 with up to 2 decimal places.");
            return;
        }

        try {
            const formBody = new URLSearchParams();
            formBody.append("sourceAccount", formData.sourceAccount);
            formBody.append("transactionCategory", formData.transactionCategory);
            formBody.append("amount", numericAmount);
            if (formData.transactionNote) {
                formBody.append("comment", formData.transactionNote);
            }

            const res = await fetch("http://localhost:8080/api/payments", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                credentials: "include",
                body: formBody.toString()
            });

            if (!res.ok) {
                const err = await res.json();
                if (err.message) {
                    throw new Error(err.message);
                } else {
                    throw new Error("An unknown error occurred.");
                }
            }

            navigate("/dashboard", { state: { showToast: "Payment Successful!" } });

        } catch (err) {
            setError(err.message || "An error occurred during the transaction.");
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
                <h3 className="fw-bold mb-3">New Payment</h3>
                <p className="text-muted">Please fill in the details for your payment or expense</p>

                <Card className="p-4 shadow-sm">
                    <Form onSubmit={handleSubmit}>
                        <h5 className="mb-3 fw-semibold">Payment Details</h5>

                        <Form.Group className="mb-3" controlId="sourceAccount">
                            <Form.Label>Select Source Bank</Form.Label>
                            <Form.Select name="sourceAccount" onChange={handleChange} value={formData.sourceAccount}>
                                <option value="">Select Account</option>
                                {accounts
                                    .filter(acc => acc.accountType.toLowerCase() === "checking")
                                    .map(acc => (
                                        <option key={acc.accountID} value={acc.accountID}>
                                            {renderAccountOption(acc)}
                                        </option>
                                    ))}
                            </Form.Select>
                        </Form.Group>

                        <Form.Group className="mb-3" controlId="transactionNote">
                            <Form.Label>Payment Note (Optional)</Form.Label>
                            <Form.Control
                                as="textarea"
                                rows={3}
                                placeholder="Write a note..."
                                name="transactionNote"
                                onChange={handleChange}
                                value={formData.transactionNote}
                            />
                        </Form.Group>

                        <Form.Group className="mb-3" controlId="transactionCategory">
                            <Form.Label>Payment Category</Form.Label>
                            <Form.Select
                                name="transactionCategory"
                                onChange={handleChange}
                                value={formData.transactionCategory}
                            >
                                <option value="">Select Category</option>
                                {categories.map((cat) => (
                                    <option key={cat.categoryID} value={cat.categoryID}>
                                        {cat.categoryName}
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

                        <Button
                            type="submit"
                            className="w-100"
                            style={{ backgroundColor: "#006649", color: "white" }}
                        >
                            Submit Payment
                        </Button>
                    </Form>
                </Card>
            </Container>
        </div>
    );
}

export default Transactions;
