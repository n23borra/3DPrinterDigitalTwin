import React, {useEffect, useState} from 'react';
import api from '../../api/api';
import Loader from '../../components/Loader';

const STRATEGY_LABELS = {
    MITIGATE: 'Mitigate',
    TRANSFER: 'Transfer',
    ELIMINATE: 'Eliminate',
    ACCEPT: 'Accept',
};

/**
 * Visualises treatment plans grouped by status and allows inline updates to strategy and ownership.
 * @returns {JSX.Element} Kanban-style board for treatment plan management.
 */
export default function TreatmentBoard() {

    const [analyses, setAnalyses] = useState([]);
    const [analysisId, setAnalysisId] = useState('');
    const [plans, setPlans] = useState([]);
    const [loading, setLoading] = useState(false);
    const [users, setUsers] = useState([]);

    useEffect(() => {
        const load = async () => {
            try {
                const {data: list} = await api.get('/analyses');
                setAnalyses(Array.isArray(list) ? list : []);
                const {data: usersData} = await api.get('/users');
                setUsers(Array.isArray(usersData) ? usersData : []);
            } catch {
                setAnalyses([]);
                setUsers([]);
            }
        };
        load();
    }, []);

    useEffect(() => {
        if (!analysisId) return setPlans([]);
        setLoading(true);
        api.get('/treatments', {params: {analysisId}})
            .then(res => setPlans(res.data))
            .catch(() => {
            })
            .finally(() => setLoading(false));
    }, [analysisId]);

    /**
     * Updates a treatment plan with the provided payload and refreshes the board data.
     * @param {number} id - Identifier of the treatment plan to update.
     * @param {object} payload - Partial plan fields to persist.
     * @returns {Promise<void>} Resolves once the update and reload complete.
     */
    const updatePlan = async (id, payload) => {
        await api.put(`/treatments/${id}`, payload);
        const {data} = await api.get('/treatments', {params: {analysisId}});
        setPlans(data);
    };

    /**
     * Convenience wrapper that updates only the status field of a treatment plan.
     * @param {number} id - Identifier of the treatment plan to update.
     * @param {string} status - New status value selected by the user.
     * @returns {Promise<void>} Resolves once the plan has been updated.
     */
    const updateStatus = async (id, status) => {
        await updatePlan(id, {status});
    };

    const columns = {
        PLANNED: [], IN_PROGRESS: [], DONE: [],
    };
    plans.forEach(p => {
        if (columns[p.status]) columns[p.status].push(p);
    });

    return (<div>
        <h2 className="text-2xl font-semibold mb-4">Treatment Board</h2>
        <div className="mb-4">
            <label className="mr-2">Assessment:</label>
            <select value={analysisId} onChange={e => setAnalysisId(e.target.value)}
                    className="border px-2 py-1 rounded">
                <option value="">Select assessment</option>
                {analyses.map(a => (<option key={a.id} value={a.id}>{a.name}</option>))}
            </select>
        </div>

        {loading ? <Loader/> : (<div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {Object.keys(columns).map(col => (<div key={col} className="bg-gray-100 p-2 rounded">
                <h3 className="text-lg font-semibold mb-2">{col.replace('_', ' ')}</h3>
                <div className="space-y-2">
                    {columns[col].map(plan => {
                        const assetName = plan.riskResult?.risk?.asset?.name ?? '—';
                        const threatLabel = plan.riskResult?.risk?.threat?.label ?? '—';
                        const vulnerabilityLabel = plan.riskResult?.risk?.vulnerability?.label ?? '—';

                        return (<div key={plan.id} className="bg-white p-3 rounded shadow border border-gray-100">
                            <div className="border-b border-gray-200 pb-2 mb-3">
                                <div className="text-xs uppercase tracking-wide text-gray-500">Asset</div>
                                <div className="font-semibold text-sm text-gray-900">{assetName}</div>
                            </div>
                            <div className="grid grid-cols-1 gap-2 mb-3 text-sm">
                                <div>
                                    <div className="text-xs uppercase tracking-wide text-gray-500">Threat</div>
                                    <div className="font-medium text-gray-800">{threatLabel}</div>
                                </div>
                                <div>
                                    <div className="text-xs uppercase tracking-wide text-gray-500">Vulnerability</div>
                                    <div className="font-medium text-gray-800">{vulnerabilityLabel}</div>
                                </div>
                            </div>
                            {plan.description ? (
                                <p className="text-xs text-gray-600 mb-3 leading-snug">{plan.description}</p>
                            ) : null}

                            <div className="space-y-3 text-sm">
                                <div>
                                    <div className="text-xs uppercase tracking-wide text-gray-500 mb-1">Strategy</div>
                                    <select
                                        value={plan.strategy}
                                        onChange={e => updatePlan(plan.id, {strategy: e.target.value})}
                                        className="border px-2 py-1 rounded w-full"
                                    >
                                        {Object.entries(STRATEGY_LABELS).map(([value, label]) => (
                                            <option key={value} value={value}>{label}</option>
                                        ))}
                                    </select>
                                </div>

                                <div>
                                    <div className="text-xs uppercase tracking-wide text-gray-500 mb-1">Responsible</div>
                                    <select
                                        value={plan.responsibleId || ''}
                                        onChange={e => updatePlan(plan.id, {responsibleId: e.target.value || null})}
                                        className="border px-2 py-1 rounded w-full"
                                    >
                                        <option value="">Unassigned</option>
                                        {users.map(u => (
                                            <option key={u.id} value={u.id}>{u.username}</option>
                                        ))}
                                    </select>
                                </div>

                                <div>
                                    <div className="text-xs uppercase tracking-wide text-gray-500 mb-1">Due date</div>
                                    <input
                                        type="date"
                                        value={plan.dueDate || ''}
                                        onChange={e => updatePlan(plan.id, {dueDate: e.target.value})}
                                        className="border px-2 py-1 rounded w-full"
                                    />
                                </div>

                                <div>
                                    <div className="text-xs uppercase tracking-wide text-gray-500 mb-1">Status</div>
                                    <select
                                        value={plan.status}
                                        onChange={e => updateStatus(plan.id, e.target.value)}
                                        className="border px-2 py-1 rounded w-full"
                                    >
                                        <option value="PLANNED">PLANNED</option>
                                        <option value="IN_PROGRESS">IN_PROGRESS</option>
                                        <option value="DONE">DONE</option>
                                    </select>
                                </div>
                            </div>
                        </div>);
                    })}
                </div>
            </div>))}
        </div>)}
    </div>);
}