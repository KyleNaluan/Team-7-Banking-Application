import React, { createContext, useContext, useEffect, useState } from "react";

const UserContext = createContext();

export const UserProvider = ({ children }) => {
    const [user, setUserState] = useState(() => {
        const stored = localStorage.getItem("user");
        return stored ? JSON.parse(stored) : null;
    });

    const [authLoading, setAuthLoading] = useState(true);

    useEffect(() => {
        async function fetchUser() {
            try {
                const res = await fetch("http://localhost:8080/auth/me", {
                    credentials: "include",
                });

                if (res.ok) {
                    const data = await res.json();
                    setUserState(data);
                    localStorage.setItem("user", JSON.stringify(data));
                } else {
                    setUserState(null);
                    localStorage.removeItem("user");
                }
            } catch (err) {
                console.error("Error checking session:", err);
                setUserState(null);
                localStorage.removeItem("user");
            } finally {
                setAuthLoading(false);
            }
        }

        fetchUser();
    }, []);

    const setUser = (user) => {
        setUserState(user);
        if (user) {
            localStorage.setItem("user", JSON.stringify(user));
        } else {
            localStorage.removeItem("user");
        }
    };

    return (
        <UserContext.Provider value={{ user, setUser, authLoading }}>
            {children}
        </UserContext.Provider>
    );
};

export const useUser = () => useContext(UserContext);
