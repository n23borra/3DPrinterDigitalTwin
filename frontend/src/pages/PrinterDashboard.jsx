import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
    createPrinter,
    fetchPrinters,
    fetchPrinterHistory,
    fetchPrinterState,
    sendPrinterCommand,
} from '../api/printerApi';
import PrinterCard from '../components/printers/PrinterCard';
import PrinterHistoryTable from '../components/printers/PrinterHistoryTable';
import Modal from '../components/Modal';

const QUICK_COMMANDS = [
    { label: 'Home axes', command: 'G28' },
    { label: 'Pause', command: 'PAUSE' },
    { label: 'Resume', command: 'RESUME' },
    { label: 'Lower bed', command: 'G1 Z200' },
];

export default function PrintersDashboard() {
    const [printers, setPrinters] = useState([]);
    const [snapshots, setSnapshots] = useState({});
    const [history, setHistory] = useState([]);
    const [selectedId, setSelectedId] = useState(null);
    const [loading, setLoading] = useState(false);
    const [autoRefresh, setAutoRefresh] = useState(true);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isCreatingPrinter, setIsCreatingPrinter] = useState(false);
    const [createError, setCreateError] = useState('');
    const [newPrinter, setNewPrinter] = useState({
        name: '',
        type: 'MOONRAKER',
        ipAddress: '',
        port: '7125',
        apiKey: '',
    });

    const loadPrinters = useCallback(async (preferredPrinterId = null) => {
        try {
            const res = await fetchPrinters();
            const printerList = res.data || [];
            setPrinters(printerList);

            if (printerList.length === 0) {
                setSelectedId(null);
                return;
            }

            if (preferredPrinterId && printerList.some((printer) => printer.id === preferredPrinterId)) {
                setSelectedId(preferredPrinterId);
                return;
            }

            if (selectedId && printerList.some((printer) => printer.id === selectedId)) {
                return;
            }

            setSelectedId(printerList[0].id);
        } catch (error) {
            console.error('Error loading printers:', error);
            setPrinters([]);
        }
    }, [selectedId]);

    // Fetch printers list on mount
    useEffect(() => {
        loadPrinters();
    }, [loadPrinters]);

    // Fetch state and history for selected printer
    useEffect(() => {
        if (!selectedId) return;

        const fetchData = async () => {
            setLoading(true);
            try {
                const [stateRes, historyRes] = await Promise.all([
                    fetchPrinterState(selectedId),
                    fetchPrinterHistory(selectedId, { limit: 50 }),
                ]);
                setSnapshots((prev) => ({ ...prev, [selectedId]: stateRes.data }));
                setHistory(historyRes.data);
            } catch (error) {
                console.error('Failed to fetch printer data:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();

        // Auto-refresh every 2 seconds if enabled
        if (autoRefresh) {
            const interval = setInterval(fetchData, 2000);
            return () => clearInterval(interval);
        }
    }, [selectedId, autoRefresh]);

    const selectedPrinter = useMemo(
        () => printers.find((p) => p.id === selectedId),
        [printers, selectedId],
    );

    const selectedSnapshot = snapshots[selectedId];

    const handleCommand = async (command) => {
        if (!selectedId) return;
        await sendPrinterCommand(selectedId, command);
        const state = await fetchPrinterState(selectedId);
        setSnapshots((prev) => ({ ...prev, [selectedId]: state.data }));
    };

    const hasData = selectedSnapshot && (
        selectedSnapshot.nozzleTemp !== null ||
        selectedSnapshot.bedTemp !== null ||
        selectedSnapshot.state !== null
    );

    const handleCreateInputChange = (event) => {
        const { name, value } = event.target;
        setNewPrinter((prev) => ({ ...prev, [name]: value }));
    };

    const resetCreateForm = () => {
        setNewPrinter({
            name: '',
            type: 'MOONRAKER',
            ipAddress: '',
            port: '7125',
            apiKey: '',
        });
        setCreateError('');
    };

    const handleCreatePrinter = async (event) => {
        event.preventDefault();
        setIsCreatingPrinter(true);
        setCreateError('');

        try {
            const payload = {
                ...newPrinter,
                port: newPrinter.port ? Number(newPrinter.port) : null,
            };

            const response = await createPrinter(payload);
            const createdPrinterId = response?.data?.id;

            await loadPrinters(createdPrinterId);
            setIsCreateModalOpen(false);
            resetCreateForm();
        } catch (error) {
            setCreateError(error.message || 'Unable to create printer.');
        } finally {
            setIsCreatingPrinter(false);
        }
    };

    return (
        <div>
            <header className="mb-6">
                <div className="flex justify-between items-center">
                    <div>
                        <h2 className="text-2xl font-semibold text-gray-800">Printers</h2>
                        <p className="text-gray-500">Monitor temperatures, progress and push basic commands.</p>
                    </div>
                    <div className="flex items-center gap-2">
                        <button
                            onClick={() => setIsCreateModalOpen(true)}
                            className="px-4 py-2 rounded-lg text-sm font-medium bg-blue-600 text-white hover:bg-blue-700"
                        >
                            + Add printer
                        </button>
                        <button
                            onClick={() => setAutoRefresh(!autoRefresh)}
                            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                                autoRefresh
                                    ? 'bg-green-100 text-green-700 hover:bg-green-200'
                                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                            }`}
                        >
                            {autoRefresh ? '🔄 Auto-refresh ON' : '⏸️ Auto-refresh OFF'}
                        </button>
                    </div>
                </div>
            </header>

            <Modal open={isCreateModalOpen} onClose={() => setIsCreateModalOpen(false)} className="w-full max-w-lg">
                <h3 className="text-xl font-semibold text-gray-800 mb-4">Create a new printer</h3>
                <form className="space-y-4" onSubmit={handleCreatePrinter}>
                    <div>
                        <label className="block text-sm text-gray-700 mb-1" htmlFor="name">Name</label>
                        <input
                            id="name"
                            name="name"
                            value={newPrinter.name}
                            onChange={handleCreateInputChange}
                            required
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        />
                    </div>

                    <div>
                        <label className="block text-sm text-gray-700 mb-1" htmlFor="type">Type</label>
                        <select
                            id="type"
                            name="type"
                            value={newPrinter.type}
                            onChange={handleCreateInputChange}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        >
                            <option value="MOONRAKER">MOONRAKER</option>
                        </select>
                    </div>

                    <div>
                        <label className="block text-sm text-gray-700 mb-1" htmlFor="ipAddress">IP address</label>
                        <input
                            id="ipAddress"
                            name="ipAddress"
                            value={newPrinter.ipAddress}
                            onChange={handleCreateInputChange}
                            required
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        />
                    </div>

                    <div>
                        <label className="block text-sm text-gray-700 mb-1" htmlFor="port">Port</label>
                        <input
                            id="port"
                            name="port"
                            type="number"
                            min="1"
                            max="65535"
                            value={newPrinter.port}
                            onChange={handleCreateInputChange}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        />
                    </div>

                    <div>
                        <label className="block text-sm text-gray-700 mb-1" htmlFor="apiKey">API key (optional)</label>
                        <input
                            id="apiKey"
                            name="apiKey"
                            value={newPrinter.apiKey}
                            onChange={handleCreateInputChange}
                            className="w-full border border-gray-300 rounded px-3 py-2"
                        />
                    </div>

                    {createError && (
                        <p className="text-sm text-red-600">{createError}</p>
                    )}

                    <div className="flex justify-end gap-2 pt-2">
                        <button
                            type="button"
                            onClick={() => {
                                setIsCreateModalOpen(false);
                                resetCreateForm();
                            }}
                            className="px-4 py-2 rounded bg-gray-100 text-gray-700 hover:bg-gray-200"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={isCreatingPrinter}
                            className="px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700 disabled:bg-blue-300"
                        >
                            {isCreatingPrinter ? 'Creating...' : 'Create printer'}
                        </button>
                    </div>
                </form>
            </Modal>

            {/* Printer Cards Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
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

            {/* Selected Printer Details */}
            {selectedPrinter && (
                <>
                    {/* Header Section */}
                    <section className="bg-white rounded-lg shadow p-5 mb-6">
                        <div className="flex items-center justify-between">
                            <div>
                                <h3 className="text-xl font-semibold text-gray-800">{selectedPrinter.name}</h3>
                                <p className="text-gray-500">
                                    {selectedPrinter.type} • {selectedPrinter.ipAddress}:{selectedPrinter.port}
                                </p>
                            </div>
                            <div className="flex items-center gap-3">
                                {loading && <span className="text-sm text-blue-600">Refreshing…</span>}
                                <div className={`px-3 py-1 rounded-full text-sm font-semibold ${
                                    hasData ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                                }`}>
                                    {hasData ? '🟢 Connected' : '🔴 Offline'}
                                </div>
                            </div>
                        </div>

                        {/* Quick Commands */}
                        <div className="flex flex-wrap gap-3 mt-4">
                            {QUICK_COMMANDS.map((action) => (
                                <button
                                    key={action.command}
                                    onClick={() => handleCommand(action.command)}
                                    className="px-3 py-2 rounded bg-blue-600 text-white text-sm hover:bg-blue-700 disabled:bg-gray-400"
                                    disabled={!hasData}
                                >
                                    {action.label}
                                </button>
                            ))}
                        </div>
                    </section>

                    {/* Comprehensive Data Display */}
                    {!hasData ? (
                        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-6 mb-6">
                            <h3 className="text-lg font-semibold text-yellow-800 mb-2">
                                No Data Available
                            </h3>
                            <p className="text-yellow-700">
                                The printer is not responding. Make sure the printer is powered on and connected to the network.
                            </p>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-6">
                            {/* Temperatures */}
                            <div className="bg-white rounded-lg shadow p-5">
                                <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center">
                                    🌡️ Temperatures
                                </h4>
                                <div className="space-y-4">
                                    <div>
                                        <div className="mb-1">
                                            <span className="text-sm text-gray-600">Nozzle</span>
                                        </div>
                                        <div className="text-2xl font-bold text-orange-600">
                                            {selectedSnapshot.nozzleTemp?.toFixed(1) || '--'}°C
                                        </div>
                                        <div className="w-full bg-gray-200 rounded-full h-2 mt-2">
                                            <div
                                                className="bg-orange-500 h-2 rounded-full transition-all"
                                                style={{
                                                    width: `${Math.min(100, (selectedSnapshot.nozzleTemp / 300) * 100)}%`
                                                }}
                                            />
                                        </div>
                                    </div>

                                    <div>
                                        <div className="mb-1">
                                            <span className="text-sm text-gray-600">Bed</span>
                                        </div>
                                        <div className="text-2xl font-bold text-red-600">
                                            {selectedSnapshot.bedTemp?.toFixed(1) || '--'}°C
                                        </div>
                                        <div className="w-full bg-gray-200 rounded-full h-2 mt-2">
                                            <div
                                                className="bg-red-500 h-2 rounded-full transition-all"
                                                style={{
                                                    width: `${Math.min(100, (selectedSnapshot.bedTemp / 120) * 100)}%`
                                                }}
                                            />
                                        </div>
                                    </div>

                                    {selectedSnapshot.chamberTemp !== null && (
                                        <div>
                                            <span className="text-sm text-gray-600">Chamber</span>
                                            <div className="text-xl font-bold text-blue-600">
                                                {selectedSnapshot.chamberTemp.toFixed(1)}°C
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>

                            {/* Position */}
                            <div className="bg-white rounded-lg shadow p-5">
                                <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center">
                                    📍 Position
                                </h4>
                                <div className="space-y-3">
                                    <div className="flex justify-between items-center">
                                        <span className="text-gray-600">X:</span>
                                        <span className="text-xl font-mono font-bold text-blue-600">
                                            {selectedSnapshot.posX?.toFixed(2) || '--'} mm
                                        </span>
                                    </div>
                                    <div className="flex justify-between items-center">
                                        <span className="text-gray-600">Y:</span>
                                        <span className="text-xl font-mono font-bold text-green-600">
                                            {selectedSnapshot.posY?.toFixed(2) || '--'} mm
                                        </span>
                                    </div>
                                    <div className="flex justify-between items-center">
                                        <span className="text-gray-600">Z:</span>
                                        <span className="text-xl font-mono font-bold text-purple-600">
                                            {selectedSnapshot.posZ?.toFixed(2) || '--'} mm
                                        </span>
                                    </div>
                                    {selectedSnapshot.homedAxes && (
                                        <div className="mt-4 pt-4 border-t border-gray-200">
                                            <span className="text-sm text-gray-600">Homed:</span>
                                            <span className="ml-2 text-sm font-semibold text-gray-900">
                                                {selectedSnapshot.homedAxes.toUpperCase()}
                                            </span>
                                        </div>
                                    )}
                                </div>
                            </div>

                            {/* Print Status */}
                            <div className="bg-white rounded-lg shadow p-5">
                                <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center">
                                    🖨️ Print Status
                                </h4>
                                <div className="space-y-3">
                                    <div>
                                        <span className="text-sm text-gray-600">State</span>
                                        <div className="text-lg font-bold text-gray-900 capitalize">
                                            {selectedSnapshot.state || 'Unknown'}
                                        </div>
                                    </div>

                                    {selectedSnapshot.filename && (
                                        <div>
                                            <span className="text-sm text-gray-600">File</span>
                                            <div className="text-sm font-medium text-gray-900 truncate">
                                                {selectedSnapshot.filename}
                                            </div>
                                        </div>
                                    )}

                                    {selectedSnapshot.progress !== null && (
                                        <div>
                                            <div className="flex justify-between items-baseline mb-1">
                                                <span className="text-sm text-gray-600">Progress</span>
                                                <span className="text-sm font-semibold text-gray-900">
                                                    {selectedSnapshot.progress.toFixed(1)}%
                                                </span>
                                            </div>
                                            <div className="w-full bg-gray-200 rounded-full h-2">
                                                <div
                                                    className="bg-blue-500 h-2 rounded-full transition-all"
                                                    style={{ width: `${selectedSnapshot.progress}%` }}
                                                />
                                            </div>
                                        </div>
                                    )}

                                    {selectedSnapshot.currentLayer !== null && (
                                        <div className="text-sm">
                                            <span className="text-gray-600">Layer: </span>
                                            <span className="font-semibold">
                                                {selectedSnapshot.currentLayer}
                                                {selectedSnapshot.totalLayers && ` / ${selectedSnapshot.totalLayers}`}
                                            </span>
                                        </div>
                                    )}
                                </div>
                            </div>

                            {/* Motion (if available) */}
                            {(selectedSnapshot.liveVelocity !== null || selectedSnapshot.maxVelocity !== null) && (
                                <div className="bg-white rounded-lg shadow p-5">
                                    <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center">
                                        ⚡ Motion
                                    </h4>
                                    <div className="space-y-3">
                                        {selectedSnapshot.liveVelocity !== null && (
                                            <div>
                                                <span className="text-sm text-gray-600">Speed</span>
                                                <div className="text-xl font-bold text-indigo-600">
                                                    {selectedSnapshot.liveVelocity.toFixed(1)} mm/s
                                                </div>
                                            </div>
                                        )}
                                        {selectedSnapshot.maxVelocity !== null && (
                                            <div>
                                                <span className="text-sm text-gray-600">Max Velocity</span>
                                                <div className="text-base font-semibold text-gray-700">
                                                    {selectedSnapshot.maxVelocity.toFixed(0)} mm/s
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            )}

                            {/* Fans & Sensors (if available) */}
                            {(selectedSnapshot.partFanSpeed !== null || selectedSnapshot.filamentDetected !== null) && (
                                <div className="bg-white rounded-lg shadow p-5">
                                    <h4 className="text-lg font-semibold text-gray-800 mb-4 flex items-center">
                                        💨 Fans & Sensors
                                    </h4>
                                    <div className="space-y-3">
                                        {selectedSnapshot.partFanSpeed !== null && (
                                            <div>
                                                <span className="text-sm text-gray-600">Part Fan</span>
                                                <div className="text-xl font-bold text-cyan-600">
                                                    {(selectedSnapshot.partFanSpeed * 100).toFixed(0)}%
                                                </div>
                                            </div>
                                        )}
                                        {selectedSnapshot.filamentDetected !== null && (
                                            <div>
                                                <span className="text-sm text-gray-600">Filament</span>
                                                <div className={`text-base font-semibold ${
                                                    selectedSnapshot.filamentDetected ? 'text-green-600' : 'text-red-600'
                                                }`}>
                                                    {selectedSnapshot.filamentDetected ? '✓ Detected' : '✗ Not Detected'}
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            )}
                        </div>
                    )}

                    {/* History Table */}
                    <PrinterHistoryTable history={history} />

                    {/* Raw Data Debug */}
                    {selectedSnapshot && (
                        <details className="mt-6 bg-white rounded-lg shadow p-5">
                            <summary className="cursor-pointer text-sm font-semibold text-gray-700">
                                🔍 Show Raw Data (Debug)
                            </summary>
                            <pre className="mt-4 p-4 bg-gray-50 rounded text-xs overflow-auto">
                                {JSON.stringify(selectedSnapshot, null, 2)}
                            </pre>
                        </details>
                    )}
                </>
            )}
        </div>
    );
}