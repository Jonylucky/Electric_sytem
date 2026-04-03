import { useEffect, useMemo, useState } from "react";
import {
    Typography,
    Container,
    Grid,
    Card,
    CardContent,
    Button,
    TextField,
    MenuItem,
    Chip,
    Stack,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Paper,
    Alert,
    CircularProgress,
    Dialog,
    IconButton,
    Box,
} from "@mui/material";
import {
    ResponsiveContainer,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    Tooltip,
    CartesianGrid,
    LineChart,
    Line,
    Cell,
} from "recharts";
import DownloadIcon from "@mui/icons-material/Download";
import BoltIcon from "@mui/icons-material/Bolt";
import WarningAmberIcon from "@mui/icons-material/WarningAmber";
import NotificationImportantIcon from "@mui/icons-material/NotificationImportant";
import BusinessIcon from "@mui/icons-material/Business";
import ElectricMeterIcon from "@mui/icons-material/ElectricMeter";
import CloseIcon from "@mui/icons-material/Close";
import ZoomInIcon from "@mui/icons-material/ZoomIn";
import ZoomOutIcon from "@mui/icons-material/ZoomOut";
import dayjs from "dayjs";
import { saveAs } from "file-saver";
import { getCompanies } from "../api/companyApi";
import { getMeters } from "../api/meterApi";
import { getReadingsByMonth, getAllAlerts } from "../api/readingApi";
import { exportCompanyReportZip } from "../api/exportApi";
import ConsumptionChart from "../components/charts/ConsumptionChart";

function StatusChip({ status }) {
    const color =
        status === "WARNING" || status === "DANGER" ? "error" : status === "RANGER" ? "warning" : "success";
    return <Chip size="small" label={status || "NORMAL"} color={color} />;
}

function StatCard({ title, value, icon, subtitle }) {
    return (
        <Card sx={{ height: "100%", borderRadius: 3 }}>
            <CardContent>
                <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
                    <Box>
                        <Typography variant="body2" color="text.secondary">
                            {title}
                        </Typography>
                        <Typography variant="h4" sx={{ mt: 1, fontWeight: 700 }}>
                            {value}
                        </Typography>
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                            {subtitle}
                        </Typography>
                    </Box>
                    <Box
                        sx={{
                            width: 44,
                            height: 44,
                            borderRadius: 2,
                            display: "grid",
                            placeItems: "center",
                            bgcolor: "action.hover",
                        }}
                    >
                        {icon}
                    </Box>
                </Stack>
            </CardContent>
        </Card>
    );
}

function inferStatus(current, previous) {
    if (!previous || previous <= 0) return "NORMAL";
    const percent = ((current - previous) / previous) * 100;
    if (percent > 50) return "WARNING";
    if (percent > 20) return "RANGER";
    return "NORMAL";
}

function normalizeCompany(raw) {
    return {
        id: String(raw.companyId ?? raw.id ?? ""),
        label: raw.companyName ?? raw.name ?? `Company ${raw.companyId ?? raw.id ?? ""}`,
    };
}

function normalizeMeter(raw) {
    return {
        meterId: raw.meterId,
        meterName: raw.meterName ?? raw.name ?? raw.meterId,
        companyId: raw.companyId,
        locationId: raw.locationId,
    };
}

function normalizeReading(raw) {
    const prev = Number(raw.indexPrevMonth ?? raw.previousIndex ?? 0);
    const present = Number(raw.indexLastMonth ?? raw.presentIndex ?? 0);
    const consumption = Number(raw.indexConsumption ?? raw.consumption ?? Math.max(present - prev, 0));
    const alertLevel = raw.alertLevel ?? "NORMAL";

    const monthStr = raw.month || "";
    const imageUrl = raw.meterId ? `uploads/${monthStr}/img/${raw.meterId}_${monthStr}.jpg` : "";

    return {
        meterId: raw.meterId,
        meterName: raw.meterName ?? raw.name ?? raw.meterId,
        month: raw.month,
        prev,
        present,
        consumption,
        locationId: raw.locationId,
        alertLevel,
        imageUrl,
    };
}

