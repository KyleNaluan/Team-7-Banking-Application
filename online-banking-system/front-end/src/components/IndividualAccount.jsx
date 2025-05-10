import React, { useEffect, useState } from "react";
import NavBar from "./NavBar";
import LoadingScreen from "./LoadingScreen";
import { useParams } from "react-router-dom";
import { Button, Container, Row, Col, Modal, Form, Toast, ToastContainer, Card, Spinner } from "react-bootstrap";
import TransactionCard from "./TransactionCard";

function IndividualAccount() {
    const { accountId } = useParams();
    const [showAddModal, setShowAddModal] = useState(false);
    const [showWithdrawModal, setShowWithdrawModal] = useState(false);
    const [amount, setAmount] = useState("");
    const [account, setAccount] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [isLoading, setIsLoading] = useState(true);
    const [isTxLoading, setIsTxLoading] = useState(true);

    useEffect(() => {
        fetchAccountDetails();
    }, [accountId]);

    useEffect(() => {
        if (account) fetchTransactions();
    }, [account]);

    useEffect(() => {
        if (success) {
            const timer = setTimeout(() => setSuccess(""), 4000);
            return () => clearTimeout(timer);
        }
    }, [success]);

    useEffect(() => {
        if (error) {
            const timer = setTimeout(() => setError(""), 4000);
            return () => clearTimeout(timer);
        }
    }, [error]);

    const fetchAccountDetails = async () => {
        try {
            setIsLoading(true);
            const res = await fetch(`http://localhost:8080/api/accounts/${accountId}`, { credentials: "include" });
            const data = await res.json();
            setAccount(data);
        } catch (err) {
            setError("Failed to load account.");
        } finally {
            setIsLoading(false);
        }
    };

    const fetchTransactions = async () => {
        try {
            setIsTxLoading(true);
            const txRes = await fetch(`http://localhost:8080/api/transactions/account/${accountId}`, { credentials: "include" });
            const txData = await txRes.json();

            const sixMonthsAgo = new Date();
            sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6);

            const recentTransactions = txData.filter(tx => new Date(tx.transactionDate) >= sixMonthsAgo);
            setTransactions(recentTransactions);
        } catch (err) {
            setError("Failed to load transactions.");
        } finally {
            setIsTxLoading(false);
        }
    };

    const handleDeletePayment = async (paymentId) => {
        try {
            const res = await fetch(`http://localhost:8080/api/payments/${paymentId}`, {
                method: "DELETE",
                credentials: "include"
            });

            if (!res.ok) {
                const err = await res.json();
                throw new Error(err.error || "Failed to delete payment.");
            }

            await fetchTransactions();
            setSuccess("Payment deleted successfully.");
        } catch (err) {
            setError(err.message || "An error occurred while deleting the payment.");
        }
    };

    const handleDeposit = async () => {
        try {
            setError("");
            setSuccess("");
            const parsedAmount = parseFloat(amount);

            if (isNaN(parsedAmount) || parsedAmount <= 0) {
                setError("Please enter a valid positive amount.");
                return;
            }

            const response = await fetch(`http://localhost:8080/api/deposits`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({
                    accountId: accountId,
                    amount: parsedAmount
                })
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || "Failed to add balance.");
            }

            await fetchAccountDetails();
            setShowAddModal(false);
            setAmount("");
            setSuccess("Balance added successfully!");
        } catch (err) {
            setError(err.message || "Failed to add balance. Please try again.");
        }
    };

    const handleWithdraw = async () => {
        try {
            setError("");
            setSuccess("");
            const parsedAmount = parseFloat(amount);

            if (isNaN(parsedAmount) || parsedAmount <= 0) {
                setError("Please enter a valid positive amount.");
                return;
            }

            const response = await fetch(`http://localhost:8080/api/withdrawals`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({
                    accountId: accountId,
                    amount: parsedAmount
                })
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || "Failed to withdraw.");
            }

            await fetchAccountDetails();
            setShowWithdrawModal(false);
            setAmount("");
            setSuccess("Withdrawal successful!");
        } catch (err) {
            setError(err.message || "Failed to withdraw. Please try again.");
        }
    };

    return (

        <div style={{ backgroundColor: "#f5f5f5", minHeight: "100vh" }}>
            <NavBar />

            <ToastContainer position="bottom-end" className="p-3">
                <Toast bg="success" onClose={() => setSuccess("")} show={!!success} delay={4000} autohide>
                    <Toast.Body className="text-white">{success}</Toast.Body>
                </Toast>
                <Toast bg="danger" onClose={() => setError("")} show={!!error} delay={4000} autohide>
                    <Toast.Body className="text-white">{error}</Toast.Body>
                </Toast>
            </ToastContainer>

            {isLoading ? (
                <LoadingScreen />
            ) : (
                <Container fluid className="mt-4 px-4">
                    <Row>
                        <Col md={4} className="d-flex flex-column justify-content-between">
                            {account && (
                                <Card className="h-100 shadow-sm d-flex flex-column">
                                    <Card.Header className="fw-bold" style={{ backgroundColor: "#f6f5f5" }}>
                                        {account.accountType.toUpperCase()} *{account.accountID.toString().slice(-4)}
                                    </Card.Header>
                                    <Card.Body>
                                        <div className="mb-2 d-flex justify-content-between">
                                            <span className="text-muted">ACCOUNT</span>
                                            <span>{account.accountID}</span>
                                        </div>
                                        <div className="mb-2 d-flex justify-content-between">
                                            <span className="text-muted">Available</span>
                                            <span>${account.accountBalance.toFixed(2)}</span>
                                        </div>
                                        <div className="mb-2 d-flex justify-content-between">
                                            <span className="text-muted">Current</span>
                                            <span>${account.accountBalance.toFixed(2)}</span>
                                        </div>
                                    </Card.Body>
                                    <Card.Footer style={{ backgroundColor: "#f8f9fa" }}>
                                        <Button size="lg" className="w-100 mb-2" onClick={() => setShowAddModal(true)} style={{ backgroundColor: '#006649', color: "white" }}>Add Balance</Button>
                                        <Button size="lg" className="w-100" onClick={() => setShowWithdrawModal(true)} style={{ backgroundColor: '#B22222', color: "white" }}>Withdraw Money</Button>
                                    </Card.Footer>
                                </Card>
                            )}
                        </Col>
                        <Col md={8} className="ps-4">
                            <Card className="shadow-sm d-flex flex-column" style={{ height: "85vh" }}>
                                <Card.Header className="fw-bold" style={{ backgroundColor: "#f6f5f5" }}>Transaction History</Card.Header>
                                <Card.Body style={{ overflowY: "auto" }}>
                                    {isTxLoading ? (
                                        <div className="d-flex justify-content-center mt-4">
                                            <Spinner animation="border" variant="success" role="status">
                                                <span className="visually-hidden">Loading...</span>
                                            </Spinner>
                                        </div>
                                    ) : transactions.length === 0 ? (
                                        <p className="text-muted">No transactions found.</p>
                                    ) : (
                                        transactions.map((tx) => (
                                            <TransactionCard
                                                key={tx.transactionID}
                                                type={tx.type}
                                                amount={tx.amount}
                                                date={tx.transactionDate}
                                                time={tx.transactionTime}
                                                note={tx.note || ""}
                                                extraInfo={tx.category || ""}
                                                onDelete={tx.type === "Payment" ? () => handleDeletePayment(tx.paymentId) : undefined}
                                            />
                                        ))
                                    )}
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>

                    <Modal show={showAddModal} onHide={() => setShowAddModal(false)} centered>
                        <Modal.Header closeButton><Modal.Title>Add Balance</Modal.Title></Modal.Header>
                        <Modal.Body>
                            <Form onSubmit={(e) => { e.preventDefault(); handleDeposit(); }}>
                                <Form.Group controlId="balanceAmount">
                                    <Form.Label>Amount</Form.Label>
                                    <Form.Control
                                        type="number"
                                        min="0.01"
                                        step="0.01"
                                        placeholder="Enter amount"
                                        value={amount}
                                        onChange={(e) => {
                                            const value = e.target.value;
                                            if (/^\d{0,6}(\.\d{0,2})?$/.test(value)) {
                                                setAmount(value);
                                            }
                                        }}
                                        onPaste={(e) => {
                                            const paste = e.clipboardData.getData('text');
                                            if (!/^\d{0,6}(\.\d{0,2})?$/.test(paste)) {
                                                e.preventDefault();
                                            }
                                        }}
                                        onKeyDown={(e) => {
                                            if (e.key === "-" || e.key === "e") {
                                                e.preventDefault();
                                            }
                                        }}
                                    />
                                </Form.Group>
                            </Form>
                        </Modal.Body>
                        <Modal.Footer>
                            <Button variant="secondary" onClick={() => setShowAddModal(false)}>Cancel</Button>
                            <Button onClick={handleDeposit} style={{ backgroundColor: '#006649', color: "white" }} disabled={!amount || isNaN(amount) || parseFloat(amount) <= 0}>Confirm</Button>
                        </Modal.Footer>
                    </Modal>

                    <Modal show={showWithdrawModal} onHide={() => setShowWithdrawModal(false)} centered>
                        <Modal.Header closeButton><Modal.Title>Withdraw Money</Modal.Title></Modal.Header>
                        <Modal.Body>
                            <Form onSubmit={(e) => { e.preventDefault(); handleWithdraw(); }}>
                                <Form.Group controlId="withdrawAmount">
                                    <Form.Label>Amount</Form.Label>
                                    <Form.Control
                                        type="number"
                                        min="0.01"
                                        step="0.01"
                                        placeholder="Enter amount"
                                        value={amount}
                                        onChange={(e) => {
                                            const value = e.target.value;
                                            if (/^\d{0,6}(\.\d{0,2})?$/.test(value)) {
                                                setAmount(value);
                                            }
                                        }}
                                        onPaste={(e) => {
                                            const paste = e.clipboardData.getData('text');
                                            if (!/^\d{0,6}(\.\d{0,2})?$/.test(paste)) {
                                                e.preventDefault();
                                            }
                                        }}
                                        onKeyDown={(e) => {
                                            if (e.key === "-" || e.key === "e") {
                                                e.preventDefault();
                                            }
                                        }}
                                    />
                                </Form.Group>
                            </Form>
                        </Modal.Body>
                        <Modal.Footer>
                            <Button variant="secondary" onClick={() => setShowWithdrawModal(false)}>Cancel</Button>
                            <Button onClick={handleWithdraw} style={{ backgroundColor: '#B22222', color: "white" }} disabled={!amount || isNaN(amount) || parseFloat(amount) <= 0}>Confirm</Button>
                        </Modal.Footer>
                    </Modal>
                </Container>
            )}
        </div>
    );
}

export default IndividualAccount;
