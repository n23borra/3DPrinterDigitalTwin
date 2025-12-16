import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import api from '../api/api';

/**
 * Authentication page collecting credentials and storing the returned JWT.
 * @returns {JSX.Element} Login form with validation feedback.
 */
export default function Login() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        identifier: '',
        password: '',
    });

    const [error, setError] = useState('');

    /**
     * Updates the credential fields as the user types and clears existing errors.
     * @param {React.ChangeEvent<HTMLInputElement>} e - Input change event from a credential field.
     * @returns {void}
     */
    const handleChange = (e) => {
        setFormData({...formData, [e.target.name]: e.target.value});
        setError('');
    };

    /**
     * Sends the login request and persists the JWT before redirecting to the dashboard.
     * @param {React.FormEvent<HTMLFormElement>} e - Form submission event.
     * @returns {Promise<void>} Resolves after navigation when authentication succeeds.
     */
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (formData.identifier.length < 3 || formData.password.length < 6) {
            setError('Identifier must be at least 3 characters and password at least 6 characters.');
            return;
        }

        try {
            const response = await api.post('/auth/login', formData);
            if (response.status === 200) {
                const {token} = response.data;
                localStorage.setItem('token', token);
                navigate('/dashboard');
            } else {
                setError('Invalid credentials.');
            }
        } catch (err) {
            setError('Server error. Try again later.');
        }
    };

    return (<div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="bg-white p-10 rounded-lg shadow-md w-full max-w-md">
            <h2 className="text-2xl font-bold text-center text-gray-800 mb-6">Login</h2>

            {error && <p className="mb-4 text-red-600">{error}</p>}

            <form onSubmit={handleSubmit}>
                <div className="mb-4">
                    <label className="block text-gray-700">Username or Email</label>
                    <input
                        type="text"
                        name="identifier"
                        value={formData.identifier}
                        onChange={handleChange}
                        className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="Minimum 3 characters"
                        required
                    />
                </div>

                <div className="mb-6">
                    <label className="block text-gray-700">Password</label>
                    <input
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="Minimum 6 characters"
                        required
                    />
                </div>

                <button
                    type="submit"
                    className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
                >
                    Login
                </button>
                <p className="mt-4 text-center">
                    <a href="/forgot-password" className="text-blue-600 hover:underline">Forgot password?</a>
                </p>
            </form>
        </div>
    </div>);
}
