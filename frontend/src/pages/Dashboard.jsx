import React, {useEffect, useState} from 'react';
import api from '../api/api';
import {useNavigate} from 'react-router-dom';
import {jwtDecode} from 'jwt-decode';
import {fetchDashboardCounts} from '../api/dashboardApi';

/**
 * Dashboard landing page showing a welcome message and key system metrics.
 * @returns {JSX.Element} Overview cards summarising users, alerts and available printers.
 */
export default function Dashboard() {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const [counts, setCounts] = useState({userCount: 0, alertCount: 0, printerAvailableCount: 0});
    const [alertsByPrinter, setAlertsByPrinter] = useState([]); // [{printerName, alertCount}]
    const [unresolvedAlerts, setUnresolvedAlerts] = useState([]); // [{id, title, printerName, severity, createdAt}]
    let username = 'User';

    useEffect(() => {
        if (!token) {
            navigate('/login');
            return;
        }
        const fetchCounts = async () => {
            try {
                const response = await fetchDashboardCounts();
                setCounts({
                    userCount: response.data?.userCount ?? 0,
                    alertCount: response.data?.alertCount ?? 0,
                    printerAvailableCount: response.data?.printerAvailableCount ?? 0,
                });
            } catch (error) {
                if (error.response?.status === 401) {
                    localStorage.removeItem('token');
                    navigate('/login');
                }
            }
        };
        
        const fetchAlertsByPrinter = async () => {
            try{
                const response = await api.get("/dashboard/alerts");
                const alertsByPrinter = response.data;
                console.log("ALERTS :", alertsByPrinter);
                const alertsArray = Object.entries(alertsByPrinter).map(([printerId, alerts]) => ({
                                    printerId,
                                    alerts
                                    }));
                console.log("Alerts array : ",alertsArray);
                setAlertsByPrinter(alertsArray);
            }
            catch(e){
                setAlertsByPrinter([]);
            }
        };

        const fetchUnresolvedAlerts = async () => {
            try {
                const { data } = await api.get('/alerts/unresolved');
                setUnresolvedAlerts(Array.isArray(data) ? data : []);
            } catch(e) {
                console.error(e);
                setUnresolvedAlerts([]);
            }
        };
        fetchCounts();
        fetchAlertsByPrinter();
        fetchUnresolvedAlerts();
    }, [token, navigate]);

    if (token) {
        try {
            const decoded = jwtDecode(token);
            username = decoded.sub || 'User';
        } catch {
            localStorage.removeItem('token');
            navigate('/login');
        }
    }

    return (<div>
        <header className="mb-8">
            <h2 className="text-3xl font-semibold text-gray-800">Welcome, {username}!</h2>
            <p className="text-gray-500">Here's your system overview.</p>
        </header>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <Widget title="Users Created" value={counts.userCount} color="bg-blue-600"/>
            <Widget title="Alerts" value={counts.alertCount} color="bg-green-600"/>
            <Widget title="Printer Available" value={counts.printerAvailableCount} color="bg-cyan-600"/>
        </div>

        {/* Zone 1 : Tableau des alertes par imprimante */}
        <section className="mt-10">
            <h3 className="text-xl font-semibold mb-4 text-gray-800">Alerts per Printer</h3>
            <div className="overflow-x-auto">
                <table className="min-w-full bg-white rounded-lg shadow">
                    <thead>
                        <tr>
                            <th className="px-4 py-2 text-left">Printer</th>
                            <th className="px-4 py-2 text-left">Alert Count</th>
                        </tr>
                    </thead>
                    <tbody>
                        {alertsByPrinter.length === 0 ? (
                            <tr><td colSpan={2} className="px-4 py-2 text-gray-500">No data</td></tr>
                        ) : alertsByPrinter.map(({printerId, alerts}) => (
                            <tr key={printerId}>
                                <td className="px-4 py-2">{printerId}</td>
                                <td className="px-4 py-2">{alerts.length}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </section>

        {/* Zone 2 : Liste des alertes non résolues */}
        <section className="mt-10">
            <h3 className="text-xl font-semibold mb-4 text-gray-800">Unresolved Alerts</h3>
            <div className="bg-white rounded-lg shadow p-4">
                {unresolvedAlerts.length === 0 ? (
                    <p className="text-gray-500">No unresolved alerts.</p>
                ) : (
                    <ul className="divide-y divide-gray-200">
                        {unresolvedAlerts.map(alert => (
                            <li key={alert.id} className="py-3 flex items-center justify-between">
                                <div>
                                    <span className="font-semibold text-gray-800">{alert.title}</span>
                                    <span className="ml-2 text-sm text-gray-500">({alert.printerId})</span>
                                    <span className={`ml-2 text-xs font-bold ${alert.severity === 'CRITICAL' ? 'text-red-600' : alert.severity === 'WARNING' ? 'text-yellow-600' : 'text-gray-600'}`}>{alert.severity}</span>
                                </div>
                                <span className="text-xs text-gray-400">{alert.logTime}</span>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </section>
    </div>);
}

function Widget({title, value, color}) {
    return (<div className={`rounded-lg shadow-lg p-6 text-white ${color}`}>
        <h3 className="text-lg font-semibold">{title}</h3>
        <p className="text-3xl mt-2 font-bold">{value}</p>
    </div>);
}
