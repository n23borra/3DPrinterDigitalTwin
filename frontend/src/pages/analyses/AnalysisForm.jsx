import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/api';
import Button from '../../components/Button';

/**
 * Form for creating a new analysis definition with required metadata fields.
 * @returns {JSX.Element} Controlled form that persists a new assessment and redirects on success.
 */
export default function AnalysisForm() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        name: '', description: '', language: '', scope: '', criticality: 'LOW', dm: 1, ta: 1,
    });
    const [error, setError] = useState('');

    /**
     * Updates a single field in the analysis form state when the user edits the control.
     * @param {React.ChangeEvent<HTMLInputElement|HTMLTextAreaElement|HTMLSelectElement>} e - Change event from a form control.
     * @returns {void}
     */
    const handleChange = (e) => {
        const {name, value} = e.target;
        setForm({...form, [name]: value});
    };

    /**
     * Sends the new analysis payload to the API and navigates back to the listing.
     * @param {React.FormEvent<HTMLFormElement>} e - Form submission event.
     * @returns {Promise<void>} Resolves once the API request completes.
     */
    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/analyses', {
                ...form, dm: Number(form.dm), ta: Number(form.ta),
            });
            navigate('/analyses');
        } catch (err) {
            setError('Error creating assessment');
        }
    };
    return (<form onSubmit={handleSubmit} className="max-w-lg bg-white p-6 rounded shadow">
        <h2 className="text-xl font-semibold mb-4">New Assessment</h2>
        {error && <p className="text-red-600 mb-2">{error}</p>}
        <div className="mb-3">
            <label className="block mb-1">Name</label>
            <input
                type="text"
                name="name"
                value={form.name}
                onChange={handleChange}
                className="w-full border px-2 py-1 rounded"
                required
            />
        </div>
        <div className="mb-3">
            <label className="block mb-1">Description</label>
            <textarea
                name="description"
                value={form.description}
                onChange={handleChange}
                className="w-full border px-2 py-1 rounded"
                required
            />
        </div>
        <div className="mb-3">
            <label className="block mb-1">Language</label>
            <input
                type="text"
                name="language"
                value={form.language}
                onChange={handleChange}
                className="w-full border px-2 py-1 rounded"
                required
            />
        </div>
        <div className="mb-3">
            <label className="block mb-1">Scope</label>
            <input
                type="text"
                name="scope"
                value={form.scope}
                onChange={handleChange}
                className="w-full border px-2 py-1 rounded"
                required
            />
        </div>
        <div className="mb-3">
            <label className="block mb-1">Criticality</label>
            <select
                name="criticality"
                value={form.criticality}
                onChange={handleChange}
                className="w-full border px-2 py-1 rounded"
            >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
            </select>
        </div>
        <div className="mb-3">
            <label className="block mb-1">DM</label>
            <select
                name="dm"
                value={form.dm}
                onChange={handleChange}
                className="w-full border px-2 py-1 rounded"
                required
            >
                {[1, 2, 3, 4, 5].map(n => (
                    <option key={n} value={n}>{n}</option>
                ))}
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
            <select
                name="ta"
                value={form.ta}
                onChange={handleChange}
                className="w-full border px-2 py-1 rounded"
                required
            >
                {[1, 2, 3, 4, 5].map(n => (
                    <option key={n} value={n}>{n}</option>
                ))}
            </select>
        </div>
        <Button type="submit">Save</Button>
    </form>);
}
