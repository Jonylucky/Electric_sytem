import React from 'react';
import {
    List,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Box
} from '@mui/material';
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
        <Box
            sx={{
                width: { xs: '100%', md: 280 },
                flexShrink: { md: 0 },
                borderRight: { md: 1 },
                borderColor: 'divider',
                height: { xs: 'auto', md: '100vh' },
                display: 'flex',
                flexDirection: 'column'
            }}
        >
            {/* Header */}
            <Box
                sx={{
                    p: 2,
                    borderBottom: 1,
                    borderColor: 'divider',
                    textAlign: { xs: 'center', md: 'left' }
                }}
            >
                <ListItemText
                    primary="Electric Web"
                    primaryTypographyProps={{
                        variant: 'h6',
                        fontWeight: 700
                    }}
                />
            </Box>

            {/* Menu */}
            <List sx={{ p: { xs: 1, md: 1.5 }, flexGrow: 1 }}>
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
                                <ListItemButton
                                    selected={isActive || active}
                                    sx={{
                                        borderRadius: 2,
                                        mb: 0.5,
                                        justifyContent: { xs: 'center', md: 'flex-start' }
                                    }}
                                >
                                    <ListItemIcon
                                        sx={{
                                            minWidth: 40,
                                            justifyContent: 'center'
                                        }}
                                    >
                                        <Icon color={isActive ? 'primary' : 'inherit'} />
                                    </ListItemIcon>

                                    {/* Hide text on small screens if needed */}
                                    <ListItemText
                                        primary={item.text}
                                        sx={{ display: { xs: 'none', sm: 'block' } }}
                                    />
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