import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {jwtDecode} from 'jwt-decode';
import api from '../api/api';
import TagInput from '../components/TagInput';

/**
 * Account settings area for password updates and category admin assignments.
 * @returns {JSX.Element} Page combining user password management and super-admin tools.
 */
export default function Settings() {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [message, setMessage] = useState('');
    const [categories, setCategories] = useState([]);
    const [emails, setEmails] = useState([]);
    const [role, setRole] = useState('');
    const [adminMsg, setAdminMsg] = useState('');

    /**
     * Updates the cached email addresses for a specific category administrator slot.
     * @param {number} index - Position of the category being updated.
     * @param {string[]} list - New email values selected for the category.
     * @returns {void}
     */
    const handleEmailsChange = (index, list) => {
        const updated = [...emails];
        updated[index] = list;
        setEmails(updated);
    };

    useEffect(() => {
        if (!token) return;
        fetch('http://localhost:8080/api/me', {
            headers: {Authorization: `Bearer ${token}`},
        })
            .then((res) => res.ok ? res.json() : null)
            .then((data) => {
                console.log('Fetched /me:', data);
                if (data) {
                    setRole(data.role);
                    console.log('Role set to', data.role);
                }
            })
            .catch((err) => {
                console.log('Error fetching /me', err);
            });
    }, [token]);


    let username = 'User';
    if (token) {
        try {
            const decoded = jwtDecode(token);
            username = decoded.sub || 'User';
        } catch {
            username = 'User';
        }
    }

    /**
     * Sends the password update request after verifying the confirmation fields.
     * @param {React.FormEvent<HTMLFormElement>} e - Password form submission event.
     * @returns {Promise<void>} Resolves after the API responds.
     */
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (newPassword !== confirmPassword) {
            setMessage("New passwords do not match.");
            return;
        }

        try {
            const res = await api.post('/account/update-password', {
                currentPassword,
                newPassword,
            }, {
                headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
            });

            const text = typeof res.data === 'string' ? res.data : '';
            if (res.status === 200) {
                setMessage('Password updated successfully.');
                setCurrentPassword('');
                setNewPassword('');
                setConfirmPassword('');
            } else {
                setMessage(text);
            }
        } catch (err) {
            setMessage('Error updating password.');
        }
    };


    return (
        <div>
            <header className="mb-8">
                <h2 className="text-3xl font-semibold text-gray-800">Settings</h2>
                <p className="text-gray-600">Manage your account, {username}</p>
            </header>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <section className="bg-white rounded-lg shadow-md p-6">
                    <h3 className="text-xl font-semibold mb-4 text-gray-800">Update Password</h3>
                    {message && <div className="mb-4 text-red-500">{message}</div>}
                    <form onSubmit={handleSubmit}>
                        <div className="mb-4">
                            <label className="block text-gray-700 mb-1">Current Password</label>
                            <input
                                type="password"
                                value={currentPassword}
                                onChange={(e) => setCurrentPassword(e.target.value)}
                                className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="Enter current password"
                            />
                        </div>
                        <div className="mb-4">
                            <label className="block text-gray-700 mb-1">New Password</label>
                            <input
                                type="password"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="Enter new password"
                            />
                        </div>
                        <div className="mb-6">
                            <label className="block text-gray-700 mb-1">Confirm New Password</label>
                            <input
                                type="password"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                className="w-full px-4 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                placeholder="Confirm new password"
                            />
                        </div>
                        <button
                            type="submit"
                            className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700"
                        >
                            Update Password
                        </button>
                    </form>
                </section>
            </div>
        </div>
    );
}
