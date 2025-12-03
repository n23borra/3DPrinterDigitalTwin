import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import api from '../api/api';

/**
 * Account creation page that validates basic fields before calling the registration API.
 * @returns {JSX.Element} Registration form with inline validation messaging.
 */
export default function Register() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
    });

    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    /**
     * Synchronises form state with user input while clearing existing alerts.
     * * @param {React.ChangeEvent<HTMLInputElement>} e - Change event from a form control.
     * @returns {void}
     */
    const handleChange = (e) => {
        setFormData({...formData, [e.target.name]: e.target.value});
        setError('');
        setSuccess('');
    };

    /**
     * Performs client-side validation then submits the registration request to the backend.
     * @param {React.FormEvent<HTMLFormElement>} e - Form submission event.
     * @returns {Promise<void>} Resolves once the API responds and navigation occurs on success.
     */
    const handleSubmit = async (e) => {
        e.preventDefault();

        // Basic client-side validation
        if (formData.username.length < 3 || formData.username.length > 50) {
            setError('Username must be between 3 and 50 characters.');
            return;
        }
        if (!formData.email) {
            setError('Email is required.');
            return;
        }
        if (formData.password.length < 6 || formData.password.length > 100) {
            setError('Password must be between 6 and 100 characters.');
            return;
        }
        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match.');
            return;
        }

        try {
            const response = await api.post('/auth/register', {
                username: formData.username,
                email: formData.email,
                password: formData.password,
            });

            if (response.status === 200 || response.status === 201) {
                setSuccess('Account created! Redirecting to login...');
                setTimeout(() => navigate('/login'), 1500);
            } else {
                const msg = response.data || 'Registration failed.';
                setError(typeof msg === 'string' ? msg : 'Registration failed.');
            }
        } catch (err) {
            setError('Server error. Try again later.');
        }
    };

    return (
        <div className="min-h-screen bg-gray-100 flex items-center justify-center">
            <div className="bg-white p-10 rounded-lg shadow-md w-full max-w-md">
                <h2 className="text-2xl font-bold text-center text-gray-800 mb-6">Register</h2>

                {error && <p className="mb-4 text-red-600">{error}</p>}
                {success && <p className="mb-4 text-green-600">{success}</p>}

                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label className="block text-gray-700">Username</label>
                        <input
                            type="text"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="3–50 characters"
                            required
                        />
                    </div>

                    <div className="mb-4">
                        <label className="block text-gray-700">Email</label>
                        <input
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>

                    <div className="mb-4">
                        <label className="block text-gray-700">Password</label>
                        <input
                            type="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="6–100 characters"
                            required
                        />
                        {formData.password && (
                            formData.password.length >= 6 && formData.password.length <= 100 ? (
                                <p className="text-green-600 text-sm mt-1">Password length OK.</p>
                            ) : (
                                <p className="text-red-600 text-sm mt-1">6–100 characters required.</p>
                            )
                        )}
                    </div>

                    <div className="mb-4">
                        <label className="block text-gray-700">Confirm Password</label>
                        <input
                            type="password"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                        {formData.confirmPassword && (
                            formData.password === formData.confirmPassword ? (
                                formData.password.length >= 6 && formData.password.length <= 100 && (
                                    <p className="text-green-600 text-sm mt-1">Passwords match.</p>
                                )
                            ) : (
                                <p className="text-red-600 text-sm mt-1">Passwords do not match.</p>
                            )
                        )}
                    </div>

                    <button
                        type="submit"
                        className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
                    >
                        Register
                    </button>
                </form>
            </div>
        </div>
    );
}
