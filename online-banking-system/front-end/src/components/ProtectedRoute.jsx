import React from "react";
import { Navigate } from "react-router-dom";
import { useUser } from "../context/UserContext";
import { Spinner } from "react-bootstrap";

const ProtectedRoute = ({ children }) => {
    const { user, authLoading } = useUser();

    if (authLoading) {
        return (
            <div className="vh-100 d-flex justify-content-center align-items-center">
                <Spinner animation="border" variant="success" role="status" />
            </div>
        );
    }

    return user ? children : <Navigate to="/login" />;
};

export default ProtectedRoute;
