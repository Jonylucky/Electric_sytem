import React from 'react';
import { Box, Typography, Container } from '@mui/material';
import { styled } from '@mui/material/styles';

const FooterStyled = styled(Box)(({ theme }) => ({
    backgroundColor: theme.palette.grey[900],
    color: theme.palette.grey[100],
    padding: theme.spacing(3, 0),
    marginTop: 'auto',
    borderTop: `1px solid ${theme.palette.divider}`,
}));

const Footer = () => {
    return (
        <FooterStyled>
            <Container maxWidth="lg">
                <Typography variant="body2" align="center" gutterBottom>
                    © {new Date().getFullYear()} Electric Management System. All rights reserved.
                </Typography>
                <Typography variant="caption" align="center" display="block">
                    Powered by React + Material UI
                </Typography>
            </Container>
        </FooterStyled>
    );
};

export default Footer;

