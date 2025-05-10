import React from "react";
import { useUser } from "../context/UserContext";
import { useNavigate } from "react-router-dom";
import { Navbar, Nav, Container, Dropdown } from "react-bootstrap";

function NavBar() {
  const navigate = useNavigate();
  const { user, setUser } = useUser();

  const initials = user
    ? `${user.fName?.[0] || ''}${user.lName?.[0] || ''}`.toUpperCase()
    : "";

  const ProfileToggle = React.forwardRef(({ onClick }, ref) => (
    <div
      ref={ref}
      onClick={(e) => {
        e.preventDefault();
        onClick(e);
      }}
      style={{
        width: "35px",
        height: "35px",
        borderRadius: "50%",
        backgroundColor: "#8a8888",
        cursor: "pointer",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        color: "white",
        fontWeight: "bold"
      }}
    >
      {initials}
    </div>
  ));

  return (
    <Navbar
      variant="dark"
      expand="lg"
      style={{ backgroundColor: "#006649", padding: "0.5rem 1rem", color: "white" }}
    >
      <Container fluid>
        <Navbar.Brand href="#" className="fw-bold">Team7Banking</Navbar.Brand>
        <Navbar.Toggle aria-controls="navbar-nav" />
        <Navbar.Collapse id="navbar-nav">
          <Nav className="me-auto">
            <Nav.Link onClick={() => navigate("/dashboard")}>Dashboard</Nav.Link>
            <Nav.Link onClick={() => navigate("/accounts")}>Accounts</Nav.Link>
            <Nav.Link onClick={() => navigate("/budgetoverview")}>Budget Tracking</Nav.Link>
            <Nav.Link onClick={() => navigate("/transfer")}>Transfer</Nav.Link>
            <Nav.Link onClick={() => navigate("/payment")}>Add Payment</Nav.Link>
          </Nav>
          <Dropdown align="end">
            <Dropdown.Toggle as={ProfileToggle} />
            <Dropdown.Menu>
              <Dropdown.Item onClick={() => navigate("/profile")}>Profile</Dropdown.Item>
              <Dropdown.Divider />
              <Dropdown.Item
                onClick={async () => {
                  try {
                    await fetch("http://localhost:8080/auth/logout", {
                      method: "POST",
                      credentials: "include"
                    });
                    localStorage.clear();
                    setUser(null);
                    navigate("/login");
                  } catch (err) {
                    console.error("Logout failed:", err);
                  }
                }}
              >
                Logout
              </Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}

export default NavBar;
