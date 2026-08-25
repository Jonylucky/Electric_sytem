import { createContext, useContext, useMemo, useState } from "react";
import { authenticateUser } from "./accounts";

const AUTH_STORAGE_KEY = "electric_auth_user";

const AuthContext = createContext(null);

function readStoredUser() {
    try {
        const raw = localStorage.getItem(AUTH_STORAGE_KEY);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => readStoredUser());

    const login = (username, password) => {
        const authenticated = authenticateUser(username, password);
        if (!authenticated) {
            return { ok: false, message: "Sai tên đăng nhập hoặc mật khẩu." };
        }
        setUser(authenticated);
        localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(authenticated));
        return { ok: true };
    };

    const logout = () => {
        setUser(null);
        localStorage.removeItem(AUTH_STORAGE_KEY);
    };

    const value = useMemo(
        () => ({
            user,
            isAuthenticated: Boolean(user),
            login,
            logout,
        }),
        [user]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error("useAuth must be used within AuthProvider");
    }
    return context;
}
