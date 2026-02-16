import React, {useEffect, useMemo, useState} from 'react';
import {Outlet, useLocation, useNavigate} from 'react-router-dom';
import Sidebar from '../components/Sidebar.jsx';
import api from '../api/api';

const DashboardIcon = () => (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="1.8">
        <rect x="3" y="3" width="8" height="8" rx="2" />
        <rect x="13" y="3" width="8" height="5" rx="2" />
        <rect x="13" y="10" width="8" height="11" rx="2" />
        <rect x="3" y="13" width="8" height="8" rx="2" />
    </svg>
);

const PrinterIcon = () => (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="1.8">
        <rect x="5" y="4" width="14" height="10" rx="2" />
        <path d="M8 14h8v6H8z" />
        <circle cx="9.5" cy="9" r="1" fill="currentColor" />
    </svg>
);

const CommandsIcon = () => (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="1.8">
        <path d="M4 7h16" />
        <path d="M4 12h16" />
        <path d="M4 17h16" />
        <circle cx="8" cy="7" r="1" fill="currentColor" />
        <circle cx="16" cy="12" r="1" fill="currentColor" />
        <circle cx="10" cy="17" r="1" fill="currentColor" />
    </svg>
);

const AuditIcon = () => (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="1.8">
        <path d="M6 4h9l3 3v11a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2z" />
        <path d="M9 12h6" />
        <path d="M9 16h3" />
    </svg>
);

const UsersIcon = () => (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="1.8">
        <path d="M16 11a4 4 0 10-8 0 4 4 0 008 0z" />
        <path d="M4 20a6 6 0 0116 0" />
    </svg>
);

const AlertIcon = () => (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="1.8">
        <path d="M12 2L2 20h20L12 2z" />
        <path d="M12 9v4" />
        <circle cx="12" cy="17" r="0.5" fill="currentColor" />
    </svg>
);

const MaintenanceIcon = () => (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="1.8">
        <path d="M14.7 6.3a1 1 0 010 1.4l-2.2 2.2a5 5 0 006.6 6.6l2.2-2.2a1 1 0 011.4 0l1.1 1.1a1 1 0 010 1.4l-1.8 1.8a7 7 0 01-9.9 0L9 15.9a7 7 0 010-9.9l1.8-1.8a1 1 0 011.4 0l2.5 2.1z" />
        <circle cx="7" cy="17" r="2" />
    </svg>
);

const SettingsIcon = () => (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="1.8">
        <circle cx="12" cy="12" r="3" />
        <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06A1.65 1.65 0 0015 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0l-.06-.06A1.65 1.65 0 008.6 19.4a1.65 1.65 0 00-1.82-.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.6 15a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83l.06-.06A1.65 1.65 0 004.6 8.6a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 112.83-2.83l.06.06A1.65 1.65 0 008.6 4.6a1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0l.06.06A1.65 1.65 0 0015 4.6a1.65 1.65 0 001.82-.33l.06-.06a2 2 0 112.83 2.83l-.06.06A1.65 1.65 0 0019.4 8.6a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83l-.06.06A1.65 1.65 0 0019.4 15z" />
    </svg>
);

const baseMenuItems  = [
    {id: 'dashboard', label: 'Dashboard', path: '/dashboard', icon: <DashboardIcon/>},
    {id: 'printers', label: 'Printers', path: '/printers', icon: <PrinterIcon/>},
    {id: 'maintenance', label: 'Maintenance', path: '/maintenance', icon: <MaintenanceIcon/>},
    {id: 'alerts', label: 'Alerts', path: '/alerts', icon: <AlertIcon/>},
    {id: 'audit', label: 'Audit Logs', path: '/audit', icon: <AuditIcon/>},
    {id: 'settings', label: 'Settings', path: '/settings', icon: <SettingsIcon/>},
];

/**
 * Layout used for dashboard sections with persistent sidebar navigation.
 * @returns {JSX.Element} Dashboard shell that renders nested routes within the main content area.
 */
export default function DashboardLayout() {
    const location = useLocation();
    const navigate = useNavigate();
    const [user, setUser] = useState({name: 'User', role: 'User', avatarUrl: ''});
    const [isDarkMode, setIsDarkMode] = useState(false);

    useEffect(() => {
        let isMounted = true;
        api.get('/me')
            .then((res) => {
                if (!isMounted) return;
                setUser({
                    name: res?.data?.username || res?.data?.name || 'User',
                    role: res?.data?.role || 'User',
                    avatarUrl: res?.data?.avatarUrl || '',
                });
            })
            .catch(() => setUser((prev) => ({...prev})));

        return () => {
            isMounted = false;
        };
    }, []);

    const menuItems = useMemo(() => {
        const items = [...baseMenuItems];

        if (['ADMIN', 'SUPER_ADMIN'].includes(user?.role)) {
            items.splice(3, 0, {
                id: 'commands',
                label: 'Commands',
                path: '/commands',
                icon: <CommandsIcon/>,
            });
        }

        if (user?.role === 'SUPER_ADMIN') {
            items.splice(4, 0, {
                id: 'user-management',
                label: 'User Management',
                path: '/admin/users',
                icon: <UsersIcon/>,
            });
        }
        return items;
    }, [user?.role]);

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/');
    };

    return (
        <div className="flex min-h-screen bg-gray-100">
            <Sidebar
                menuItems={menuItems}
                appTitle="3D Printer Digital Twin"
                user={user}
                onLogout={handleLogout}
                isDarkMode={isDarkMode}
                onToggleDarkMode={() => setIsDarkMode((prev) => !prev)}
                activePath={location.pathname}
            />
            <main className="p-6 flex-1">
                <Outlet/>
            </main>
        </div>
    );
}