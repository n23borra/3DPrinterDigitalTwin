import React, {useEffect, useMemo, useState} from 'react';
import {fetchPrinters, fetchPrinterHistory, fetchPrinterState, sendPrinterCommand} from '../api/printerApi';
import PrinterCard from '../components/printers/PrinterCard';
import PrinterHistoryTable from '../components/printers/PrinterHistoryTable';

const QUICK_COMMANDS = [
    {label: 'Home axes', command: 'G28'},
    {label: 'Pause', command: 'PAUSE'},
    {label: 'Resume', command: 'RESUME'},
    {label: 'Lower bed', command: 'G1 Z200'},
];

export default function PrintersDashboard() {
    const [printers, setPrinters] = useState([]);
    const [snapshots, setSnapshots] = useState({});
    const [history, setHistory] = useState([]);
    const [selectedId, setSelectedId] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        fetchPrinters().then((res) => {
            setPrinters(res.data);
            if (res.data.length > 0) {
                setSelectedId(res.data[0].id);
            }
        });
    }, []);

    useEffect(() => {
        if (!selectedId) return;
        setLoading(true);
        Promise.all([
            fetchPrinterState(selectedId),
            fetchPrinterHistory(selectedId, {limit: 50}),
        ]).then(([stateRes, historyRes]) => {
            setSnapshots((prev) => ({...prev, [selectedId]: stateRes.data}));
            setHistory(historyRes.data);
        }).finally(() => setLoading(false));
    }, [selectedId]);

    const selectedPrinter = useMemo(
        () => printers.find((p) => p.id === selectedId),
        [printers, selectedId],
    );

    const handleCommand = async (command) => {
        if (!selectedId) return;
        await sendPrinterCommand(selectedId, command);
        const state = await fetchPrinterState(selectedId);
        setSnapshots((prev) => ({...prev, [selectedId]: state.data}));
    };

    return (
        <div>
            <header className="mb-6">
                <h2 className="text-2xl font-semibold text-gray-800">Printers</h2>
                <p className="text-gray-500">Monitor temperatures, progress and push basic commands.</p>
            </header>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {printers.map((printer) => (
                    <PrinterCard
                        key={printer.id}
                        printer={printer}
                        snapshot={snapshots[printer.id]}
                        onSelect={setSelectedId}
                        isActive={printer.id === selectedId}
                    />
                ))}
            </div>

            {selectedPrinter && (
                <section className="mt-8 bg-white rounded-lg shadow p-5">
                    <div className="flex items-center justify-between">
                        <div>
                            <h3 className="text-xl font-semibold text-gray-800">{selectedPrinter.name}</h3>
                            <p className="text-gray-500">{selectedPrinter.type} • {selectedPrinter.ipAddress}:{selectedPrinter.port}</p>
                        </div>
                        {loading && <span className="text-sm text-blue-600">Refreshing…</span>}
                    </div>

                    <div className="flex flex-wrap gap-3 mt-4">
                        {QUICK_COMMANDS.map((action) => (
                            <button
                                key={action.command}
                                onClick={() => handleCommand(action.command)}
                                className="px-3 py-2 rounded bg-blue-600 text-white text-sm hover:bg-blue-700"
                            >
                                {action.label}
                            </button>
                        ))}
                    </div>

                    <PrinterHistoryTable history={history}/>
                </section>
            )}
        </div>
    );
}