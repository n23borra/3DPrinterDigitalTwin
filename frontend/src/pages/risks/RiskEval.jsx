import React, {useEffect, useMemo, useState} from 'react';
import api from '../../api/api';
import Loader from '../../components/Loader';
import Button from '../../components/Button';
import Modal from '../../components/Modal';

/**
 * Provides tools to evaluate risks, assign controls and set mitigation levels for an analysis.
 * @returns {JSX.Element} Risk evaluation workspace with control assignment modal.
 */
export default function RiskEval() {
    const [analyses, setAnalyses] = useState([]);
    const [analysisId, setAnalysisId] = useState('');
    const [risks, setRisks] = useState([]);
    const [controlsByRisk, setControlsByRisk] = useState({});
    const [levels, setLevels] = useState({});
    const [loading, setLoading] = useState(false);
    const [mappingData, setMappingData] = useState([]);
    const [allControls, setAllControls] = useState([]);
    const [assignData, setAssignData] = useState(null); // {riskId, vulnLabel}
    const [checkedControls, setCheckedControls] = useState(() => new Set());
    const [role, setRole] = useState('USER');


    useEffect(() => {
        api.get('/analyses')
            .then(res => setAnalyses(Array.isArray(res.data) ? res.data : []))
            .catch(() => setAnalyses([]));
    }, []);

    useEffect(() => {
        let active = true;
        api.get('/me')
            .then(res => {
                if (!active) return;
                setRole(res?.data?.role ?? 'USER');
            })
            .catch(() => {
                if (!active) return;
                setRole('USER');
            });
        return () => {
            active = false;
        };
    }, []);

    const isStandardUser = role === 'USER';

    /**
     * Loads risks for the selected analysis and prepares cached control data.
     * @param {string|number} aid - Identifier of the analysis whose risks should be loaded.
     * @returns {Promise<void>} Resolves when related control metadata has been cached.
     */
    const loadRisks = async (aid) => {
        const {data: list} = await api.get('/risks', {params: {analysisId: aid}});
        const risksList = Array.isArray(list) ? list : [];
        setRisks(risksList);
        const ctrlMap = {};
        const lvlMap = {};
        await Promise.all(risksList.map(async r => {
            try {
                const {data} = await api.get(`/risks/${r.risk.id}/controls`);
                const ctrls = Array.isArray(data) ? data : [];
                ctrlMap[r.risk.id] = ctrls;
                lvlMap[r.risk.id] = buildLevelMap(ctrls);
            } catch {
                ctrlMap[r.risk.id] = [];
                lvlMap[r.risk.id] = {};
            }
        }));
        setControlsByRisk(ctrlMap);
        setLevels(lvlMap);
    };

    /**
     * Builds a lookup mapping control identifiers to their stored level.
     * @param {Array<object>} list - List of control assignments for a risk.
     * @returns {Record<string|number, number>} Normalised map of controlId to level.
     */
    const buildLevelMap = (list) => {
        const map = {};
        (Array.isArray(list) ? list : []).forEach(ctrl => {
            map[ctrl.controlId] = ctrl.level ?? 0;
        });
        return map;
    };

    /**
     * Reloads control assignments for the specified risk and updates caches.
     * @param {number} riskId - Identifier of the base risk whose controls need refreshing.
     * @returns {Promise<Array<object>>} The refreshed list of controls for the risk.
     */
    const refreshControlsForRisk = async (riskId) => {
        const {data} = await api.get(`/risks/${riskId}/controls`);
        const list = Array.isArray(data) ? data : [];
        setControlsByRisk(prev => ({
            ...prev,
            [riskId]: list
        }));
        setLevels(prev => ({
            ...prev,
            [riskId]: buildLevelMap(list)
        }));
        return list;
    };

    useEffect(() => {
        fetch('/control_to_vuln_mapping.json')
            .then(res => res.json())
            .then(data => setMappingData(Array.isArray(data) ? data : []))
            .catch(() => setMappingData([]));
        api.get('/controls')
            .then(res => setAllControls(Array.isArray(res.data) ? res.data : []))
            .catch(() => setAllControls([]));
    }, []);

    const vulnControlMap = useMemo(() => {
        const map = {};
        mappingData.forEach(ctrl => {
            const ctrlInfo = allControls.find(c => c.uuid === ctrl.control_uuid || c.label === ctrl.control_label);
            if (!ctrlInfo) return;
            const ctrlObj = {
                id: ctrlInfo.id,
                label: ctrlInfo.label,
                description: ctrlInfo.description
            };
            ctrl.vulnerabilities.forEach(v => {
                if (!map[v.vuln_label]) map[v.vuln_label] = [];
                if (!map[v.vuln_label].some(c => c.id === ctrlObj.id)) {
                    map[v.vuln_label].push(ctrlObj);
                }
            });
        });
        return map;
    }, [mappingData, allControls]);

    const allControlsById = useMemo(() => {
        const map = new Map();
        allControls.forEach(ctrl => {
            if (ctrl?.id != null) {
                map.set(String(ctrl.id), ctrl);
            }
        });
        return map;
    }, [allControls]);

    useEffect(() => {
        if (!assignData) {
            setCheckedControls(new Set());
            return;
        }
        const {riskId} = assignData;
        const current = controlsByRisk[riskId] || [];
        const next = new Set(current.map(ctrl => String(ctrl.controlId ?? ctrl.id)));
        setCheckedControls(prev => {
            if (prev.size === next.size) {
                let identical = true;
                for (const id of next) {
                    if (!prev.has(id)) {
                        identical = false;
                        break;
                    }
                }
                if (identical) return prev;
            }
            return next;
        });
    }, [assignData, controlsByRisk]);

    const modalControls = useMemo(() => {
        if (!assignData) return [];
        const {riskId, vulnLabel} = assignData;
        const assigned = controlsByRisk[riskId] || [];
        const recommended = vulnControlMap[vulnLabel] || [];
        const combined = new Map();

        recommended.forEach(ctrl => {
            if (!ctrl || ctrl.id == null) return;
            const idStr = String(ctrl.id);
            const fallback = allControlsById.get(idStr);
            combined.set(idStr, {
                id: ctrl.id,
                label: ctrl.label || fallback?.label || `Control ${ctrl.id}`,
                description: ctrl.description || fallback?.description || ''
            });
        });

        assigned.forEach(ctrl => {
            const id = ctrl.controlId ?? ctrl.id;
            if (id == null) return;
            const idStr = String(id);
            const fallback = allControlsById.get(idStr);
            const label = ctrl.label || ctrl.controlLabel || fallback?.label || `Control ${id}`;
            const description = ctrl.description || ctrl.controlDescription || fallback?.description || '';
            if (combined.has(idStr)) {
                const existing = combined.get(idStr);
                combined.set(idStr, {
                    id,
                    label: existing?.label || label,
                    description: existing?.description || description
                });
            } else {
                combined.set(idStr, {id, label, description});
            }
        });

        if (combined.size === 0) return [];

        return Array.from(combined.values()).sort((a, b) => {
            const labelA = a.label || '';
            const labelB = b.label || '';
            return labelA.localeCompare(labelB, undefined, {sensitivity: 'base'});
        });
    }, [assignData, controlsByRisk, vulnControlMap, allControlsById]);

    /**
     * Toggles whether a control is selected for assignment in the modal and syncs with the API when removing existing links.
     * @param {number|string} controlId - Identifier of the control being toggled.
     * @returns {Promise<void>} Resolves after potential API calls finish.
     */
    const toggleControlSelection = async (controlId) => {
        const idStr = String(controlId);
        const isChecked = checkedControls.has(idStr);
        if (isChecked) {
            setCheckedControls(prev => {
                const next = new Set(prev);
                next.delete(idStr);
                return next;
            });
            if (!assignData) return;
            const {riskId} = assignData;
            const assignedSet = new Set((controlsByRisk[riskId] || []).map(ctrl => String(ctrl.controlId ?? ctrl.id)));
            if (!assignedSet.has(idStr)) {
                return;
            }
            const normalizedId = normalizeControlId(controlId);
            try {
                const response = await api.delete('/risks/controls', {
                    data: {
                        riskId,
                        controlId: normalizedId
                    }
                });
                const {data} = response || {};
                if (data && data.id) {
                    setRisks(prev => prev.map(risk => risk.id === data.id ? {...risk, ...data} : risk));
                }
                try {
                    await refreshControlsForRisk(riskId);
                } catch {
                    setControlsByRisk(prev => {
                        const currentList = prev[riskId] || [];
                        const filtered = currentList.filter(ctrl => String(ctrl.controlId ?? ctrl.id) !== idStr);
                        if (filtered.length === currentList.length) return prev;
                        return {
                            ...prev,
                            [riskId]: filtered
                        };
                    });
                    setLevels(prev => {
                        const currentLevels = prev[riskId];
                        if (!currentLevels || !Object.prototype.hasOwnProperty.call(currentLevels, normalizedId)) {
                            return prev;
                        }
                        const updatedLevels = {...currentLevels};
                        delete updatedLevels[normalizedId];
                        return {
                            ...prev,
                            [riskId]: updatedLevels
                        };
                    });
                }
            } catch {
                setCheckedControls(prev => {
                    const next = new Set(prev);
                    next.add(idStr);
                    return next;
                });
            }
        } else {
            setCheckedControls(prev => {
                const next = new Set(prev);
                next.add(idStr);
                return next;
            });
        }
    };

    const hasAssignmentChanges = useMemo(() => {
        if (!assignData) return false;
        const {riskId} = assignData;
        const current = new Set((controlsByRisk[riskId] || []).map(ctrl => String(ctrl.controlId ?? ctrl.id)));
        if (current.size !== checkedControls.size) return true;
        for (const id of checkedControls) {
            if (!current.has(id)) return true;
        }
        return false;
    }, [assignData, controlsByRisk, checkedControls]);

    /**
     * Converts control identifiers to numbers when possible to satisfy backend expectations.
     * @param {string|number} value - Control identifier to normalise.
     * @returns {string|number} Numeric identifier when parsable, otherwise the original value.
     */
    const normalizeControlId = (value) => {
        if (typeof value === 'number') return value;
        const parsed = Number(value);
        return Number.isNaN(parsed) ? value : parsed;
    };

    /**
     * Resolves metadata for a control from recommended or global control datasets.
     * @param {string|number} controlId - Identifier of the control.
     * @param {string} vulnLabel - Vulnerability label providing context for recommendations.
     * @returns {{label: string, description: string}} Object describing the control for display.
     */
    const getControlMeta = (controlId, vulnLabel) => {
        const idStr = String(controlId);
        const mapped = vulnLabel ? (vulnControlMap[vulnLabel] || []).find(ctrl => String(ctrl.id) === idStr) : null;
        const fallback = allControlsById.get(idStr);
        return {
            label: mapped?.label || fallback?.label || `Control ${controlId}`,
            description: mapped?.description || fallback?.description || ''
        };
    };

    useEffect(() => {
        if (!analysisId) {
            setRisks([]);
            setControlsByRisk({});
            setLevels({});
            return;
        }
        setLoading(true);
        loadRisks(analysisId).finally(() => setLoading(false));
    }, [analysisId]);

    /**
     * Updates the mitigation level for a control and re-syncs the risk data.
     * @param {number} riskBaseId - Identifier of the risk assignment being modified.
     * @param {number} controlId - Identifier of the control whose level is being set.
     * @param {number} level - Chosen control level (0, 0.5, or 1).
     * @returns {Promise<void>} Resolves once the update is confirmed or rolled back.
     */
    const updateControlLevel = async (riskBaseId, controlId, level) => {
        const previousLevel = levels[riskBaseId]?.[controlId] ?? 0;

        setLevels(prev => ({
            ...prev,
            [riskBaseId]: {
                ...(prev[riskBaseId] || {}),
                [controlId]: level
            }
        }));
        try {
            const {data} = await api.post('/risks/controls', {riskId: riskBaseId, controlId, level});

            if (data && data.id) {
                setRisks(prev => prev.map(risk => risk.id === data.id ? {...risk, ...data} : risk));
            }
            try {
                await refreshControlsForRisk(riskBaseId);
            } catch {
                setLevels(prev => ({
                    ...prev,
                    [riskBaseId]: {
                        ...(prev[riskBaseId] || {}),
                        [controlId]: level
                    }
                }));
            }
        } catch {
            setLevels(prev => ({
                ...prev,
                [riskBaseId]: {
                    ...(prev[riskBaseId] || {}),
                    [controlId]: previousLevel
                }
            }));
        }
    };

    /**
     * Persists modal selections by creating or removing control assignments for the active risk.
     * @returns {Promise<void>} Resolves after all add/remove operations conclude.
     */
    const assignControl = async () => {
        if (!assignData) return;
        const {riskId, vulnLabel} = assignData;
        const desired = new Set(Array.from(checkedControls));
        const current = new Set((controlsByRisk[riskId] || []).map(ctrl => String(ctrl.controlId ?? ctrl.id)));

        const toAdd = [];
        desired.forEach(id => {
            if (!current.has(id)) toAdd.push(id);
        });
        const toRemove = [];
        current.forEach(id => {
            if (!desired.has(id)) toRemove.push(id);
        });

        if (toAdd.length === 0 && toRemove.length === 0) {
            setAssignData(null);
            return;
        }


        setLoading(true);
        const added = [];
        const removed = [];
        try {
            for (const id of toAdd) {
                const normalizedId = normalizeControlId(id);
                await api.post('/risks/controls', {
                    riskId,
                    controlId: normalizedId,
                    level: 0
                });
                added.push({id: String(id), normalizedId});
            }
            for (const id of toRemove) {
                const normalizedId = normalizeControlId(id);
                await api.delete('/risks/controls', {
                    data: {
                        riskId,
                        controlId: normalizedId
                    }
                });
                removed.push({id: String(id), normalizedId});
            }
            try {
                await refreshControlsForRisk(riskId);
            } catch {
                if (added.length > 0 || removed.length > 0) {
                    const removedSet = new Set(removed.map(item => String(item.normalizedId)));
                    setControlsByRisk(prev => {
                        const currentList = prev[riskId] || [];
                        const filtered = removedSet.size > 0
                            ? currentList.filter(ctrl => !removedSet.has(String(ctrl.controlId ?? ctrl.id)))
                            : currentList;
                        const existingIds = new Set(filtered.map(ctrl => String(ctrl.controlId ?? ctrl.id)));
                        const additions = [];
                        added.forEach(item => {
                            const idStr = String(item.normalizedId);
                            if (existingIds.has(idStr)) return;
                            const meta = getControlMeta(item.normalizedId, vulnLabel);
                            additions.push({controlId: item.normalizedId, label: meta.label, description: meta.description, level: 0});
                            existingIds.add(idStr);
                        });
                        if (removedSet.size === 0 && additions.length === 0) return prev;
                        return {
                            ...prev,
                            [riskId]: additions.length > 0 ? [...filtered, ...additions] : filtered
                        };
                    });
                    setLevels(prev => {
                        const riskLevels = {...(prev[riskId] || {})};
                        let changed = false;
                        added.forEach(item => {
                            if (riskLevels[item.normalizedId] !== 0) {
                                riskLevels[item.normalizedId] = 0;
                                changed = true;
                            }
                        });
                        removed.forEach(item => {
                            if (Object.prototype.hasOwnProperty.call(riskLevels, item.normalizedId)) {
                                delete riskLevels[item.normalizedId];
                                changed = true;
                            }
                        });
                        if (!changed) return prev;
                        return {
                            ...prev,
                            [riskId]: riskLevels
                        };
                    });
                }
            }
        } finally {
            setLoading(false);
            setAssignData(null);
        }
    };

    return (<div>
        <Modal open={!!assignData} onClose={() => setAssignData(null)}>
            <div>
                <h3 className="text-lg font-semibold mb-2">Assign Control</h3>
                <div className="max-h-64 overflow-y-auto border rounded mb-4">
                    <table className="w-full text-left">
                        <tbody>
                        {modalControls.length === 0 ? (
                            <tr>
                                <td className="px-3 py-2 text-sm text-gray-500 text-center" colSpan={2}>
                                    No controls available for this vulnerability.
                                </td>
                            </tr>
                        ) : (
                            modalControls.map(ctrl => {
                                const idStr = String(ctrl.id);
                                const isChecked = checkedControls.has(idStr);
                                return (
                                    <tr key={idStr} className="border-b last:border-b-0">
                                        <td className="px-3 py-2 align-top w-1/3">
                                            <label className="flex items-start gap-2 cursor-pointer select-none">
                                                <input
                                                    type="checkbox"
                                                    checked={isChecked}
                                                    onChange={() => toggleControlSelection(ctrl.id)}
                                                    className="mt-1"
                                                />
                                                <span className="font-medium">{ctrl.label}</span>
                                            </label>
                                        </td>
                                        <td className="px-3 py-2 text-sm text-gray-600">
                                            {ctrl.description ? ctrl.description : (
                                                <span className="italic text-gray-400">No description provided</span>
                                            )}
                                        </td>
                                    </tr>
                                );
                            })
                        )}
                        </tbody>
                    </table>
                </div>
                <div className="text-right">
                    <Button onClick={assignControl} disabled={!hasAssignmentChanges}>Save</Button>
                </div>
            </div>
        </Modal>
        <h2 className="text-2xl font-semibold mb-4">Risk Evaluation</h2>
        <div className="mb-4">
            <label className="mr-2">Assessment:</label>
            <select value={analysisId} onChange={e => setAnalysisId(e.target.value)}
                    className="border px-2 py-1 rounded">
                <option value="">Select assessment</option>
                {analyses.map(a => (<option key={a.id} value={a.id}>{a.name}</option>))}
            </select>
        </div>

        {loading ? <Loader/> : (<table className="w-full bg-white rounded shadow">
            <thead>
            <tr className="bg-gray-200">
                <th className="p-2 text-left">Asset</th>
                <th className="p-2 text-left">Threat</th>
                <th className="p-2 text-left">Vulnerability</th>
                <th className="p-2 text-left">FR</th>
                <th className="p-2 text-left">Status</th>
                <th className="p-2 text-left">Controls</th>
                <th className="p-2 text-left">Level of control</th>
                <th className="p-2 text-left">Actions</th>
            </tr>
            </thead>
            <tbody>
            {risks.map(r => (<tr key={r.id} className="border-t">
                <td className="p-2">{r.risk.asset.name}</td>
                <td className="p-2">{r.risk.threat.label}</td>
                <td className="p-2">{r.risk.vulnerability.label}</td>
                <td className="p-2">{r.fr?.toFixed(2)}</td>
                <td className="p-2">{r.status}</td>
                <td className="p-2">
                    {(controlsByRisk[r.risk.id] || []).map(ctrl => {
                        const ctrlId = ctrl.controlId ?? ctrl.id;
                        const label = ctrl.label || ctrl.controlLabel || (ctrlId ? `Control ${ctrlId}` : 'Control');
                        const description = ctrl.description || ctrl.controlDescription || ctrl.control?.description;
                        return (
                            <div key={ctrlId ?? label} className="mb-2">
                                <div>{label}</div>
                                {description ? (
                                    <div className="text-sm text-gray-600">{description}</div>
                                ) : null}
                            </div>
                        );
                    })}
                </td>
                <td className="p-2">
                    {(controlsByRisk[r.risk.id] || []).map(c => {
                        const ctrlId = c.controlId;
                        const current = levels[r.risk.id]?.[ctrlId] ?? 0;
                        return (
                            <div key={ctrlId} className="mb-2">
                                {[0, 0.5, 1].map(val => (
                                    <label key={val} className="mr-2">
                                        <input
                                            type="radio"
                                            name={`ctrl-${r.risk.id}-${ctrlId}`}
                                            value={val}
                                            checked={current === val}
                                            onChange={() => updateControlLevel(r.risk.id, ctrlId, val)}
                                            disabled={isStandardUser}
                                        /> {val === 0 ? '❌ 0' : val === 0.5 ? '⚠️ 0.5' : '✅ 1'}
                                    </label>
                                ))}
                            </div>
                        );
                    })}
                </td>
                <td className="p-2">
                    {isStandardUser ? (
                        <span className="text-sm text-gray-500">Control assignment restricted for your role.</span>
                    ) : (
                        <div className="flex items-center gap-2">
                            <Button onClick={() => {
                                setAssignData({riskId: r.risk.id, vulnLabel: r.risk.vulnerability.label});
                            }}>Add control</Button>
                        </div>
                    )}
                </td>
            </tr>))}
            </tbody>
        </table>)}
    </div>);
}