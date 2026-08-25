export const HARDCODED_ACCOUNTS = [
    { username: "admin", password: "1", role: "admin", displayName: "Admin" },
    { username: "user", password: "1", role: "user", displayName: "User" },
    { username: "manager", password: "1", role: "manager", displayName: "Manager" },
];

export function authenticateUser(username, password) {
    const normalizedUsername = String(username || "").trim().toLowerCase();
    const account = HARDCODED_ACCOUNTS.find(
        (item) => item.username.toLowerCase() === normalizedUsername && item.password === password
    );
    if (!account) return null;
    return {
        username: account.username,
        role: account.role,
        displayName: account.displayName,
    };
}
