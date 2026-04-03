import React, { useState } from "react";
import {
    Paper, Table, TableBody, TableCell, TableContainer, TableHead,
    TableRow, Typography, Box, IconButton, Chip, Stack,
    FormControl, InputLabel, Select, MenuItem, alpha, CircularProgress, Divider
} from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";

export default function CompanyMeterList({
    companies,
    meters,
    loading,
    onEdit,
    activeId
}) {
    const [filterCompany, setFilterCompany] = useState("");

    const filteredMeters = filterCompany
        ? meters.filter(m => String(m.companyId) === String(filterCompany))
        : meters;

    const getCompanyName = (id) => {
        const company = companies.find(c => String(c.companyId) === String(id));
        return company ? company.companyName : "N/A";
    };

    const getFloorLabel = (meter) => {
        // Ưu tiên show dữ liệu dễ hiểu nếu backend đã trả về
        if (meter.locationName && meter.floor !== undefined && meter.floor !== null) {
            return `${meter.locationName} - Floor ${meter.floor}`;
        }

        if (meter.floor !== undefined && meter.floor !== null) {
            return `Floor ${meter.floor}`;
        }

        if (meter.locationName) {
            return meter.locationName;
        }

        if (meter.locationId !== undefined && meter.locationId !== null) {
            return `Location ID: ${meter.locationId}`;
        }

        return "N/A";
    };

    return (
        <Stack spacing={4}>
            <Paper elevation={2} sx={{ borderRadius: 3, overflow: "hidden" }}>
                <Box
                    sx={{
                        p: 2,
                        bgcolor: alpha("#1976d2", 0.05),
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center"
                    }}
                >
                    <Typography variant="subtitle1" fontWeight={700} color="primary">
                        DANH SÁCH CÔNG TY
                    </Typography>
                    <Chip
                        label={`${companies.length} đơn vị`}
                        size="small"
                        color="primary"
                        variant="outlined"
                    />
                </Box>

                <TableContainer sx={{ maxHeight: 300 }}>
                    <Table stickyHeader size="small">
                        <TableHead>
                            <TableRow>
                                <TableCell sx={{ fontWeight: "bold", bgcolor: "#fafafa" }}>
                                    Mã công ty
                                </TableCell>
                                <TableCell sx={{ fontWeight: "bold", bgcolor: "#fafafa" }}>
                                    Tên công ty
                                </TableCell>
                                <TableCell align="right" sx={{ fontWeight: "bold", bgcolor: "#fafafa" }}>
                                    Sửa
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {loading.company ? (
                                <TableRow>
                                    <TableCell colSpan={3} align="center" sx={{ py: 3 }}>
                                        <CircularProgress size={24} />
                                    </TableCell>
                                </TableRow>
                            ) : (
                                companies.map((company) => (
                                    <TableRow
                                        key={company.companyId}
                                        hover
                                        sx={{
                                            bgcolor: activeId === company.companyId ? alpha("#ed6c02", 0.1) : "inherit",
                                            transition: "background-color 0.3s"
                                        }}
                                    >
                                        <TableCell>{company.companyCode || "-"}</TableCell>
                                        <TableCell sx={{ fontWeight: activeId === company.companyId ? 700 : 400 }}>
                                            {company.companyName}
                                        </TableCell>
                                        <TableCell align="right">
                                            <IconButton
                                                size="small"
                                                color="primary"
                                                onClick={() => onEdit("company", company)}
                                            >
                                                <EditIcon fontSize="small" />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
            </Paper>

            <Divider sx={{ borderStyle: "dashed" }} />

            <Paper elevation={2} sx={{ borderRadius: 3, overflow: "hidden" }}>
                <Box
                    sx={{
                        p: 2,
                        bgcolor: alpha("#9c27b0", 0.05),
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        flexWrap: "wrap",
                        gap: 2
                    }}
                >
                    <Box>
                        <Typography variant="subtitle1" fontWeight={700} color="secondary">
                            DANH SÁCH METER
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                            Lọc theo công ty quản lý
                        </Typography>
                    </Box>

                    <FormControl size="small" sx={{ minWidth: 220 }}>
                        <InputLabel>Công ty</InputLabel>
                        <Select
                            value={filterCompany}
                            label="Công ty"
                            onChange={(e) => setFilterCompany(e.target.value)}
                        >
                            <MenuItem value="">Tất cả công ty</MenuItem>
                            {companies.map((c) => (
                                <MenuItem key={c.companyId} value={c.companyId}>
                                    {c.companyName}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Box>

                <TableContainer sx={{ maxHeight: 400 }}>
                    <Table stickyHeader size="small">
                        <TableHead>
                            <TableRow>
                                <TableCell sx={{ fontWeight: "bold", bgcolor: "#fafafa" }}>
                                    Meter ID
                                </TableCell>
                                <TableCell sx={{ fontWeight: "bold", bgcolor: "#fafafa" }}>
                                    Tên đồng hồ
                                </TableCell>
                                <TableCell sx={{ fontWeight: "bold", bgcolor: "#fafafa" }}>
                                    Công ty
                                </TableCell>
                                <TableCell sx={{ fontWeight: "bold", bgcolor: "#fafafa" }}>
                                    Floor / Vị trí
                                </TableCell>
                                <TableCell align="right" sx={{ fontWeight: "bold", bgcolor: "#fafafa" }}>
                                    Sửa
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {loading.meter ? (
                                <TableRow>
                                    <TableCell colSpan={5} align="center" sx={{ py: 3 }}>
                                        <CircularProgress size={24} />
                                    </TableCell>
                                </TableRow>
                            ) : filteredMeters.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={5} align="center" sx={{ py: 3 }}>
                                        Không có dữ liệu meter
                                    </TableCell>
                                </TableRow>
                            ) : (
                                filteredMeters.map((meter) => (
                                    <TableRow
                                        key={meter.meterId}
                                        hover
                                        sx={{
                                            bgcolor: activeId === meter.meterId ? alpha("#1976d2", 0.1) : "inherit",
                                            transition: "background-color 0.3s"
                                        }}
                                    >
                                        <TableCell sx={{ fontWeight: "bold", color: "secondary.main" }}>
                                            {meter.meterId}
                                        </TableCell>
                                        <TableCell>{meter.meterName || "-"}</TableCell>
                                        <TableCell>{getCompanyName(meter.companyId)}</TableCell>
                                        <TableCell>
                                            <Chip
                                                label={getFloorLabel(meter)}
                                                size="small"
                                                variant="outlined"
                                            />
                                        </TableCell>
                                        <TableCell align="right">
                                            <IconButton
                                                size="small"
                                                color="secondary"
                                                onClick={() => onEdit("meter", meter)}
                                            >
                                                <EditIcon fontSize="small" />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
            </Paper>
        </Stack>
    );
}