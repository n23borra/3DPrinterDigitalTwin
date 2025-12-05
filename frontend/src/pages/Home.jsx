import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {jwtDecode} from 'jwt-decode';

/**
 * Public landing page that greets the visitor and provides navigation to auth flows.
 * @returns {JSX.Element} Hero section with login/register or dashboard shortcuts.
 */
export default function Home() {
    const navigate = useNavigate();
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [username, setUsername] = useState('Guest');

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const decoded = jwtDecode(token);
                setUsername(decoded.sub || 'User');
                setIsAuthenticated(true);
            } catch (err) {
                localStorage.removeItem('token');
                setIsAuthenticated(false);
            }
        }
    }, []);

    return (<div
        className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-black text-white flex flex-col items-center justify-center px-6">
        <h1 className="text-5xl font-bold mb-4 text-blue-400">Welcome to 3D Printer Digital Twin</h1>
        <p className="text-lg text-gray-300 mb-8">Manage your 3D Printer</p>

        {isAuthenticated ? (<>
            <p className="mb-4 text-green-400">Logged in as <strong>{username}</strong></p>
            <button
                onClick={() => navigate('/dashboard')}
                className="bg-blue-600 hover:bg-blue-700 px-6 py-3 rounded text-white font-semibold shadow-lg"
            >
                Go to Dashboard
            </button>
        </>) : (<div className="space-x-4">
            <button
                onClick={() => navigate('/login')}
                className="bg-blue-600 hover:bg-blue-700 px-6 py-3 rounded text-white font-semibold shadow-lg"
            >
                Login
            </button>
            <button
                onClick={() => navigate('/register')}
                className="bg-gray-600 hover:bg-gray-700 px-6 py-3 rounded text-white font-semibold shadow-lg"
            >
                Register
            </button>
        </div>)}
    </div>);
}
