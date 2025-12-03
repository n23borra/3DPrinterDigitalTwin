import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import api from '../../api/api';
import Button from '../../components/Button';
import Loader from '../../components/Loader';

/**
 * Administration page for managing reference data such as threats, vulnerabilities and controls.
 * @returns {JSX.Element} Tabbed interface for browsing and editing reference catalogues.
 */
export default function References() {
    const navigate = useNavigate();
    const [role, setRole] = useState('');
    const [activeTab, setActiveTab] = useState('threats');
    const [threats, setThreats] = useState([]);
    const [vulns, setVulns] = useState([]);
    const [controls, setControls] = useState([]);

    const [tForm, setTForm] = useState({label: '', probability: ''});
    const [vForm, setVForm] = useState({label: '', gravity: ''});
    const [cForm, setCForm] = useState({label: '', efficiency: ''});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const load = async () => {
            const {data: user} = await api.get('/me');
            setRole(user.role);
            const [th, vu, co] = await Promise.all([api.get('/threats'), api.get('/vulnerabilities'), api.get('/controls')]);
            setThreats(Array.isArray(th.data) ? th.data : []);
            setVulns(Array.isArray(vu.data) ? vu.data : []);
            setControls(Array.isArray(co.data) ? co.data : []);
            setLoading(false);
        };
        load().catch(() => setLoading(false));
    }, []);

    useEffect(() => {
        if (!loading && role && role !== 'SUPER_ADMIN') navigate('/dashboard');
    }, [loading, role, navigate]);

    const isSuper = role === 'SUPER_ADMIN';

    /**
     * Validates and submits a new threat entry then refreshes the threat list.
     * @param {React.FormEvent<HTMLFormElement>} e - Submission event from the threat creation form.
     * @returns {Promise<void>} Resolves once the API interaction completes.
     */
    const addThreat = async (e) => {
        e.preventDefault();
        const probability = parseFloat(tForm.probability);
        if (Number.isNaN(probability) || probability < 0 || probability > 1) {
            return;
        }
        await api.post('/threats', {...tForm, probability});
        const {data} = await api.get('/threats');
        setThreats(Array.isArray(data) ? data : []);
        setTForm({label: '', probability: ''});
    };

    /**
     * Validates and submits a new vulnerability entry then refreshes the listing.
     * @param {React.FormEvent<HTMLFormElement>} e - Submission event from the vulnerability form.
     * @returns {Promise<void>} Resolves after the API request finishes.
     */
    const addVuln = async (e) => {
        e.preventDefault();
        const gravity = parseFloat(vForm.gravity);
        if (Number.isNaN(gravity) || gravity < 0 || gravity > 1) {
            return;
        }
        await api.post('/vulnerabilities', {...vForm, gravity});
        const {data} = await api.get('/vulnerabilities');
        setVulns(Array.isArray(data) ? data : []);
        setVForm({label: '', gravity: ''});
    };

    /**
     * Validates and submits a new control entry then refreshes the listing.
     * @param {React.FormEvent<HTMLFormElement>} e - Submission event from the control form.
     * @returns {Promise<void>} Resolves once the control is persisted.
     */
    const addControl = async (e) => {
        e.preventDefault();
        const efficiency = parseFloat(cForm.efficiency);
        if (Number.isNaN(efficiency) || efficiency < 0 || efficiency > 1) {
            return;
        }
        await api.post('/controls', {...cForm, efficiency});
        const {data} = await api.get('/controls');
        setControls(Array.isArray(data) ? data : []);
        setCForm({label: '', efficiency: ''});
    };

    /**
     * Deletes a reference record and refreshes the relevant dataset.
     * @param {'threats'|'vulnerabilities'|'controls'} type - Resource collection to update.
     * @param {number} id - Identifier of the record to remove.
     * @returns {Promise<void>} Resolves after the deletion completes.
     */
    const remove = async (type, id) => {
        await api.delete(`/${type}/${id}`);
        const {data} = await api.get(`/${type}`);
        if (type === 'threats') setThreats(Array.isArray(data) ? data : []);
        if (type === 'vulnerabilities') setVulns(Array.isArray(data) ? data : []);
        if (type === 'controls') setControls(Array.isArray(data) ? data : []);
    };

    /**
     * Updates a reference record and synchronises the local list with the backend response.
     * @param {'threats'|'vulnerabilities'|'controls'} type - Resource collection to update.
     * @param {number} id - Identifier of the record to modify.
     * @param {object} payload - Partial fields to persist.
     * @returns {Promise<void>} Resolves when the API call and refresh finish.
     */
    const update = async (type, id, payload) => {
        let finalPayload = payload;
        if (type === 'controls') {
            const existingControl = controls.find((control) => control.id === id);
            if (existingControl) {
                finalPayload = {...existingControl, ...payload};
            }
        }
        await api.put(`/${type}/${id}`, finalPayload);
        const {data} = await api.get(`/${type}`);
        if (type === 'threats') setThreats(Array.isArray(data) ? data : []);
        if (type === 'vulnerabilities') setVulns(Array.isArray(data) ? data : []);
        if (type === 'controls') setControls(Array.isArray(data) ? data : []);
    };

    if (loading) return <Loader/>;

    return (<div>
        <div className="space-x-2 mb-4">
            <Button onClick={() => setActiveTab('threats')}>Threats</Button>
            <Button onClick={() => setActiveTab('vulnerabilities')}>Vulnerabilities</Button>
            <Button onClick={() => setActiveTab('controls')}>Controls</Button>
        </div>
        {activeTab === 'threats' && (<Section
            title="Threats"
            data={threats}
            form={tForm}
            setForm={setTForm}
            onAdd={addThreat}
            onDelete={(id) => remove('threats', id)}
            onUpdate={(id, d) => update('threats', id, d)}
            editable={isSuper}
            valueKey="probability"
        />)}
        {activeTab === 'vulnerabilities' && (<Section
            title="Vulnerabilities"
            data={vulns}
            form={vForm}
            setForm={setVForm}
            onAdd={addVuln}
            onDelete={(id) => remove('vulnerabilities', id)}
            onUpdate={(id, d) => update('vulnerabilities', id, d)}
            editable={isSuper}
            valueKey="gravity"
        />)}
        {activeTab === 'controls' && (<Section
            title="Controls"
            data={controls}
            form={cForm}
            setForm={setCForm}
            onAdd={addControl}
            onDelete={(id) => remove('controls', id)}
            onUpdate={(id, d) => update('controls', id, d)}
            editable={isSuper}
            valueKey="efficiency"
        />)}
    </div>);
}

