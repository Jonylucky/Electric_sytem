import React from 'react';
import { Box, Typography, Container, Paper, Divider, List, ListItem, ListItemText, Switch, Button } from '@mui/material';
import SettingsIcon from '@mui/icons-material/Settings';

const SettingsPage = () => {
    const [notifications, setNotifications] = React.useState(true);
    const [darkMode, setDarkMode] = React.useState(false);

    return (
        <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
            <Paper elevation={3} sx={{ p: 4, borderRadius: 3 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
                    <SettingsIcon sx={{ fontSize: 48, mr: 2, color: 'primary.main' }} />
                    <Box>
                        <Typography variant="h4" component="h1" gutterBottom fontWeight={700}>
                            Settings
                        </Typography>
                        <Typography variant="h6" color="text.secondary">
                            Configure application preferences
                        </Typography>
                    </Box>
                </Box>
                <Divider sx={{ mb: 3 }} />
                <List>
                    <ListItem>
                        <ListItemText primary="Email Notifications" secondary="Receive alerts for overdue readings" />
                        <Switch edge="end" checked={notifications} onChange={(e) => setNotifications(e.target.checked)} />
                    </ListItem>
                    <ListItem>
                        <ListItemText primary="Dark Mode" secondary="Enable dark theme" />
                        <Switch edge="end" checked={darkMode} onChange={(e) => setDarkMode(e.target.checked)} />
                    </ListItem>
                </List>
                <Box sx={{ mt: 4, textAlign: 'right' }}>
                    <Button variant="outlined" sx={{ mr: 2 }}>Cancel</Button>
                    <Button variant="contained">Save Changes</Button>
                </Box>
            </Paper>
        </Container>
    );
};

export default SettingsPage;

