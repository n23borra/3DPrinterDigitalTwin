import React from 'react';

export default function PrinterHistoryTable({history}) {
    return (
        <div className="overflow-x-auto border rounded-lg mt-4">
            <table className="min-w-full text-sm">
                <thead className="bg-gray-100">
                <tr>
                    <th className="px-3 py-2 text-left">Timestamp</th>
                    <th className="px-3 py-2 text-left">Nozzle</th>
                    <th className="px-3 py-2 text-left">Bed</th>
                    <th className="px-3 py-2 text-left">Progress</th>
                    <th className="px-3 py-2 text-left">State</th>
                </tr>
                </thead>
                <tbody>
                {history.length === 0 && (
                    <tr>
                        <td className="px-3 py-3 text-gray-500" colSpan={5}>No history captured yet.</td>
                    </tr>
                )}
                {history.map((snapshot) => (
                    <tr key={snapshot.id} className="border-t">
                        <td className="px-3 py-2">{new Date(snapshot.timestamp).toLocaleString()}</td>
                        <td className="px-3 py-2">{snapshot.nozzleTemp ?? '--'}°C / {snapshot.targetNozzle ?? '--'}°C</td>
                        <td className="px-3 py-2">{snapshot.bedTemp ?? '--'}°C / {snapshot.targetBed ?? '--'}°C</td>
                        <td className="px-3 py-2">{snapshot.progress ? `${snapshot.progress.toFixed(1)}%` : 'N/A'}</td>
                        <td className="px-3 py-2">{snapshot.state || 'Unknown'}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}