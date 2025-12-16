import React, {useEffect, useState} from 'react';
import api from '../api/api';
import Loader from '../components/Loader';

/**
 * Shows the list of audit trail entries for the authenticated user.
 * @returns {JSX.Element} Page section displaying audit log items or a loader.
 */
export default function AuditLogs() {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const load = async () => {
            try {
                const {data: user} = await api.get('/me');
                const {data} = await api.get('/audit', {params: {userId: user.id}});
                setLogs(Array.isArray(data) ? data : []);
            } finally {
                setLoading(false);
            }
        };
        load().catch(() => setLogs([]));
    }, []);

    if (loading) return <Loader/>;

    return (<div>
        <h2 className="text-2xl font-semibold mb-4">Audit Logs</h2>
        <ul className="bg-white rounded shadow divide-y">
            {logs.map(log => (<li key={log.id} className="p-2 text-sm">
                <div className="font-semibold">{log.action}</div>
                {log.details && (<div className="text-xs text-gray-600">{log.details}</div>)}
            </li>))}
        </ul>
    </div>);
}