/**
 * Reusable table section rendering CRUD controls for a reference list.
 * @param {object} props - Section props.
 * @param {string} props.title - Section heading displayed above the table.
 * @param {Array<object>} props.data - Records to display in the table.
 * @param {object} props.form - Controlled form state for creating new records.
 * @param {Function} props.setForm - Setter used to update the creation form state.
 * @param {(e: React.FormEvent<HTMLFormElement>) => Promise<void>} props.onAdd - Handler for creating a new record.
 * @param {(id: number) => Promise<void>} props.onDelete - Handler invoked when deleting a record.
 * @param {(id: number, payload: object) => Promise<void>} props.onUpdate - Handler invoked when saving edits.
 * @param {boolean} props.editable - Whether editing controls should be displayed.
 * @param {string} props.valueKey - Key referencing the numeric value field within the records.
 * @returns {JSX.Element} Table with inline editing and creation controls.
 */
export function Section({title, data, form, setForm, onAdd, onDelete, onUpdate, editable, valueKey}) {
    const [editId, setEditId] = useState(null);
    const [editForm, setEditForm] = useState({label: '', value: ''});

    const rawEditValue = editForm.value === null || editForm.value === undefined ? '' : `${editForm.value}`;
    const parsedEditValue = parseFloat(rawEditValue);
    const hasEmptyEditValue = rawEditValue === '';
    const hasInvalidNumber = rawEditValue !== '' && Number.isNaN(parsedEditValue);
    const isEditOutOfRange = rawEditValue !== '' && !Number.isNaN(parsedEditValue) && (parsedEditValue < 0 || parsedEditValue > 1);
    const shouldShowValidation = editId !== null && (hasEmptyEditValue || hasInvalidNumber || isEditOutOfRange);
    const validationMessage = !shouldShowValidation ? '' : hasEmptyEditValue ? 'Value is required.' : hasInvalidNumber ? 'Please enter a valid number.' : 'Value must be between 0 and 1.';

    const rawCreateValue = form[valueKey] ?? '';
    const parsedCreateValue = parseFloat(rawCreateValue);
    const hasInvalidCreateNumber = rawCreateValue !== '' && Number.isNaN(parsedCreateValue);
    const isCreateOutOfRange = rawCreateValue !== '' && !Number.isNaN(parsedCreateValue) && (parsedCreateValue < 0 || parsedCreateValue > 1);
    const shouldShowCreateValidation = hasInvalidCreateNumber || isCreateOutOfRange;
    const createValidationMessage = !shouldShowCreateValidation ? '' : hasInvalidCreateNumber ? 'Please enter a valid number.' : 'Value must be between 0 and 1.';

    /**
     * Populates the edit form with the selected item and enables edit mode.
     * @param {object} item - Record to edit.
     * @returns {void}
     */
    const startEdit = (item) => {
        setEditId(item.id);
        setEditForm({
            label: item.label ?? '',
            value: item[valueKey] !== undefined && item[valueKey] !== null ? String(item[valueKey]) : ''
        });
    };

    /**
     * Exits edit mode and clears temporary edit form state.
     * @returns {void}
     */
    const cancelEdit = () => {
        setEditId(null);
        setEditForm({label: '', value: ''});
    };

    /**
     * Validates the edited values and persists them through the update handler.
     * @returns {Promise<void>} Resolves when the update completes successfully.
     */
    const saveEdit = async () => {
        const nextValue = parseFloat(rawEditValue);
        if (hasEmptyEditValue || Number.isNaN(nextValue) || nextValue < 0 || nextValue > 1) {
            return;
        }

        await onUpdate(editId, {label: editForm.label, [valueKey]: nextValue});
        setEditId(null);
        setEditForm({label: '', value: ''});
    };

    return (<div>
        <h2 className="text-xl font-semibold mb-2">{title}</h2>
        <table className="w-full bg-white rounded shadow mb-2">
            <thead>
            <tr className="bg-gray-200">
                <th className="p-2 text-left">ID</th>
                <th className="p-2 text-left">Label</th>
                <th className="p-2 text-left">Value</th>
                {editable && <th className="p-2"/>}
            </tr>
            </thead>
            <tbody>
            {data.map(d => (editId === d.id ? (<tr key={d.id} className="border-t">
                <td className="p-2">{d.id}</td>
                <td className="p-2">
                    <input
                        type="text"
                        value={editForm.label}
                        onChange={e => setEditForm(prev => ({...prev, label: e.target.value}))}
                        className="border px-2 py-1 rounded w-full"
                    />
                </td>
                <td className="p-2">
                    <div className="space-y-1">
                        <input
                            type="number"
                            step="0.1"
                            min="0"
                            max="1"
                            value={rawEditValue}
                            onChange={e => setEditForm(prev => ({...prev, value: e.target.value}))}
                            className="border px-2 py-1 rounded w-full"
                            data-testid="edit-value-input"
                        />
                        {validationMessage && (
                            <p className="text-sm text-red-600">{validationMessage}</p>
                        )}
                    </div>
                </td>
                {editable && (<td className="p-2 text-right space-x-2">
                    <Button
                        onClick={saveEdit}
                        disabled={shouldShowValidation}
                        data-testid="save-edit-button"
                        type="button"
                    >Save</Button>
                    <Button onClick={cancelEdit} type="button">Cancel</Button>
                </td>)}
            </tr>) : (<tr key={d.id} className="border-t">
                <td className="p-2">{d.id}</td>
                <td className="p-2">{d.label}</td>
                <td className="p-2">{d[valueKey]}</td>
                {editable && (<td className="p-2 text-right space-x-2">
                    <Button onClick={() => startEdit(d)}>Edit</Button>
                    <Button onClick={() => onDelete(d.id)}>Delete</Button>
                </td>)}
            </tr>)))}
            </tbody>
        </table>
        {editable && (<form onSubmit={onAdd} className="space-x-2">
            <input
                type="text"
                value={form.label}
                onChange={e => setForm({...form, label: e.target.value})}
                placeholder="Label"
                className="border px-2 py-1 rounded"
                required
            />
            <div className="inline-block space-y-1">
                <input
                    type="number"
                    step="0.1"
                    min="0"
                    max="1"
                    value={form[valueKey]}
                    onChange={e => setForm({...form, [valueKey]: e.target.value})}
                    placeholder="Value"
                    className="border px-2 py-1 rounded"
                    required
                />
                {createValidationMessage && (
                    <p className="text-sm text-red-600">{createValidationMessage}</p>
                )}
            </div>
            <Button type="submit" disabled={shouldShowCreateValidation}>Add</Button>
        </form>)}
    </div>);
}