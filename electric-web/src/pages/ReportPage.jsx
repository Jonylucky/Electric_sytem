import React from 'react';
import { Box, Typography, Container, Paper, Button } from '@mui/material';
import AssessmentIcon from '@mui/icons-material/Assessment';

const ReportPage = () => {
    return (
        <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
            <Paper elevation={3} sx={{ p: 4, borderRadius: 3 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                    <AssessmentIcon sx={{ fontSize: 48, mr: 2, color: 'primary.main' }} />
                    <Box>
                        <Typography variant="h4" component="h1" gutterBottom fontWeight={700}>
                            Reports
                        </Typography>
                        <Typography variant="h6" color="text.secondary">
                            Generate and view detailed electricity reports
                        </Typography>
                    </Box>
                </Box>
                <Box sx={{ p: 3, bgcolor: 'grey.50', borderRadius: 2 }}>
                    <Typography variant="body1">
                        Report page under development. Ready for custom charts, filters, and export features.
                    </Typography>
                    <Button variant="contained" sx={{ mt: 2 }} size="large">
                        Generate Report
                    </Button>
                </Box>
            </Paper>
        </Container>
    );
};

export default ReportPage;

