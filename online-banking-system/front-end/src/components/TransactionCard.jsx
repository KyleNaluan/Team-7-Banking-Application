import React from "react";
import { Card, Row, Col, Badge, Button } from "react-bootstrap";

function TransactionCard({ type, amount, date, time, note, extraInfo, onDelete }) {
    const safeDate = date || "Unknown Date";
    const safeTime = time ? time.substring(0, 5) : "Unknown Time";

    const getBadgeVariant = (type) => {
        switch (type) {
            case "Withdrawal":
                return "danger";
            case "Payment":
                return "warning";
            case "Deposit":
                return "success";
            case "Transfer Sent":
                return "warning";
            case "Transfer Received":
                return "success";
            default:
                return "primary";
        }
    };

    return (
        <Card className="mb-3 shadow-sm">
            <Card.Body>
                <Row className="align-items-center">
                    <Col md={9}>
                        <h6 className="fw-bold mb-1">{type}</h6>
                        <div className="text-muted small">{safeDate} at {safeTime}</div>
                        {extraInfo && <div className="text-muted small"><em>Category:</em> {extraInfo}</div>}
                        {note && <div className="text-muted small"><em>Note:</em> {note}</div>}
                    </Col>
                    <Col md={3} className="text-end">
                        <h5>
                            <Badge bg={getBadgeVariant(type)}>
                                {['Withdrawal', 'Payment', 'Transfer Sent'].includes(type) ? '-' : '+'}${Math.abs(amount).toFixed(2)}
                            </Badge>
                        </h5>
                        {type === "Payment" && onDelete && (
                            <Button variant="outline-danger" size="sm" className="mt-2" onClick={onDelete}>
                                Delete
                            </Button>
                        )}
                    </Col>
                </Row>
            </Card.Body>
        </Card>
    );
}

export default TransactionCard;