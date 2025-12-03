import React, {useState} from 'react';
import api from '../../api/api';
import Button from '../../components/Button';
import Modal from '../../components/Modal';

/**
 * Modal dialog used to create or edit an analysis entry.
 * @param {object} props - Component props.
 * @param {object|null} props.analysis - Existing analysis to edit; null when creating a new one.
 * @param {() => void} props.onClose - Callback executed when the modal should be dismissed.
 * @param {() => void} props.onSaved - Callback triggered after a successful save operation.
 * @returns {JSX.Element} Modal containing the analysis form.
 */
export default function AnalysisModal({analysis, onClose, onSaved}) {
    const isEdit = !!analysis;
    const [form, setForm] = useState({
        name: analysis?.name || '',
        description: analysis?.description || '',
        language: analysis?.language || '',
        scope: analysis?.scope || '',
        criticality: analysis?.criticality || 'LOW',
        dm: analysis?.dm || 1,
        ta: analysis?.ta || 1,
    });
    const [error, setError] = useState('');

    /**
     * Updates the form state as fields change within the modal.
     * @param {React.ChangeEvent<HTMLInputElement|HTMLTextAreaElement|HTMLSelectElement>} e - Change event from a field.
     * @returns {void}
     */
    const handleChange = e => {
        setForm({...form, [e.target.name]: e.target.value});
    };

    /**
     * Sends the appropriate create or update request based on the current mode.
     * @param {React.FormEvent<HTMLFormElement>} e - Form submission event from the modal.
     * @returns {Promise<void>} Resolves when the save completes and callbacks have been invoked.
     */
    const handleSubmit = async e => {
        e.preventDefault();
        try {
            const payload = {
                ...form,
                dm: Number(form.dm),
                ta: Number(form.ta),
            };
            if (isEdit) {
                await api.put(`/analyses/${analysis.id}`, payload);
            } else {
                await api.post('/analyses', payload);
            }
            if (onSaved) onSaved();
        } catch (err) {
            setError('Error saving assessment');
        }
    };

    return (
        <Modal open={true} onClose={onClose} className="w-full max-w-4xl">
            <form onSubmit={handleSubmit} className="p-4">
                <h2 className="text-xl font-semibold mb-4">{isEdit ? 'Edit' : 'New'} Assessment</h2>
                {error && <p className="text-red-600 mb-2">{error}</p>}
                <div className="mb-3">
                    <label className="block mb-1">Name</label>
                    <input type="text" name="name" value={form.name} onChange={handleChange} className="w-full border px-2 py-1 rounded" required/>
                </div>
                <div className="mb-3">
                    <label className="block mb-1">Description</label>
                    <textarea name="description" value={form.description} onChange={handleChange} className="w-full border px-2 py-1 rounded" required/>
                </div>
                <div className="mb-3">
                    <label className="block mb-1">Language</label>
                    <input type="text" name="language" value={form.language} onChange={handleChange} className="w-full border px-2 py-1 rounded" required/>
                </div>
                <div className="mb-3">
                    <label className="block mb-1">Scope</label>
                    <input type="text" name="scope" value={form.scope} onChange={handleChange} className="w-full border px-2 py-1 rounded" required/>
                </div>
                <div className="mb-3">
                    <label className="block mb-1">Criticality</label>
                    <select name="criticality" value={form.criticality} onChange={handleChange} className="w-full border px-2 py-1 rounded">
                        <option value="LOW">Low</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="HIGH">High</option>
                    </select>
                </div>
                <div className="mb-3">
                    <label className="block mb-1">DM</label>
                    <select name="dm" value={form.dm} onChange={handleChange} className="w-full border px-2 py-1 rounded" required>
                        {[1,2,3,4,5].map(n => <option key={n} value={n}>{n}</option>)}
                    </select>
                    <a
                        href="https://trustsense-xu4xd.ondigitalocean.app/form.htm"
                        className="text-sm text-blue-600 underline"
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        assess your DM level Here
                    </a>
                </div>
                <div className="mb-4">
                    <label className="block mb-1">TA</label>
                    <select name="ta" value={form.ta} onChange={handleChange} className="w-full border px-2 py-1 rounded" required>
                        {[1,2,3,4,5].map(n => <option key={n} value={n}>{n}</option>)}
                    </select>
                </div>
                <div className="text-right">
                    <Button type="submit" className="mr-2">Save</Button>
                    <Button type="button" onClick={onClose}>Cancel</Button>
                </div>
            </form>
        </Modal>
    );
}