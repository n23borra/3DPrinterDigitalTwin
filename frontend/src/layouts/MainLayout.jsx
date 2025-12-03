import React from 'react';
import { Outlet } from 'react-router-dom';
import Navbar from '../components/Navbar';

/**
 * Wraps authenticated routes with a top navigation bar and centered content container.
 * @returns {JSX.Element} Layout fragment containing the navbar and routed content.
 */
const MainLayout = () => (
    <>
        <Navbar />
        <main className="container mx-auto px-4 py-6">
            <Outlet />
        </main>
    </>
);

export default MainLayout;
