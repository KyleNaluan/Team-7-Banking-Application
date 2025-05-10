import React from "react";
import { useNavigate } from "react-router-dom";

function AccountBox({ type, available, current, accountId }) {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/account/${accountId}`);
  };

  return (
    <div
      className="border rounded p-3 mb-3 bg-white shadow-sm"
      style={{ width: "100%", cursor: "pointer" }}
      onClick={handleClick}
    >
      <p className="fw-semibold fs-5 mb-3">{type}</p>
      <div className="d-flex justify-content-between mb-2">
        <span className="text-muted">Available</span>
        <span className="fw-bold">
          ${Number(available).toFixed(2)}
        </span>
      </div>
      <div className="d-flex justify-content-between">
        <span className="text-muted">Current</span>
        <span className="fw-bold">
          ${Number(current).toFixed(2)}
        </span>
      </div>
    </div>
  );
}

export default AccountBox;
