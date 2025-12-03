import React, {useEffect, useState, useMemo} from 'react';
import api from '../../api/api';
import Loader from '../../components/Loader';
import Button from '../../components/Button';
import {Link, useSearchParams} from 'react-router-dom';
import AssetEditModal from './AssetEditModal';

/**
 * Displays assets for the selected analysis and exposes actions depending on permissions.
 * @returns {JSX.Element} Asset table with filtering, duplication and edit controls.
 */
export default function AssetList() {
    const [analyses, setAnalyses] = useState([]);
    const [analysisId, setAnalysisId] = useState('');
    const [assets, setAssets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [canCreate, setCanCreate] = useState(false);
    const [editAsset, setEditAsset] = useState(null);
    const [role, setRole] = useState('');
    const [adminCats, setAdminCats] = useState([]);
    const [searchParams, setSearchParams] = useSearchParams();

    const analysisLookup = useMemo(
        () => Object.fromEntries(analyses.map(a => [a.id, a.name])),
        [analyses],
    );

    /**
     * Deletes an asset after confirmation and reloads the table data.
     * @param {number} id - Identifier of the asset to delete.
     * @returns {Promise<void>} Resolves once the delete operation finishes.
     */
    const handleDelete = async id => {
        if (!window.confirm('Delete this asset?')) return;
        try {
            await api.delete(`/assets/${id}`);
            api.get('/assets', {params: analysisId ? {analysisId} : {}}).then(r => setAssets(r.data));
        } catch (e) {
            console.error('Failed to delete asset', e);
        }
    };

    /**
     * Creates a duplicate of the given asset and refreshes the current view.
     * @param {object} asset - Asset record to duplicate.
     * @returns {Promise<void>} Resolves once duplication completes.
     */
    const handleDuplicate = async asset => {
        try {
            const payload = {
                analysisId: Number(asset.analysisId),
                categoryId: Number(asset.categoryId ?? asset.category?.id),
                name: `Copy of ${asset.name}`,
                description: asset.description,
                impactC: Number(asset.impactC),
                impactI: Number(asset.impactI),
                impactA: Number(asset.impactA),
            };
            await api.post('/assets', payload);
            const {data} = await api.get('/assets', {params: {analysisId}});
            setAssets(data);
        } catch (e) {
            console.error('Failed to duplicate asset', e);
        }
    };

    useEffect(() => {
        const initialId = searchParams.get('analysisId');
        if (initialId) setAnalysisId(initialId);
        const load = async () => {
            try {
                const {data} = await api.get('/analyses');
                setAnalyses(Array.isArray(data) ? data : []);
            } catch {
                setAnalyses([]);
            }
        };
        load();
        const check = async () => {
            try {
                const {data: me} = await api.get('/me');
                setRole(me.role);
                if (me.role === 'SUPER_ADMIN') {
                    setCanCreate(true);
                    setAdminCats([]);
                    return;
                }
                const {data: admins} = await api.get('/category-admins');
                const allowed = Array.isArray(admins)
                    ? admins.filter(a => Array.isArray(a.emails) && a.emails.includes(me.email)).map(a => a.categoryId)
                    : [];
                setCanCreate(allowed.length > 0);
                setAdminCats(allowed);
            } catch {
                setCanCreate(false);
                setRole('');
                setAdminCats([]);
            }
        };
        check();
    }, []);

    useEffect(() => {
        setLoading(true);
        api.get('/assets', {params: analysisId ? {analysisId} : {}}).then(res => setAssets(res.data))
            .catch(() => setAssets([]))
            .finally(() => setLoading(false));
        setSearchParams(analysisId ? {analysisId} : {});
    }, [analysisId]);

    if (loading) return <Loader/>;

    return (
        <div>
            <div className="flex justify-between mb-4">
                <h2 className="text-2xl font-semibold">Assets</h2>
                {canCreate && (
                    <Link to={`/assets/new?analysisId=${analysisId}`}>
                        <Button>New Asset</Button>
                    </Link>
                )}
            </div>
            <div className="mb-4">
                <label className="mr-2">Assessment:</label>
                <select value={analysisId} onChange={e => setAnalysisId(e.target.value)}
                        className="border px-2 py-1 rounded">
                    <option value="">All assessments</option>
                    {analyses.map(a => (
                        <option key={a.id} value={a.id}>{a.name}</option>
                    ))}
                </select>
            </div>
            <table className="w-full bg-white rounded shadow">
                <thead>
                <tr className="bg-gray-200">
                    <th className="p-2 text-left">Name</th>
                    <th className="p-2 text-left">Assessment</th>
                    <th className="p-2 text-left">Category</th>
                    <th className="p-2 text-left">Description</th>
                    <th className="p-2 text-left">Impact Confidentiality</th>
                    <th className="p-2 text-left">Impact Integrity</th>
                    <th className="p-2 text-left">Impact Availability</th>
                    {(role === 'SUPER_ADMIN' || adminCats.length > 0) && (
                        <th className="p-2 text-center">Actions</th>
                    )}
                </tr>
                </thead>
                <tbody>
                {assets.map(a => (
                    <tr key={a.id} className="border-t">
                        <td className="p-2">{a.name}</td>
                        <td className="p-2">{analysisLookup[a.analysisId]}</td>
                        <td className="p-2">{a.categoryLabel || a.category?.label}</td>
                        <td className="p-2">{a.description}</td>
                        <td className="p-2">{a.impactC}</td>
                        <td className="p-2">{a.impactI}</td>
                        <td className="p-2">{a.impactA}</td>
                        {(role === 'SUPER_ADMIN' || adminCats.includes(a.categoryId || (a.category && a.category.id))) && (
                            <td className="p-2 text-right space-x-2">
                                <Button type="button" onClick={() => setEditAsset(a)}>Edit</Button>
                                <Button type="button" onClick={() => handleDuplicate(a)}>Duplicate</Button>
                                <Button type="button" onClick={() => handleDelete(a.id)}>Delete</Button>
                            </td>
                        )}
                    </tr>
                ))}
                </tbody>
            </table>
            {editAsset && (
                <AssetEditModal asset={editAsset} onClose={() => setEditAsset(null)} onSaved={() => {
                    api.get('/assets', {params: analysisId ? {analysisId} : {}}).then(r => setAssets(r.data));
                    setEditAsset(null);
                }}/>
            )}
        </div>
    );
}