import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import api from '../api/api';

/**
 * Password recovery page that requests a reset code via email.
 * @returns {JSX.Element} Form allowing users to submit their email for password recovery.
 */
export default function ForgotPassword() {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');

    /**
     * Submits the entered email to request a password reset code from the API.
     * @param {React.FormEvent<HTMLFormElement>} e - Form submission event.
     * @returns {Promise<void>} Resolves once the request completes.
     */
    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setMessage('');
        if (!email) {
            setError('Email is required');
            return;
        }
        try {
            const res = await api.post('/auth/forgot-password', {email});
            if (res.status === 200) {
                setMessage('Check your email for the reset code.');
                setTimeout(() => navigate('/reset-password'), 1000);
            } else {
                const msg = typeof res.data === 'string' ? res.data : '';
                setError(msg || 'Request failed');
            }
        } catch (err) {
            setError('Server error');
        }
    };

    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center">
            <div className="bg-white p-10 rounded-lg shadow-md w-full max-w-md">
                <h2 className="text-2xl font-bold text-center text-gray-800 mb-6">Forgot Password</h2>
                {message && <p className="mb-4 text-green-600">{message}</p>}
                {error && <p className="mb-4 text-red-600">{error}</p>}
                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label className="block text-gray-700">Email</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    <button type="submit" className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700">
                        Send Code
                    </button>
                </form>
            </div>
        </div>
    );
}