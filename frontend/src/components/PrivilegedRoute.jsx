import React, {useEffect, useState} from 'react';
import {Navigate, Outlet} from 'react-router-dom';
import {jwtDecode} from 'jwt-decode';
import api from '../api/api';
import Loader from './Loader';

/**
 * Restricts access to nested routes to non-USER roles (ADMIN or SUPER_ADMIN).
 * @param {object} props - Component props.
 * @param {React.ReactNode} [props.children] - Content rendered when the user is authorized.
 * @returns {JSX.Element} Children, an outlet, or a redirect/loading state based on access.
 */
export default function PrivilegedRoute({children}) {
    const token = localStorage.getItem('token');
    const [role, setRole] = useState(null);
    const [loading, setLoading] = useState(true);

    if (!token) return <Navigate to="/login" replace/>;

    try {
        jwtDecode(token);
    } catch {
        localStorage.removeItem('token');
        return <Navigate to="/login" replace/>;
    }

    useEffect(() => {
        api.get('/me')
            .then(res => setRole(res.data.role))
            .catch(() => setRole(null))
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <Loader/>;

    const allowedRoles = ['ADMIN', 'SUPER_ADMIN'];
    if (!allowedRoles.includes(role)) return <Navigate to="/dashboard" replace/>;

    return children || <Outlet/>;
}