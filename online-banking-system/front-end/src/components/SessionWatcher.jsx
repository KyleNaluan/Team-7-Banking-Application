import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useUser } from "../context/UserContext";
import { useSessionToast } from "../context/SessionToastContext";

export default function SessionWatcher() {
    const location = useLocation();
    const navigate = useNavigate();
    const { user, setUser } = useUser();
    const { showSessionToast } = useSessionToast();

    useEffect(() => {
        async function checkSession() {
            try {
                const res = await fetch("http://localhost:8080/auth/me", {
                    credentials: "include",
                });

                if (!res.ok) {
                    showSessionToast("Session expired — please log in again.");
                    setUser(null);
                    navigate("/login");
                }
            } catch (err) {
                console.error("Session check failed:", err);
                setUser(null);
                navigate("/login");
            }
        }

        if (user) {
            checkSession();
        }
    }, [location.pathname]);

    return null;
}
