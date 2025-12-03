import React from 'react';
import {useNavigate} from 'react-router-dom';

/**
 * Button component that clears the stored JWT and redirects the user to the login screen.
 * @returns {JSX.Element} Action button triggering logout when clicked.
 */
export default function LogoutButton() {
    const navigate = useNavigate();

    /**
     * Removes the authentication token and redirects to the login page.
     * @returns {void}
     */
    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/login');
    };

    return (
        <button
            onClick={handleLogout}
            className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
        >
            Logout
        </button>
    );
}
