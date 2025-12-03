import React from 'react';
import {BrowserRouter as Router, Navigate, Route, Routes} from 'react-router-dom';

import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Dashboard from './pages/Dashboard';
import Settings from './pages/Settings';

import ProtectedRoute from './components/ProtectedRoute';
import SuperAdminRoute from './components/SuperAdminRoute';
import PrivilegedRoute from './components/PrivilegedRoute';
import DashboardLayout from './layouts/DashboardLayout';

import AuditLogs from './pages/AuditLogs';
import Export from './pages/Export';

import AnalysisList from './pages/analyses/AnalysisList';
import AssetList from './pages/assets/AssetList';
import AssetForm from './pages/assets/AssetForm';
import CategoryList from './pages/assets/CategoryList';
import RiskEval from './pages/risks/RiskEval';
import RiskSelection from './pages/risks/RiskSelection';
import TreatmentBoard from './pages/actions/TreatmentBoard';
import References from './pages/references/References';


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
                    <Route path="/analyses" element={<AnalysisList/>}/>
                    <Route path="/assets" element={<AssetList/>}/>
                    <Route path="/assets/new" element={<AssetForm/>}/>
                    <Route path="/categories" element={<CategoryList/>}/>
                    <Route element={<PrivilegedRoute/>}>
                        <Route path="/risk-select" element={<RiskSelection/>}/>
                    </Route>
                    <Route path="/risks" element={<RiskEval/>}/>
                    <Route path="/actions" element={<TreatmentBoard/>}/>
                    <Route path="/audit" element={<AuditLogs/>}/>
                    <Route path="/export" element={<Export/>}/>
                    <Route element={<SuperAdminRoute/>}>
                        <Route path="/references" element={<References/>}/>
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
