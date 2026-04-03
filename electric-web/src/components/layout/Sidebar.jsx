import React from 'react';
import { List, ListItemButton, ListItemIcon, ListItemText, Box } from '@mui/material';
import { NavLink, useLocation } from 'react-router-dom';
import DashboardIcon from '@mui/icons-material/Dashboard';
import BusinessIcon from '@mui/icons-material/Business';
import StraightenIcon from '@mui/icons-material/Straighten';
import AssessmentIcon from '@mui/icons-material/Assessment';
import SettingsIcon from '@mui/icons-material/Settings';
import TableChartIcon from '@mui/icons-material/TableChart';

const menuItems = [
    { text: 'Dashboard', path: '/dashboard', icon: DashboardIcon },
    { text: 'Company Meters', path: '/company-meter', icon: TableChartIcon },
    { text: 'Company', path: '/company', icon: BusinessIcon },
    { text: 'Meter', path: '/meter', icon: StraightenIcon },
    { text: 'Report', path: '/report', icon: AssessmentIcon },
    { text: 'Settings', path: '/settings', icon: SettingsIcon },
];

const Sidebar = () => {
    const location = useLocation();

    return (
        <Box sx={{ width: 280, flexShrink: 0, borderRight: 1, borderColor: 'divider' }}>
            <Box sx={{ p: 3, borderBottom: 1, borderColor: 'divider' }}>
                <ListItemText primary="Electric Web" primaryTypographyProps={{ variant: 'h6', fontWeight: 700 }} />
            </Box>
            <List sx={{ p: 1.5 }}>
                {menuItems.map((item) => {
                    const active = location.pathname === item.path;
                    const Icon = item.icon;
                    return (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            style={{ textDecoration: 'none', color: 'inherit' }}
                        >
                            {({ isActive }) => (
                                <ListItemButton selected={isActive || active} sx={{ borderRadius: 2, mb: 0.5 }}>
                                    <ListItemIcon>
                                        <Icon color={isActive ? 'primary' : 'inherit'} />
                                    </ListItemIcon>
                                    <ListItemText primary={item.text} />
                                </ListItemButton>
                            )}
                        </NavLink>
                    );
                })}
            </List>
        </Box>
    );
};

export default Sidebar;

