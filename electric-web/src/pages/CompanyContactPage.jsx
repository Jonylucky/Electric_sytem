import React, { useState, useEffect, useCallback } from 'react';
import {
    Container, Typography, Box, Paper, Table, TableBody, TableCell,
    TableContainer, TableHead, TableRow, Button, IconButton, Dialog,
    DialogTitle, DialogContent, DialogActions, TextField, FormControl,
    InputLabel, Select, MenuItem, Switch, FormControlLabel, CircularProgress,
    Snackbar, Alert, Stack
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import ContactPhoneIcon from '@mui/icons-material/ContactPhone';
import AddIcon from '@mui/icons-material/Add';

import { getCompanyContacts, createCompanyContact, updateCompanyContact, deleteCompanyContact, getCompanies } from '../api/companyApi';

export default function CompanyContactPage() {
    const [contacts, setContacts] = useState([]);
    const [companies, setCompanies] = useState([]);
    const [loading, setLoading] = useState(false);
    const [dialogOpen, setDialogOpen] = useState(false);
    const [editingContact, setEditingContact] = useState(null);
    const [formData, setFormData] = useState({
        companyId: '',
        contactName: '',
        email: '',
        phone: '',
        contactType: 'admin',
        isPrimary: false,
        isActive: true
    });
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

    // Load data on mount
    const loadData = useCallback(async () => {
        setLoading(true);
        try {
            const [contactsRes, companiesRes] = await Promise.all([
                getCompanyContacts(),
                getCompanies()
            ]);
            setContacts(contactsRes.data || []);
            setCompanies(companiesRes.data || []);
        } catch (error) {
            showSnackbar('Lỗi tải dữ liệu', 'error');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadData();
    }, [loadData]);

    const showSnackbar = (message, severity = 'success') => {
        setSnackbar({ open: true, message, severity });
    };

    const handleOpenDialog = (contact = null) => {
        if (contact) {
            setEditingContact(contact);
            setFormData({
                companyId: contact.companyId || '',
                contactName: contact.contactName || '',
                email: contact.email || '',
                phone: contact.phone || '',
                contactType: contact.contactType || 'admin',
                isPrimary: contact.isPrimary || false,
                isActive: contact.isActive || true
            });
        } else {
            setEditingContact(null);
            setFormData({
                companyId: '',
                contactName: '',
                email: '',
                phone: '',
                contactType: 'admin',
                isPrimary: false,
                isActive: true
            });
        }
        setDialogOpen(true);
    };

    const handleCloseDialog = () => {
        setDialogOpen(false);
        setEditingContact(null);
        setFormData({
            companyId: '',
            contactName: '',
            email: '',
            phone: '',
            contactType: 'admin',
            isPrimary: false,
            isActive: true
        });
    };

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async () => {
        try {
            const payload = {
                company: { companyId: parseInt(formData.companyId) },
                contactName: formData.contactName,
                email: formData.email,
                phone: formData.phone,
                contactType: formData.contactType,
                isPrimary: formData.isPrimary,
                isActive: formData.isActive
            };

            if (editingContact) {
                await updateCompanyContact(editingContact.contactId, payload);
                showSnackbar('Cập nhật liên hệ thành công!');
            } else {
                await createCompanyContact(payload);
                showSnackbar('Tạo liên hệ mới thành công!');
            }

            setDialogOpen(false);
            loadData();
        } catch (error) {
            showSnackbar('Lỗi xử lý dữ liệu: ' + (error.message || 'Unknown error'), 'error');
        }
    };

    const handleDelete = async (contactId) => {
        if (!window.confirm('Xác nhận xóa liên hệ này?')) return;

        try {
            await deleteCompanyContact(contactId);
            showSnackbar('Xóa liên hệ thành công!');
            loadData();
        } catch (error) {
            showSnackbar('Lỗi xóa liên hệ', 'error');
        }
    };

    const contactTypeOptions = ['admin', 'accountant', 'technical', 'manager'];

    return (
        <Container maxWidth="xl" sx={{ py: 4 }}>
            <Box sx={{ mb: 4 }}>
                <Typography variant="h4" fontWeight={800} color="primary" gutterBottom>
                    <ContactPhoneIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
                    Quản lý liên hệ công ty
                </Typography>
                <Typography variant="body1" color="text.secondary">
                    Quản lý thông tin liên hệ của các công ty
                </Typography>
            </Box>

            <Paper sx={{ p: 3, borderRadius: 3 }}>
                <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
                    <Typography variant="h6" fontWeight={600}>
                        Danh sách liên hệ ({contacts.length})
                    </Typography>
                    <Button
                        variant="contained"
                        startIcon={<AddIcon />}
                        onClick={() => handleOpenDialog()}
                    >
                        Thêm liên hệ
                    </Button>
                </Stack>

                {loading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <TableContainer sx={{ maxHeight: 600, borderRadius: 2, overflow: 'auto' }}>
                        <Table stickyHeader size="small">
                            <TableHead sx={{ bgcolor: '#f5f5f5' }}>
                                <TableRow>
                                    <TableCell sx={{ fontWeight: 700 }}>ID</TableCell>
                                    <TableCell sx={{ fontWeight: 700 }}>Công ty</TableCell>
                                    <TableCell sx={{ fontWeight: 700 }}>Tên liên hệ</TableCell>
                                    <TableCell sx={{ fontWeight: 700 }}>Email</TableCell>
                                    <TableCell sx={{ fontWeight: 700 }}>Phone</TableCell>
                                    <TableCell sx={{ fontWeight: 700 }}>Loại</TableCell>
                                    <TableCell sx={{ fontWeight: 700, minWidth: 80 }}>Chính</TableCell>
                                    <TableCell sx={{ fontWeight: 700, minWidth: 80 }}>Hoạt động</TableCell>
                                    <TableCell align="right" sx={{ fontWeight: 700 }}>Thao tác</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {contacts.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={9} align="center" sx={{ py: 4 }}>
                                            <Typography color="text.secondary">
                                                Chưa có dữ liệu liên hệ
                                            </Typography>
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    contacts.map((contact) => (
                                        <TableRow key={contact.contactId} hover>
                                            <TableCell>{contact.contactId}</TableCell>
                                            <TableCell>{contact.company?.companyName || '-'}</TableCell>
                                            <TableCell>{contact.contactName}</TableCell>
                                            <TableCell>{contact.email}</TableCell>
                                            <TableCell>{contact.phone}</TableCell>
                                            <TableCell>
                                                <Box sx={{ px: 1.5, py: 0.5, bgcolor: 'primary.50', borderRadius: 1, fontSize: '0.75rem' }}>
                                                    {contact.contactType}
                                                </Box>
                                            </TableCell>
                                            <TableCell>
                                                <Box sx={{ p: 0.5, borderRadius: 1, bgcolor: contact.isPrimary ? 'success.100' : 'grey.100' }}>
                                                    {contact.isPrimary ? '✓' : '✗'}
                                                </Box>
                                            </TableCell>
                                            <TableCell>
                                                <Box sx={{ p: 0.5, borderRadius: 1, bgcolor: contact.isActive ? 'success.100' : 'error.100' }}>
                                                    {contact.isActive ? '✓' : '✗'}
                                                </Box>
                                            </TableCell>
                                            <TableCell align="right">
                                                <IconButton
                                                    size="small"
                                                    onClick={() => handleOpenDialog(contact)}
                                                    title="Sửa"
                                                >
                                                    <EditIcon fontSize="small" />
                                                </IconButton>
                                                <IconButton
                                                    size="small"
                                                    color="error"
                                                    onClick={() => handleDelete(contact.contactId)}
                                                    title="Xóa"
                                                >
                                                    <DeleteIcon fontSize="small" />
                                                </IconButton>
                                            </TableCell>
                                        </TableRow>
                                    ))
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}
            </Paper>

            {/* Dialog Form */}
            <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
                <DialogTitle>
                    {editingContact ? 'Chỉnh sửa liên hệ' : 'Thêm liên hệ mới'}
                </DialogTitle>
                <DialogContent sx={{ p: 3 }}>
                    <Stack spacing={2.5}>
                        <FormControl fullWidth size="small">
                            <InputLabel>Công ty</InputLabel>
                            <Select
                                name="companyId"
                                value={formData.companyId}
                                label="Công ty"
                                onChange={handleInputChange}
                            >
                                {companies.map((company) => (
                                    <MenuItem key={company.companyId} value={company.companyId}>
                                        {company.companyName || company.companyId}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                        <TextField
                            fullWidth
                            size="small"
                            label="Tên liên hệ"
                            name="contactName"
                            value={formData.contactName}
                            onChange={handleInputChange}
                        />

                        <TextField
                            fullWidth
                            size="small"
                            label="Email"
                            name="email"
                            type="email"
                            value={formData.email}
                            onChange={handleInputChange}
                        />

                        <TextField
                            fullWidth
                            size="small"
                            label="Số điện thoại"
                            name="phone"
                            value={formData.phone}
                            onChange={handleInputChange}
                        />

                        <FormControl fullWidth size="small">
                            <InputLabel>Loại liên hệ</InputLabel>
                            <Select
                                name="contactType"
                                value={formData.contactType}
                                label="Loại liên hệ"
                                onChange={handleInputChange}
                            >
                                {contactTypeOptions.map((type) => (
                                    <MenuItem key={type} value={type}>
                                        {type.charAt(0).toUpperCase() + type.slice(1)}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>

                        <Stack direction="row" spacing={3} justifyContent="space-between">
                            <FormControlLabel
                                control={
                                    <Switch
                                        name="isPrimary"
                                        checked={formData.isPrimary}
                                        onChange={handleInputChange}
                                    />
                                }
                                label="Liên hệ chính"
                            />
                            <FormControlLabel
                                control={
                                    <Switch
                                        name="isActive"
                                        checked={formData.isActive}
                                        onChange={handleInputChange}
                                    />
                                }
                                label="Hoạt động"
                            />
                        </Stack>
                    </Stack>
                </DialogContent>
                <DialogActions sx={{ px: 3, pb: 3 }}>
                    <Button onClick={handleCloseDialog}>Hủy</Button>
                    <Button
                        variant="contained"
                        onClick={handleSubmit}
                        disabled={!formData.companyId || !formData.contactName.trim()}
                    >
                        {editingContact ? 'Cập nhật' : 'Tạo mới'}
                    </Button>
                </DialogActions>
            </Dialog>

            {/* Snackbar */}
            <Snackbar
                open={snackbar.open}
                autoHideDuration={4000}
                onClose={() => setSnackbar(prev => ({ ...prev, open: false }))}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
            >
                <Alert
                    onClose={() => setSnackbar(prev => ({ ...prev, open: false }))}
                    severity={snackbar.severity}
                    variant="filled"
                    sx={{ width: '100%' }}
                >
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </Container>
    );
}
