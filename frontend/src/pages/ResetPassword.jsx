import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import api from '../api/api';

/**
 * Final step of the password reset flow where users submit the received code and new password.
 * @returns {JSX.Element} Form capturing the reset token and updated credentials.
 */
export default function ResetPassword() {
    const navigate = useNavigate();
    const [form, setForm] = useState({email: '', code: '', newPassword: '', confirmPassword: ''});
    const [error, setError] = useState('');
    const [message, setMessage] = useState('');

    /**
     * Keeps the reset form state in sync with user input and clears error messages.
     * @param {React.ChangeEvent<HTMLInputElement>} e - Input change event from the form fields.
     * @returns {void}
     */
    const handleChange = (e) => {
        setForm({...form, [e.target.name]: e.target.value});
        setError('');
    };

    /**
     * Validates the reset payload and submits it to the API to change the password.
     * @param {React.FormEvent<HTMLFormElement>} e - Form submission event.
     * @returns {Promise<void>} Resolves when the server confirms or rejects the reset request.
     */
    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setMessage('');
        if (!form.email || !form.code || form.newPassword.length < 6 || form.confirmPassword.length < 6) {
            setError('All fields are required and passwords must be at least 6 characters');
            return;
        }
        if (form.newPassword !== form.confirmPassword) {
            setError('Passwords do not match');
            return;
        }
        try {
            const res = await api.post('/auth/reset-password', {
                email: form.email,
                code: form.code,
                newPassword: form.newPassword,
            });
            if (res.status === 200) {
                setMessage('Password updated. Redirecting to login...');
                setTimeout(() => navigate('/login'), 1500);
            } else {
                const msg = typeof res.data === 'string' ? res.data : '';
                setError(msg || 'Reset failed');
            }
        } catch (err) {
            const msg = typeof err.response?.data === 'string' ? err.response.data : '';
            setError(msg || 'Server error');
        }
    };

    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center">
            <div className="bg-white p-10 rounded-lg shadow-md w-full max-w-md">
                <h2 className="text-2xl font-bold text-center text-gray-800 mb-6">Reset Password</h2>
                {message && <p className="mb-4 text-green-600">{message}</p>}
                {error && <p className="mb-4 text-red-600">{error}</p>}
                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label className="block text-gray-700">Email</label>
                        <input
                            type="email"
                            name="email"
                            value={form.email}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-700">Reset Code</label>
                        <input
                            type="text"
                            name="code"
                            value={form.code}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    <div className="mb-4">
                        <label className="block text-gray-700">New Password</label>
                        <input
                            type="password"
                            name="newPassword"
                            value={form.newPassword}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                        {form.newPassword && (
                            form.newPassword.length >= 6 && form.newPassword.length <= 100 ? (
                                <p className="text-green-600 text-sm mt-1">Password length OK.</p>
                            ) : (
                                <p className="text-red-600 text-sm mt-1">6–100 characters required.</p>
                            )
                        )}
                    </div>
                    <div className="mb-6">
                        <label className="block text-gray-700">Confirm New Password</label>
                        <input
                            type="password"
                            name="confirmPassword"
                            value={form.confirmPassword}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                        {form.confirmPassword && (
                            form.newPassword === form.confirmPassword ? (
                                form.newPassword.length >= 6 && form.newPassword.length <= 100 && (
                                    <p className="text-green-600 text-sm mt-1">Passwords match.</p>
                                )
                            ) : (
                                <p className="text-red-600 text-sm mt-1">Passwords do not match.</p>
                            )
                        )}
                    </div>
                    <button type="submit" className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700">
                        Reset Password
                    </button>
                </form>
            </div>
        </div>
    );
}