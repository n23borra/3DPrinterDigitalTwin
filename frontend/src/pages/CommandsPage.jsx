import React, { useEffect, useMemo, useState } from 'react';
import { fetchPrinters } from '../api/printerApi';
import { executeCommand, getAvailableCommands } from '../api/commandsApi';
import Modal from '../components/Modal';

const GROUP_ORDER = ['Mouvement', 'Filament', 'Sécurité', 'Impression', 'Calibration'];

export default function CommandsPage() {
    const [printers, setPrinters] = useState([]);
    const [selectedPrinterId, setSelectedPrinterId] = useState('');
    const [commands, setCommands] = useState([]);
    const [loading, setLoading] = useState(false);
    const [notice, setNotice] = useState(null);
    const [confirmCommand, setConfirmCommand] = useState(null);

    useEffect(() => {
        const loadPrinters = async () => {
            try {
                const response = await fetchPrinters();
                const printerList = response?.data || [];
                setPrinters(printerList);
                if (printerList.length > 0) {
                    setSelectedPrinterId(printerList[0].id);
                }
            } catch {
                setPrinters([]);
            }
        };

        loadPrinters();
    }, []);

    useEffect(() => {
        if (!selectedPrinterId) {
            setCommands([]);
            return;
        }

        const loadCommands = async () => {
            setLoading(true);
            try {
                const response = await getAvailableCommands(selectedPrinterId);
                setCommands(response?.data || []);
            } catch {
                setCommands([]);
            } finally {
                setLoading(false);
            }
        };

        loadCommands();
    }, [selectedPrinterId]);

    const groupedCommands = useMemo(() => {
        const groups = new Map();
        GROUP_ORDER.forEach((group) => groups.set(group, []));

        commands.forEach((command) => {
            if (!groups.has(command.group)) {
                groups.set(command.group, []);
            }
            groups.get(command.group).push(command);
        });

        return Array.from(groups.entries()).filter(([, items]) => items.length > 0);
    }, [commands]);

    const execute = async (command) => {
        if (!selectedPrinterId) return;
        try {
            await executeCommand(selectedPrinterId, command.commandKey);
            setNotice({ type: 'success', message: `Commande « ${command.label} » envoyée.` });
        } catch (error) {
            const message = error?.response?.data?.message || 'Échec de l’envoi de la commande.';
            setNotice({ type: 'error', message });
        }
    };

    const handleClick = async (command) => {
        if (command.dangerous) {
            setConfirmCommand(command);
            return;
        }
        await execute(command);
    };

    return (
        <div className="space-y-6">
            <div>
                <h2 className="text-2xl font-semibold text-gray-800">Commands</h2>
                <p className="text-gray-500">Exécutez des commandes sécurisées vers l’imprimante via Moonraker.</p>
            </div>

            {notice && (
                <div className={`rounded-md px-4 py-3 text-sm ${notice.type === 'success' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                    {notice.message}
                </div>
            )}

            <div className="bg-white rounded-lg shadow p-4">
                <label className="block text-sm font-medium text-gray-700 mb-2" htmlFor="printer-selector">
                    Imprimante
                </label>
                <select
                    id="printer-selector"
                    className="w-full border border-gray-300 rounded-md p-2"
                    value={selectedPrinterId}
                    onChange={(event) => setSelectedPrinterId(event.target.value)}
                >
                    {printers.map((printer) => (
                        <option key={printer.id} value={printer.id}>{printer.name}</option>
                    ))}
                </select>
            </div>

            {loading ? (
                <div className="text-sm text-gray-500">Chargement des commandes...</div>
            ) : (
                <div className="space-y-6">
                    {groupedCommands.map(([group, items]) => (
                        <section key={group} className="bg-white rounded-lg shadow p-5">
                            <h3 className="text-lg font-semibold text-gray-800 mb-3">{group}</h3>
                            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                                {items.map((command) => (
                                    <button
                                        key={command.commandKey}
                                        type="button"
                                        className={`rounded-md border px-4 py-3 text-left transition ${command.dangerous ? 'border-red-300 hover:bg-red-50' : 'border-gray-200 hover:bg-gray-50'}`}
                                        onClick={() => handleClick(command)}
                                    >
                                        <div className="font-medium text-gray-900">{command.label}</div>
                                        <div className="text-xs text-gray-500 mt-1">{command.commandKey}</div>
                                    </button>
                                ))}
                            </div>
                        </section>
                    ))}
                </div>
            )}

            <Modal open={Boolean(confirmCommand)} onClose={() => setConfirmCommand(null)} className="w-full max-w-md">
                <h4 className="text-lg font-semibold mb-2">Confirmer la commande</h4>
                <p className="text-sm text-gray-600 mb-4">
                    Voulez-vous vraiment envoyer « {confirmCommand?.label} » ?
                </p>
                <div className="flex justify-end gap-2">
                    <button className="px-3 py-2 border rounded" type="button" onClick={() => setConfirmCommand(null)}>
                        Annuler
                    </button>
                    <button
                        className="px-3 py-2 bg-red-600 text-white rounded"
                        type="button"
                        onClick={async () => {
                            if (confirmCommand) {
                                await execute(confirmCommand);
                            }
                            setConfirmCommand(null);
                        }}
                    >
                        Confirmer
                    </button>
                </div>
            </Modal>
        </div>
    );
}