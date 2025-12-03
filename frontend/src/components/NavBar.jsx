import React from 'react'

const links = [
    { id: 'dashboard', label: 'Dashboard' },
    { id: 'history', label: 'Historique' },
    { id: 'maintenance', label: 'Maintenance' },
    { id: 'admin', label: 'Admin' },
    { id: 'login', label: 'Login' }
]

export default function Navbar({ current, onNavigate, user }) {
    return (
        <nav style={{ display: 'flex', gap: '1rem', padding: '1rem', background: '#222', color: 'white' }}>
            <strong>3D Printer Twin</strong>
            {links.map(link => (
                <button
                    key={link.id}
                    onClick={() => onNavigate(link.id)}
                    style={{ background: link.id === current ? '#555' : '#333', color: 'white', border: 'none', padding: '0.5rem 1rem' }}
                >
                    {link.label}
                </button>
            ))}
            <span style={{ marginLeft: 'auto' }}>{user ? user.email : 'Guest'}</span>
        </nav>
    )
}