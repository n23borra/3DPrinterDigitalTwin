import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
    createPrinter,
    fetchPrinters,
    fetchPrinterHistory,
    fetchPrinterState,
    sendPrinterCommand,
} from '../api/printerApi';
import PrinterHistoryTable from '../components/printers/PrinterHistoryTable';
import Modal from '../components/Modal';
import Button from '../components/Button';

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
    const [staleData, setStaleData] = useState({});
    const failCountRef = useRef(0);
    const MAX_RETRIES = 3;
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

            setSelectedId(null);
        } catch (error) {
            console.error('Error loading printers:', error);
            setPrinters([]);
        }
    }, [selectedId]);

    // Fetch printers list on mount
    useEffect(() => {
        loadPrinters();
    }, [loadPrinters]);

    // Check if a snapshot has any meaningful data (not just null fields)
    const hasRealData = (data) =>
        data && (data.nozzleTemp != null || data.bedTemp != null || data.state != null);

    // Core fetch logic shared by auto-refresh and manual actions
    const fetchData = useCallback(async (printerId, { isAutoRefresh = false, onDone } = {}) => {
        if (!printerId) return;
        if (!isAutoRefresh) setLoading(true);
        let gotData = false;
        try {
            const stateRes = await fetchPrinterState(printerId);
            if (hasRealData(stateRes.data)) {
                setSnapshots((prev) => ({ ...prev, [printerId]: stateRes.data }));
                setStaleData((prev) => ({ ...prev, [printerId]: null }));
                gotData = true;
            } else {
                setStaleData((prev) => prev[printerId] ? prev : ({ ...prev, [printerId]: new Date() }));
            }
        } catch (error) {
            console.error('Failed to fetch printer state:', error);
            setStaleData((prev) => prev[printerId] ? prev : ({ ...prev, [printerId]: new Date() }));
        }
        try {
            const historyRes = await fetchPrinterHistory(printerId, { limit: 50 });
            if (historyRes.data) {
                setHistory(historyRes.data);
            }
        } catch (error) {
            console.error('Failed to fetch printer history:', error);
        }
        if (!isAutoRefresh) setLoading(false);
        if (onDone) onDone(gotData);
    }, []);

    // Auto-refresh with retry limit: stops after MAX_RETRIES consecutive failures
    useEffect(() => {
        if (!selectedId) return;
        let cancelled = false;
        failCountRef.current = 0;

        // Initial fetch
        fetchData(selectedId, {
            isAutoRefresh: false,
            onDone: (ok) => { failCountRef.current = ok ? 0 : 1; },
        });

        if (!autoRefresh) return () => { cancelled = true; };

        const interval = setInterval(() => {
            if (cancelled) return;
            if (failCountRef.current >= MAX_RETRIES) {
                clearInterval(interval);
                return;
            }
            fetchData(selectedId, {
                isAutoRefresh: true,
                onDone: (ok) => {
                    failCountRef.current = ok ? 0 : failCountRef.current + 1;
                },
            });
        }, 2000);

        return () => { cancelled = true; clearInterval(interval); };
    }, [selectedId, autoRefresh, fetchData]);

    const selectedPrinter = useMemo(
        () => printers.find((p) => p.id === selectedId),
        [printers, selectedId],
    );

    const selectedSnapshot = snapshots[selectedId];

    // Manual action: send command, re-fetch, and reset retry counter to resume auto-refresh
    const handleCommand = async (command) => {
        if (!selectedId) return;
        await sendPrinterCommand(selectedId, command);
        failCountRef.current = 0;
        fetchData(selectedId, {
            onDone: (ok) => {
                failCountRef.current = ok ? 0 : 1;
                // Re-trigger the auto-refresh effect so the interval restarts
                if (ok) setAutoRefresh((v) => v);
            },
        });
    };

    // Manual retry for disconnected printers
    const handleRetry = () => {
        failCountRef.current = 0;
        setAutoRefresh((v) => !v);
        setTimeout(() => setAutoRefresh((v) => !v), 0);
    };

    const hasData = selectedSnapshot && (
        selectedSnapshot.nozzleTemp !== null ||
        selectedSnapshot.bedTemp !== null ||
        selectedSnapshot.state !== null
    );

    const isStale = selectedId && !!staleData[selectedId];

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
                    <div className="ml-auto flex items-center gap-2">
                        {/* Printer Selector */}
                        <div className="w-56">
                            <select
                                id="printer-select"
                                className="w-full border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                                value={selectedId ?? ''}
                                onChange={(e) => setSelectedId(e.target.value)}
                            >
                                <option value="" disabled={selectedId !== null && selectedId !== ''}>Select a Printer</option>
                                {printers.map((printer) => (
                                    <option key={printer.id} value={printer.id}>
                                        {printer.name} — {printer.ipAddress}
                                    </option>
                                ))}
                            </select>
                        </div>
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
                            {autoRefresh ? 'Auto-refresh ON' : 'Auto-refresh OFF'}
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
                        <Button type="submit" disabled={isCreatingPrinter}>
                            {isCreatingPrinter ? 'Creating...' : 'Create printer'}
                        </Button>
                    </div>
                </form>
            </Modal>

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
                                    isStale ? 'bg-orange-100 text-orange-800'
                                        : hasData ? 'bg-green-100 text-green-800'
                                        : 'bg-red-100 text-red-800'
                                }`}>
                                    {isStale ? 'Connection lost' : hasData ? 'Connected' : 'Offline'}
                                </div>
                            </div>
                        </div>

                        {/* Quick Commands */}
                        <div className="flex flex-wrap gap-3 mt-4">
                            {QUICK_COMMANDS.map((action) => (
                                <Button
                                    key={action.command}
                                    onClick={() => handleCommand(action.command)}
                                    className="text-sm px-3 py-2"
                                    disabled={!hasData}
                                >
                                    {action.label}
                                </Button>
                            ))}
                        </div>
                    </section>

                    {/* Stale data warning */}
                    {isStale && hasData && (
                        <div className="bg-orange-50 border border-orange-200 rounded-lg p-4 mb-6 flex items-center justify-between">
                            <div className="flex items-center gap-3">
                                <span className="text-orange-600 text-lg">&#9888;</span>
                                <div>
                                    <p className="text-sm font-semibold text-orange-800">
                                        Printer disconnected — showing last known data
                                    </p>
                                    <p className="text-xs text-orange-600">
                                        Since {staleData[selectedId].toLocaleTimeString()}
                                    </p>
                                </div>
                            </div>
                            <button
                                onClick={handleRetry}
                                className="px-3 py-1.5 rounded bg-orange-600 text-white text-sm hover:bg-orange-700"
                            >
                                Retry
                            </button>
                        </div>
                    )}

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
                                            <span className="text-sm font-normal text-gray-500 ml-2">
                                                → {selectedSnapshot.targetNozzle ? `${selectedSnapshot.targetNozzle.toFixed(0)}°C` : '--'}
                                            </span>
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
                                            <span className="text-sm font-normal text-gray-500 ml-2">
                                                → {selectedSnapshot.targetBed ? `${selectedSnapshot.targetBed.toFixed(0)}°C` : '--'}
                                            </span>
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
                        <details className="mt-6 bg-white rounded-lg shadow p-5 min-w-0">
                            <summary className="cursor-pointer text-sm font-semibold text-gray-700">
                                Show Raw Data (Debug)
                            </summary>
                            <pre className="mt-4 p-4 bg-gray-50 rounded text-xs overflow-auto max-w-full whitespace-pre-wrap break-words">
                                {JSON.stringify(selectedSnapshot, null, 2)}
                            </pre>
                        </details>
                    )}
                </>
            )}
        </div>
    );
}