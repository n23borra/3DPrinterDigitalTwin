import React from 'react';
import {Navigate, Outlet} from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';

/**
 * Guards private routes by ensuring a valid JWT is present before rendering children.
 * @param {object} props - Component props.
 * @param {React.ReactNode} [props.children] - Optional nested routes rendered when authentication succeeds.
 * @returns {JSX.Element} Nested content or a redirect to the login page.
 */
const ProtectedRoute = ({children}) => {
    const token = localStorage.getItem('token');

    if (!token) return <Navigate to="/login" replace/>;

    try {
        jwtDecode(token);            // throws if the token is invalid or expired
    } catch {
        localStorage.removeItem('token');
        return <Navigate to="/login" replace/>;
    }

    // Standalone mode (with <Outlet />) or explicit wrapper mode
    return children || <Outlet/>;
};

export default ProtectedRoute;
