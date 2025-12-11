import React from 'react';
import { Outlet } from 'react-router-dom';

/**
 * Wraps authenticated routes with a centered content container.
 * @returns {JSX.Element} Layout fragment rendering the routed content.
 */
const MainLayout = () => (
    <>
        <main className="container mx-auto px-4 py-6">
            <Outlet />
        </main>
    </>
);

export default MainLayout;
