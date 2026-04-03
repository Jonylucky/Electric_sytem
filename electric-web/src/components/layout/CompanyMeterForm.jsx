import React, { useEffect, useMemo, useState } from "react";
import {
    Box,
    Button,
    Chip,
    Divider,
    MenuItem,
    Paper,
    Stack,
    TextField,
    Typography,
    alpha,
} from "@mui/material";
import { createCompany, updateCompany } from "../../api/companyApi";
import { createMeter, updateMeter } from "../../api/meterApi";
import { getLocations } from "../../api/apiLocation";

const CREATE_MODES = {
    FULL: "full",
    METER_ONLY: "meterOnly",
    COMPANY_ONLY: "create_company_only",
};

const initialForm = {
    companyCode: "",
    companyName: "",
    customerCode: "",
    meterId: "",
    meterCode: "",
    meterName: "",
    companyId: "",
    locationId: "",
};

export default function CompanyMeterForm({
    mode,
    editType,
    editingData,
    companies = [],
    onSuccess,
    onCancelEdit,
}) {
    const isUpdate = mode === "update";

    const [form, setForm] = useState(initialForm);
    const [createMode, setCreateMode] = useState(CREATE_MODES.FULL);
    const [locations, setLocations] = useState([]);
    const [loadingLocations, setLoadingLocations] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    const isCreateFull = !isUpdate && createMode === CREATE_MODES.FULL;
    const isCreateMeterOnly = !isUpdate && createMode === CREATE_MODES.METER_ONLY;
    const isCreateCompanyOnly =
        !isUpdate && createMode === CREATE_MODES.COMPANY_ONLY;

    const showCompanySection =
        (isUpdate && editType === "company") || isCreateFull || isCreateCompanyOnly;

    const showMeterSection =
        (isUpdate && editType === "meter") || isCreateFull || isCreateMeterOnly;

    useEffect(() => {
        async function loadLocations() {
            setLoadingLocations(true);
            try {
                const data = await getLocations();
                setLocations(Array.isArray(data) ? data : []);
            } catch (error) {
                console.error("Error loading locations:", error);
                setLocations([]);
            } finally {
                setLoadingLocations(false);
            }
        }
        loadLocations();
    }, []);

    useEffect(() => {
        if (isUpdate && editingData) {
            if (editType === "company") {
                setForm({
                    ...initialForm,
                    companyId: editingData.companyId ?? "",
                    companyCode: editingData.companyCode ?? "",
                    companyName: editingData.companyName ?? "",
                    customerCode: editingData.customerCode ?? editingData.companyCode ?? "",
                });
            } else if (editType === "meter") {
                setForm({
                    ...initialForm,
                    meterId: editingData.meterId ?? "",
                    meterCode: editingData.meterCode ?? "",
                    meterName: editingData.meterName ?? "",
                    companyId: editingData.companyId ?? "",
                    locationId: editingData.locationId ?? "",
                });
            }
        } else {
            setForm(initialForm);
            setCreateMode(CREATE_MODES.FULL);
        }
    }, [editingData, editType, isUpdate]);

    useEffect(() => {
        if (isUpdate) return;

        setForm((prev) => {
            if (createMode === CREATE_MODES.FULL) {
                return {
                    ...prev,
                    companyId: "",
                };
            }

            if (createMode === CREATE_MODES.METER_ONLY) {
                return {
                    ...prev,
                    companyCode: "",
                    companyName: "",
                    customerCode: "",
                    meterId: "",
                    meterCode: "",
                    meterName: "",
                    companyId: "",
                    locationId: "",
                };
            }

            if (createMode === CREATE_MODES.COMPANY_ONLY) {
                return {
                    ...prev,
                    meterId: "",
                    meterCode: "",
                    meterName: "",
                    companyId: "",
                    locationId: "",
                };
            }

            return prev;
        });
    }, [createMode, isUpdate]);

    const successMessage = useMemo(() => {
        if (isUpdate) return "Cập nhật thành công!";
        if (isCreateCompanyOnly) return "Tạo mới công ty thành công!";
        if (isCreateMeterOnly) return "Tạo mới đồng hồ thành công!";
        return "Tạo mới công ty và đồng hồ thành công!";
    }, [isUpdate, isCreateCompanyOnly, isCreateMeterOnly]);

    const resetByCreateMode = () => {
        if (isCreateFull) {
            setForm({
                ...initialForm,
            });
            return;
        }

        if (isCreateMeterOnly) {
            setForm((prev) => ({
                ...initialForm,
                companyCode: "",
                companyName: "",
                customerCode: "",
            }));
            return;
        }

        if (isCreateCompanyOnly) {
            setForm({
                ...initialForm,
            });
        }
    };

    const handleFieldChange = (name, value) => {
        if (name === "companyCode") {
            setForm((prev) => ({
                ...prev,
                companyCode: value,
                customerCode: value,
            }));
            return;
        }

        setForm((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSave = async (e) => {
        e.preventDefault();
        setSubmitting(true);

        try {
            console.log("handleSave:", { mode, editType, createMode, form });

            if (!isUpdate) {
                if (isCreateFull) {
                    if (
                        !form.companyCode?.trim() ||
                        !form.companyName?.trim() ||
                        !form.meterId?.trim() ||
                        !form.meterName?.trim()
                    ) {
                        throw new Error(
                            "Vui lòng nhập đầy đủ Mã công ty, Tên công ty, Meter ID và Tên đồng hồ"
                        );
                    }

                    const companyPayload = {
                        companyCode: form.companyCode.trim(),
                        companyName: form.companyName.trim(),
                        customerCode: form.customerCode?.trim() || null,
                    };

                    console.log("create_full companyPayload =", companyPayload);

                    const companyRes = await createCompany(companyPayload);
                    const createdCompany = companyRes?.data ?? companyRes;

                    if (!createdCompany?.companyId) {
                        throw new Error("Tạo công ty thành công nhưng không nhận được companyId");
                    }

                    const meterPayload = {
                        meterId: form.meterId.trim(),
                        meterCode: form.meterCode?.trim() || null,
                        meterName: form.meterName.trim(),
                        companyId: Number(createdCompany.companyId),
                        locationId: form.locationId ? Number(form.locationId) : null,
                    };

                    console.log("create_full meterPayload =", meterPayload);

                    await createMeter(meterPayload);
                } else if (isCreateMeterOnly) {
                    if (!form.companyId || !form.meterId?.trim() || !form.meterName?.trim()) {
                        throw new Error("Vui lòng chọn công ty, nhập Meter ID và Tên đồng hồ");
                    }

                    const meterPayload = {
                        meterId: form.meterId.trim(),
                        meterCode: form.meterCode?.trim() || null,
                        meterName: form.meterName.trim(),
                        companyId: Number(form.companyId),
                        locationId: form.locationId ? Number(form.locationId) : null,
                    };

                    console.log("create_meter_only meterPayload =", meterPayload);

                    await createMeter(meterPayload);
                } else if (isCreateCompanyOnly) {
                    if (!form.companyCode?.trim() || !form.companyName?.trim()) {
                        throw new Error("Vui lòng nhập đầy đủ Mã công ty và Tên công ty");
                    }

                    const companyPayload = {
                        companyCode: form.companyCode.trim(),
                        companyName: form.companyName.trim(),
                        customerCode: form.customerCode?.trim() || null,
                    };

                    console.log("create_company_only companyPayload =", companyPayload);

                    await createCompany(companyPayload);
                }
            } else {
                if (editType === "company") {
                    if (!form.companyCode?.trim() || !form.companyName?.trim()) {
                        throw new Error("Vui lòng nhập đầy đủ Mã công ty và Tên công ty");
                    }

                    const companyPayload = {
                        companyCode: form.companyCode.trim(),
                        companyName: form.companyName.trim(),
                    };

                    await updateCompany(editingData.companyId, companyPayload);
                } else if (editType === "meter") {
                    if (!form.meterName?.trim()) {
                        throw new Error("Vui lòng nhập Tên đồng hồ");
                    }

                    const meterPayload = {
                        meterId: editingData.meterId,
                        meterCode: form.meterCode?.trim() || editingData.meterCode || null,
                        meterName: form.meterName.trim(),
                        companyId: form.companyId ? Number(form.companyId) : Number(editingData.companyId),
                        locationId: form.locationId ? Number(form.locationId) : (editingData.locationId ? Number(editingData.locationId) : null),
                    };

                    console.log("update_meter meterPayload =", meterPayload);
                    await updateMeter(editingData.meterId, meterPayload);
                }
            }

            onSuccess?.(successMessage, "success");

            if (!isUpdate) {
                resetByCreateMode();
            }
        } catch (err) {
            console.error("Save error:", err);
            onSuccess?.(err?.message || "Có lỗi xảy ra", "error");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Paper
            elevation={3}
            sx={{
                p: 3,
                borderRadius: 3,
                borderTop: `6px solid ${isUpdate ? "#ed6c02" : "#1976d2"}`,
            }}
        >
            <Typography
                variant="h6"
                fontWeight={700}
                color={isUpdate ? "#ed6c02" : "#1976d2"}
            >
                {isUpdate ? `SỬA ${editType?.toUpperCase()}` : "THÊM MỚI DỮ LIỆU"}
            </Typography>

            {!isUpdate && (
                <Stack direction="row" spacing={1.5} sx={{ mt: 2, flexWrap: "wrap" }}>
                    <Chip
                        label="Tạo đầy đủ (công ty + đồng hồ)"
                        color={createMode === CREATE_MODES.FULL ? "primary" : "default"}
                        onClick={() => setCreateMode(CREATE_MODES.FULL)}
                        sx={{ cursor: "pointer" }}
                    />
                    <Chip
                        label="Chỉ tạo đồng hồ"
                        color={createMode === CREATE_MODES.METER_ONLY ? "primary" : "default"}
                        onClick={() => setCreateMode(CREATE_MODES.METER_ONLY)}
                        sx={{ cursor: "pointer" }}
                    />
                    <Chip
                        label="Chỉ tạo công ty"
                        color={
                            createMode === CREATE_MODES.COMPANY_ONLY ? "primary" : "default"
                        }
                        onClick={() => setCreateMode(CREATE_MODES.COMPANY_ONLY)}
                        sx={{ cursor: "pointer" }}
                    />
                </Stack>
            )}

            <Box component="form" onSubmit={handleSave} sx={{ mt: 3 }}>
                <Stack spacing={2.5}>
                    {showCompanySection && (
                        <Stack
                            spacing={2}
                            sx={{
                                p: 2,
                                bgcolor: alpha("#1976d2", 0.03),
                                borderRadius: 2,
                            }}
                        >
                            <Typography variant="caption" fontWeight={900} color="primary">
                                THÔNG TIN CÔNG TY
                            </Typography>

                            <TextField
                                label="Mã công ty"
                                size="small"
                                fullWidth
                                value={form.companyCode}
                                onChange={(e) => handleFieldChange("companyCode", e.target.value)}
                            />

                            <TextField
                                label="Tên công ty"
                                size="small"
                                fullWidth
                                required
                                value={form.companyName}
                                onChange={(e) => handleFieldChange("companyName", e.target.value)}
                            />

                            {!isUpdate && (
                                <TextField
                                    label="Mã khách hàng (Customer Code)"
                                    size="small"
                                    fullWidth
                                    disabled
                                    value={form.customerCode}
                                    helperText="Tự động lấy theo Mã công ty"
                                />
                            )}
                        </Stack>
                    )}

                    {!isUpdate && showCompanySection && showMeterSection && (
                        <Divider sx={{ my: 1 }}>
                            <Chip label="HOẶC TIẾP TỤC" size="small" variant="outlined" />
                        </Divider>
                    )}

                    {showMeterSection && (
                        <Stack
                            spacing={2}
                            sx={{
                                p: 2,
                                bgcolor: alpha("#9c27b0", 0.03),
                                borderRadius: 2,
                            }}
                        >
                            <Typography variant="caption" fontWeight={900} color="secondary">
                                THÔNG TIN ĐỒNG HỒ
                            </Typography>

                            {(isCreateMeterOnly || (isUpdate && editType === "meter")) && (
                                <TextField
                                    select
                                    label="Company"
                                    size="small"
                                    fullWidth
                                    value={form.companyId}
                                    onChange={(e) => handleFieldChange("companyId", e.target.value)}
                                    helperText={isUpdate ? "Có thể thay đổi công ty" : "Chọn công ty"}
                                >
                                    <MenuItem value="">
                                        <em>Chọn công ty</em>
                                    </MenuItem>
                                    {companies.map((c) => (
                                        <MenuItem key={c.companyId} value={c.companyId}>
                                            {`${c.companyCode} - ${c.companyName}`}
                                        </MenuItem>
                                    ))}
                                </TextField>
                            )}

                            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                                <TextField
                                    label="Meter ID"
                                    size="small"
                                    fullWidth
                                    required={!isUpdate || isCreateMeterOnly || isCreateFull}
                                    value={form.meterId}
                                    onChange={(e) => handleFieldChange("meterId", e.target.value)}
                                    helperText="Mã định danh của đồng hồ"
                                    disabled={isUpdate}
                                />

                                <TextField
                                    label="Mã Meter"
                                    size="small"
                                    fullWidth
                                    value={form.meterCode}
                                    onChange={(e) => handleFieldChange("meterCode", e.target.value)}
                                />
                            </Stack>

                            <TextField
                                label="Tên đồng hồ"
                                size="small"
                                fullWidth
                                required
                                value={form.meterName}
                                onChange={(e) => handleFieldChange("meterName", e.target.value)}
                            />

                            <TextField
                                select
                                label="Floor"
                                size="small"
                                fullWidth
                                value={form.locationId}
                                onChange={(e) => handleFieldChange("locationId", e.target.value)}
                                disabled={loadingLocations}
                                helperText="Chọn vị trí/tầng cho đồng hồ"
                            >
                                <MenuItem value="">
                                    <em>Chọn vị trí</em>
                                </MenuItem>
                                {locations.map((location) => (
                                    <MenuItem key={location.locationId} value={location.locationId}>
                                        {`${location.locationName} - Floor ${location.floor}`}
                                    </MenuItem>
                                ))}
                            </TextField>
                        </Stack>
                    )}

                    <Stack direction="row" spacing={2} pt={2}>
                        <Button
                            type="submit"
                            variant="contained"
                            fullWidth
                            disabled={submitting}
                            sx={{
                                bgcolor: isUpdate ? "#ed6c02" : "#1976d2",
                                "&:hover": {
                                    bgcolor: isUpdate ? "#e65100" : "#1565c0",
                                },
                            }}
                        >
                            {submitting
                                ? "ĐANG XỬ LÝ..."
                                : isUpdate
                                    ? "LƯU THAY ĐỔI"
                                    : "TẠO DỮ LIỆU"}
                        </Button>

                        {isUpdate && (
                            <Button variant="outlined" color="inherit" onClick={onCancelEdit}>
                                HỦY
                            </Button>
                        )}
                    </Stack>
                </Stack>
            </Box>
        </Paper>
    );
}