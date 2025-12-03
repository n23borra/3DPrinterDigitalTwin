import React, {useEffect, useState} from 'react';
import api from '../../api/api';
import Loader from '../../components/Loader';
import Button from '../../components/Button';

/**
 * Manages asset categories and allows administrators to create new entries.
 * @returns {JSX.Element} Category listing with an inline creation form.
 */
export default function CategoryList() {
    const [categories, setCategories] = useState([]);
    const [label, setLabel] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        api.get('/categories')
            .then(res => setCategories(res.data))
            .catch(() => {})
            .finally(() => setLoading(false));
    }, []);

    /**
     * Creates a new category and refreshes the displayed list.
     * @param {React.FormEvent<HTMLFormElement>} e - Form submission event.
     * @returns {Promise<void>} Resolves after the category is created and reloaded.
     */
    const handleSubmit = async (e) => {
        e.preventDefault();
        await api.post('/categories', {label});
        const {data} = await api.get('/categories');
        setCategories(data);
        setLabel('');
    };

    if (loading) return <Loader />;

    return (
        <div>
            <h2 className="text-2xl font-semibold mb-4">Categories</h2>
            <form onSubmit={handleSubmit} className="flex mb-4 space-x-2">
                <input
                    type="text"
                    value={label}
                    onChange={e => setLabel(e.target.value)}
                    className="border px-2 py-1 rounded flex-1"
                    placeholder="New category"
                />
                <Button type="submit">Add</Button>
            </form>
            <ul className="bg-white rounded shadow divide-y">
                {categories.map(c => (
                    <li key={c.id} className="p-2">{c.label}</li>
                ))}
            </ul>
        </div>
    );
}