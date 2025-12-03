import React, {useEffect, useState} from 'react';
import api from '../../api/api';
import Loader from '../../components/Loader';
import Button from '../../components/Button';
import AnalysisModal from './AnalysisModal';

/**
 * Lists existing analyses and provides controls for creation, duplication and deletion.
 * @returns {JSX.Element} Table of analyses with role-based management actions.
 */
export default function AnalysisList() {
    const [analyses, setAnalyses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [role, setRole] = useState('');
    const [modalAnalysis, setModalAnalysis] = useState(null);

    /**
     * Removes an analysis after user confirmation and refreshes the listing.
     * @param {number} id - Identifier of the analysis to delete.
     * @returns {Promise<void>} Resolves when the deletion request completes.
     */
    const handleDelete = async id => {
        if (!window.confirm('Delete this assessment?')) return;
        try {
            await api.delete(`/analyses/${id}`);
            fetchAnalyses();
        } catch (e) {
            console.error(e);
        }
    };

    /**
     * Creates a copy of the provided analysis and refreshes the table.
     * @param {object} analysis - Analysis data used as the source for duplication.
     * @returns {Promise<void>} Resolves once the duplication call finishes.
     */
    const handleDuplicate = async analysis => {
        try {
            await api.post('/analyses', {
                name: `${analysis.name} (copy)`,
                description: analysis.description,
                language: analysis.language,
                scope: analysis.scope,
                criticality: analysis.criticality,
                dm: analysis.dm,
                ta: analysis.ta,
            });
            fetchAnalyses();
        } catch (e) {
            console.error(e);
        }
    };

    /**
     * Loads the analyses from the API and updates local state.
     * @returns {Promise<void>} Resolves once the data has been processed.
     */
    const fetchAnalyses = async () => {
        try {
            const {data} = await api.get('/analyses');
            setAnalyses(Array.isArray(data) ? data : []);
        } catch {
            setAnalyses([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAnalyses();
    }, []);

    useEffect(() => {
        api.get('/me')
            .then(res => setRole(res.data.role))
            .catch(() => setRole(''));
    }, []);

    if (loading) return <Loader/>;

    return (
        <div>
            <div className="flex justify-between mb-4">
                <h2 className="text-2xl font-semibold">Assessments</h2>
                {role === 'SUPER_ADMIN' && (
                    <Button onClick={() => setModalAnalysis({})}>New Assessment</Button>
                )}
            </div>
            <table className="w-full bg-white rounded shadow">
                <thead>
                <tr className="bg-gray-200">
                    <th className="p-2 text-left">Name</th>
                    <th className="p-2 text-left">Description</th>
                    <th className="p-2 text-left">Language</th>
                    <th className="p-2 text-left">Scope</th>
                    <th className="p-2 text-left">Criticality</th>
                    <th className="p-2 text-left">DM</th>
                    <th className="p-2 text-left">TA</th>
                    {role === 'SUPER_ADMIN' && <th className="p-2 text-center">Actions</th>}
                </tr>
                </thead>
                <tbody>
                {analyses.map(a => (
                    <tr key={a.id} className="border-t">
                        <td className="p-2">{a.name}</td>
                        <td className="p-2">{a.description}</td>
                        <td className="p-2">{a.language}</td>
                        <td className="p-2">{a.scope}</td>
                        <td className="p-2">{a.criticality}</td>
                        <td className="p-2">{a.dm}</td>
                        <td className="p-2">{a.ta}</td>
                        {role === 'SUPER_ADMIN' && (
                            <td className="p-2 text-right space-x-2">
                                <Button type="button" onClick={() => setModalAnalysis(a)}>Edit</Button>
                                <Button type="button" onClick={() => handleDuplicate(a)}>Duplicate</Button>
                                <Button type="button" onClick={() => handleDelete(a.id)}>Delete</Button>
                            </td>
                        )}
                    </tr>
                ))}
                </tbody>
            </table>
            {modalAnalysis !== null && (
                <AnalysisModal analysis={modalAnalysis.id ? modalAnalysis : null}
                               onClose={() => setModalAnalysis(null)}
                               onSaved={() => {
                                   setModalAnalysis(null);
                                   fetchAnalyses();
                               }}/>
            )}
        </div>
    );
}
