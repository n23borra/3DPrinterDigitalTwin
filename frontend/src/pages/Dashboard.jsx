import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {jwtDecode} from 'jwt-decode';
import api from '../api/api';

/**
 * Dashboard landing page showing a welcome message and key system metrics.
 * @returns {JSX.Element} Overview cards summarising users and assessments.
 */
export default function Dashboard() {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const [counts, setCounts] = useState({userCount: 0, assessmentCount: 0});
    let username = 'User';

    useEffect(() => {
        if (!token) {
            navigate('/login');
            return;
        }
        const fetchCounts = async () => {
            try {
                const [usersResponse, analysesResponse] = await Promise.all([
                    api.get('/users'),
                    api.get('/analyses'),
                ]);
                setCounts({
                    userCount: Array.isArray(usersResponse.data) ? usersResponse.data.length : 0,
                    assessmentCount: Array.isArray(analysesResponse.data) ? analysesResponse.data.length : 0,
                });
            } catch (error) {
                if (error.response?.status === 401) {
                    localStorage.removeItem('token');
                    navigate('/login');
                }
            }
        };

        fetchCounts();
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
            <p className="text-gray-500">Here’s your system overview.</p>
        </header>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <Widget title="Users Created" value={counts.userCount} color="bg-blue-600"/>
            <Widget title="Assessments Created" value={counts.assessmentCount} color="bg-green-600"/>
        </div>
    </div>);
}

function Widget({title, value, color}) {
    return (<div className={`rounded-lg shadow-lg p-6 text-white ${color}`}>
        <h3 className="text-lg font-semibold">{title}</h3>
        <p className="text-3xl mt-2 font-bold">{value}</p>
    </div>);
}
