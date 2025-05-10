import React, { useState, useEffect, useMemo } from "react";
import NavBar from "./NavBar";
import LoadingScreen from "./LoadingScreen";
import {
    Card, Row, Col, Button, ProgressBar, Dropdown, DropdownButton,
    Modal, Form, Toast, ToastContainer
} from "react-bootstrap";
import { Doughnut } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
import { marked } from "marked";

ChartJS.register(ArcElement, Tooltip, Legend);

function BudgetOverview() {
    const [chartData, setChartData] = useState({ labels: [], datasets: [{ data: [], backgroundColor: [], borderWidth: 1 }] });
    const [categoryBudgets, setCategoryBudgets] = useState([]);
    const [totalBudget, setTotalBudget] = useState(null);
    const [timeRange, setTimeRange] = useState("Past 6 Months");
    const [categorySpending, setCategorySpending] = useState([]);
    const [currentMonthCategorySpending, setCurrentMonthCategorySpending] = useState([]);
    const [totalSpent, setTotalSpent] = useState(0);
    const [currentMonthSpent, setCurrentMonthSpent] = useState(0);
    const [showModal, setShowModal] = useState(false);
    const [selectedCategory, setSelectedCategory] = useState("Total");
    const [budgetAmount, setBudgetAmount] = useState("");
    const [categories, setCategories] = useState([]);
    const [aiAnalysis, setAiAnalysis] = useState("");
    const [loadingAnalysis, setLoadingAnalysis] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [isLoading, setIsLoading] = useState(true);

    const cleanMarkdown = (aiAnalysis || "Click Generate Analysis.")
        .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
        .replace(/(^|\n)\* (.*)/g, '$1• $2')
        .replace(/\n{2,}/g, '\n')
        .replace(/\n/g, "<br/>");

    const rendered = marked.parseInline(cleanMarkdown);

    useEffect(() => {
        const loadAll = async () => {
            try {
                await Promise.all([
                    fetchBudgets(),
                    fetchCategories(),
                    fetchSpendingStats()
                ]);
            } catch (err) {
                console.error("Error loading budget overview:", err);
            } finally {
                setIsLoading(false);
            }
        };

        loadAll();
    }, []);

    useEffect(() => {
        fetchSpendingStats();
    }, [timeRange]);

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

    const chartColors = ["#ff0000", "#00ff00", "#0000ff", "#ffff00", "#ffa500", "#800080", "#008080", "#ff69b4", "#a52a2a", "#2e8b57"];

    const fetchBudgets = async () => {
        const totalRes = await fetch("http://localhost:8080/api/totalbudgets", { credentials: "include" });
        const totalData = await totalRes.json();
        const now = new Date();
        const current = totalData.find(b =>
            new Date(b.year, b.month - 1).getMonth() === now.getMonth() &&
            new Date(b.year, b.month - 1).getFullYear() === now.getFullYear()
        );
        setTotalBudget(current || null);

        const categoryRes = await fetch("http://localhost:8080/api/categorybudgets", { credentials: "include" });
        const categoryData = await categoryRes.json();
        setCategoryBudgets(categoryData);
    };

    const fetchCategories = async () => {
        const res = await fetch("http://localhost:8080/api/categories", { credentials: "include" });
        const data = await res.json();
        setCategories(data);
    };

    const fetchSpendingStats = async () => {
        try {
            const statsRes = await fetch(`http://localhost:8080/api/paymentstats/totals?period=${encodeURIComponent(timeRange)}`, { credentials: "include" });
            const statsData = await statsRes.json();
            setCategorySpending(statsData.categorySpending || []);
            setTotalSpent(statsData.totalSpent || 0);

            const currentRes = await fetch("http://localhost:8080/api/paymentstats/current-month", { credentials: "include" });
            const currentData = await currentRes.json();
            setCurrentMonthSpent(currentData.currentMonthSpent || 0);

            const currentCatRes = await fetch("http://localhost:8080/api/paymentstats/current-month-categories", { credentials: "include" });
            const currentCatData = await currentCatRes.json();
            setCurrentMonthCategorySpending(currentCatData || []);
        } catch (error) {
            console.error("Failed to fetch stats:", error);
        }
    };

    const handleSubmitBudget = async () => {
        setError("");
        const amount = parseFloat(budgetAmount);
        if (isNaN(amount) || amount <= 0) {
            setError("Please enter a valid amount.");
            return;
        }

        const url = selectedCategory === "Total"
            ? "http://localhost:8080/api/totalbudgets"
            : "http://localhost:8080/api/categorybudgets";

        const body = selectedCategory === "Total"
            ? new URLSearchParams({ amount })
            : new URLSearchParams({ categoryId: selectedCategory, amount });

        const res = await fetch(url, {
            method: "POST",
            body,
            credentials: "include",
            headers: { "Content-Type": "application/x-www-form-urlencoded" }
        });

        const result = await res.json();
        if (!res.ok) {
            setError(result.error || "Failed to set budget.");
            return;
        }

        setSuccess("Budget updated successfully!");
        setShowModal(false);
        setBudgetAmount("");
        fetchBudgets();
    };

    const totalLimit = totalBudget?.monthlyTotalLimit ?? 0;
    const totalPercentUsed = totalLimit > 0 ? (currentMonthSpent / totalLimit) * 100 : 0;

    const fetchAiAnalysis = async () => {
        setLoadingAnalysis(true);
        setError("");
        try {
            const res = await fetch("http://localhost:8080/api/ai/analyze", {
                method: "POST",
                credentials: "include"
            });
            const data = await res.json();
            if (!res.ok || !data.analysis) {
                setError("AI analysis failed to generate.");
            }
            setAiAnalysis(data.analysis || "No feedback received.");
        } catch (error) {
            console.error("Failed to fetch AI analysis:", error);
            setError("Could not generate analysis at this time.");
        } finally {
            setLoadingAnalysis(false);
        }
    };

    const getBarColor = percent => `hsl(${Math.max(0, 120 - percent)}, 100%, 45%)`;

    const mergedSpending = useMemo(() => {
        return categories.map((cat, idx) => {
            const match = categorySpending.find(e => e.category === cat.categoryName);
            return {
                category: cat.categoryName,
                amount: match ? parseFloat(match.amount) : 0,
                color: chartColors[idx % chartColors.length]
            };
        });
    }, [categories, categorySpending]);

    const mergedCurrentSpending = useMemo(() => {
        return categories.map(cat => {
            const match = currentMonthCategorySpending.find(e => e.category === cat.categoryName);
            return {
                category: cat.categoryName,
                amount: match ? parseFloat(match.amount) : 0
            };
        });
    }, [categories, currentMonthCategorySpending]);

    useEffect(() => {
        setChartData({
            labels: mergedSpending.map(e => e.category),
            datasets: [{
                data: mergedSpending.map(e => e.amount),
                backgroundColor: mergedSpending.map(e => e.color),
                borderWidth: 1
            }]
        });
    }, [mergedSpending]);

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
                <div className="px-4 py-3" style={{ minHeight: "90vh" }}>
                    <Row className="d-flex align-items-stretch h-100">
                        <Col md={7} className="d-flex flex-column justify-content-between">
                            <Card className="mb-3 flex-grow-1" style={{ minHeight: "430px", maxHeight: "430px" }}>
                                <Card.Body>
                                    <div className="d-flex justify-content-between align-items-center mb-2">
                                        <h5 className="mb-0">Spending</h5>
                                        <DropdownButton title={timeRange} onSelect={setTimeRange} variant="outline-secondary" size="sm">
                                            <Dropdown.Item eventKey="Current Month">Current Month</Dropdown.Item>
                                            <Dropdown.Item eventKey="Past 7 Days">Past 7 Days</Dropdown.Item>
                                            <Dropdown.Item eventKey="Past 30 Days">Past 30 Days</Dropdown.Item>
                                            <Dropdown.Item eventKey="Past 6 Months">Past 6 Months</Dropdown.Item>
                                        </DropdownButton>
                                    </div>
                                    <div style={{ height: "320px" }}>
                                        <Doughnut
                                            data={chartData}
                                            options={{ maintainAspectRatio: false, plugins: { legend: { display: false } } }}
                                        />
                                    </div>
                                    <h6 className="text-center mt-3">Total Spending ${totalSpent.toFixed(2)}</h6>
                                </Card.Body>
                            </Card>

                            <Card style={{ height: "200px", overflow: "hidden", display: "flex", flexDirection: "column" }}>
                                <Card.Header
                                    className="fw-bold text-white"
                                    style={{
                                        backgroundColor: "#006649",
                                        position: "sticky",
                                        top: 0,
                                        zIndex: 1
                                    }}
                                >
                                    Spending Categories
                                </Card.Header>
                                <Card.Body className="p-2" style={{ overflowY: "auto" }}>
                                    <ul className="list-unstyled mb-0">
                                        {mergedSpending.map((entry, idx) => (
                                            <li key={idx} className="d-flex justify-content-between border-bottom py-1">
                                                <span>
                                                    <span style={{ color: entry.color }}>⬤</span> {entry.category}
                                                </span>
                                                <span>${entry.amount.toFixed(2)}</span>
                                            </li>
                                        ))}
                                    </ul>
                                </Card.Body>
                            </Card>
                        </Col>

                        <Col md={5} className="d-flex flex-column">
                            <Card
                                className="mb-3"
                                style={{ flex: 1, minHeight: "315px", maxHeight: "315px", display: "flex", flexDirection: "column" }}
                            >

                                <Card.Header
                                    style={{
                                        backgroundColor: "#f6f5f5",
                                        position: "sticky",
                                        top: 0,
                                        zIndex: 1
                                    }}
                                >
                                    <Card.Title className="fw-bold">Budget Goals</Card.Title>
                                    <p className="mb-1 text-muted">
                                        ({new Date(new Date().getFullYear(), new Date().getMonth(), 1).toLocaleDateString()} -{" "}
                                        {new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).toLocaleDateString()})
                                    </p>
                                </Card.Header>
                                <Card.Body style={{ overflowY: "auto", paddingTop: 0 }}>
                                    <div
                                        className="bg-white"
                                        style={{
                                            position: "sticky",
                                            top: 0,
                                            zIndex: 2,
                                            paddingTop: "0.5rem",
                                        }}
                                    >
                                    </div>
                                    <strong className="d-block mt-3 mb-2">Total Budget</strong>
                                    {totalBudget ? (
                                        <>
                                            <ProgressBar now={totalPercentUsed} style={{ backgroundColor: "#e0e0e0", height: "20px" }}>
                                                <div className="progress-bar" style={{ width: `${totalPercentUsed}%`, backgroundColor: getBarColor(totalPercentUsed), color: "#fff" }} />
                                            </ProgressBar>
                                            <div className="text-end text-dark fw-semibold mb-3">
                                                ${currentMonthSpent.toFixed(2)} of ${totalLimit.toFixed(2)}
                                            </div>
                                        </>
                                    ) : (
                                        <p className="text-muted">No total budget set for this month</p>
                                    )}

                                    <hr />

                                    {categoryBudgets.map((b, idx) => {
                                        const catName = b.category?.categoryName || "Unnamed Category";
                                        const limit = parseFloat(b.monthlyLimit || 0);
                                        const spent = mergedCurrentSpending.find(e => e.category === catName)?.amount || 0;
                                        const percentUsed = limit > 0 ? (spent / limit) * 100 : 0;
                                        return (
                                            <div key={idx} className="mb-3">
                                                <strong className="d-block mb-1">{catName}</strong>
                                                <ProgressBar now={percentUsed} style={{ backgroundColor: "#e0e0e0", height: "20px" }}>
                                                    <div className="progress-bar" style={{ width: `${percentUsed}%`, backgroundColor: getBarColor(percentUsed), color: "#fff" }} />
                                                </ProgressBar>
                                                <div className="text-end text-dark fw-semibold">
                                                    ${spent.toFixed(2)} of ${limit.toFixed(2)}
                                                </div>
                                            </div>
                                        );
                                    })}
                                </Card.Body>
                                <Card.Footer className="text-center">
                                    <Button onClick={() => setShowModal(true)} style={{ backgroundColor: "#006649" }}>Set Budget</Button>
                                </Card.Footer>
                            </Card>

                            <Card style={{ height: "315px", display: "flex", flexDirection: "column" }}>
                                <Card.Header
                                    style={{
                                        backgroundColor: "#f6f5f5",
                                        position: "sticky",
                                        top: 0,
                                        zIndex: 1
                                    }}
                                >
                                    <Card.Title className="fw-bold">AI Budget Analysis</Card.Title>
                                </Card.Header>
                                <Card.Body style={{ overflowY: "auto", flexGrow: 1 }}>
                                    {loadingAnalysis ? (
                                        <div className="d-flex justify-content-center align-items-center h-100">
                                            <div className="spinner-border text-success" role="status">
                                                <span className="visually-hidden">Loading...</span>
                                            </div>
                                        </div>
                                    ) : (
                                        <div
                                            className="text-muted"
                                            style={{ lineHeight: "1.5" }}
                                            dangerouslySetInnerHTML={{ __html: rendered }}
                                        />
                                    )}
                                </Card.Body>
                                <Card.Footer style={{ backgroundColor: "#f8f9fa", position: "sticky", bottom: 0 }}>
                                    <div className="d-flex justify-content-center">
                                        <Button onClick={fetchAiAnalysis} disabled={loadingAnalysis} style={{ backgroundColor: "#006649" }}>
                                            Generate Analysis
                                        </Button>
                                    </div>
                                </Card.Footer>
                            </Card>

                        </Col>
                    </Row>

                    <Modal show={showModal} onHide={() => setShowModal(false)} centered>
                        <Modal.Header closeButton><Modal.Title>Set Budget</Modal.Title></Modal.Header>
                        <Modal.Body>
                            <Form onSubmit={(e) => { e.preventDefault(); handleSubmitBudget(); }}>
                                <Form.Group className="mb-3">
                                    <Form.Label>Type</Form.Label>
                                    <Form.Select value={selectedCategory} onChange={(e) => setSelectedCategory(e.target.value)}>
                                        <option value="Total">Total Budget</option>
                                        {categories.map(c => (
                                            <option key={c.categoryID} value={c.categoryID}>{c.categoryName || `Category ${c.categoryID}`}</option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>

                                <Form.Group>
                                    <Form.Label>Amount</Form.Label>
                                    <Form.Control type="number" value={budgetAmount} onChange={(e) => setBudgetAmount(e.target.value)} placeholder="Enter amount" min="0.01" step="0.01" />
                                </Form.Group>
                            </Form>
                        </Modal.Body>
                        <Modal.Footer>
                            <Button variant="secondary" onClick={() => setShowModal(false)}>Cancel</Button>
                            <Button onClick={handleSubmitBudget} style={{ backgroundColor: "#006649" }}>Submit</Button>
                        </Modal.Footer>
                    </Modal>
                </div>
            )}
        </div >
    );
}

export default BudgetOverview;