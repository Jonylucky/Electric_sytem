import { useEffect, useState } from "react";
import {
    Dialog,
    DialogTitle,
    DialogContent,
    IconButton,
    Box,
    Typography,
    CircularProgress,
    Stack,
} from "@mui/material";
import {
    ResponsiveContainer,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    Tooltip,
    CartesianGrid,
    Cell,
    LineChart,
    Line,
} from "recharts";
import CloseIcon from "@mui/icons-material/Close";
import { getChartConsumption } from "../../api/readingApi";

const ALERT_COLORS = {
    NORMAL: "#2e7d32",
    WARNING: "#d32f2f",
    RANGER: "#ed6c02",
    NO_DATA: "#9e9e9e",
};

export default function ConsumptionChart({ open, onClose, meterId, month }) {
    const [loading, setLoading] = useState(false);
    const [chartData, setChartData] = useState(null);
    const [error, setError] = useState("");

    useEffect(() => {
        if (open && meterId && month) {
            loadChartData();
        }
    }, [open, meterId, month]);

    async function loadChartData() {
        try {
            setLoading(true);
            setError("");
            const res = await getChartConsumption(meterId, month);
            setChartData(res.data);
        } catch (err) {
            console.error("Failed to load chart data:", err);
            setError("Không tải được dữ liệu biểu đồ");
        } finally {
            setLoading(false);
        }
    }

    const formatMonth = (monthStr) => {
        if (!monthStr) return "";
        const [year, mon] = monthStr.split("-");
        return `T${mon}/${year.slice(2)}`;
    };

    return (
        <Dialog
            open={open}
            onClose={onClose}
            maxWidth="md"
            fullWidth
            PaperProps={{
                sx: { borderRadius: 3 },
            }}
        >
            <DialogTitle sx={{ pr: 6 }}>
                <Stack direction="row" justifyContent="space-between" alignItems="center">
                    <Box>
                        <Typography variant="h6" sx={{ fontWeight: 700 }}>
                            Biểu đồ tiêu thụ điện
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            {meterId} - Tháng {month}
                        </Typography>
                    </Box>
                </Stack>
                <IconButton
                    onClick={onClose}
                    sx={{
                        position: "absolute",
                        right: 12,
                        top: 12,
                    }}
                >
                    <CloseIcon />
                </IconButton>
            </DialogTitle>

            <DialogContent>
                {loading && (
                    <Stack alignItems="center" justifyContent="center" sx={{ py: 6 }}>
                        <CircularProgress />
                    </Stack>
                )}

                {error && (
                    <Typography color="error" align="center" sx={{ py: 4 }}>
                        {error}
                    </Typography>
                )}

                {!loading && !error && chartData && (
                    <Box>
                        {/* Bar Chart for Consumption */}
                        <Box sx={{ mb: 4 }}>
                            <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 600 }}>
                                Tiêu thụ theo tháng (kWh)
                            </Typography>
                            <Box sx={{ height: 280 }}>
                                <ResponsiveContainer width="100%" height="100%">
                                    <BarChart data={chartData.points}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis
                                            dataKey="month"
                                            tickFormatter={formatMonth}
                                            tick={{ fontSize: 12 }}
                                        />
                                        <YAxis tick={{ fontSize: 12 }} />
                                        <Tooltip
                                            formatter={(value, name) => [
                                                value.toLocaleString(),
                                                name === "indexConsumption" ? "Tiêu thụ" : name,
                                            ]}
                                            labelFormatter={(label) => `Tháng ${formatMonth(label)}`}
                                        />
                                        <Bar
                                            dataKey="indexConsumption"
                                            radius={[6, 6, 0, 0]}
                                            name="Tiêu thụ"
                                        >
                                            {chartData.points.map((entry, index) => (
                                                <Cell
                                                    key={`cell-${index}`}
                                                    fill={ALERT_COLORS[entry.alertLevel] || ALERT_COLORS.NORMAL}
                                                />
                                            ))}
                                        </Bar>
                                    </BarChart>
                                </ResponsiveContainer>
                            </Box>
                        </Box>

                        {/* Line Chart for Index */}
                        <Box>
                            <Typography variant="subtitle1" sx={{ mb: 2, fontWeight: 600 }}>
                                Chỉ số đồng hồ theo tháng
                            </Typography>
                            <Box sx={{ height: 280 }}>
                                <ResponsiveContainer width="100%" height="100%">
                                    <LineChart data={chartData.points}>
                                        <CartesianGrid strokeDasharray="3 3" />
                                        <XAxis
                                            dataKey="month"
                                            tickFormatter={formatMonth}
                                            tick={{ fontSize: 12 }}
                                        />
                                        <YAxis tick={{ fontSize: 12 }} />
                                        <Tooltip
                                            formatter={(value, name) => {
                                                if (name === "indexPrevMonth") return [value?.toLocaleString() || 0, "Chỉ số đầu"];
                                                if (name === "indexLastMonth") return [value?.toLocaleString() || 0, "Chỉ số cuối"];
                                                return [value, name];
                                            }}
                                            labelFormatter={(label) => `Tháng ${formatMonth(label)}`}
                                        />
                                        <Line
                                            type="monotone"
                                            dataKey="indexPrevMonth"
                                            stroke="#1976d2"
                                            strokeWidth={2}
                                            dot={{ r: 4 }}
                                            name="Chỉ số đầu"
                                        />
                                        <Line
                                            type="monotone"
                                            dataKey="indexLastMonth"
                                            stroke="#388e3c"
                                            strokeWidth={2}
                                            dot={{ r: 4 }}
                                            name="Chỉ số cuối"
                                        />
                                    </LineChart>
                                </ResponsiveContainer>
                            </Box>
                        </Box>

                        {/* Legend */}
                        <Stack direction="row" spacing={2} justifyContent="center" sx={{ mt: 2 }}>
                            {Object.entries(ALERT_COLORS).map(([level, color]) => (
                                <Stack key={level} direction="row" alignItems="center" spacing={0.5}>
                                    <Box
                                        sx={{
                                            width: 12,
                                            height: 12,
                                            borderRadius: "50%",
                                            bgcolor: color,
                                        }}
                                    />
                                    <Typography variant="caption">{level}</Typography>
                                </Stack>
                            ))}
                        </Stack>
                    </Box>
                )}
            </DialogContent>
        </Dialog>
    );
}

