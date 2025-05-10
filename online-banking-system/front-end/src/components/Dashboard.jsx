import React, { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Container, Row, Col, Card, Toast, ToastContainer } from "react-bootstrap";
import NavBar from "./NavBar";
import AccountBox from "./AccountBox";
import TransactionCard from "./TransactionCard";
import LoadingScreen from "./LoadingScreen";
import { Doughnut } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
import { ArrowLeftRight } from "react-bootstrap-icons";
import { Spinner } from "react-bootstrap";

ChartJS.register(ArcElement, Tooltip, Legend);

function Dashboard() {
  const navigate = useNavigate();
  const location = useLocation();
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [firstName, setFirstName] = useState("");
  const [success, setSuccess] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isTxLoading, setIsTxLoading] = useState(true);

  useEffect(() => {
    if (location.state?.showToast) {
      setSuccess(location.state.showToast);
      window.history.replaceState({}, document.title);
    }
  }, [location]);

  useEffect(() => {
    fetch("http://localhost:8080/auth/me", { credentials: "include" })
      .then(res => res.ok ? res.json() : Promise.reject("Unauthorized"))
      .then(user => {
        localStorage.setItem("user", JSON.stringify(user));
        setFirstName(user.fName || "");
        return fetch(`http://localhost:8080/api/accounts/customer/${user.customerID}`, {
          credentials: "include"
        });
      })
      .then(res => res.json())
      .then(accData => {
        setAccounts(accData);
        setIsLoading(false);
      })
      .catch(err => {
        console.error("Auth or account fetch error:", err);
        setIsLoading(false);
      });
  }, []);

  useEffect(() => {
    if (accounts.length === 0) return;

    setIsTxLoading(true);
    const txFetches = accounts.map(acc =>
      fetch(`http://localhost:8080/api/transactions/account/${acc.accountID}`, {
        credentials: "include"
      }).then(res => res.json())
    );

    Promise.all(txFetches)
      .then(allTxArrays => {
        const flatTx = allTxArrays.flat().filter(tx =>
          tx.type !== "Transfer Sent" && tx.type !== "Transfer Received"
        );
        const sortedTx = flatTx.sort((a, b) => {
          const dateA = new Date(a.transactionDate + 'T' + a.transactionTime);
          const dateB = new Date(b.transactionDate + 'T' + b.transactionTime);
          return dateB - dateA;
        });
        setTransactions(sortedTx.slice(0, 10));
        setIsTxLoading(false);
      })
      .catch(err => {
        console.error("Transaction fetch error:", err);
        setIsTxLoading(false);
      });
  }, [accounts]);


  const dummyChartData = {
    labels: ["Rent", "Food", "Utilities", "Entertainment"],
    datasets: [
      {
        label: "Spending",
        data: [500, 300, 150, 100],
        backgroundColor: ["#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0"],
        borderWidth: 1,
      }
    ]
  };

  const totalBalance = accounts.reduce((sum, acc) => sum + acc.accountBalance, 0);

  return (
    <div style={{ backgroundColor: "#f5f5f5", minHeight: "100vh" }}>
      <NavBar />

      <ToastContainer position="bottom-end" className="p-3">
        <Toast bg="success" onClose={() => setSuccess("")} show={!!success} delay={4000} autohide>
          <Toast.Body className="text-white">{success}</Toast.Body>
        </Toast>
      </ToastContainer>

      {isLoading ? (
        <LoadingScreen />
      ) : (
        <Container fluid className="mt-4 px-4">
          <h2 className="fw-bold">Welcome {firstName}!</h2>
          <p className="text-muted mb-4">
            You have {accounts.length} active {accounts.length === 1 ? "account" : "accounts"} with a total balance of ${totalBalance.toFixed(2)}. Here's a quick overview of your recent activity.
          </p>
          <Row>
            <Col md={8}>
              <Card className="shadow-sm" style={{ height: "547px" }}>
                <Card.Header className="text-center border-bottom" style={{ backgroundColor: "#f6f5f5", position: "sticky", top: 0, zIndex: 1 }}>
                  <Card.Title className="fw-bold mb-0">Accounts</Card.Title>
                </Card.Header>
                <Card.Body className="d-flex flex-column">
                  <div style={{ overflowY: "auto", maxHeight: "495px" }}>
                    {accounts.length === 0 ? (
                      <div className="text-muted mt-2">No accounts</div>
                    ) : (
                      accounts.map(account => (
                        <AccountBox
                          key={account.accountID}
                          type={`${account.accountType.toUpperCase()} *${account.accountID.toString().slice(-4)}`}
                          available={account.accountBalance}
                          current={account.accountBalance}
                          accountId={account.accountID}
                        />
                      ))
                    )}
                  </div>
                </Card.Body>
              </Card>
            </Col>

            <Col md={4} className="d-flex flex-column" style={{ gap: "12px" }}>
              <Card className="shadow-sm" style={{ height: "355px" }}>
                <Card.Header className="text-center border-bottom" style={{ backgroundColor: "#f6f5f5", position: "sticky", top: 0, zIndex: 1 }}>
                  <Card.Title className="fw-bold mb-0">Recent Activity</Card.Title>
                </Card.Header>
                <Card.Body className="d-flex flex-column">
                  <div style={{ overflowY: "auto", maxHeight: '290px' }}>
                    {accounts.length === 0 ? (
                      <div className="text-muted mt-2">No accounts</div>
                    ) : isTxLoading ? (
                      <div className="d-flex justify-content-center mt-4">
                        <Spinner animation="border" variant="success" role="status">
                          <span className="visually-hidden">Loading...</span>
                        </Spinner>
                      </div>
                    ) : transactions.length === 0 ? (
                      <div className="text-muted mt-2">No recent activity</div>
                    ) : (
                      transactions.map((tx) => (
                        <TransactionCard
                          key={tx.transactionID}
                          type={tx.type}
                          amount={tx.amount}
                          date={tx.transactionDate}
                          time={tx.transactionTime}
                          extraInfo={tx.category || ""}
                        />
                      ))
                    )}
                  </div>
                </Card.Body>
              </Card>

              <Row>
                <Col xs={6}>
                  <Card className="shadow-sm" style={{ height: "180px", cursor: "pointer" }} onClick={() => navigate("/budgetoverview")}>
                    <Card.Body className="d-flex flex-column align-items-center justify-content-center">
                      <Card.Title>Budget Overview</Card.Title>
                      <div style={{ height: "110px", width: "100%" }}>
                        <Doughnut
                          data={dummyChartData}
                          options={{
                            maintainAspectRatio: false,
                            plugins: {
                              legend: { display: false },
                              tooltip: { enabled: false }
                            },
                            hover: { mode: null }
                          }}
                        />
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
                <Col xs={6}>
                  <Card className="shadow-sm" style={{ height: "180px", cursor: "pointer" }} onClick={() => navigate("/transfer")}>
                    <Card.Body className="d-flex flex-column align-items-center justify-content-center">
                      <ArrowLeftRight size={36} className="mb-2 text-secondary" />
                      <Card.Title>Transfer</Card.Title>
                    </Card.Body>
                  </Card>
                </Col>
              </Row>
            </Col>
          </Row>
        </Container>
      )}
    </div>
  );
}

export default Dashboard;
