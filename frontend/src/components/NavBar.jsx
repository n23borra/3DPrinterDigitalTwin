import { useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import React, { useEffect, useState } from "react";

/**
 * Displays the application navigation bar with the current username and logout control.
 * @returns {JSX.Element} Sticky navbar with greeting and logout button.
 */
export default function Navbar() {
    const navigate = useNavigate();
    const [username, setUsername] = useState('User');
    const token = localStorage.getItem('token');

    useEffect(() => {
        if (token) {
            try {
                const decoded = jwtDecode(token);
                setUsername(decoded.sub || 'User');
            } catch {
                localStorage.removeItem('token');
                navigate('/login');
            }
        }
    }, [token, navigate]);

    /**
     * Clears the stored token and returns the user to the landing page.
     * @returns {void}
     */
    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/');
    };

    return (
        <nav className="sticky top-0 w-full z-50 bg-white text-[var(--tp-carrot-orange-500)] px-6 py-4 flex items-center">
            <div className="flex items-center space-x-4 ml-auto">
                <span>Hello, {username}</span>
                <button
                    onClick={handleLogout}
                    className="bg-red-600 hover:bg-red-700 text-white font-bold py-1 px-4 rounded"
                >
                    Logout
                </button>
            </div>
        </nav>
    );
}
