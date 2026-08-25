import { useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    Container,
    Stack,
    TextField,
    Typography,
} from "@mui/material";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import { useAuth } from "../auth/AuthContext";

export default function LoginPage() {
    const navigate = useNavigate();
    const location = useLocation();
    const { login, isAuthenticated } = useAuth();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const redirectTo = location.state?.from || "/dashboard";

    if (isAuthenticated) {
        return <Navigate to={redirectTo} replace />;
    }

    const handleSubmit = (event) => {
        event.preventDefault();
        setError("");
        setSubmitting(true);

        const result = login(username, password);
        if (!result.ok) {
            setError(result.message);
            setSubmitting(false);
            return;
        }

        navigate(redirectTo, { replace: true });
    };

    return (
        <Box
            sx={{
                minHeight: "100vh",
                display: "flex",
                alignItems: "center",
                bgcolor: "grey.100",
            }}
        >
            <Container maxWidth="xs">
                <Card sx={{ borderRadius: 3, boxShadow: 4 }}>
                    <CardContent sx={{ p: 4 }}>
                        <Stack spacing={2.5} alignItems="center">
                            <Box
                                sx={{
                                    width: 52,
                                    height: 52,
                                    borderRadius: "50%",
                                    bgcolor: "primary.main",
                                    color: "primary.contrastText",
                                    display: "grid",
                                    placeItems: "center",
                                }}
                            >
                                <LockOutlinedIcon />
                            </Box>
                            <Box sx={{ textAlign: "center" }}>
                                <Typography variant="h5" sx={{ fontWeight: 700 }}>
                                    Đăng nhập
                                </Typography>
                                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                                    Electric Management Dashboard
                                </Typography>
                            </Box>
                        </Stack>

                        <Box component="form" onSubmit={handleSubmit} sx={{ mt: 3 }}>
                            <Stack spacing={2}>
                                {error && <Alert severity="error">{error}</Alert>}

                                <TextField
                                    label="Tên đăng nhập"
                                    value={username}
                                    onChange={(e) => setUsername(e.target.value)}
                                    autoComplete="username"
                                    autoFocus
                                    required
                                    fullWidth
                                />
                                <TextField
                                    label="Mật khẩu"
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    autoComplete="current-password"
                                    required
                                    fullWidth
                                />
                                <Button type="submit" variant="contained" size="large" disabled={submitting} fullWidth>
                                    {submitting ? "Đang đăng nhập..." : "Đăng nhập"}
                                </Button>
                            </Stack>
                        </Box>

                    
                    </CardContent>
                </Card>
            </Container>
        </Box>
    );
}
