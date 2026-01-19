import React from 'react';
import clsx from 'clsx';

export default function PrinterCard({printer, snapshot, onSelect, isActive}) {
    const statusColor = {
        PRINTING: 'bg-green-500',
        PAUSED: 'bg-yellow-500',
        OFFLINE: 'bg-gray-400',
        IDLE: 'bg-blue-500',
    }[printer.status] || 'bg-gray-400';

    return (
        <button
            className={clsx(
                'w-full text-left rounded-lg border p-4 shadow-sm transition hover:shadow-md',
                isActive ? 'border-blue-500 ring-2 ring-blue-200' : 'border-gray-200'
            )}
            onClick={() => onSelect(isActive ? null : printer.id)}
        >
            <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold text-gray-800">{printer.name}</h3>
                <span className={clsx('text-xs text-white px-2 py-1 rounded-full', statusColor)}>
                    {printer.status}
                </span>
            </div>
            <p className="text-sm text-gray-500">{printer.type} • {printer.ipAddress}:{printer.port}</p>
            {snapshot && (
                <div className="grid grid-cols-2 gap-3 mt-3 text-sm text-gray-700">
                    <div>
                        <p className="font-semibold">Nozzle</p>
                        <p>{snapshot.nozzleTemp ?? '--'}°C</p>
                    </div>
                    <div>
                        <p className="font-semibold">Bed</p>
                        <p>{snapshot.bedTemp ?? '--'}°C</p>
                    </div>
                    <div>
                        <p className="font-semibold">Progress</p>
                        <p>{snapshot.progress ? `${snapshot.progress.toFixed(1)}%` : 'N/A'}</p>
                    </div>
                    <div>
                        <p className="font-semibold">State</p>
                        <p>{snapshot.state || 'Unknown'}</p>
                    </div>
                </div>
            )}
        </button>
    );
}