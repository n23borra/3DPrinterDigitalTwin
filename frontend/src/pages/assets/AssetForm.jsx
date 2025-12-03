import React, {useState, useEffect} from 'react';
import api from '../../api/api';
import Button from '../../components/Button';
import Loader from '../../components/Loader';
import {useNavigate, useSearchParams} from 'react-router-dom';

/**
 * Form for creating a new asset while enforcing category permissions.
 * @returns {JSX.Element} Asset creation form that redirects back to the list on success.
 */
export default function AssetForm() {
    const [searchParams] = useSearchParams();
    const [analysisId, setAnalysisId] = useState(searchParams.get('analysisId') || '');
    const [form, setForm] = useState({
        name: '',
        description: '',
        categoryId: '',
        impactC: 0,
        impactI: 0,
        impactA: 0,
    });
    const [categories, setCategories] = useState([]);
    const [analyses, setAnalyses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        const load = async () => {
            try {
                const {data: me} = await api.get('/me');
                const {data: cats} = await api.get('/categories');
                const {data: admins} = await api.get('/category-admins');
                let catList = Array.isArray(cats) ? cats : [];
                if (me.role !== 'SUPER_ADMIN') {
                    const allowed = Array.isArray(admins) ? admins
                        .filter(a => Array.isArray(a.emails) && a.emails.includes(me.email))
                        .map(a => a.categoryId) : [];
                    catList = catList.filter(c => allowed.includes(c.id));
                }
                setCategories(catList);
                if (catList.length > 0) {
                    setForm(f => ({...f, categoryId: catList[0].id}));
                }
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
     * Updates the asset form state whenever a field value changes.
     * @param {React.ChangeEvent<HTMLInputElement|HTMLTextAreaElement|HTMLSelectElement>} e - Change event from a form control.
     * @returns {void}
     */
    const handleChange = (e) => {
        setForm({...form, [e.target.name]: e.target.value});
    };

    /**
     * Persists the new asset and navigates back to the asset list filtered by the selected analysis.
     * @param {React.FormEvent<HTMLFormElement>} e - Form submission event.
     * @returns {Promise<void>} Resolves after the API call finishes.
     */
    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post('/assets', {
                ...form,
                analysisId,
                impactC: Number(form.impactC),
                impactI: Number(form.impactI),
                impactA: Number(form.impactA),
            });
            navigate(`/assets?analysisId=${analysisId}`);
        } catch (err) {
            setError('Error saving asset');
        }
    };

    if (loading) return <Loader />;
    if (categories.length === 0) return <p className="text-red-600">Not authorized to create assets.</p>;

    return (
        <form onSubmit={handleSubmit} className="max-w-lg bg-white p-6 rounded shadow">
            <h2 className="text-xl font-semibold mb-4">New Asset</h2>
            {error && <p className="text-red-600 mb-2">{error}</p>}
            <div className="mb-3">
                <label className="block mb-1">Assessment</label>
                <select
                    value={analysisId}
                    onChange={e => setAnalysisId(e.target.value)}
                    className="w-full border px-2 py-1 rounded"
                    required
                >
                    <option value="">Select assessment</option>
                    {analyses.map(a => (
                        <option key={a.id} value={a.id}>{a.name}</option>
                    ))}
                </select>
            </div>
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
                <label className="block mb-1">Category</label>
                <select
                    name="categoryId"
                    value={form.categoryId}
                    onChange={handleChange}
                    className="w-full border px-2 py-1 rounded"
                    required
                >
                    <option value="">Select category</option>
                    {categories.map(c => (
                        <option key={c.id} value={c.id}>{c.label}</option>
                    ))}
                </select>
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
                <label className="block mb-1">Impact Confidentiality</label>
                <select
                    name="impactC"
                    value={form.impactC}
                    onChange={handleChange}
                    className="w-full border px-2 py-1 rounded"
                    required
                >
                    {[0, 1, 2, 3, 4].map(n => (
                        <option key={n} value={n}>{n}</option>
                    ))}
                </select>
            </div>
            <div className="mb-3">
                <label className="block mb-1">Impact Integrity</label>
                <select
                    name="impactI"
                    value={form.impactI}
                    onChange={handleChange}
                    className="w-full border px-2 py-1 rounded"
                    required
                >
                    {[0, 1, 2, 3, 4].map(n => (
                        <option key={n} value={n}>{n}</option>
                    ))}
                </select>
            </div>
            <div className="mb-4">
                <label className="block mb-1">Impact Availability</label>
                <select
                    name="impactA"
                    value={form.impactA}
                    onChange={handleChange}
                    className="w-full border px-2 py-1 rounded"
                    required
                >
                    {[0, 1, 2, 3, 4].map(n => (
                        <option key={n} value={n}>{n}</option>
                    ))}
                </select>
            </div>
            <Button type="submit">Save</Button>
        </form>
    );
}