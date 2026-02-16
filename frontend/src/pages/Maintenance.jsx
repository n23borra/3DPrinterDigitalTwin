import React, { useEffect, useMemo, useState } from 'react';
import { fetchPrinters } from '../api/printerApi';

const IMPORTANCE_OPTIONS = ['LOW', 'MEDIUM', 'HIGH'];
const STORAGE_KEY = 'maintenance-items-by-printer';

const emptyForm = {
    name: '',
    importance: 'MEDIUM',
    lifespanHours: '',
};

export default function Maintenance() {
    const [printers, setPrinters] = useState([]);
    const [selectedPrinterId, setSelectedPrinterId] = useState('');
    const [itemsByPrinter, setItemsByPrinter] = useState({});
    const [form, setForm] = useState(emptyForm);
    const [hoursToAddByItem, setHoursToAddByItem] = useState({});

    useEffect(() => {
        try {
            const cached = localStorage.getItem(STORAGE_KEY);
            if (cached) {
                const parsed = JSON.parse(cached);
                if (parsed && typeof parsed === 'object') {
                    setItemsByPrinter(parsed);
                }
            }
        } catch (error) {
            console.error('Unable to restore maintenance items from local storage:', error);
        }
    }, []);

    useEffect(() => {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(itemsByPrinter));
    }, [itemsByPrinter]);

    useEffect(() => {
        const loadPrinters = async () => {
            try {
                const response = await fetchPrinters();
                const availablePrinters = Array.isArray(response?.data) ? response.data : [];
                setPrinters(availablePrinters);

                if (availablePrinters.length > 0) {
                    setSelectedPrinterId(String(availablePrinters[0].id));
                }
            } catch (error) {
                console.error('Failed to load printers for maintenance:', error);
                setPrinters([]);
            }
        };

        loadPrinters();
    }, []);

    const selectedItems = useMemo(() => {
        if (!selectedPrinterId) return [];
        return itemsByPrinter[selectedPrinterId] || [];
    }, [itemsByPrinter, selectedPrinterId]);

    const handleCreateItem = (event) => {
        event.preventDefault();

        if (!selectedPrinterId) {
            alert('Veuillez sélectionner une imprimante.');
            return;
        }

        if (!form.name.trim()) {
            alert('Veuillez saisir un nom pour l\'objet.');
            return;
        }

        const lifespanHours = Number(form.lifespanHours);
        if (!Number.isFinite(lifespanHours) || lifespanHours <= 0) {
            alert('La durée de vie doit être un nombre d\'heures supérieur à 0.');
            return;
        }

        const newItem = {
            id: `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`,
            name: form.name.trim(),
            importance: form.importance,
            lifespanHours,
            usedHours: 0,
        };

        setItemsByPrinter((prev) => ({
            ...prev,
            [selectedPrinterId]: [...(prev[selectedPrinterId] || []), newItem],
        }));

        setForm(emptyForm);
    };

    const handleAddHours = (itemId) => {
        const rawValue = hoursToAddByItem[itemId] || '1';
        const hoursToAdd = Number(rawValue);

        if (!Number.isFinite(hoursToAdd) || hoursToAdd <= 0) {
            alert('Veuillez saisir un nombre d\'heures valide.');
            return;
        }

        setItemsByPrinter((prev) => ({
            ...prev,
            [selectedPrinterId]: (prev[selectedPrinterId] || []).map((item) => (
                item.id === itemId
                    ? { ...item, usedHours: Number((item.usedHours + hoursToAdd).toFixed(2)) }
                    : item
            )),
        }));
    };

    const handleDeleteItem = (itemId) => {
        setItemsByPrinter((prev) => ({
            ...prev,
            [selectedPrinterId]: (prev[selectedPrinterId] || []).filter((item) => item.id !== itemId),
        }));
    };

    return (
        <div className="space-y-6">
            <header>
                <h2 className="text-2xl font-semibold text-gray-800">Maintenance</h2>
                <p className="text-gray-500">Créez des objets de maintenance et suivez leur durée de vie par imprimante.</p>
            </header>

            <section className="bg-white border border-gray-200 rounded-xl p-4 shadow-sm">
                <label className="block text-sm font-medium text-gray-700 mb-2" htmlFor="printer-select">
                    Imprimante
                </label>
                <select
                    id="printer-select"
                    value={selectedPrinterId}
                    onChange={(event) => setSelectedPrinterId(event.target.value)}
                    className="w-full md:w-96 border border-gray-300 rounded px-3 py-2"
                >
                    {printers.length === 0 && <option value="">Aucune imprimante disponible</option>}
                    {printers.map((printer) => (
                        <option key={printer.id} value={String(printer.id)}>
                            {printer.name || `Imprimante ${printer.id}`}
                        </option>
                    ))}
                </select>
            </section>

            <section className="bg-white border border-gray-200 rounded-xl p-4 shadow-sm">
                <h3 className="text-lg font-semibold text-gray-800 mb-4">Ajouter un objet</h3>
                <form className="grid grid-cols-1 md:grid-cols-4 gap-3" onSubmit={handleCreateItem}>
                    <input
                        type="text"
                        placeholder="Nom de l'objet"
                        value={form.name}
                        onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))}
                        className="border border-gray-300 rounded px-3 py-2"
                    />
                    <select
                        value={form.importance}
                        onChange={(event) => setForm((prev) => ({ ...prev, importance: event.target.value }))}
                        className="border border-gray-300 rounded px-3 py-2"
                    >
                        {IMPORTANCE_OPTIONS.map((option) => (
                            <option key={option} value={option}>{option.toLowerCase()}</option>
                        ))}
                    </select>
                    <input
                        type="number"
                        min="1"
                        step="1"
                        placeholder="Durée de vie (heures)"
                        value={form.lifespanHours}
                        onChange={(event) => setForm((prev) => ({ ...prev, lifespanHours: event.target.value }))}
                        className="border border-gray-300 rounded px-3 py-2"
                    />
                    <button
                        type="submit"
                        disabled={!selectedPrinterId}
                        className="bg-blue-600 text-white rounded px-3 py-2 hover:bg-blue-700 disabled:bg-blue-300"
                    >
                        Créer l'objet
                    </button>
                </form>
            </section>

            <section className="bg-white border border-gray-200 rounded-xl p-4 shadow-sm">
                <h3 className="text-lg font-semibold text-gray-800 mb-4">Objets de maintenance</h3>

                {selectedItems.length === 0 ? (
                    <p className="text-gray-500">Aucun objet de maintenance pour cette imprimante.</p>
                ) : (
                    <div className="space-y-3">
                        {selectedItems.map((item) => {
                            const progress = Math.min(100, Math.round((item.usedHours / item.lifespanHours) * 100));
                            const hasReachedLimit = item.usedHours >= item.lifespanHours;

                            return (
                                <div key={item.id} className="border border-gray-200 rounded-lg p-3">
                                    <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-2">
                                        <div>
                                            <div className="flex items-center gap-2 flex-wrap">
                                                <span className="font-semibold text-gray-800">{item.name}</span>
                                                <span className="text-xs bg-gray-100 text-gray-700 px-2 py-1 rounded-full">
                                                    importance: {item.importance.toLowerCase()}
                                                </span>
                                                {hasReachedLimit && (
                                                    <span className="text-xs bg-red-100 text-red-700 px-2 py-1 rounded-full font-medium">
                                                        ⚠️ Durée de vie atteinte
                                                    </span>
                                                )}
                                            </div>
                                            <p className="text-sm text-gray-600 mt-1">
                                                {item.usedHours}h / {item.lifespanHours}h
                                            </p>
                                        </div>

                                        <div className="flex items-center gap-2 flex-wrap">
                                            <input
                                                type="number"
                                                min="0.1"
                                                step="0.1"
                                                value={hoursToAddByItem[item.id] || '1'}
                                                onChange={(event) => {
                                                    const value = event.target.value;
                                                    setHoursToAddByItem((prev) => ({ ...prev, [item.id]: value }));
                                                }}
                                                className="w-28 border border-gray-300 rounded px-2 py-1"
                                            />
                                            <button
                                                type="button"
                                                onClick={() => handleAddHours(item.id)}
                                                className="bg-emerald-600 text-white rounded px-3 py-1.5 hover:bg-emerald-700"
                                            >
                                                Ajouter des heures
                                            </button>
                                            <button
                                                type="button"
                                                onClick={() => handleDeleteItem(item.id)}
                                                className="bg-gray-100 text-gray-700 rounded px-3 py-1.5 hover:bg-gray-200"
                                            >
                                                Supprimer
                                            </button>
                                        </div>
                                    </div>

                                    <div className="w-full h-2 bg-gray-100 rounded mt-3 overflow-hidden">
                                        <div
                                            className={`h-2 ${hasReachedLimit ? 'bg-red-500' : 'bg-blue-500'}`}
                                            style={{ width: `${progress}%` }}
                                        />
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </section>
        </div>
    );
}