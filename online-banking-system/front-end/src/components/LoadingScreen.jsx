import React from "react";
import { Spinner } from "react-bootstrap";

function LoadingScreen() {
  return (
    <div className="d-flex justify-content-center align-items-center" style={{ height: "100vh" }}>
      <Spinner animation="border" variant="success" role="status" />
    </div>
  );
}

export default LoadingScreen;