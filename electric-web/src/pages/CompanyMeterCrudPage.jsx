import React, { useEffect, useState, useCallback } from "react";
import { Container, Grid, Snackbar, Alert, Stack, Typography, Box } from "@mui/material";
import CompanyMeterForm from "../components/layout/CompanyMeterForm";
import CompanyMeterList from "../components/layout/CompanyMeterList"; // Đảm bảo bạn đã tạo file này
import { getCompanies } from "../api/companyApi";
import { getMeters } from "../api/meterApi";

export default function CompanyMeterPage() {
    const [companies, setCompanies] = useState([]);
    const [meters, setMeters] = useState([]);
    const [loading, setLoading] = useState(false);

    // State điều khiển Form
    const [mode, setMode] = useState("create");
    const [editType, setEditType] = useState(null);
    const [editingData, setEditingData] = useState(null);

    const [status, setStatus] = useState({ open: false, msg: "", sev: "success" });

    const loadData = useCallback(async () => {
        setLoading(true);
        try {
            const [resC, resM] = await Promise.all([getCompanies(), getMeters()]);
            setCompanies(resC.data || []);
            setMeters(resM.data || []);
        } catch (err) {
            setStatus({ open: true, msg: "Lỗi đồng bộ dữ liệu", sev: "error" });
        } finally { setLoading(false); }
    }, []);

    useEffect(() => { loadData(); }, [loadData]);

    const handleEdit = (type, data) => {
        setMode("update");
        setEditType(type);
        setEditingData(data);
    };

    const handleCancel = () => {
        setMode("create");
        setEditType(null);
        setEditingData(null);
    };

    return (
        <Container maxWidth="xl" sx={{ py: 4 }}>
            <Box sx={{ mb: 4 }}>
                <Typography variant="h4" fontWeight={800} color="primary">Quản lý Company & Meter</Typography>
                <Typography variant="body2" color="text.secondary">Hệ thống quản lý tập trung thông tin khách hàng</Typography>
            </Box>

            <Grid container spacing={4}>
                {/* BÊN TRÁI: SMART FORM DUY NHẤT */}
                <Grid item xs={12} md={4}>
                    <CompanyMeterForm
                        mode={mode}
                        editType={editType}
                        editingData={editingData}
                        companies={companies}
                        onSuccess={(msg) => { setStatus({ open: true, msg, sev: "success" }); loadData(); handleCancel(); }}
                        onCancelEdit={handleCancel}
                    />
                </Grid>

                {/* BÊN PHẢI: DANH SÁCH (THAY THẾ CHO 2 FORM CŨ) */}
                <Grid item xs={12} md={8}>
                    <CompanyMeterList
                        companies={companies}
                        meters={meters}
                        loading={loading}
                        onEdit={handleEdit}
                        activeId={editingData?.companyId || editingData?.meterId}
                    />
                </Grid>
            </Grid>

            <Snackbar open={status.open} autoHideDuration={3000} onClose={() => setStatus({ ...status, open: false })}>
                <Alert severity={status.sev} variant="filled">{status.msg}</Alert>
            </Snackbar>
        </Container>
    );
}