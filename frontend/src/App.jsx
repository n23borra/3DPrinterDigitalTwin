import React from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes} from 'react-router-dom';

import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Dashboard from './pages/Dashboard';
import PrintersDashboard from './pages/PrinterDashboard.jsx';
import Settings from './pages/Settings';

import ProtectedRoute from './components/ProtectedRoute';
import SuperAdminRoute from './components/SuperAdminRoute';
import PrivilegedRoute from './components/PrivilegedRoute';
import DashboardLayout from './layouts/DashboardLayout';

import AuditLogs from './pages/AuditLogs';
import Alerts from './pages/Alerts';


function App() {
    return (<Router>
        <Routes>
            {/* --- Public routes --- */}
            <Route path="/" element={<Home/>}/>
            <Route path="/login" element={<Login/>}/>
            <Route path="/register" element={<Register/>}/>
            <Route path="/forgot-password" element={<ForgotPassword/>}/>
            <Route path="/reset-password" element={<ResetPassword/>}/>

            {/* --- Protected routes --- */}
            <Route element={<ProtectedRoute/>}>
                <Route element={<DashboardLayout/>}>
                    <Route path="/dashboard" element={<Dashboard/>}/>
                    <Route path="/printers" element={<PrintersDashboard/>}/>
                    <Route path="/alerts" element={<Alerts/>}/>
                    <Route path="/audit" element={<AuditLogs/>}/>
                    <Route element={<SuperAdminRoute/>}>
                    </Route>
                    <Route path="/settings" element={<Settings/>}/>
                </Route>
            </Route>

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace/>}/>
        </Routes>
    </Router>);
}

export default App;
