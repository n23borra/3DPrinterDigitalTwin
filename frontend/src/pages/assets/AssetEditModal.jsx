import React, {useEffect, useState} from 'react';
import api from '../../api/api';
import Button from '../../components/Button';
import Loader from '../../components/Loader';
import Modal from '../../components/Modal';

/**
 * Modal dialog for editing an existing asset with role-aware category filtering.
 * @param {object} props - Component props.
 * @param {object} props.asset - Asset currently being edited.
 * @param {() => void} props.onClose - Callback invoked to close the modal without saving.
 * @param {() => void} props.onSaved - Callback triggered after a successful update.
 * @returns {JSX.Element} Modal containing the asset edit form.
 */
export default function AssetEditModal({asset, onClose, onSaved}) {
    const [form, setForm] = useState({
        analysisId: asset.analysisId,
        categoryId: asset.categoryId,
        name: asset.name,
        description: asset.description,
        impactC: asset.impactC,
        impactI: asset.impactI,
        impactA: asset.impactA,
    });
    const [categories, setCategories] = useState([]);
    const [analyses, setAnalyses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const load = async () => {
            try {
                const {data: me} = await api.get('/me');
                const {data: cats} = await api.get('/categories');
                const {data: admins} = await api.get('/category-admins');
                let catList = Array.isArray(cats) ? cats : [];
                if (me.role !== 'SUPER_ADMIN') {
                    const allowed = Array.isArray(admins)
                        ? admins.filter(a => Array.isArray(a.emails) && a.emails.includes(me.email))
                            .map(a => a.categoryId)
                        : [];
                    catList = catList.filter(c => allowed.includes(c.id));
                }
                setCategories(catList);
                const {data: anns} = await api.get('/analyses');
                setAnalyses(Array.isArray(anns) ? anns : []);
            } catch {
                setCategories([]);
                setAnalyses([]);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, []);

    /**
     * Synchronises local form state with user modifications.
     * @param {React.ChangeEvent<HTMLInputElement|HTMLTextAreaElement|HTMLSelectElement>} e - Change event from a form field.
     * @returns {void}
     */
    const handleChange = e => {
        setForm({...form, [e.target.name]: e.target.value});
    };

    /**
     * Submits the updated asset payload to the API and triggers the saved callback.
     * @param {React.FormEvent<HTMLFormElement>} e - Form submission event.
     * @returns {Promise<void>} Resolves after the asset has been persisted.
     */
    const handleSubmit = async e => {
        e.preventDefault();
        try {
            await api.put(`/assets/${asset.id}`, {
                ...form,
                impactC: Number(form.impactC),
                impactI: Number(form.impactI),
                impactA: Number(form.impactA)
            });
            if (onSaved) onSaved();
        } catch (err) {
            setError('Error saving asset');
        }
    };

    return (
        <Modal open={true} onClose={onClose} className="w-full max-w-4xl">
            {loading ? <Loader/> : (
                <form onSubmit={handleSubmit} className="p-4">{
                    error && <p className="text-red-600 mb-2">{error}</p>}
                    <div className="mb-3">
                        <label className="block mb-1">Assessment</label>
                        <select name="analysisId" value={form.analysisId} onChange={handleChange}
                                className="w-full border px-2 py-1 rounded" required>
                            <option value="">Select assessment</option>
                            {analyses.map(a => <option key={a.id} value={a.id}>{a.name}</option>)}
                        </select>
                    </div>
                    <div className="mb-3">
                        <label className="block mb-1">Name</label>
                        <input type="text" name="name" value={form.name} onChange={handleChange}
                               className="w-full border px-2 py-1 rounded" required/>
                    </div>
                    <div className="mb-3">
                        <label className="block mb-1">Category</label>
                        <select name="categoryId" value={form.categoryId} onChange={handleChange}
                                className="w-full border px-2 py-1 rounded" required>
                            <option value="">Select category</option>
                            {categories.map(c => <option key={c.id} value={c.id}>{c.label}</option>)}
                        </select>
                    </div>
                    <div className="mb-3">
                        <label className="block mb-1">Description</label>
                        <textarea name="description" value={form.description} onChange={handleChange}
                                  className="w-full border px-2 py-1 rounded" required/>
                    </div>
                    <div className="mb-3">
                        <label className="block mb-1">Impact Confidentiality</label>
                        <select name="impactC" value={form.impactC} onChange={handleChange}
                                className="w-full border px-2 py-1 rounded" required>
                            {[0,1,2,3,4].map(n => <option key={n} value={n}>{n}</option>)}
                        </select>
                    </div>
                    <div className="mb-3">
                        <label className="block mb-1">Impact Integrity</label>
                        <select name="impactI" value={form.impactI} onChange={handleChange}
                                className="w-full border px-2 py-1 rounded" required>
                            {[0,1,2,3,4].map(n => <option key={n} value={n}>{n}</option>)}
                        </select>
                    </div>
                    <div className="mb-4">
                        <label className="block mb-1">Impact Availability</label>
                        <select name="impactA" value={form.impactA} onChange={handleChange}
                                className="w-full border px-2 py-1 rounded" required>
                            {[0,1,2,3,4].map(n => <option key={n} value={n}>{n}</option>)}
                        </select>
                    </div>
                    <div className="text-right">
                        <Button type="submit" className="mr-2">Save</Button>
                        <Button type="button" onClick={onClose}>Cancel</Button>
                    </div>
                </form>) }
        </Modal>
    );
}