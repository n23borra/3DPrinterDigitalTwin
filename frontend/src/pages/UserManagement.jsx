import React, {useEffect, useState} from 'react';
import Button from '../components/Button';
import Loader from '../components/Loader';
import Modal from '../components/Modal';
import {deleteUser, fetchUsers, updateUserRole} from '../api/adminUserApi.js';

const ROLE_OPTIONS = ['ALL', 'USER', 'ADMIN', 'SUPER_ADMIN'];

/**
 * Super admin page to manage user accounts.
 * @returns {JSX.Element}
 */
export default function UserManagement() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');
    const [roleFilter, setRoleFilter] = useState('ALL');
    const [activeUser, setActiveUser] = useState(null);
    const [actionLoadingId, setActionLoadingId] = useState(null);

    const getErrorMessage = (err, fallback) => {
        const status = err?.response?.status;
        if (status === 401) {
            return 'Your session has expired. Please sign in again.';
        }
        if (status === 403) {
            return 'You do not have access to manage users.';
        }
        return err?.response?.data?.message || err?.response?.data?.error || err?.message || fallback;
    };

    const loadUsers = async ({searchValue = search, roleValue = roleFilter} = {}) => {
        setLoading(true);
        setError('');
        try {
            const {data} = await fetchUsers({
                search: searchValue.trim() ? searchValue.trim() : undefined,
                role: roleValue !== 'ALL' ? roleValue : undefined,
            });
            setUsers(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(getErrorMessage(err, 'Unable to load users.'));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadUsers().catch(() => setError('Unable to load users.'));
    }, [roleFilter]);

    const handleSearch = (event) => {
        event.preventDefault();
        loadUsers({searchValue: search, roleValue: roleFilter}).catch(() => setError('Unable to load users.'));
    };

    const handleRoleToggle = async (user) => {
        if (!user) return;
        const nextRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
        setActionLoadingId(user.id);
        setError('');
        try {
            await updateUserRole(user.id, nextRole);
            await loadUsers({searchValue: search, roleValue: roleFilter});
        } catch (err) {
            setError(getErrorMessage(err, 'Unable to update role.'));
        } finally {
            setActionLoadingId(null);
        }
    };

    const handleDelete = async () => {
        if (!activeUser) return;
        setActionLoadingId(activeUser.id);
        setError('');
        try {
            await deleteUser(activeUser.id);
            setActiveUser(null);
            await loadUsers({searchValue: search, roleValue: roleFilter});
        } catch (err) {
            setError(getErrorMessage(err, 'Unable to delete user.'));
        } finally {
            setActionLoadingId(null);
        }
    };

    return (
        <div className="space-y-6">
            <div>
                <h2 className="text-2xl font-semibold">User Management</h2>
                <p className="text-sm text-gray-600">Manage users, roles, and access permissions.</p>
            </div>

            <form onSubmit={handleSearch} className="flex flex-col gap-4 bg-white p-4 rounded shadow md:flex-row md:items-end">
                <div className="flex-1">
                    <label className="block text-sm font-medium text-gray-700 mb-1" htmlFor="user-search">
                        Search
                    </label>
                    <input
                        id="user-search"
                        type="text"
                        className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="Search by username or email"
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                    />
                </div>
                <div className="w-full md:w-56">
                    <label className="block text-sm font-medium text-gray-700 mb-1" htmlFor="role-filter">
                        Role
                    </label>
                    <select
                        id="role-filter"
                        className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        value={roleFilter}
                        onChange={(event) => setRoleFilter(event.target.value)}
                    >
                        {ROLE_OPTIONS.map((role) => (
                            <option key={role} value={role}>
                                {role === 'ALL' ? 'All roles' : role}
                            </option>
                        ))}
                    </select>
                </div>
                <Button type="submit" className="w-full md:w-auto">
                    Search
                </Button>
            </form>

            {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-2 rounded">
                    {error}
                </div>
            )}

            {loading ? (
                <Loader />
            ) : (
                <div className="bg-white rounded shadow overflow-hidden">
                    <table className="min-w-full divide-y">
                        <thead className="bg-gray-50 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                        <tr>
                            <th className="px-4 py-3">Username</th>
                            <th className="px-4 py-3">Email</th>
                            <th className="px-4 py-3">Role</th>
                            <th className="px-4 py-3">Actions</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y text-sm">
                        {users.length === 0 ? (
                            <tr>
                                <td className="px-4 py-6 text-center text-gray-500" colSpan={4}>
                                    No users found.
                                </td>
                            </tr>
                        ) : (
                            users.map((user) => (
                                <tr key={user.id} className="hover:bg-gray-50">
                                    <td className="px-4 py-3 font-medium text-gray-900">{user.username}</td>
                                    <td className="px-4 py-3 text-gray-600">{user.email}</td>
                                    <td className="px-4 py-3">
                                        <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-blue-50 text-blue-700">
                                            {user.role}
                                        </span>
                                    </td>
                                    <td className="px-4 py-3">
                                        <div className="flex flex-wrap gap-2">
                                            {user.role === 'SUPER_ADMIN' ? (
                                                <span className="text-xs text-gray-500">Protected</span>
                                            ) : (
                                                <Button
                                                    type="button"
                                                    className="text-xs px-3 py-1"
                                                    disabled={actionLoadingId === user.id}
                                                    onClick={() => handleRoleToggle(user)}
                                                >
                                                    {user.role === 'ADMIN' ? 'Make User' : 'Make Admin'}
                                                </Button>
                                            )}
                                            <Button
                                                type="button"
                                                className="text-xs px-3 py-1 bg-red-600 hover:bg-red-700"
                                                disabled={actionLoadingId === user.id}
                                                onClick={() => setActiveUser(user)}
                                            >
                                                Delete
                                            </Button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
            )}

            <Modal open={Boolean(activeUser)} onClose={() => setActiveUser(null)} className="w-full max-w-md">
                <div className="space-y-4">
                    <h3 className="text-lg font-semibold">Delete user</h3>
                    <p className="text-sm text-gray-600">
                        Are you sure you want to delete{' '}
                        <span className="font-semibold text-gray-800">{activeUser?.username}</span>? This action
                        cannot be undone.
                    </p>
                    <div className="flex justify-end gap-3">
                        <Button type="button" className="bg-gray-500 hover:bg-gray-600" onClick={() => setActiveUser(null)}>
                            Cancel
                        </Button>
                        <Button
                            type="button"
                            className="bg-red-600 hover:bg-red-700"
                            onClick={handleDelete}
                            disabled={actionLoadingId === activeUser?.id}
                        >
                            Delete
                        </Button>
                    </div>
                </div>
            </Modal>
        </div>
    );
}