export default function DashboardPage() {
    const currentMonth = dayjs();
    const monthOptions = Array.from({ length: 12 }, (_, i) => {
        const m = currentMonth.subtract(i, "month");
        return {
            value: m.format("YYYY-MM"),
            label: `Tháng ${m.format("MM/YYYY")}`,
        };
    });

    const [month, setMonth] = useState(monthOptions[0].value);
    const [companies, setCompanies] = useState([]);
    const [companyId, setCompanyId] = useState("");
    const [meters, setMeters] = useState([]);
    const [readingRows, setReadingRows] = useState([]);
    const [loading, setLoading] = useState(false);
    const [exporting, setExporting] = useState(false);
    const [error, setError] = useState("");
    const [hoverImage, setHoverImage] = useState(null);
    const [selectedImage, setSelectedImage] = useState(null);
    const [zoom, setZoom] = useState(1);
    const [chartDialog, setChartDialog] = useState({ open: false, meterId: "", month: "" });
    const [alertsData, setAlertsData] = useState(null);
    const [loadingAlerts, setLoadingAlerts] = useState(false);

    useEffect(() => {
        loadCompanies();
    }, []);

    useEffect(() => {
        if (companyId) {
            loadDashboardData(companyId, month);
        }
    }, [companyId, month]);

    async function loadAlertsData() {
        try {
            setLoadingAlerts(true);
            const res = await getAllAlerts(month);
            setAlertsData(res.data);
        } catch (err) {
            console.error("Failed to load alerts:", err);
            setAlertsData(null);
        } finally {
            setLoadingAlerts(false);
        }
    }

    async function loadCompanies() {
        try {
            setError("");
            const res = await getCompanies();
            const list = Array.isArray(res.data) ? res.data.map(normalizeCompany) : [];
            setCompanies(list);
            if (list.length > 0) {
                setCompanyId(list[0].id);
            }
        } catch (err) {
            setError("Không tải được danh sách công ty.");
            console.error(err);
        }
    }

    async function loadDashboardData(selectedCompanyId, selectedMonth) {
        try {
            setLoading(true);
            setError("");

            const [meterRes, readingRes] = await Promise.all([
                getMeters({ companyId: selectedCompanyId }),
                getReadingsByMonth({ month: selectedMonth, companyId: selectedCompanyId }),
            ]);

            const meterList = Array.isArray(meterRes.data) ? meterRes.data.map(normalizeMeter) : [];
            const readingList = Array.isArray(readingRes.data) ? readingRes.data.map(normalizeReading) : [];

            const meterMap = new Map(meterList.map((m) => [m.meterId, m]));
            const merged = readingList.map((r) => ({
                ...r,
                meterName: r.meterName || meterMap.get(r.meterId)?.meterName || r.meterId,
                locationId: r.locationId ?? meterMap.get(r.meterId)?.locationId ?? "",
            }));

            setMeters(meterList);
            setReadingRows(merged);
        } catch (err) {
            setError("Không tải được dữ liệu đồng hồ hoặc chỉ số tháng.");
            console.error(err);
            setMeters([]);
            setReadingRows([]);
        } finally {
            setLoading(false);
        }
    }

    async function handleExportZip() {
        if (!companyId || !month) return;
        try {
            setExporting(true);
            const res = await exportCompanyReportZip({ companyId, month });
            saveAs(res.data, `bao_cao_${companyId}_${month}.zip`);
        } catch (err) {
            setError("Tải file ZIP thất bại.");
            console.error(err);
        } finally {
            setExporting(false);
        }
    }

    const selectedCompany = companies.find((c) => c.id === companyId)?.label || "";

    const totals = useMemo(() => {
        const totalConsumption = readingRows.reduce((sum, item) => sum + Number(item.consumption || 0), 0);
        const warningCount = readingRows.filter((item) => item.alertLevel === "WARNING" || item.alertLevel === "DANGER").length;
        const dangerCount = readingRows.filter((item) => item.alertLevel === "DANGER").length;
        const warningOnlyCount = readingRows.filter((item) => item.alertLevel === "WARNING").length;
        return {
            totalConsumption,
            warningCount,
            dangerCount,
            warningOnlyCount,
            rangerCount: 0,
            meterCount: meters.length,
        };
    }, [readingRows, meters]);

    const monthlyData = useMemo(() => {
        const currentMonth = dayjs(`${month}-01`);
        return [2, 1, 0].map((offset) => {
            const m = currentMonth.subtract(offset, "month").format("YYYY-MM");
            const rows = offset === 0 ? readingRows : [];
            const consumption = rows.reduce((sum, item) => sum + Number(item.consumption || 0), 0);
            const lastIndex = rows.reduce((sum, item) => sum + Number(item.present || 0), 0);
            return {
                consumption,
                lastIndex,
                month: m,
                status: inferStatus(consumption, 0),
            };
        });
    }, [month, readingRows]);

    const abnormalMeters = useMemo(() => {
        return [...readingRows]
            .filter((item) => item.status === "WARNING" || item.status === "RANGER")
            .sort((a, b) => Number(b.consumption || 0) - Number(a.consumption || 0))
            .slice(0, 5)
            .map((item) => ({
                ...item,
                delta: item.status === "WARNING" ? "> 50%" : "> 20%",
            }));
    }, [readingRows]);

    const barColors = monthlyData.map((item) => {
        if (item.status === "WARNING") return "#d32f2f";
        if (item.status === "RANGER") return "#ed6c02";
        return "#2e7d32";
    });

    const handleZoomIn = () => setZoom((prev) => Math.min(prev + 0.25, 3));
    const handleZoomOut = () => setZoom((prev) => Math.max(prev - 0.25, 0.5));

    return (
        <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
            <Stack
                direction={{ xs: "column", md: "row" }}
                justifyContent="space-between"
                alignItems={{ xs: "stretch", md: "center" }}
                spacing={2}
                sx={{ mb: 3 }}
            >
                <Box>
                    <Typography color="text.secondary">
                        Theo dõi sản lượng điện, đồng hồ bất thường và tải báo cáo theo tháng.
                    </Typography>
                </Box>

                <Stack direction={{ xs: "column", sm: "row" }} spacing={1.5}>
                    <TextField
                        select
                        size="small"
                        label="Company"
                        value={companyId}
                        onChange={(e) => setCompanyId(e.target.value)}
                        sx={{ minWidth: 220 }}
                    >
                        {companies.map((item) => (
                            <MenuItem key={item.id} value={item.id}>
                                {item.label}
                            </MenuItem>
                        ))}
                    </TextField>

                    <TextField
                        select
                        size="small"
                        label="Month"
                        value={month}
                        onChange={(e) => setMonth(e.target.value)}
                        sx={{ minWidth: 160 }}
                    >
                        {monthOptions.map((item) => (
                            <MenuItem key={item.value} value={item.value}>
                                {item.label}
                            </MenuItem>
                        ))}
                    </TextField>

                    <Button
                        variant="contained"
                        startIcon={exporting ? <CircularProgress size={16} color="inherit" /> : <DownloadIcon />}
                        onClick={handleExportZip}
                        disabled={!companyId || exporting}
                    >
                        {exporting ? "Đang tải..." : "Export ZIP"}
                    </Button>

                    <Button
                        variant="outlined"
                        color="warning"
                        startIcon={loadingAlerts ? <CircularProgress size={16} color="inherit" /> : <NotificationImportantIcon />}
                        onClick={loadAlertsData}
                        disabled={loadingAlerts}
                    >
                        {loadingAlerts ? "Đang kiểm tra..." : "Kiểm tra cảnh báo"}
                    </Button>
                </Stack>
            </Stack>

            {error && (
                <Alert severity="error" sx={{ mb: 2 }}>
                    {error}
                </Alert>
            )}

            <Grid container spacing={2.5}>
                <Grid item xs={12} sm={6} lg={3}>
                    <StatCard
                        title="Tổng công ty"
                        value={companies.length}
                        subtitle={selectedCompany || "Chưa chọn công ty"}
                        icon={<BusinessIcon />}
                    />
                </Grid>
                <Grid item xs={12} sm={6} lg={3}>
                    <StatCard
                        title="Số đồng hồ"
                        value={totals.meterCount}
                        subtitle="Theo công ty đang chọn"
                        icon={<ElectricMeterIcon />}
                    />
                </Grid>
                <Grid item xs={12} sm={6} lg={3}>
                    <StatCard
                        title="Tổng tiêu thụ"
                        value={totals.totalConsumption.toLocaleString()}
                        subtitle="kWh"
                        icon={<BoltIcon />}
                    />
                </Grid>
                <Grid item xs={12} sm={6} lg={3}>
                    <StatCard
                        title="Cảnh báo"
                        value={totals.warningCount}
                        subtitle={`${totals.dangerCount} danger / ${totals.warningOnlyCount} warning`}
                        icon={<WarningAmberIcon />}
                    />
                </Grid>

                <Grid item xs={12} lg={8}>
                    <Card sx={{ borderRadius: 3, height: "100%" }}>
                        <CardContent>
                            <Typography variant="h6" sx={{ mb: 2, fontWeight: 700 }}>
                                Consumption 3 tháng gần nhất
                            </Typography>
                            <Box sx={{ height: 340 }}>
                                <ResponsiveContainer width="100%" height="100%">
                                    <BarChart data={monthlyData}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis dataKey="month" />
                                        <YAxis />
                                        <Tooltip />
                                        <Bar dataKey="consumption" radius={[8, 8, 0, 0]}>
                                            {monthlyData.map((entry, index) => (
                                                <Cell key={entry.month} fill={barColors[index]} />
                                            ))}
                                        </Bar>
                                    </BarChart>
                                </ResponsiveContainer>
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} lg={4}>
                    <Card sx={{ borderRadius: 3, height: "100%" }}>
                        <CardContent>
                            <Typography variant="h6" sx={{ mb: 2, fontWeight: 700 }}>
                                Last Index Trend
                            </Typography>
                            <Box sx={{ height: 340 }}>
                                <ResponsiveContainer width="100%" height="100%">
                                    <LineChart data={monthlyData}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis dataKey="month" />
                                        <YAxis />
                                        <Tooltip />
                                        <Line type="monotone" dataKey="lastIndex" stroke="#1976d2" strokeWidth={3} dot={{ r: 4 }} />
                                    </LineChart>
                                </ResponsiveContainer>
                            </Box>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} lg={5}>
                    <Card sx={{ borderRadius: 3, height: "100%" }}>
                        <CardContent>
                            <Typography variant="h6" sx={{ mb: 2, fontWeight: 700 }}>
                                Đồng hồ bất thường
                            </Typography>
                            <Stack spacing={1.5}>
                                {loadingAlerts ? (
                                    <Stack alignItems="center" py={2}>
                                        <CircularProgress size={24} />
                                    </Stack>
                                ) : !alertsData || !alertsData.readings || alertsData.readings.length === 0 ? (
                                    <Paper variant="outlined" sx={{ p: 2, borderRadius: 2 }}>
                                        <Typography color="text.secondary">không có chỉ số bất thường</Typography>
                                    </Paper>
                                ) : (
                                    alertsData.readings.map((item) => (
                                        <Paper
                                            key={item.meterId}
                                            variant="outlined"
                                            sx={{ p: 1.5, borderRadius: 2, cursor: "pointer" }}
                                            onClick={() => setChartDialog({ open: true, meterId: item.meterId, month })}
                                        >
                                            <Stack direction="row" justifyContent="space-between" alignItems="center">
                                                <Box>
                                                    <Typography sx={{ fontWeight: 600 }}>{item.meterName}</Typography>
                                                    <Typography variant="body2" color="text.secondary">
                                                        {item.meterId} · {Number(item.indexConsumption || 0).toLocaleString()} kWh
                                                    </Typography>
                                                </Box>
                                                <Stack alignItems="flex-end" spacing={0.5}>
                                                    <Chip
                                                        size="small"
                                                        label={item.alertLevel}
                                                        color={item.alertLevel === "DANGER" ? "error" : item.alertLevel === "WARNING" ? "warning" : "default"}
                                                    />
                                                </Stack>
                                            </Stack>
                                        </Paper>
                                    ))
                                )}
                            </Stack>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} lg={7}>
                    <Card sx={{ borderRadius: 3, height: "100%" }}>
                        <CardContent>
                            <Typography variant="h6" sx={{ mb: 2, fontWeight: 700 }}>
                                Bảng chỉ số tháng {month}
                            </Typography>
                            {loading ? (
                                <Stack alignItems="center" justifyContent="center" sx={{ py: 6 }}>
                                    <CircularProgress />
                                </Stack>
                            ) : (
                                <Table size="small">
                                    <TableHead>
                                        <TableRow>
                                            <TableCell align="center">Ảnh</TableCell>
                                            <TableCell>Meter ID</TableCell>
                                            <TableCell>Tên đồng hồ</TableCell>
                                            <TableCell align="right">Prev</TableCell>
                                            <TableCell align="right">Present</TableCell>
                                            <TableCell align="right">Consumption</TableCell>
                                            <TableCell align="center">Status</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {readingRows.map((row) => (
                                            <TableRow
                                                key={row.meterId}
                                                hover
                                                onClick={() => setChartDialog({ open: true, meterId: row.meterId, month })}
                                                sx={{ cursor: "pointer" }}
                                            >
                                                <TableCell align="center">
                                                    {row.imageUrl && (
                                                        <Box
                                                            component="img"
                                                            src={`http://localhost:8081/${row.imageUrl}`}
                                                            alt={row.meterId}
                                                            onMouseEnter={() => setHoverImage(`http://localhost:8081/${row.imageUrl}`)}
                                                            onMouseLeave={() => setHoverImage(null)}
                                                            onClick={() => {
                                                                setSelectedImage(`http://localhost:8081/${row.imageUrl}`);
                                                                setZoom(1);
                                                            }}
                                                            sx={{
                                                                width: 60,
                                                                height: 60,
                                                                objectFit: "cover",
                                                                borderRadius: 1,
                                                                cursor: "pointer",
                                                                boxShadow: 2,
                                                                transition: "transform 0.2s",
                                                                "&:hover": {
                                                                    transform: "scale(1.1)",
                                                                },
                                                            }}
                                                        />
                                                    )}
                                                </TableCell>
                                                <TableCell>{row.meterId}</TableCell>
                                                <TableCell>{row.meterName}</TableCell>
                                                <TableCell align="right">{Number(row.prev || 0).toLocaleString()}</TableCell>
                                                <TableCell align="right">{Number(row.present || 0).toLocaleString()}</TableCell>
                                                <TableCell align="right">{Number(row.consumption || 0).toLocaleString()}</TableCell>
                                                <TableCell align="center">
                                                    <StatusChip status={row.alertLevel} />
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                        {!loading && readingRows.length === 0 && (
                                            <TableRow>
                                                <TableCell colSpan={7} align="center">
                                                    Không có dữ liệu chỉ số.
                                                </TableCell>
                                            </TableRow>
                                        )}
                                    </TableBody>
                                </Table>
                            )}
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            {/* Hover preview image */}
            {hoverImage && !selectedImage && (
                <Box
                    sx={{
                        position: "fixed",
                        inset: 0,
                        zIndex: 1300,
                        pointerEvents: "none",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                    }}
                >
                    <Box
                        sx={{
                            bgcolor: "rgba(0,0,0,0.15)",
                            p: 1,
                            borderRadius: 2,
                            backdropFilter: "blur(4px)",
                        }}
                    >
                        <Box
                            component="img"
                            src={hoverImage}
                            alt="preview"
                            sx={{
                                width: "28vw",
                                maxWidth: 420,
                                minWidth: 260,
                                maxHeight: "55vh",
                                objectFit: "contain",
                                display: "block",
                                borderRadius: 2,
                                boxShadow: 8,
                                bgcolor: "#fff",
                            }}
                        />
                    </Box>
                </Box>
            )}

            {/* Dialog for large image view with zoom */}
            <Dialog
                open={!!selectedImage}
                onClose={() => setSelectedImage(null)}
                maxWidth="xl"
                sx={{
                    "& .MuiDialog-paper": {
                        bgcolor: "transparent",
                        boxShadow: "none",
                        overflow: "visible",
                    },
                }}
            >
                <Box
                    sx={{
                        position: "relative",
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                    }}
                >
                    <IconButton
                        onClick={() => setSelectedImage(null)}
                        sx={{
                            position: "absolute",
                            top: -50,
                            right: -50,
                            color: "white",
                            bgcolor: "rgba(0,0,0,0.5)",
                            zIndex: 1,
                            "&:hover": {
                                bgcolor: "rgba(0,0,0,0.7)",
                            },
                        }}
                    >
                        <CloseIcon />
                    </IconButton>

                    <Box sx={{ display: "flex", gap: 1, mb: 2 }}>
                        <IconButton
                            onClick={handleZoomOut}
                            sx={{
                                color: "white",
                                bgcolor: "rgba(0,0,0,0.5)",
                                "&:hover": { bgcolor: "rgba(0,0,0,0.7)" },
                            }}
                        >
                            <ZoomOutIcon />
                        </IconButton>
                        <IconButton
                            onClick={handleZoomIn}
                            sx={{
                                color: "white",
                                bgcolor: "rgba(0,0,0,0.5)",
                                "&:hover": { bgcolor: "rgba(0,0,0,0.7)" },
                            }}
                        >
                            <ZoomInIcon />
                        </IconButton>
                    </Box>

                    {selectedImage && (
                        <Box
                            component="img"
                            src={selectedImage}
                            alt="full preview"
                            sx={{
                                maxWidth: "90vw",
                                maxHeight: "80vh",
                                objectFit: "contain",
                                borderRadius: 2,
                                boxShadow: 24,
                                transform: `scale(${zoom})`,
                                transition: "transform 0.2s ease-in-out",
                            }}
                        />
                    )}
                </Box>
            </Dialog>

            {/* Consumption Chart Dialog */}
            <ConsumptionChart
                open={chartDialog.open}
                onClose={() => setChartDialog({ open: false, meterId: "", month: "" })}
                meterId={chartDialog.meterId}
                month={chartDialog.month}
            />
        </Container>
    );
}

