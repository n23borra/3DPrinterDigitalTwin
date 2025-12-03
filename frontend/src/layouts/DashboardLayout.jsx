import React from 'react';
import {Outlet} from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';

/**
 * Layout used for dashboard sections with persistent sidebar and navbar navigation.
 * @returns {JSX.Element} Dashboard shell that renders nested routes within the main content area.
 */
export default function DashboardLayout() {
    return (
        <div className="flex min-h-screen bg-gray-100">
            <Sidebar/>
            <div className="flex-1 flex flex-col">
                <Navbar/>
                <main className="p-6 flex-1">
                    <Outlet/>
                </main>
            </div>
        </div>
    );
}