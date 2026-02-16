import React, { useEffect, useState } from 'react';
import Button from '../components/Button';
import api from '../api/api';
import Loader from '../components/Loader';
import Modal from '../components/Modal';

const SEVERITY_OPTIONS = ['ALL', 'INFO', 'WARNING', 'CRITICAL'];
const PRIORITY_OPTIONS = ['ALL', 'LOW', 'MEDIUM', 'HIGH'];
const STATUS_OPTIONS = ['ALL', 'UNRESOLVED', 'RESOLVED'];

/**
 * Shows the list of alerts for the authenticated user with management capabilities.
 * @returns {JSX.Element} Page section displaying alerts or a loader.
 */
export default function Alerts() {
    const [alerts, setAlerts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [userId, setUserId] = useState(null);
    const [userRole, setUserRole] = useState('USER');
    const [showModal, setShowModal] = useState(false);
    const [search, setSearch] = useState('');
    const [severityFilter, setSeverityFilter] = useState('ALL');
    const [priorityFilter, setPriorityFilter] = useState('ALL');
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [formData, setFormData] = useState({
        title: '',
        details: '',
        severity: 'INFO',
        priority: 'MEDIUM',
        category: ''
    });

    const allowedRoles = ['ADMIN', 'SUPER_ADMIN'];

    useEffect(() => {
        const load = async () => {
            try {
                const { data: user } = await api.get('/me');
                setUserId(user.id);
                setUserRole(user.role || 'USER');
                const { data } = await api.get('/alerts');
                setAlerts(Array.isArray(data) ? data : []);
            } catch (e) {
                console.error(e);
                setAlerts([]);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, []);

    const isAdmin = () => {
        return allowedRoles.includes(userRole);
    };

    const getFilteredAlerts = () => {
        return alerts.filter((alert) => {
            // Search filter
            const searchLower = search.toLowerCase();
            const matchesSearch = !search || 
                alert.title.toLowerCase().includes(searchLower) ||
                (alert.details && alert.details.toLowerCase().includes(searchLower)) ||
                (alert.category && alert.category.toLowerCase().includes(searchLower));

            // Severity filter
            const matchesSeverity = severityFilter === 'ALL' || alert.severity === severityFilter;

            // Priority filter
            const matchesPriority = priorityFilter === 'ALL' || alert.priority === priorityFilter;

            // Status filter
            const matchesStatus = statusFilter === 'ALL' || 
                (statusFilter === 'RESOLVED' && alert.resolved) ||
                (statusFilter === 'UNRESOLVED' && !alert.resolved);

            return matchesSearch && matchesSeverity && matchesPriority && matchesStatus;
        });
    };

    const refreshAlerts = async () => {
        try {
            const { data } = await api.get('/alerts');
            setAlerts(Array.isArray(data) ? data : []);
        } catch (e) {
            console.error(e);
        }
    };

    const handleCreateAlert = async () => {
        if(!isAdmin()){
            formData.details = "USER_ALERT: "+formData.details;
        }
        console.log('handleCreateAlert called, formData:', formData);
        if (!formData.title.trim()) {
            alert('Alert title is required');
            return;
        }
        try {
            console.log('Sending POST to /alerts with:', {userId, ...formData});
            await api.post('/alerts', {
                userId,
                title: formData.title,
                details: formData.details || null,
                severity: formData.severity,
                priority: formData.priority,
                category: formData.category || null,
                assignedTo: null
            });
            console.log('Alert created successfully');
            setFormData({ title: '', details: '', severity: 'INFO', priority: 'MEDIUM', category: '' });
            setShowModal(false);
            await refreshAlerts();
        } catch (e) {
            console.error('Error creating alert:', e);
            alert('Failed to create alert: ' + (e.response?.data?.message || e.message));
        }
    };

    const toggleResolved = async (alert) => {
        try {
            await api.patch(`/alerts/${alert.id}/resolved`, { resolved: !alert.resolved });
            await refreshAlerts();
        } catch (e) {
            console.error(e);
            alert('Failed to update alert');
        }
    };

    const deleteAlert = async (id) => {
        if (window.confirm('Delete this alert?')) {
            try {
                await api.delete(`/alerts/${id}`);
                await refreshAlerts();
            } catch (e) {
                console.error(e);
                alert('Failed to delete alert');
            }
        }
    };

    const getSeverityColor = (severity) => {
        switch (severity) {
            case 'CRITICAL':
                return 'bg-red-100 text-red-800';
            case 'WARNING':
                return 'bg-yellow-100 text-yellow-800';
            default:
                return 'bg-blue-100 text-blue-800';
        }
    };

    const getPriorityColor = (priority) => {
        switch (priority) {
            case 'HIGH':
                return 'bg-red-600 text-white';
            case 'MEDIUM':
                return 'bg-orange-600 text-white';
            default:
                return 'bg-gray-600 text-white';
        }
    };

    if (loading) return <Loader />;

    function Tooltip({ text }) {
    return (
        <div className="relative group inline-block">
            <span className="cursor-help text-gray-400 font-bold">?</span>
            <div className="absolute z-10 hidden group-hover:block bg-gray-800 text-white text-xs rounded px-2 py-1 w-48 -top-2 left-5">
                {text}
            </div>
        </div>
    );
}
    return (
        <div>
            <div className="flex items-center justify-between mb-4">
                <h2 className="text-2xl font-semibold">Alerts</h2>
                <button
                    onClick={() => {
                        console.log('New Alert button clicked, showModal:', showModal);
                        setShowModal(true);
                    }}
                    className="px-3 py-2 bg-green-600 text-white rounded hover:bg-green-700"
                >
                    New Alert
                </button>
            </div>

            {showModal && (
                <Modal open={showModal} onClose={() => setShowModal(false)}>
                    <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
                        <h3 className="text-xl font-semibold mb-4">Create Alert</h3>
                        <input
                            type="text"
                            placeholder="Alert Title"
                            value={formData.title}
                            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                            className="w-full border rounded px-2 py-1 mb-3"
                        />
                        <textarea
                            placeholder="Details (optional)"
                            value={formData.details}
                            onChange={(e) => setFormData({ ...formData, details: e.target.value })}
                            className="w-full border rounded px-2 py-1 mb-3"
                            rows="3"
                        />
                        <input
                            type="text"
                            placeholder="Category (optional)"
                            value={formData.category}
                            onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                            className="w-full border rounded px-2 py-1 mb-3"
                        />
                        <div className="flex gap-3 mb-3">
                            <Tooltip
                                text={
                                    <>
                                    <strong>INFO</strong>: Informational only<br />
                                    <strong>WARNING</strong>: Needs attention<br />
                                    <strong>CRITICAL</strong>: Immediate action required
                                    </>
                                }
                            />
                            <select
                                value={formData.severity}
                                onChange={(e) => setFormData({ ...formData, severity: e.target.value })}
                                className="flex-1 border rounded px-2 py-1"
                                title={`INFO: Information only\nWARNING: Attention needed\nCRITICAL: Immediate action required`}
                            >
                                <option>INFO</option>
                                <option>WARNING</option>
                                <option>CRITICAL</option>
                            </select>
                            <Tooltip
                                text={
                                    <>
                                    <strong>LOW</strong>: Can be handled later<br />
                                    <strong>MEDIUM</strong>: Should be handled soon<br />
                                    <strong>HIGH</strong>: Needs immediate attention
                                    </>
                                }
                            />
                            <select
                                value={formData.priority}
                                onChange={(e) => setFormData({ ...formData, priority: e.target.value })}
                                className="flex-1 border rounded px-2 py-1"
                            >
                                <option>LOW</option>
                                <option>MEDIUM</option>
                                <option>HIGH</option>
                            </select>
                        </div>
                        <div className="flex gap-2">
                            <button
                                onClick={handleCreateAlert}
                                className="flex-1 px-3 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                            >
                                Create
                            </button>
                            <button
                                onClick={() => setShowModal(false)}
                                className="flex-1 px-3 py-2 bg-gray-400 text-white rounded hover:bg-gray-500"
                            >
                                Cancel
                            </button>
                        </div>
                    </div>
                </Modal>
            )}

            <form onSubmit={getFilteredAlerts} className="flex flex-col gap-4 bg-white p-4 rounded shadow md:flex-row md:items-end">
                <div className="flex-1">
                    <label className="block text-sm font-medium text-gray-700 mb-1" htmlFor="alert-search">
                        Search
                    </label>
                    <input
                        id="alert-search"
                        type="text"
                        className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        placeholder="Search by name"
                        value={search}
                        onChange={(event) => setSearch(event.target.value)}
                    />
                </div>
                <div className="w-full md:w-56">
                    <label className="block text-sm font-medium text-gray-700 mb-1" htmlFor="severity-filter">
                        Severity
                    </label>
                    <select
                        id="severity-filter"
                        className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        value={severityFilter}
                        onChange={(event) => setSeverityFilter(event.target.value)}
                    >
                        {SEVERITY_OPTIONS.map((severity) => (
                            <option key={severity} value={severity}>
                                {severity === 'ALL' ? 'All' : severity}
                            </option>
                        ))}
                    </select>
                </div>
                <div className="w-full md:w-56">
                    <label className="block text-sm font-medium text-gray-700 mb-1" htmlFor="priority-filter">
                        Priority
                    </label>
                    <select
                        id="priority-filter"
                        className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        value={priorityFilter}
                        onChange={(event) => setPriorityFilter(event.target.value)}
                    >
                        {PRIORITY_OPTIONS.map((priority) => (
                            <option key={priority} value={priority}>
                                {priority === 'ALL' ? 'All' : priority}
                            </option>
                        ))}
                    </select>
                </div>
                <div className="w-full md:w-56">
                    <label className="block text-sm font-medium text-gray-700 mb-1" htmlFor="status-filter">
                        Status
                    </label>
                    <select
                        id="status-filter"
                        className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                        value={statusFilter}
                        onChange={(event) => setStatusFilter(event.target.value)}
                    >
                        {STATUS_OPTIONS.map((status) => (
                            <option key={status} value={status}>
                                {status === 'ALL' ? 'All' : status}
                            </option>
                        ))}
                    </select>
                </div>
                <Button type="submit" className="w-full md:w-auto">
                    Search
                </Button>
            </form>


            {alerts.length === 0 ? (
                <p className="text-gray-500">No alerts</p>
            ) : getFilteredAlerts().length === 0 ? (
                <p className="text-gray-500">No alerts match the filters</p>
            ) : (
                <ul className="bg-white rounded shadow divide-y">
                    {getFilteredAlerts().map((alert) => (
                        <li key={alert.id} className={`p-4 ${alert.resolved ? 'opacity-60' : ''}`}>
                            <div className="flex items-start justify-between">
                                <div className="flex-1">
                                    <div className="flex items-center gap-2 mb-2">
                                        <span className={`text-lg font-semibold ${alert.resolved ? 'line-through text-gray-500' : ''}`}>
                                            {alert.title}
                                        </span>
                                        <span className={`px-2 py-1 rounded text-xs font-semibold ${getSeverityColor(alert.severity)}`}>
                                            {alert.severity}
                                        </span>
                                        <span className={`px-2 py-1 rounded text-xs font-semibold ${getPriorityColor(alert.priority)}`}>
                                            {alert.priority}
                                        </span>
                                    </div>
                                    {alert.details && <p className="text-sm text-gray-700 mb-2">{alert.details}</p>}
                                    {alert.category && <p className="text-xs text-gray-500">Category: {alert.category}</p>}
                                    <p className="text-xs text-gray-400 mt-1">
                                        {new Date(alert.logTime).toLocaleString()}
                                    </p>
                                </div>
                                <div className="flex gap-2 ml-4">
                                    {isAdmin() && (
                                        <button
                                            onClick={() => toggleResolved(alert)}
                                            className={`px-2 py-1 rounded text-xs font-semibold ${
                                                alert.resolved
                                                    ? 'bg-yellow-500 text-white hover:bg-yellow-600'
                                                    : 'bg-green-600 text-white hover:bg-green-700'
                                            }`}
                                        >
                                            {alert.resolved ? 'Reopen' : 'Mark Fixed'}
                                        </button>
                                    )}
                                    {isAdmin() && (
                                        <button
                                            onClick={() => deleteAlert(alert.id)}
                                            className="px-2 py-1 bg-red-600 text-white rounded text-xs hover:bg-red-700"
                                        >
                                            Delete
                                        </button>
                                    )}
                                </div>
                            </div>
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
