import { createContext, useContext, useState } from "react";

const SessionToastContext = createContext();

export const SessionToastProvider = ({ children }) => {
    const [toastMessage, setToastMessage] = useState("");

    const showSessionToast = (message) => {
        setToastMessage(message);
    };

    const clearToast = () => setToastMessage("");

    return (
        <SessionToastContext.Provider value={{ toastMessage, showSessionToast, clearToast }}>
            {children}
        </SessionToastContext.Provider>
    );
};

export const useSessionToast = () => useContext(SessionToastContext);
