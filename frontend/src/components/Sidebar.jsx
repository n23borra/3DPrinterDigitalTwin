import React, {useEffect, useState} from 'react';
import {Link, useLocation} from 'react-router-dom';
import api from '../api/api';
import clsx from 'clsx';

const links = [
    {to: '/dashboard', label: 'Dashboard'},
    {to: '/analyses', label: 'Assessment'},
    {to: '/assets', label: 'Assets'},
    {to: '/risk-select', label: 'Risk Selection'},
    {to: '/risks', label: 'Risk Evaluation'},
    {to: '/actions', label: 'Actions'},
    {to: '/audit', label: 'Audit Logs'},
    {to: '/export', label: 'Export'},
    {to: '/references', label: 'References'},
    {to: '/settings', label: 'Settings'},
];

/**
 * Renders the dashboard sidebar navigation and adapts links to the user's role.
 * @returns {JSX.Element} Vertical navigation listing the available application sections.
 */
export default function Sidebar() {
    const {pathname} = useLocation();
    const [role, setRole] = useState('USER');

    useEffect(() => {
        api.get('/me').then(res => setRole(res.data.role)).catch(() => setRole(''));
    }, []);

    const visibleLinks = links.filter(l => {
        if (l.to === '/references') return role === 'SUPER_ADMIN';
        if (l.to === '/risk-select') return role !== 'USER';
        return true;
    });

    return (
        <aside className="w-48 bg-[#5d5d63] text-[var(--tp-carrot-orange-500)] flex flex-col px-6 py-4">
            <h1 className="text-2xl font-bold mb-4">HRM</h1>
            <nav className="space-y-2 flex-1">
                {visibleLinks.map((link) => (
                    <Link
                        key={link.to}
                        to={link.to}
                        className={clsx(
                            'block rounded px-2 py-1 hover:bg-[#5d5d63]',
                            pathname.startsWith(link.to) && 'bg-[#5d5d63]'
                        )}
                    >
                        {link.label}
                    </Link>
                ))}
            </nav>
        </aside>
    );
}