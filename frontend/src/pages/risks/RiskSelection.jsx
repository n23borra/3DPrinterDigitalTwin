import React, {useEffect, useState, useMemo} from 'react';
import api from '../../api/api';
import Loader from '../../components/Loader';
import Button from '../../components/Button';
import Modal from '../../components/Modal';

/**
 * Facilitates the selection of risk combinations by mapping threats to vulnerabilities for an asset.
 * @returns {JSX.Element} Interactive grid to create or remove risk records.
 */
export default function RiskSelection() {
    const [analyses, setAnalyses] = useState([]);
    const [analysisId, setAnalysisId] = useState('');
    const [assets, setAssets] = useState([]);
    const [threats, setThreats] = useState([]);
    const [assetId, setAssetId] = useState('');
    const [categories, setCategories] = useState([]);
    const [categoryId, setCategoryId] = useState('');
    const [vulns, setVulns] = useState([]);
    const [existing, setExisting] = useState(new Map());
    const [selections, setSelections] = useState({});
    const [_openAssets, setOpenAssets] = useState({});
    const [_openThreats, setOpenThreats] = useState({});
    const [loading, setLoading] = useState(false);
    const [mappingData, setMappingData] = useState([]);
    const [vulnSearch, setVulnSearch] = useState('');
    const [showSuccess, setShowSuccess] = useState(false);
    const [showFailure, setShowFailure] = useState(false);
    const [failureMessage, setFailureMessage] = useState('');

    useEffect(() => {
        fetch('/threat_to_vuln_mapping.json')
            .then(res => res.json())
            .then(data => setMappingData(Array.isArray(data) ? data : []))
            .catch(() => setMappingData([]));
    }, []);

    const threatVulnMap = useMemo(() => {
        const map = {};
        mappingData.forEach(t => {
            map[t.threat_label] = t.vulnerabilities.map(v => v.vuln_label);
        });
        return map;
    }, [mappingData]);

    useEffect(() => {
        api.get('/threats').then(r => setThreats(Array.isArray(r.data) ? r.data : []))
            .catch(() => setThreats([]));
        api.get('/vulnerabilities').then(r => setVulns(Array.isArray(r.data) ? r.data : []))
            .catch(() => setVulns([]));
        api.get('/analyses').then(r => setAnalyses(Array.isArray(r.data) ? r.data : []))
            .catch(() => setAnalyses([]));
    }, []);

    useEffect(() => {
        setOpenAssets({});
        setOpenThreats({});
        setCategoryId('');
        if (!analysisId) {
            setAssets([]);
            setExisting(new Map());
            setAssetId('');
            setCategories([]);
            return;
        }
        setLoading(true);
        setExisting(new Map());
        const assetsPromise = api.get('/assets', {params: {analysisId}})
            .then(aRes => {
                const assetsData = Array.isArray(aRes.data) ? aRes.data : [];
                setAssets(assetsData);
                setAssetId('');
            })
            .catch(() => {
                setAssets([]);
                setAssetId('');
            });
        const risksPromise = api.get('/risks', {params: {analysisId}})
            .then(rRes => {
                const map = new Map();
                if (Array.isArray(rRes.data)) {
                    rRes.data.forEach(rr => {
                        const key = [rr.risk.assetId, rr.risk.threatId, rr.risk.vulnerabilityId]
                            .map(String)
                            .join('-');
                        const riskId = rr.riskId ?? rr.risk?.id;
                        map.set(key, riskId);
                    });
                }
                setExisting(map);
            })
            .catch(err => {
                console.error('Failed to load risks', err);
                setExisting(new Map());
            });
        Promise.allSettled([assetsPromise, risksPromise])
            .finally(() => setLoading(false));
    }, [analysisId]);

    useEffect(() => {
        if (!analysisId || !assetId) {
            setExisting(new Map());
            return;
        }
        setLoading(true);
        api.get('/risks', {params: {analysisId}})
            .then(rRes => {
                const map = new Map();
                if (Array.isArray(rRes.data)) {
                    rRes.data.forEach(rr => {
                        if (rr.risk?.assetId === Number(assetId)) {
                            const key = [rr.risk.assetId, rr.risk.threatId, rr.risk.vulnerabilityId]
                                .map(String)
                                .join('-');
                            const riskId = rr.riskId ?? rr.risk?.id;
                            map.set(key, riskId);
                        }
                    });
                }
                setExisting(map);
            })
            .catch(err => {
                console.error('Failed to load risks', err);
                setExisting(new Map());
            })
            .finally(() => setLoading(false));
    }, [analysisId, assetId]);

    /**
     * Toggles a vulnerability selection for the given asset and threat, deleting existing risks when needed.
     * @param {number|string} assetId - Identifier of the asset involved in the risk.
     * @param {number|string} threatId - Identifier of the threat involved in the risk.
     * @param {number|string} vulnId - Identifier of the vulnerability being toggled.
     * @returns {void}
     */
    const toggle = (assetId, threatId, vulnId) => {
        const combKey = [assetId, threatId, vulnId].map(String).join('-');
        if (existing.has(combKey)) {
            const riskId = existing.get(combKey);
            api.delete(`/risks/${riskId}`)
                .then(() => {
                    setExisting(prev => {
                        const map = new Map(prev);
                        map.delete(combKey);
                        return map;
                    });
                    alert('Risk removed successfully');
                })
                .catch(() => {
                    alert('Failed to remove risk');
                });
            return;
        }
        const key = [assetId, threatId].map(String).join('-');
        setSelections(prev => {
            const current = prev[key] || new Set();
            const next = new Set(current);
            if (next.has(vulnId)) next.delete(vulnId); else next.add(vulnId);
            return {...prev, [key]: next};
        });
    };

    useEffect(() => {
        setCategoryId('');
        setOpenThreats({});
        if (!assetId) {
            setCategories([]);
            return;
        }
        api.get('/risk-categories', {params: {assetId}})
            .then(r => setCategories(Array.isArray(r.data) ? r.data : []))
            .catch(() => setCategories([]));
    }, [assetId]);

    /**
     * Persists all pending risk selections to the backend and reports success or failure.
     * @returns {Promise<void>} Resolves after API requests finish and UI feedback is displayed.
     */
    const save = async () => {
        setLoading(true);
        try {
            const calls = [];
            Object.entries(selections).forEach(([key, vulnSet]) => {
                const [assetId, threatId] = key.split('-').map(Number);
                vulnSet.forEach(vulnId => {
                    const combKey = [assetId, threatId, vulnId].map(String).join('-');
                    if (!existing.has(combKey)) {
                        calls.push(api.post('/risks', {assetId, threatId, vulnerabilityId: vulnId}));
                    }
                });
            });
            let results = [];
            if (calls.length) {
                results = await Promise.allSettled(calls);
                try {
                    const {data} = await api.get('/risks', {params: {analysisId}});
                    const map = new Map();
                    if (Array.isArray(data)) data.forEach(rr => {
                        if (rr.risk?.assetId === Number(assetId)) {
                            const key = [rr.risk.assetId, rr.risk.threatId, rr.risk.vulnerabilityId]
                                .map(String)
                                .join('-');
                            const riskId = rr.riskId ?? rr.risk?.id;
                            map.set(key, riskId);
                        }
                    });
                    setExisting(map);
                } catch (_e) {
                    // ignore refresh errors
                }
            }
            setSelections({});
            if (results.length) {
                const failures = results.filter(r => r.status === 'rejected' && r.reason?.response?.status !== 409);
                const successes = results.length - failures.length;
                if (failures.length === 0) {
                    setShowSuccess(true);
                } else if (successes > 0) {
                    setFailureMessage('Some risks could not be created');
                    setShowFailure(true);
                } else {
                    setFailureMessage('Failed to create risks');
                    setShowFailure(true);
                }
            }
        } catch (e) {
            setFailureMessage('Failed to create risks');
            setShowFailure(true);
        } finally {
            setLoading(false);
        }
    };

    const filteredThreats = categoryId ? threats.filter(t => t.id === Number(categoryId)) : [];
    const selectedThreat = filteredThreats[0];
    const searchWords = vulnSearch.toLowerCase().split(/\s+/).filter(Boolean);
    const filteredVulns = selectedThreat ?
        vulns.filter(v => {
            const allowed = (threatVulnMap[selectedThreat.label] || []).includes(v.label);
            if (!allowed) return false;
            const label = v.label.toLowerCase();
            return searchWords.every(w => label.includes(w));
        }) : [];

    if (loading) return <Loader/>;

    return (
        <div>
            <Modal open={showSuccess} onClose={() => setShowSuccess(false)}>
                <div className="text-center">
                    <p>Risks created successfully</p>
                    <Button className="mt-4" onClick={() => setShowSuccess(false)}>OK</Button>
                </div>
            </Modal>
            <Modal open={showFailure} onClose={() => setShowFailure(false)}>
                <div className="text-center">
                    <p>{failureMessage}</p>
                    <Button className="mt-4" onClick={() => setShowFailure(false)}>OK</Button>
                </div>
            </Modal>
            <h2 className="text-2xl font-semibold mb-4">Risk Selection</h2>
            <div className="mb-4 flex items-center">
                <label className="mr-2">Assessment:</label>
                <select value={analysisId} onChange={e => setAnalysisId(e.target.value)}
                        className="border px-2 py-1 rounded">
                    <option value="">Select assessment</option>
                    {analyses.map(a => <option key={a.id} value={a.id}>{a.name}</option>)}
                </select>
                <label className="ml-4 mr-2">Asset:</label>
                <select value={assetId} onChange={e => setAssetId(e.target.value)} className="border px-2 py-1 rounded">
                    <option value="">Select asset</option>
                    {assets.map(a => <option key={a.id} value={a.id}>{a.name}</option>)}
                </select>
                <label className="ml-4 mr-2">Risk category:</label>
                <select value={categoryId} onChange={e => setCategoryId(e.target.value)}
                        className="border px-2 py-1 rounded">
                    <option value="">Select category</option>
                    {categories.map(c => <option key={c.id} value={c.id}>{c.label}</option>)}
                </select>
            </div>
            {categoryId ? (
                <div className="mb-4 border rounded bg-white">
                    <input
                        type="text"
                        placeholder="Search for vulnerabilities…"
                        value={vulnSearch}
                        onChange={e => setVulnSearch(e.target.value)}
                        className="ml-4 mt-2 mb-2 border px-2 py-1 rounded"
                    />
                    {filteredThreats.map(th => (
                        <div key={th.id} className="border-t py-2">
                            <div className="ml-4 mt-1 grid grid-cols-2 gap-3">
                                {filteredVulns.map(v => {
                                    const combKey = [assetId, th.id, v.id].map(String).join('-');
                                    const checked = existing.has(combKey) || (
                                        selections[[assetId, th.id].map(String).join('-')]?.has(v.id)
                                    );
                                    return (
                                        <label key={v.id} className="inline-flex items-center text-sm">
                                            <input
                                                type="checkbox"
                                                className="mr-1"
                                                checked={checked}
                                                onChange={() => toggle(assetId, th.id, v.id)}
                                            />
                                            {v.label}
                                        </label>
                                    );
                                })}
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <p className="text-sm italic">Select a category</p>
            )}
            {assets.length > 0 && <Button onClick={save}>Create Risks</Button>}
        </div>
    );
}
